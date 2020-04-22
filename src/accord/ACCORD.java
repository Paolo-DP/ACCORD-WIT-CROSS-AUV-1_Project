
package accord;

import java.util.Scanner;
import com.fazecast.jSerialComm.*;
import java.time.LocalTime;
import java.util.ArrayList;
import simulator.SimulationConstants;

/**
 *
 * @author Paolo
 */
public class ACCORD implements SimulationConstants{
    
    /**
     * @param args the command line arguments
     */
    static Scanner sc = null;
    static boolean testModules = true;
    
    public static final int CAR_COMM_BAUD_RATE = 19200;
    

    public static void main(String[] args) {
        sc = new Scanner(System.in);
        //Visualizer vs = new Visualizer();
        //runTestModules();
        
        //ComPortSelectDB cpSelect = new ComPortSelectDB();
        ComPortSelect cpSelect = new ComPortSelect();
        cpSelect.setVisible(true);
        //ACCORD_UI ui = new ACCORD_UI();
        //setup();
    }
    
    private static void setup(){
        initPozyx();
    }
    private static void initPozyx(){
        System.out.println("Initializing Pozyx...\nSelect Com port:");
        SerialPort[] ports = SerialPort.getCommPorts();
        for(int i=0; i<ports.length; i++){
            System.out.println(i+". " + ports[i].getSystemPortName());
        }
        int selectport = 0;
        do{
            System.out.print(">");
            selectport=sc.nextInt();
        }while(selectport<0 && selectport>=ports.length);
        SerialPort comPort = ports[selectport];
        if(comPort.openPort())
            System.out.println(comPort.getSystemPortName() 
                    + "Port Open Successful with Baud: " 
                    + comPort.getBaudRate());
        
        
    }
    
