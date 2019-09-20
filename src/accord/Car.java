/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accord;

import java.nio.ByteBuffer;
/**
 *
 * @author Paolo
 */
public class Car {
    private int carID = 0;
    private int xloc = 0;
    private int yloc = 0;
    private double orient = 0;
    private int xdimen = 0;
    private int ydimen = 0;
    private int speed = 0;
    
    private PozyxSerialComm pozyx = null;
    
    Car(int ID, PozyxSerialComm pozyx){
        carID = ID;
        this.pozyx = pozyx;
    }
    public void updateLocation(){
        Coordinates coor = pozyx.getCoordinates(carID);
        if(coor!=null && coor.ID==carID){
            xloc = (int)coor.x;
            yloc = (int)coor.y;
            orient = coor.eulerAngles[0];
            
        }
    }
    public void updateOrientation(){
        
    }
    public int getXLocation(){
        return xloc;
    }
    public int getYLocation(){
        return yloc;
    }
    public double getOrientation(){
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
    public boolean adjustSpeed(int speed){
        byte[] message = ByteBuffer.allocate(2).putShort((short)carID).array();
        return true;
    }
    public boolean adjustSteering(int steer){
        
        return true;
    }
    public void setPozyxComm(PozyxSerialComm pozyx){
        this.pozyx = pozyx;
    }
}
