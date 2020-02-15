/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accord;

import java.util.ArrayList;

/**
 *
 * @author Paolo
 */
public class CarSimulator {
    private int minDistanceToCorrect = 70;
    private double minAngleToCorrect = 10;
    private double orientCorrection = 15;
    private double timePredictionSet = 500; //prediction of car location time step (ms)
    private final int MAX_LOCATION_SPIKE = 1000;
    private boolean verboseOutput = false;
    ArrayList <Car> carList = new ArrayList<Car>();
    Track track = null;
    
    private static final double MIN_TIME_TO_COLLISION = 1000; //ms until collision
    private static final double MAX_TIME_TO_COLLISION = 3000;
    //Set up methods
    public void addCar(Car c){
        carList.add(c);
        c.updateLocation();
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
    
    public void simulate(){
        if(track!=null && carList.size()>0){
            for(int i=0; i<carList.size(); i++){
                Car c = carList.get(i);
                if(c.updateLocation()){
                    if(!isValidData(c))
                        estimateCarLocation(c, null);
                    CarTracker ct = track.updateCarTracker(c);
                    doSteering(c, ct);
                    doThrottle(c, ct);
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
        else{
            c.adjustThrottle(computeNextThrottle(c));
        }
    }
    //computation methods
    private int computeNextSteering(Car c){
        int steer_dist = 0;
        int steer_orient = 0;
        int distCLine = track.distfromCenterLine(c);
        double correctOrient = track.directionDeviation(c);
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
        int distCLine = track.distfromCenterLine(c);
        //double correctOrient = track.directionDeviation(c);
        double followOrient = track.idealDirection(c.getXLocation(), c.getYLocation());
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
        int distCLine = track.distfromCenterLine(c);
        double followOrient = track.idealDirection(c.getXLocation(), c.getYLocation());
        
        return computeSteerCompensateTime(distCLine, followOrient, c.minSpeedmm);
    }
    private double[] computeSteerCompensateTime(int distCLine, double followOrient, int speed){
        double[] data = new double[2];
        double time = 0;
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
        double deltaX = (deets.xLocHistory[0]-deets.xLocHistory[1])/(deets.timeStampHist[0]-deets.timeStampHist[1]);
        double deltaY = (deets.yLocHistory[0]-deets.yLocHistory[1])/(deets.timeStampHist[0]-deets.timeStampHist[1]);
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
        
    }
    
    private void validateDataHistory(CarDetails deets){
        boolean[] valid = new boolean[deets.timeStampHist.length];
        for(int i=valid.length-1; i>=0; i--){
            valid[i] = false;
        }
        for(int i=0; i<valid.length; i++){
            valid[i] = (deets.timeStampHist[i] > 0) && 
                    (deets.timeStampHist[i] > deets.timeStampHist[i+1]) &&
                    track.isWithinTrack(deets.xLocHistory[i], deets.yLocHistory[i]);       
        }
    }
    
    public void setVerboseOutput(boolean v){
        verboseOutput = v;
    }
}
