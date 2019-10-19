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
    
    private int steering_power = 0;
    private int throttle_power = 0;
    
    public static final int THROTTLE_INCREMENT_STEP = 10;
    public static final int STEERING_INCREMENT_STEP = 10;
    
    private static final byte carIDLen = 2;
    private static final byte minMessageLen = 2;
    private static final byte SET_SPEED = 2;
    private static final byte SET_STEERING = 4;
    
    private PozyxSerialComm pozyx = null;
    
    Car(int ID, PozyxSerialComm pozyx){
        carID = ID;
        this.pozyx = pozyx;
    }
    public void updateLocation(){
        Coordinates coor = pozyx.getCoordinates(carID);
        if(coor!=null){
            
            xloc = (int)coor.x;
            yloc = (int)coor.y;
            orient = coor.eulerAngles[0];
            
        }
        else
            System.out.println("Update Error");
    }
    public void updateOrientation(){
        
    }
    public int getID(){
        return carID;
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
    public int getSteeringPower(){
        return steering_power;
    }
    public int getThrottlePower(){
        return throttle_power;
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
        throttle_power = speed;
        byte[] message = new byte[carIDLen + minMessageLen +1];
        byte[] id = ByteBuffer.allocate(carIDLen).putShort((short)carID).array();
        System.arraycopy(id, 0, message, 0, id.length);
        message[carIDLen] = minMessageLen+1;
        message[carIDLen+1] = SET_SPEED;
        message[carIDLen+2] = (byte)speed;
        byte[] ack = pozyx.sendCarCommand(message, true);
        return(ack!=null);
    }
    public boolean adjustSteering(int steer){
        steering_power = steer;
        byte[] message = new byte[carIDLen + minMessageLen +1];
        byte[] id = ByteBuffer.allocate(carIDLen).putShort((short)carID).array();
        System.arraycopy(id, 0, message, 0, id.length);
        message[carIDLen] = minMessageLen+1;
        message[carIDLen+1] = SET_STEERING;
        message[carIDLen+2] = (byte)steer;
        byte[] ack = pozyx.sendCarCommand(message, true);
        return(ack!=null);
    }
    public boolean throttleIncrement(){
        return adjustSpeed(throttle_power + THROTTLE_INCREMENT_STEP);
    }
    public boolean throttleDecrement(){
        return adjustSpeed(throttle_power - THROTTLE_INCREMENT_STEP);
    }
    public boolean steeringIncrement(){
        return adjustSpeed(steering_power + STEERING_INCREMENT_STEP);
    }
    public boolean steeringDecrement(){
        return adjustSpeed(steering_power - STEERING_INCREMENT_STEP);
    }
    public void setPozyxComm(PozyxSerialComm pozyx){
        this.pozyx = pozyx;
    }
}