    public static Track createIntersectionTestTrack(int laneWidth, int startUpLength, int straightWayLength, int margin){
        Track tr = new Track();
        TrackSegment[] startUps = new TrackSegment[4];
        TrackSegment[] straights = new TrackSegment[4];
        TrackSegment[] exits = new TrackSegment[4];
        TrackSegment intersection = new IntersectionSegment(laneWidth*2);
        //((IntersectionSegment)intersection).setVerbose(true);
        //((IntersectionSegment)intersection).printAllSegments();
        
        double direction=0;
        for(int i=0; i<4; i++){
            startUps[i] = new TrackSegment();
            startUps[i].createLineSegment(startUpLength, laneWidth, direction);
            startUps[i].setSegmentID("StartUp " + direction);
            straights[i] = new TrackSegment();
            straights[i].createLineSegment(straightWayLength, laneWidth, direction);
            straights[i].setSegmentID("Straight " + direction);
            exits[i] = new TrackSegment();
            exits[i].createLineSegment(startUpLength + straightWayLength, laneWidth, direction);
            exits[i].setSegmentID("Exit " + direction);
            
            direction += 90;
        }
        intersection.setSegmentID("Intersection");
        
        startUps[0].setAbsoluteLocation(0, startUpLength + straightWayLength + (laneWidth/2));
        straights[0].setAbsoluteLocation(startUpLength, startUpLength + straightWayLength + (laneWidth/2));
        exits[0].setAbsoluteLocation(startUpLength + straightWayLength + laneWidth*2, startUpLength + straightWayLength + (laneWidth/2));
        
        startUps[1].setAbsoluteLocation(startUpLength + straightWayLength + (int)(laneWidth*1.5), 0);
        straights[1].setAbsoluteLocation(startUpLength + straightWayLength + (int)(laneWidth*1.5), startUpLength);
        exits[1].setAbsoluteLocation(startUpLength + straightWayLength + (int)(laneWidth*1.5), startUpLength + straightWayLength + laneWidth*2);
        
        startUps[2].setAbsoluteLocation(startUpLength*2 + straightWayLength*2 + laneWidth*2, startUpLength + straightWayLength + (int)(laneWidth*1.5));
        straights[2].setAbsoluteLocation(startUpLength + straightWayLength*2 + laneWidth*2, startUpLength + straightWayLength + (int)(laneWidth*1.5));
        exits[2].setAbsoluteLocation(startUpLength + straightWayLength, startUpLength + straightWayLength + (int)(laneWidth*1.5));
        
        startUps[3].setAbsoluteLocation(startUpLength + straightWayLength + laneWidth/2, startUpLength*2 + straightWayLength*2 + laneWidth*2);
        straights[3].setAbsoluteLocation(startUpLength + straightWayLength + laneWidth/2, startUpLength + straightWayLength*2 + laneWidth*2);
        exits[3].setAbsoluteLocation(startUpLength + straightWayLength + laneWidth/2, startUpLength + straightWayLength);
        
        intersection.setAbsoluteLocation(startUpLength + straightWayLength, startUpLength + straightWayLength);
        
        for(int i=0; i<4; i++){
            startUps[i].connectSegments(null, straights[i]);
            straights[i].connectSegments(startUps[i], intersection);
            exits[i].connectSegments(intersection, null);
        }
        ((IntersectionSegment)intersection).connectEntranceExits(straights, exits);
        
        for(int i=0; i<4; i++){
            tr.addTrackSegment(startUps[i], false);
            tr.addTrackSegment(straights[i], false);
            tr.addTrackSegment(exits[i], false);
        }
        tr.addTrackSegment(intersection, false);
        tr.setTrackMargin(margin);
        
        return tr;
    }
    public static CommMessageScheduler runPredetermined2DGrid(int[] tags, int[] headings, int[] maneuvers){
        CommMessageScheduler commSched = new CommMessageScheduler();
        
        int nCars = tags.length;
        nCars = Math.min(nCars, headings.length);
        nCars = Math.min(nCars, maneuvers.length);        
        Car[] cars = new Car[nCars];
        
        Track tr = ACCORD.createIntersectionTestTrack(640, 800, 1160, 0);
        tr.printAllSegments();
        Track[] routes = new Track[nCars];
        
        CarSimulator carSim = new CarSimulator();
        carSim.setScheduler(commSched);
        carSim.setVerboseOutput(true);
        carSim.setTrack(tr);
        
        
        for(int i=0; i<cars.length; i++){
            cars[i] = new SimulatedCar(tags[i]);
            initCar(cars[i], headings[i], maneuvers[i]);
            initSpeedTable(cars[i]);
            cars[i].adjustThrottle(SimulatedCar.SPEED_LIMIT);
            routes[i] = tr.getRouteTrack(cars[i], tr.updateCarTracker(cars[i]).currentSeg);
            System.out.println("Track route for Car 0x" + Integer.toHexString(cars[i].getID()));
            routes[i].printAllSegments();
            
            //cars[i].setVerbose(true);
            carSim.addCar(cars[i], routes[i]);
            cars[i].updateLocation();
            cars[i].alignXAxis();
        }
        
        LocalTime initWait = LocalTime.now().plusSeconds(10);
        while(LocalTime.now().isBefore(initWait)){
            //for(int i=0; i<cars.length; i++){
                carSim.simulate();
                carSim.printAllCarDetails();
                try{
                    Thread.sleep(100);
                }catch(Exception e){};
                //if(cars[i].isUpdated()){
                    //cars[i].printCarAttributes();
                //}
                
            //}
        }
        commSched.exportCSV("C:\\THESIS_Data");
        //commSched.sendAll();
        
        return commSched;
    }
    public static CommMessageScheduler runPredetermined2DGrid(int[] tags, int[] headings, int[] maneuvers, ArrayList<Car> customCars){
        CommMessageScheduler commSched = new CommMessageScheduler();
        
        int nCars = tags.length;   
        Car[] cars = new Car[nCars];
        
        Track tr = ACCORD.createIntersectionTestTrack(640, 800, 1160, 0);
        tr.printAllSegments();
        Track[] routes = new Track[nCars];
        
        CarSimulator carSim = new CarSimulator();
        carSim.setScheduler(commSched);
        carSim.setVerboseOutput(true);
        carSim.setTrack(tr);
        
        
        for(int i=0; i<cars.length; i++){
            cars[i] = new SimulatedCar(tags[i]);
            initCar(cars[i], headings[i], maneuvers[i]);
            initSpeedTable(cars[i]);
            cars[i].adjustThrottle(SimulatedCar.SPEED_LIMIT);
            routes[i] = tr.getRouteTrack(cars[i], tr.updateCarTracker(cars[i]).currentSeg);
            System.out.println("Track route for Car 0x" + Integer.toHexString(cars[i].getID()));
            routes[i].printAllSegments();
            
            //cars[i].setVerbose(true);
            carSim.addCar(cars[i], routes[i]);
            cars[i].updateLocation();
            cars[i].alignXAxis();
        }
        
        for(Car c : customCars){
            initSpeedTable(c);
            Track route = tr.getRouteTrack(c, tr.updateCarTracker(c).currentSeg);
            System.out.println("Track route for Car 0x" + Integer.toHexString(c.getID()));
            route.printAllSegments();
            
            carSim.addCar(c, route);
            c.updateLocation();
            c.alignXAxis();
        }
        
        LocalTime initWait = LocalTime.now().plusSeconds(10);
        while(LocalTime.now().isBefore(initWait)){
            //for(int i=0; i<cars.length; i++){
                carSim.simulate();
                carSim.printAllCarDetails();
                try{
                    Thread.sleep(100);
                }catch(Exception e){};
                //if(cars[i].isUpdated()){
                    //cars[i].printCarAttributes();
                //}
                
            //}
        }
        commSched.exportCSV("C:\\THESIS_Data");
        //commSched.sendAll();
        
        return commSched;
    }
    private static void runTestModules(){
        System.out.println("Running Module Tests...");
        //ModuleUnitTests.testLinearTrackSegment();
        //ModuleUnitTests.testSimpleOvalTrack();
        //ModuleUnitTests.testRouteSubTrack();
        
        //ModuleUnitTests.testPozyxSerialComm();
        //ModuleUnitTests.testPozyxIncommingFrame();
        //ModuleUnitTests.testPozyxAck();
        //ModuleUnitTests.testPozyxLocalization();
        //ModuleUnitTests.testArduinoTimeSync();
        //ModuleUnitTests.testCarSpeedCalculations();
        //ModuleUnitTests.testSpeedEquivalentThrottle();
        //ModuleUnitTests.testCarCSV();
        
        //ModuleUnitTests.testCoordinatesPolling();
        //ModuleUnitTests.testCarSetOrientation();
        //ModuleUnitTests.testCarSetOrientationTimed();
        //ModuleUnitTests.testCarLocationPolling();
        //ModuleUnitTests.testCarSimulationLine();
        //ModuleUnitTests.testCarSimulationStraightTurn();
        //ModuleUnitTests.testCarSimulationOval();
        //ModuleUnitTests.testCarCommand(); 
        
        //ModuleUnitTests.testCommSchedulerExport();
        
        //ModuleUnitTests.testVisualizer();
        //ModuleUnitTests.testTimeSyncing();
        
        //ACCORD.createIntersectionTestTrack(50,50,50,50);
        //ModuleUnitTests.testIntersectionTrack();
        ModuleUnitTests.testIntersectionTrackSubtracking();
        
        //ModuleUnitTests.miscTests();
    }
    
