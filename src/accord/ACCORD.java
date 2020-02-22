
package accord;

import java.util.Scanner;
import com.fazecast.jSerialComm.*;

/**
 *
 * @author Paolo
 */
public class ACCORD {
    
    /**
     * @param args the command line arguments
     */
    static Scanner sc = null;
    static boolean testModules = true;
    
    

    public static void main(String[] args) {
        sc = new Scanner(System.in);
        //Visualizer vs = new Visualizer();
        runTestModules();
        
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
    
    private static Track createIntersectionTestTrack(int laneWidth, int startUpLength, int straightWayLength, int margin){
        Track tr = new Track();
        TrackSegment[] startUps = new TrackSegment[4];
        TrackSegment[] straights = new TrackSegment[4];
        TrackSegment[] exits = new TrackSegment[4];
        TrackSegment intersection = new IntersectionSegment(laneWidth*2);
        
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
    
    private static void runTestModules(){
        System.out.println("Running Module Tests...");
        //ModuleUnitTests.testLinearTrackSegment();
        //ModuleUnitTests.testSimpleOvalTrack();
        
        //ModuleUnitTests.testPozyxSerialComm();
        //ModuleUnitTests.testPozyxIncommingFrame();
        //ModuleUnitTests.testPozyxAck();
        //ModuleUnitTests.testPozyxLocalization();
        
        //ModuleUnitTests.testCarSpeedCalculations();
        //ModuleUnitTests.testCarCSV();
        
        //ModuleUnitTests.testCoordinatesPolling();
        //ModuleUnitTests.testCarSetOrientation();
        //ModuleUnitTests.testCarSetOrientationTimed();
        //ModuleUnitTests.testCarLocationPolling();
        //ModuleUnitTests.testCarSimulationLine();
        //ModuleUnitTests.testCarSimulationStraightTurn();
        //ModuleUnitTests.testCarSimulationOval();
        //ModuleUnitTests.testCarCommand();
        
        //ModuleUnitTests.testVisualizer();
        //ModuleUnitTests.testTimeSyncing();
        
        //ACCORD.createIntersectionTestTrack(50,50,50,50);
    }
}
