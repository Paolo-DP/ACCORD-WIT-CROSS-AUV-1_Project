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
import java.util.Arrays;

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
    private int distanceOfCollision = 300; 
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
    private String carCollisionLocation = "\\carCollision";
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
        dataFileHeader = rootPath + dataFileHeader;
        if(verboseOutput)
            System.out.println("CarSimulator: fileHeader = " + dataFileHeader);
        initFileWriters();
        
       if(commSched != null)
           commSched.setTimeZero();
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
        pathchk = new File(path);
        pathchk.mkdir();
        for(Car c : carList){
            fwCarTrackers.add(initCSVCarTracker(path, c.getID()));
        }
        
        //Collisions CSV initialization
        path = dataFileHeader + carCollisionLocation;
        pathchk = new File(path);
        pathchk.mkdir();
        fwCollisions = initCSVCollision(path);
        
        initScheduler();
        
        track.setCSVOutput(dataFileHeader);
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
                    CarTracker ct = subtrack.updateCarTracker(c);
                                        
                    if(ct.nextSeg != null && ct.nextSeg.isIntersection()){ //if approaching an intersection
                        IntersectionSegment intersect = (IntersectionSegment)ct.nextSeg;
                        if(intersect.isReserved(c)){ //Car already has reservation
                            //maintain current speed
                        }
                        else{
                            if(intersect.reserve(c, ct.currentSeg, c.getNextRouteDirection())){
                                //maintain current speed
                            }
                            else //reservation is denied
                                c.throttleDecrement();
                        }
                        track.getCarTracker(c);
                    }
                    if(ct.currentSeg != null && ct.currentSeg.isIntersection()){
                        CarTracker temp = new CarTracker(c);
                        temp.x = ct.x;
                        temp.y = ct.y;
                        temp.angleDeviation = ct.angleDeviation;
                        temp.distanceFromDrivingLine = ct.distanceFromDrivingLine;
                        temp.hasReservation = ct.hasReservation;
                        temp.idealAngle = ct.idealAngle;
                        temp.isOutOfBounds = ct.isOutOfBounds;
                        temp.nextSeg = ct.nextSeg;
                        temp.currentSeg = ((IntersectionSegment)ct.currentSeg).getCurrentInternalSegment(c);
                        if(temp.currentSeg == null){
                            temp.isOutOfBounds = true;
                        }
                        ct = temp;
                    }
                    c.setOutOfBounds(ct.isOutOfBounds);
                    doSteering(c, ct);
                    doThrottle(c, ct);
                    
                    outputCSVCarTracker(c, ct);
                    
                }
            }
            double[][] collisionDist = collisionCheckDistance();
            boolean[][] collisions = collisionCheck(collisionDist, distanceOfCollision);
            outputCSVCollisions(collisionDist);
        }
    }
    
    //manuver handling methods
    private void doSteering(Car c, CarTracker tracker){
        if(!tracker.isOutOfBounds){
            String segShape = tracker.currentSeg.getSegShape();
            if(segShape == TrackSegment.SEGSHAPE_LINEAR){
                c.maintainOrientation(computeNextOrientation(c, tracker), false);
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
        else if(tracker.currentSeg != null && tracker.currentSeg.isIntersection()){
            c.adjustThrottle(c.getThrottlePower());
        }
        else if(tracker.hasReservation){
            c.adjustThrottle(c.getThrottlePower());
        }
        else if(tracker.nextSeg != null && tracker.nextSeg.isIntersection()){
            if(tracker.hasReservation)
                c.adjustThrottle(c.getThrottlePower());
            else{
                c.throttleDecrement();
            }
        }
        else{
            //c.adjustThrottle(computeNextThrottle(c));
            c.throttleIncrement();
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
            c.setOutOfBounds(true);
            if(verboseOutput)
                System.out.println("CarSimulator: ID " + Integer.toHexString(c.getID())
                    + " Out of Bounds");
            
            return 0;
        }
        else
            c.setOutOfBounds(false);
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
    private double computeNextOrientation(Car c, CarTracker ct){
        Track subtrack = getCarRoute(c);
        int distCLine = ct.distanceFromDrivingLine;
        //double correctOrient = subtrack.directionDeviation(c);
        double followOrient = ct.idealAngle;
        if(distCLine == Integer.MAX_VALUE){
            c.setOutOfBounds(true);
            if(verboseOutput)
                System.out.println("CarSimulator: ID " + Integer.toHexString(c.getID())
                    + " Out of Bounds");
            
            return c.getOrientation();
        }
        else
            c.setOutOfBounds(false);
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
        
        return computeSteerCompensateTime(distCLine, followOrient, c.getSpeed());
    }
    private double[] computeSteerCompensateTime(int distCLine, double followOrient, double speed){
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
        
        return (int) (c.getThrottlePower() * 1.2);
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
    
    private boolean[][] collisionCheck(int distance){
        boolean[][] collisionChk = new boolean[carList.size()][carList.size()];
        for(boolean[] row : collisionChk)
            Arrays.fill(row, false);
        
        for(int car1=0; car1<carList.size(); car1++){
            for(int car2=car1; car2<carList.size(); car2++){
                if(car1 == car2)
                    continue;
                Car c1 = carList.get(car1);
                Car c2 = carList.get(car2);
                double x = c1.getXLocation() - c2.getXLocation();
                double y = c1.getYLocation() - c2.getYLocation();
                
                double colDist = Math.sqrt((x*x) + (y*y));
                collisionChk[car1][car2] = colDist<distance;
                collisionChk[car2][car1] = collisionChk[car1][car2];
                //System.out.println(colDist);
            }
        }
        return collisionChk;
    }
    private boolean[][] collisionCheck(double[][] collisionDistances, int colDist){
        boolean[][] collisionChk = new boolean[collisionDistances.length][collisionDistances.length];
        for(boolean[] row : collisionChk)
            Arrays.fill(row, false);
        for(int car1=0; car1<collisionDistances.length; car1++){
            for(int car2=car1; car2<collisionDistances.length; car2++){
                if(car1 == car2)
                    continue;
                if(collisionDistances[car1][car2] < colDist)
                    collisionChk[car1][car2] = true;
                collisionChk[car2][car1] = collisionChk[car1][car2];
                //System.out.println(colDist);
            }
        }
        
        return collisionChk;
    }
    
    private double[][] collisionCheckDistance(){
        double[][] collisionChk = new double[carList.size()][carList.size()];
        for(double[] row : collisionChk)
            Arrays.fill(row, -1);
        
        for(int car1=0; car1<carList.size(); car1++){
            for(int car2=car1; car2<carList.size(); car2++){
                if(car1 == car2)
                    continue;
                Car c1 = carList.get(car1);
                Car c2 = carList.get(car2);
                double x = c1.getXLocation() - c2.getXLocation();
                double y = c1.getYLocation() - c2.getYLocation();
                
                double colDist = Math.sqrt((x*x) + (y*y)) + 250;
                collisionChk[car1][car2] = colDist;
                collisionChk[car2][car1] = collisionChk[car1][car2];
                //System.out.println(colDist);
            }
        }
        return collisionChk;
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
    
    private CarTracker getBestCarTracker(Car car, Track tr, CarTracker ct){
        CarTracker best = ct;
        if(ct.isOutOfBounds)
            return ct;
        CarDetails deets = car.getFullDetails();
        CarDetails nextStep;
        //nextStep = incrementLocationConstant(deets);
        nextStep = incrementLocationFixedDistance(deets, 600);
        /*if(!(deets.timeStampHist[0] <=0 || deets.timeStampHist[1] <= 0)){
            nextStep =  incrementLocationConstant(deets, (deets.timeStampHist[0]-deets.timeStampHist[1])/1000);
        }
        else
            nextStep = incrementLocationConstant(deets);
        */
        CarTracker nextCt = tr.nullTracker(nextStep);
        if(!nextCt.isOutOfBounds){
            best = nextCt;
            best.car = car;
        }
        
        return best;
    }
    //private int totalLookAheadTime = 2000;
    private double incrementLocTimeStep = .2;
    private CarDetails incrementLocationConstant(CarDetails deets){
        deets.xloc += (int) (deets.speed * incrementLocTimeStep * Math.cos(deets.orient));
        deets.yloc += (int) (deets.speed * incrementLocTimeStep * Math.sin(deets.orient));
        return deets;
    }
    private CarDetails incrementLocationConstant(CarDetails deets, double timeStep){
        deets.xloc += (int) (deets.speed * timeStep * Math.cos(deets.orient));
        deets.yloc += (int) (deets.speed * timeStep * Math.sin(deets.orient));
        return deets;
    }
    private CarDetails incrementLocationFixedDistance(CarDetails deets, int magnitude){
        deets.xloc += (int) (magnitude * Math.cos(deets.orient));
        deets.yloc += (int) (magnitude * Math.sin(deets.orient));
        return deets;
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
    
    CommMessageScheduler commSched = null;
    public void setScheduler(CommMessageScheduler commSched){
        this.commSched = commSched;
    }
    public void initScheduler(){
        if(commSched == null) return;
        commSched.setCSVOutput(dataFileHeader);
        for(Car c : carList){
            ((SimulatedCar)c).setCommMessageScheduler(commSched);
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
                fw = new FileWriter(path + "\\0x" + Integer.toHexString(carID) + "_CarTracker.csv");
                
                fw.append("Local Time");
                fw.append(",CarID");
                fw.append(",Last Time Stamp");
                fw.append(",X");
                fw.append(",Y");
                fw.append(",Current Segment");
                fw.append(",Next Segment");
                fw.append(",is Out of Bounds");
                fw.append(",has Reservation");
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
            fw.append("," + Integer.toHexString(c.getID()));
            fw.append("," + Double.toString(c.getLastTimeStamp()));
            fw.append("," + Integer.toString(ct.x));
            fw.append("," + Integer.toString(ct.y));
            if(ct.currentSeg == null)
                fw.append(",NULL");
            else
                fw.append("," + ct.currentSeg.getSegmentID());
            if(ct.nextSeg == null)
                fw.append(",NULL");
            else
                fw.append("," + ct.nextSeg.getSegmentID());
            fw.append("," + Boolean.toString(ct.isOutOfBounds));
            fw.append("," + Boolean.toString(ct.hasReservation));
            fw.append("," + Integer.toString(ct.distanceFromDrivingLine));
            fw.append("," + Double.toString(ct.angleDeviation));
            fw.append("," + Double.toString(ct.idealAngle) + "\n");
            fw.flush();
            
        }catch(Exception e){
            if(verboseOutput)
                System.out.println("CarSimulator: ERROR! Failed to write to Car Tracker CSV 0x" + Integer.toHexString(c.getID()));
        }
    }
    FileWriter fwCollisions = null;
    private FileWriter initCSVCollision(String path){
        FileWriter fw = null;
        File pathchk = new File(path);
        if(pathchk.isDirectory()){
            try{
                fw = new FileWriter(path + "\\Collisions.csv");
                fw.append("Local Time");
                fw.append(",N Collisions");
                for(int i=0; i<carList.size(); i++){
                    for(int j=0; j<carList.size(); j++){
                        fw.append(",0x" + Integer.toHexString(carList.get(i).getID()) + " - 0x" + Integer.toHexString(carList.get(j).getID()));
                     }
                }
                fw.append("\n");
                fw.flush();
            }catch(Exception e){
                if(verboseOutput){
                    System.out.println("CarSimulator: Failed to open Collisions file writer");
                }
            }
        }
        else if(verboseOutput)
            System.out.println("CarSimulator: ERROR! Path for Collisions does not exist");
        return fw;
    }
    private void outputCSVCollisions(boolean[][] collisions){
        int count = countNCollisions(collisions);
        try{
            fwCollisions.append(LocalTime.now().toString());
            fwCollisions.append("," + Integer.toString(count));
            for(int i=0; i<collisions.length; i++){
                for(int j=0; j<collisions[i].length; j++){
                    fwCollisions.append("," + Boolean.toString(collisions[i][j]));
                }
            }
            fwCollisions.append("\n");
            fwCollisions.flush();
        }catch(Exception e){
            
        }
            
    }
    private void outputCSVCollisions(double[][] collisions){
        int count = countNCollisions(collisions, distanceOfCollision);
        try{
            fwCollisions.append(LocalTime.now().toString());
            fwCollisions.append("," + Integer.toString(count));
            for(int i=0; i<collisions.length; i++){
                for(int j=0; j<collisions[i].length; j++){
                    fwCollisions.append("," + Double.toString(collisions[i][j]));
                }
            }
            fwCollisions.append("\n");
            fwCollisions.flush();
        }catch(Exception e){
            
        }
    }
    private int countNCollisions(boolean[][] collisions){
        int count = 0;
        for(int i=0; i<collisions.length; i++){
            for(int j=i; j<collisions[i].length; j++){
                if(collisions[i][j])
                    count++;
            }
        }
        return count;
    }
    private int countNCollisions(double[][] collisions, int distance){
        int count = 0;
        for(int i=0; i<collisions.length; i++){
            for(int j=i; j<collisions[i].length; j++){
                if(collisions[i][j] >= 0 && collisions[i][j] < distance)
                    count++;
            }
        }
        return count;
    }
    
}
