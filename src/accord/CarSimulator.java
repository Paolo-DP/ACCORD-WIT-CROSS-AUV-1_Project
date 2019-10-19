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
    private int minDistanceToCorrect = 10;
    ArrayList <Car> carList = new ArrayList<Car>();
    Track track = null;
    
    private static final double MIN_TIME_TO_COLLISION = 1000; //ms until collision
    private static final double MAX_TIME_TO_COLLISION = 3000;
    public void addCar(Car c){
        carList.add(c);
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
                c.updateLocation();
                int distCLine = track.distfromCenterLine(c);
                int steer=0;
                if(Math.abs(distCLine)>minDistanceToCorrect){
                    steer = 127;
                    if(distCLine>0);
                        steer *= -1;
                    
                }
                else
                    steer=0;
                c.adjustSteering(computeNextSteering(c));
                c.adjustSpeed(computeNextThrottle(c));
            }
        }
    }
    private int computeNextSteering(Car c){
        int steer = 0;
        int distCLine = track.distfromCenterLine(c);
        if(Math.abs(distCLine)>minDistanceToCorrect){
            steer = 127;
            if(distCLine>0);
                steer *= -1;
        }
        else
            steer=0;
        return steer;
    }
    private int computeNextThrottle(Car c){
        int throttle = c.getThrottlePower();
        double frontCollision = checkFront(c);
        if(frontCollision == -1){
            return throttle;
        }
        double RearCollision = checkRear(c);
        if(RearCollision == -1){
            return throttle;
        }
        if(frontCollision <= MIN_TIME_TO_COLLISION)
            throttle -= Car.THROTTLE_INCREMENT_STEP;
        else if(frontCollision >= MAX_TIME_TO_COLLISION)
            throttle += Car.THROTTLE_INCREMENT_STEP;
        
        return throttle;
    }
    private double checkFront(Car c){
        double timeToCollision = -1;
        return timeToCollision;
    }
    private double checkRear(Car c){
        double timeToCollision = -1;
        return timeToCollision;
    }
}
