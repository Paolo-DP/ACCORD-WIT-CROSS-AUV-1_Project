/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accord;

import java.io.File;
import java.io.FileWriter;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 *
 * @author Paolo
 */
public class CarSimulator {
    private int minDistanceToCorrect = 100;
    private double minAngleToCorrect = 10;
    private double orientCorrection = 15;
    private double timePredictionSet = 500; //prediction of car location time step (ms)
    private final int MAX_LOCATION_SPIKE = 1000;
    private boolean verboseOutput = false;
    ArrayList <Car> carList = new ArrayList<Car>();
    Track track = null;
    ArrayList<Track> routeTracks = new ArrayList<>();
    
    LocalTime startTime = LocalTime.now();
    LocalDate startDate = LocalDate.now();
    private String rootPath = "C:\\THESIS_Data";
    private String instancePath = "";
    private String carDataLocation = "\\CarData";
    private String carTrackerLocation = "\\carTracker";
    private String dataFileHeader = "";
    ArrayList <FileWriter> fwCarTrackers = new ArrayList<>();
        
    private boolean start = false;
    
    private static final double MIN_TIME_TO_COLLISION = 1000; //ms until collision
    private static final double MAX_TIME_TO_COLLISION = 3000;
    //Set up methods
    public void addCar(Car c){
        carList.add(c);
        if(!c.updateLocation() & verboseOutput)
            System.out.print("CarSimulator: Car 0x" + c.getID() + "Failed to initial update");
    }
    public void addCar(Car c, Track route){
        addCar(c);
        routeTracks.add(route);
    }
    public void allignXAxis(){
        for(int i=0; i<carList.size(); i++){
            carList.get(i).alignXAxis();
        }
    }
    public boolean allignXAxis(int index){
        if(index<carList.size() && index > 0)
            return carList.get(index).alignXAxis();
        else
            return false;
    }
    public boolean allignXAxis(Car c){
        int index = carList.indexOf(c);
        if(index >= 0)
            return carList.get(index).alignXAxis();
        else
            return false;
    }
    public void setCarList(ArrayList<Car> cList){
        carList = cList;
    }
    public void setTrack(Track t){
        track = t;
    }
    private Track getCarRoute(Car c){
        int carIndex = carListIndex(c);
        if(carIndex < 0)
            return null;
        if(carIndex >= routeTracks.size())
            return track;
        return routeTracks.get(carIndex);
    }
    public void start(){
        startTime = LocalTime.now();
        dataFileHeader = "\\" + startDate.toString() + "_" + startTime.toString();
        dataFileHeader = dataFileHeader.replace(':', '-');
        if(verboseOutput)
            System.out.println("CarSimulator: fileHeader = " + dataFileHeader);
        initFileWriters();
        start = true;
    }
    public void stop(){
        exportSimulationSummary();
        start = false;
    }
    public void initFileWriters(){
        //Car Data CSV initialization
        String path = dataFileHeader + carDataLocation;
        File pathchk = new File(path);
            pathchk.mkdirs();
        for(Car c : carList){
            try{
                c.setCSVOutput(path);
            }catch(Exception e){
                if(verboseOutput)
                    System.out.println("CarSimulator: failed to create CSV file " + Integer.toHexString(c.getID()));
            };
        }
        //Car Tracker CSV initialization
        for(FileWriter fw : fwCarTrackers)
            try{fw.close();}catch(Exception e){}
        fwCarTrackers.clear();
        
        path = dataFileHeader + carTrackerLocation;
        for(Car c : carList){
            fwCarTrackers.add(initCSVCarTracker(path, c.getID()));
        }
    }
    
    public void simulate(){
        if(!start)
            start();
        Track subtrack;
        if(track!=null && carList.size()>0){
            for(int i=0; i<carList.size(); i++){
                Car c = carList.get(i);
                if(i>=routeTracks.size())
                    subtrack = track;
                else
                    subtrack = routeTracks.get(i);
                if(c.updateLocation()){
                    //if(!isValidData(c))
                    //    estimateCarLocation(c, null);
                    CarTracker ct = subtrack.updateCarTracker(c);
                    doSteering(c, ct);
                                        
                    if(ct.nextSeg != null && ct.nextSeg.isIntersection()){ //if approaching an intersection
                        IntersectionSegment intersect = (IntersectionSegment)ct.nextSeg;
                        if(intersect.isReserved(c)){ //Car already has reservation
                            
                        }
                        else{
                            if(intersect.reserve(c, ct.currentSeg, c.getNextRouteDirection())){
                                
                            }
                            else //reservation is denied
                                c.throttleDecrement();
                        }
                    }
                    doThrottle(c, ct);
                    
                    outputCSVCarTracker(c, ct);
                }
                /*
                c.maintainOrientation(computeNextOrientation(c));
                double[] orientTimed = computeSteerCompensateTime(c);
                c.maintainOrientationTimed(orientTimed[0], orientTimed[1]);
                c.adjustThrottle(computeNextThrottle(c));
                */
            }
        }
    }
    
