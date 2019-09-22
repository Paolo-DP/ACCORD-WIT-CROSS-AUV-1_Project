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
    
    public void addCar(Car c){
        carList.add(c);
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
                c.adjustSteering(steer);
            }
        }
    }
}
