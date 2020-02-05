/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accord;

import java.nio.ByteBuffer;
import vehicle.VehicleProperty;
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
    
    private double xAxisCalib = 0;
    
    private int steering_power = 0;
    private int throttle_power = 0;
    
    public boolean outOfBounds = false;
    
    public static final int THROTTLE_INCREMENT_STEP = 1;
    public static final int STEERING_INCREMENT_STEP = 10;
    public int speedLimit=10;
    
    private static final byte carIDLen = 2;
    private static final byte minMessageLen = 2;
    private static final byte SET_SPEED = 2;
    private static final byte SET_STEERING = 4;
    private static final byte SET_ORIENT = 7;
    private static final int REDUNDANT_MSG_RESEND = 10;
    private static final String POZYX_ONLINE = "Pozyx Tag online";
    private static final String POZYX_OFFLINE = "Pozyx Tag OFFLINE";
    private String pozyxStatus = POZYX_OFFLINE;
    private PozyxSerialComm pozyx = null;
    
    private final int historyLength = 5;
    private int[] xLocHistory = new int[historyLength];
    private int[] yLocHistory = new int[historyLength];
    private double[] orientHistory = new double[historyLength];
    private double[] timeStampHist = new double[historyLength];
    
    public boolean verbose = false;
    
    Car(int ID, PozyxSerialComm pozyx){
        carID = ID;
        this.pozyx = pozyx;
    }
    public boolean updateLocation(){
        Coordinates coor = pozyx.getCoordinates(carID);
        if(coor!=null){
            pozyxStatus = POZYX_ONLINE;
            if(coor.x>=0  && coor.y>=0){
            //if(true){
                xloc = ((int)coor.x + xloc)/2;
                yloc = ((int)coor.y + yloc)/2;
                orient = ((5759-coor.eulerAngles[0])*360)/5759;

                adjustHistory();
                xLocHistory[0] =  xloc;
                yLocHistory[0] =  yloc;
                orientHistory[0] =  orient;
                timeStampHist[0] =  coor.timeStamp;
                calculateSpeed();
                return true;
            }
            else
                adjustSteering(0);
                throttleDecrement();
                return false;
        }
        else {
            pozyxStatus = POZYX_OFFLINE;
            if(verbose)
                System.out.println("Car " + carID + ": Update Error\n");
            return false;
        }
    }
    public boolean alignXAxis(){
        if(pozyx!=null){
            updateLocation();
            xAxisCalib = orient;
            return true;
        }
        else {
            if(verbose)
                System.out.println("Car " + carID + ": alignXAxis() - pozyx NULL Error");
            return false;
        }
    }
    public void updateOrientation(){
        updateLocation();
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
        if(orient<xAxisCalib)
            return 360 - (xAxisCalib-orient);
        else
            return orient - xAxisCalib;
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
    public VehicleProperty getVehicleProperty(){
        VehicleProperty vp = new VehicleProperty(
                xdimen,
                ydimen,
                xdimen*.9,
                100,
                10,
                Math.toRadians(15)
        );
        return vp;
    }
    
    
    private void calculateSpeed(){
        if(timeStampHist[0] - timeStampHist[timeStampHist.length-1] == 0)
            return;
        int xspeed = ((xLocHistory[0] + xLocHistory[xLocHistory.length-1])*1000) / (int)(timeStampHist[0] - timeStampHist[timeStampHist.length-1]);
        int yspeed = ((yLocHistory[0] + yLocHistory[yLocHistory.length-1])*1000) / (int)(timeStampHist[0] - timeStampHist[timeStampHist.length-1]);
        speed = (int)Math.sqrt((xspeed*xspeed) + (yspeed*yspeed));
    }
    private void adjustHistory(){
        for(int i=historyLength-1; i>0; i--){
            xLocHistory[i] =  xLocHistory[i-1];
            yLocHistory[i] =  yLocHistory[i-1];
            orientHistory[i] =  orientHistory[i-1];
            timeStampHist[i] =  timeStampHist[i-1];
        }
                
    }
    private int redundant_throttle = 0;
    public boolean adjustThrottle(int throttle){
        if(throttle_power!=throttle || redundant_throttle==REDUNDANT_MSG_RESEND){
            if(throttle > speedLimit)
                throttle_power = speedLimit;
            else
                throttle_power = throttle;
            byte[] message = new byte[carIDLen + minMessageLen +1];
            byte[] id = ByteBuffer.allocate(carIDLen).putShort((short)(carID)).array();
            System.arraycopy(id, 0, message, 0, id.length);
            message[carIDLen] = minMessageLen+1;
            message[carIDLen+1] = SET_SPEED;
            if(outOfBounds)
                message[carIDLen+2] = 0;
            else
                message[carIDLen+2] = (byte)throttle_power;
            byte[] ack = pozyx.sendCarCommand(message, false);
            redundant_throttle=0;
            return(ack!=null);
        }
        else{
            redundant_throttle++;
        }
        return true;
    }
    private int redundant_steer=0;
    public boolean adjustSteering(int steer){
        if(steering_power != steer || redundant_steer==REDUNDANT_MSG_RESEND){
            steering_power = steer;
            byte[] message = new byte[carIDLen + minMessageLen +1];
            byte[] id = ByteBuffer.allocate(carIDLen).putShort((short)(carID)).array();
            System.arraycopy(id, 0, message, 0, id.length);
            message[carIDLen] = minMessageLen+1;
            message[carIDLen+1] = SET_STEERING;
            message[carIDLen+2] = (byte)steer;
            byte[] ack = pozyx.sendCarCommand(message, false);
            redundant_steer=0;
            return(ack!=null);
        }
        else
            redundant_steer++;
        return true;
    }
    private int redundant_orient = 0;
    public boolean setOrientation(double orient){
        if(true || redundant_orient==REDUNDANT_MSG_RESEND){
            double orientUncalib = (orient + xAxisCalib)%360;
            int orientInt = (int)((360-orientUncalib)*5759)/360;
            byte[] orientdata = ByteBuffer.allocate(2).putShort((short)(orientInt)).array();
            byte[] id = ByteBuffer.allocate(carIDLen).putShort((short)(carID)).array();
            byte[] message = new byte[carIDLen + minMessageLen +2];            
            System.arraycopy(id, 0, message, 0, id.length);
            message[carIDLen] = minMessageLen+2;
            message[carIDLen+1] = SET_ORIENT;
            System.arraycopy(orientdata, 0, message, minMessageLen, orientdata.length);
            pozyx.sendCarCommand(message, false);
            redundant_orient = 0;
            return true;
        }
        else
            redundant_orient++;
        return true;
        
    }
    public boolean throttleIncrement(){
        return adjustThrottle(throttle_power + THROTTLE_INCREMENT_STEP);
    }
    public boolean throttleDecrement(){
        return adjustThrottle(throttle_power - THROTTLE_INCREMENT_STEP);
    }
    public boolean steeringIncrement(){
        return adjustThrottle(steering_power + STEERING_INCREMENT_STEP);
    }
    public boolean steeringDecrement(){
        return adjustThrottle(steering_power - STEERING_INCREMENT_STEP);
    }
    public void setPozyxComm(PozyxSerialComm pozyx){
        this.pozyx = pozyx;
    }
    public String getPozyxStatus(){
        return pozyxStatus;
    }
    public boolean isPozyxOnline(){
        return pozyxStatus==POZYX_ONLINE;
    }
    
    /**
     * Manually set all car attributes. Disregards pozyx data.
     * @param id Car ID
     * @param x x location
     * @param y y location
     * @param orien orientation in degrees
     * @param xdim x dimension, typically length of car
     * @param ydim y dimension, typically width of car
     * @param speed speed in mm/s
     */
    public void setAttributesManual(int id, int x, int y, double orien, int xdim, int ydim, int speed){
        carID = id;
        xloc = x;
        yloc = y;
        orient = orien;
        xdimen = xdim;
        ydimen = ydim;
        this.speed = speed;
    }
}