    //manuver handling methods
    private void doSteering(Car c, CarTracker tracker){
        if(!tracker.isOutOfBounds){
            String segShape = tracker.currentSeg.getSegShape();
            if(segShape == TrackSegment.SEGSHAPE_LINEAR){
                c.maintainOrientation(computeNextOrientation(c), false);
                double[] orientTimed = computeSteerCompensateTime(c);
                c.maintainOrientationTimed(orientTimed[0], orientTimed[1]);
            }
            else if(segShape == TrackSegment.SEGSHAPE_90DEGTURN){
                double exitDirection = tracker.currentSeg.idealDirection(
                        tracker.currentSeg.getExitXLocation(), 
                        tracker.currentSeg.getExitYLocation());
                double deviat = Math.abs(c.getOrientation()-exitDirection);
                if(deviat>180)
                    deviat = -(360-deviat);
                if(Math.abs(deviat) > minAngleToCorrect){
                    c.maintainOrientation(exitDirection, true);
                }
                else{
                    double[] orientTimed = computeSteerCompensateTime(c);
                    c.maintainOrientationTimed(orientTimed[0], orientTimed[1]);
                }
            }
        }
    }
    private void doThrottle(Car c, CarTracker tracker){
        if(tracker.isOutOfBounds)
            c.throttleDecrement();
        else if(tracker.currentSeg.isIntersection())
            c.adjustThrottle(c.getThrottlePower());
        else{
            c.adjustThrottle(computeNextThrottle(c));
        }
    }
    //computation methods
    private int computeNextSteering(Car c){
        Track subtrack = getCarRoute(c);
        int steer_dist = 0;
        int steer_orient = 0;
        int distCLine = subtrack.distfromCenterLine(c);
        double correctOrient = subtrack.directionDeviation(c);
        //System.out.println(distCLine);
        if(distCLine == Integer.MAX_VALUE){
            c.outOfBounds = true;
            if(verboseOutput)
                System.out.println("CarSimulator: ID " + Integer.toHexString(c.getID())
                    + " Out of Bounds");
            
            return 0;
        }
        else
            c.outOfBounds = false;
        if(Math.abs(distCLine)>minDistanceToCorrect){
            steer_dist = 127;
            if(distCLine>0)
                steer_dist *= -1;            
        }
        if(Math.abs(correctOrient) > minAngleToCorrect){
            steer_orient = 127;
            if(correctOrient<0)
                steer_orient *= -1;
        }
        
        return (steer_dist + steer_orient)%128;
    }
    private double computeNextOrientation(Car c){
        Track subtrack = getCarRoute(c);
        int distCLine = subtrack.distfromCenterLine(c);
        //double correctOrient = subtrack.directionDeviation(c);
        double followOrient = subtrack.idealDirection(c.getXLocation(), c.getYLocation());
        if(distCLine == Integer.MAX_VALUE){
            c.outOfBounds = true;
            if(verboseOutput)
                System.out.println("CarSimulator: ID " + Integer.toHexString(c.getID())
                    + " Out of Bounds");
            
            return c.getOrientation();
        }
        else
            c.outOfBounds = false;
        /*if(Math.abs(distCLine)>minDistanceToCorrect){
            
            if(distCLine>0)
                followOrient += orientCorrection;
            else
                followOrient -= orientCorrection;
        }
        */     
        return followOrient;
    }
    private double[] computeSteerCompensateTime(Car c){
        Track subtrack = getCarRoute(c);
        int distCLine = subtrack.distfromCenterLine(c);
        double followOrient = subtrack.idealDirection(c.getXLocation(), c.getYLocation());
        
        return computeSteerCompensateTime(distCLine, followOrient, c.minSpeedmm);
    }
    private double[] computeSteerCompensateTime(int distCLine, double followOrient, int speed){
        double[] data = new double[2];
        double time = 0;
        if(speed != 0)
            time = (distCLine*1000)/speed;
        if(Math.abs(distCLine)>minDistanceToCorrect){            
            if(distCLine>0)
                followOrient += orientCorrection;
            else
                followOrient -= orientCorrection;
        }
        data[0] = followOrient;
        data[1] = time;
        return data;
    }
    private int computeNextThrottle(Car c){
        int throttle = c.getThrottlePower();
        double frontCollision = checkFront(c);
        double RearCollision = checkRear(c);
        
        return throttle+c.THROTTLE_INCREMENT_STEP;
        /*else if(frontCollision == -1){
            return throttle += Car.THROTTLE_INCREMENT_STEP;
        }
        else if(RearCollision == -1){
            return throttle+= Car.THROTTLE_INCREMENT_STEP;
        }
        if(frontCollision <= MIN_TIME_TO_COLLISION)
            throttle -= Car.THROTTLE_INCREMENT_STEP;
        else if(frontCollision >= MAX_TIME_TO_COLLISION)
            throttle += Car.THROTTLE_INCREMENT_STEP;

        return throttle;
        */
    }
    private double checkFront(Car c){
        double timeToCollision = -1;
        return timeToCollision;
    }
    private double checkRear(Car c){
        double timeToCollision = -1;
        return timeToCollision;
    }
    
