/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accord;

/**
 *
 * @author Paolo
 */
public class Car {
    private int carID = 0;
    private int xloc = 0;
    private int yloc = 0;
    private int orient = 0;
    private int xdimen = 0;
    private int ydimen = 0;
    private int speed = 0;
    
    public void updateLocation(){
        
    }
    public int getXLocation(){
        return xloc;
    }
    public int getYLocation(){
        return yloc;
    }
    public int getOrientation(){
        return orient;
    }
    public int getSpeed(){
        return speed;
    }
    public CarDetails getFullDetails(){
        CarDetails deets = new CarDetails();
        deets.carID = carID;
        deets.xloc = xloc;
        deets.yloc = yloc;
        deets.orient = orient;
        deets.speed = speed;
        deets.xdimen = xdimen;
        deets.ydimen = ydimen;
                
        return deets;
    }
}