    public static void initCar(Car c, int heading, int direction){
        int startX = 0;
        int startY = 0;
        double orient = 0;
        switch(heading){
            case EAST:
                startX = 1;
                startY = 2280;
                orient = 0;
                break;
            case NORTH:
                startX = 2920;
                startY = 1;
                orient = 90;
                break;
            case WEST:
                startX = 5199;
                startY = 2920;
                orient = 180;
                break;
            case SOUTH:
                startX = 2280;
                startY = 5199;
                orient = 270;
                break;
        }
        
        c.setAttributesManual(c.getID(), startX, startY, orient, Car.DEFAULT_XDim, Car.DEFAULT_YDim, c.getSpeedEquivalent(Car.DEFAULT_THROTTLE_FLOOR));
        c.addRouteDirection(direction);
    }
    public static void initSpeedTable(Car c){
        
        int[] throttleColumn = new int[] {
                0, 50, 60, 75, 90, 100
        };
        double[] speedColumn = new double[] {
                0, 370, 750, 1035, 1200, 1300
        };
        ((SimulatedCar)c).initThrottleToSpeedTable(throttleColumn, speedColumn);
        
        switch(c.getID()){
            case 0x6a40:
                throttleColumn[0] = 0;
                throttleColumn[1] = 50;
                throttleColumn[2] = 60;
                throttleColumn[3] = 75;
                throttleColumn[4] = 90;
                throttleColumn[5] = 100;

                speedColumn[0] = 0;
                speedColumn[1] = 618;
                speedColumn[2] = 870;
                speedColumn[3] = 1090;
                speedColumn[4] = 1394;
                speedColumn[5] = 1456;
                ((SimulatedCar)c).initThrottleToSpeedTable(throttleColumn, speedColumn);
                break;
            case 0x6743:
                throttleColumn[0] = 0;
                throttleColumn[1] = 40;
                throttleColumn[2] = 60;
                throttleColumn[3] = 75;
                throttleColumn[4] = 90;
                throttleColumn[5] = 100;

                speedColumn[0] = 0;
                speedColumn[1] = 0;
                speedColumn[2] = 571;
                speedColumn[3] = 942;
                speedColumn[4] = 1235;
                speedColumn[5] = 1318;
                ((SimulatedCar)c).initThrottleToSpeedTable(throttleColumn, speedColumn);
                break;
            case 0x673b:
                throttleColumn[0] = 0;
                throttleColumn[1] = 50;
                throttleColumn[2] = 60;
                throttleColumn[3] = 75;
                throttleColumn[4] = 90;
                throttleColumn[5] = 100;

                speedColumn[0] = 0;
                speedColumn[1] = 295;
                speedColumn[2] = 722;
                speedColumn[3] = 920;
                speedColumn[4] = 1218;
                speedColumn[5] = 1281;
                ((SimulatedCar)c).initThrottleToSpeedTable(throttleColumn, speedColumn);
                break;
            case 0x6a1a:
                throttleColumn[0] = 0;
                throttleColumn[1] = 50;
                throttleColumn[2] = 60;
                throttleColumn[3] = 75;
                throttleColumn[4] = 90;
                throttleColumn[5] = 100;

                speedColumn[0] = 0;
                speedColumn[1] = 570;
                speedColumn[2] = 870;
                speedColumn[3] = 1190;
                speedColumn[4] = 1330;
                speedColumn[5] = 1420;
                ((SimulatedCar)c).initThrottleToSpeedTable(throttleColumn, speedColumn);
                break;
        }
        ((SimulatedCar)c).initThrottleToSpeedTable(throttleColumn, speedColumn);
    }
}