    private boolean isValidData(Car car){
        if(car == null)
            return false;
        CarDetails deets = car.getFullDetails();
        return isValidData(deets);
    }
    private boolean isValidData(CarDetails deets){
        if(deets == null)
            return false;
        double deltaX = (deets.xLocHistory[0]-deets.xLocHistory[1])*1000/(deets.timeStampHist[0]-deets.timeStampHist[1]);
        double deltaY = (deets.yLocHistory[0]-deets.yLocHistory[1])*1000/(deets.timeStampHist[0]-deets.timeStampHist[1]);
        return (Math.sqrt((deltaX*deltaX)+(deltaY*deltaY)) < MAX_LOCATION_SPIKE);
    }
    private void estimateCarLocation(Car car, CarDetails deets){
        if(deets == null)
            deets = car.getFullDetails();
        if(!deets.isValidated)
            validateDataHistory(deets);
        int startIndex = deets.timeStampHist.length - 1;
        while(deets.timeStampHist[startIndex]<=0)
            startIndex--;
        
        
        double timeLookAhead = car.timeSinceLastUpdate();
    }
    
    private void validateDataHistory(CarDetails deets){
        boolean[] valid = new boolean[deets.timeStampHist.length];
        for(int i=valid.length-1; i>=0; i--){
            valid[i] = false;
        }
        for(int i=0; i<valid.length-1; i++){
            valid[i] = (deets.timeStampHist[i] > 0) && 
                    (deets.timeStampHist[i] > deets.timeStampHist[i+1]) &&
                    track.isWithinTrack(deets.xLocHistory[i], deets.yLocHistory[i]);       
        }
        deets.isValidated = true;
        deets.isValidData = valid;
    }
    
    private int carListIndex(Car c){
        for(int i=0; i<carList.size(); i++){
            if(carList.get(i) == c)
                return i;
        }
        return -1;
    }
    
    public void setVerboseOutput(boolean v){
        verboseOutput = v;
    }
    public void printAllCarDetails(){
        if(verboseOutput){
            for(int i=0; i<carList.size(); i++){
                try{ carList.get(i).printCarAttributes(); }
                catch(Exception e){
                    if(verboseOutput)
                        System.out.println("CarSimulator: Failed to print Car attributes");
                };
            }
        }
    }
    
    //ouput CSVs
    public void exportSimulationSummary(){
        
    }
    public void writeCarDetailesCSV(FileWriter writer){
        if(writer != null){
            
        }
    }
    public void setRootPath(String path){
        rootPath = path;
    }
    public void setCarOutputPath(String path){
        carDataLocation = path;
    }
    public void setCarTrackertPath(String path){
        carTrackerLocation = path;
    }
    public FileWriter initCSVCarTracker(String path, int carID){
        FileWriter fw = null;
        File pathchk = new File(path);
        if(pathchk.isDirectory()){
            try{
                fw = new FileWriter(path + "\\0x" + Integer.toHexString(carID) + "_CarTracker");
                
                fw.append("Local Time");
                fw.append(",CarID");
                fw.append(",Current Segment");
                fw.append(",Next Segment");
                fw.append(",is Out of Bounds");
                fw.append(",Distance from Driving Line");
                fw.append(",Angle Deviation");
                fw.append(",Ideal Angle\n");
                fw.flush();
            }catch(Exception e){
                if(verboseOutput){
                    System.out.println("CarSimulator: Failed to open Car Tracker file writer 0x" + Integer.toBinaryString(carID));
                }
            }
        }
        else if(verboseOutput)
            System.out.println("CarSimulator: ERROR! Path for Car Tracker does not exist");
        return fw;
    }
    private void outputCSVCarTracker(Car c, CarTracker ct){
        int carIndex = carList.indexOf(c);
        if(carIndex<0)
            return;
        FileWriter fw = fwCarTrackers.get(carIndex);
        try{
            fw.append((LocalTime.now(Clock.systemDefaultZone())).toString());
            fw.append("," + Integer.toBinaryString(c.getID()));
            if(ct.currentSeg == null)
                fw.append(",NULL");
            else
                fw.append("," + ct.currentSeg.getSegmentID());
            if(ct.nextSeg == null)
                fw.append(",NULL");
            else
                fw.append("," + ct.nextSeg.getSegmentID());
            fw.append("," + Boolean.toString(ct.isOutOfBounds));
            fw.append("," + Integer.toString(ct.distanceFromDrivingLine));
            fw.append("," + Double.toString(ct.angleDeviation));
            fw.append("," + Double.toString(ct.idealAngle) + "\n");
            fw.flush();
            
        }catch(Exception e){
            if(verboseOutput)
                System.out.println("CarSimulator: ERROR! Failed to write to Car Tracker CSV 0x" + Integer.toHexString(c.getID()));
        }
    }
}
