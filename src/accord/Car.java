/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Clock;
import java.time.LocalTime;
import java.util.Arrays;
import simulator.SimulationConstants;
import vehicle.VehicleProperty;
/**
 *
 * @author Paolo
 */
public class Car implements SimulationConstants{
    private int carID = 0;
    private int xloc = 0;
    private int yloc = 0;
    private double orient = 0;
    private int xdimen = 0;
    private int ydimen = 0;
    private double speed = 0;
    private double xAxisCalib = 0;
    
    //car stati
    public boolean outOfBounds = false;
    public final int MVSTATUS_NORMAL = 1;
    public final int MVSTATUS_WAIT_UNTIL_ORIENT = 2;
    private int movementStatus = MVSTATUS_NORMAL;
    
    //throttle and steering control
    private int steering_power = 0;
    private int throttle_power = 0;
    private double maintain_orient = 0;
    private double temp_orient = 0;
    public int speedLimit = 80;
    public int speedFloor = 30;
    public int THROTTLE_INCREMENT_STEP = speedLimit-speedFloor/5;
    public int STEERING_INCREMENT_STEP = 10;
    public int minSpeedmm = 300; //mm/s
    
    //For Communications
    private static final byte carIDLen = 2;
    private static final byte minMessageLen = 2;
    private static final byte SET_SPEED = 2;
    private static final byte SET_STEERING = 4;
    private static final byte SET_ORIENT = 7;
    private static final byte SET_ORIENT_TIMED = 8;
    private static final int REDUNDANT_MSG_RESEND = 0;
    private static final String POZYX_ONLINE = "Pozyx Tag online";
    private static final String POZYX_OFFLINE = "Pozyx Tag OFFLINE";
    private String pozyxStatus = POZYX_OFFLINE;
    private PozyxSerialComm pozyx = null;
    
    //positioning and movement data
    private final int historyLength = 5;
    private int[] xLocHistory = new int[historyLength];
    private int[] yLocHistory = new int[historyLength];
    private double[] orientHistory = new double[historyLength];
    private double[] timeStampHist = new double[historyLength];
    private int[] routeDirections = new int[16];
    private int routeCount = 0;
    LocalTime timeSync = null;
    
    public boolean verbose = false;
    Car(){
        Arrays.fill(xLocHistory, 0);
        Arrays.fill(yLocHistory, 0);
        Arrays.fill(orientHistory, 0);
        Arrays.fill(timeStampHist, 0);
        Arrays.fill(routeDirections, 0);
    }
    Car(int ID, PozyxSerialComm pozyx){
        carID = ID;
        this.pozyx = pozyx;
        Arrays.fill(xLocHistory, 0);
        Arrays.fill(yLocHistory, 0);
        Arrays.fill(orientHistory, 0);
        Arrays.fill(timeStampHist, 0);
    }
    public boolean updateLocation(){
        if(pozyx == null)
            return false;
        
        Coordinates coor = pozyx.getCoordinates(carID);
        if(coor!=null){
            boolean retVal = false;
            pozyxStatus = POZYX_ONLINE;
            //if(coor.x>=0  && coor.y>=0 && coor.timeStamp!=timeStampHist[0]){
            if(coor.timeStamp>timeStampHist[0]){
            //if(true){
                adjustHistory();
                xLocHistory[0] =  ((int)coor.x + xloc)/2;
                yLocHistory[0] =  ((int)coor.y + yloc)/2;
                orientHistory[0] =  ((5759-coor.eulerAngles[0])*360)/5759;
                timeStampHist[0] =  coor.timeStamp;
                
                xloc = (xLocHistory[0] + xLocHistory[1])/2;
                yloc = (yLocHistory[0] + yLocHistory[1])/2;
                orient = orientHistory[0];
                
                calculateSpeed();
                updated = true;
                if(verbose)
                    printCarAttributes();
                //outputCSV("Coordinates Update");
                outputCarStateCSV("valid coor update");
                retVal = true;
            }
            else{
                updated = false;
                outputCarStateCSV("INVALID coor update");
                retVal = false;
            }
                //adjustSteering(0);
                //throttleDecrement();
            outputCoordinatesCSV(coor);    
            return retVal;
        }
        else {
            updated = false;
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
    public double getSpeed(){
        //return speed;
        return minSpeedmm;
    }
    public int getSteeringPower(){
        return steering_power;
    }
    public int getThrottlePower(){
        return throttle_power;
    }
    public double getMaintainOrient(){
        return maintain_orient;
    }
    public double getTempOrient(){
        return temp_orient;
    }
    public CarDetails getFullDetails(){
        CarDetails deets = new CarDetails();
        deets.carID = carID;
        deets.xloc = xloc;
        deets.yloc = yloc;
        deets.orient = getOrientation();
        deets.speed = getSpeed();
        deets.xdimen = xdimen;
        deets.ydimen = ydimen;
        deets.xLocHistory = Arrays.copyOf(xLocHistory, xLocHistory.length);
        deets.yLocHistory = Arrays.copyOf(yLocHistory, yLocHistory.length);
        deets.orientHistory = Arrays.copyOf(orientHistory, orientHistory.length);
        deets.timeStampHist = Arrays.copyOf(timeStampHist, timeStampHist.length);
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
    private boolean updated = false;
    public boolean isUpdated(){
        return updated;
    }
    public double timeSinceLastUpdate(){
        return 500;
    }
    public double getLastTimeStamp(){
        return timeStampHist[0];
    }
    public LocalTime timeStampToLocalTime(double time){
        return pozyx.syncTimePlusMs(time);
    }
    public int getMovementStatus(){
        return movementStatus;
    }
    
    public double calculateSpeed(){
        if(timeStampHist[0] - timeStampHist[timeStampHist.length-1] == 0)
            return 0;
        //double xspeed = ((xLocHistory[0] - xLocHistory[xLocHistory.length-1])*1000) / (timeStampHist[0] - timeStampHist[timeStampHist.length-1]);
        //double yspeed = ((yLocHistory[0] - yLocHistory[yLocHistory.length-1])*1000) / (timeStampHist[0] - timeStampHist[timeStampHist.length-1]);
        double xspeed = ((xLocHistory[0] - xLocHistory[1])*1000) / (timeStampHist[0] - timeStampHist[1]);
        double yspeed = ((yLocHistory[0] - yLocHistory[1])*1000) / (timeStampHist[0] - timeStampHist[1]);
        speed = Math.sqrt((xspeed*xspeed) + (yspeed*yspeed));
        if(verbose){
            System.out.println("Car " + Integer.toHexString(carID) + " x speed: " + xspeed);
            System.out.println("Car " + Integer.toHexString(carID) + " y speed: " + yspeed);
        }
        return speed;
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
        //if(throttle_power!=throttle || redundant_throttle==REDUNDANT_MSG_RESEND){
        //outputCSV("throttle");
        outputCarStateCSV("throttle");
        if(true){
            if(throttle > speedLimit)
                throttle_power = speedLimit;
            else if(throttle > 0 && throttle <= speedFloor)
                throttle_power = speedFloor;
            else if(throttle <= 0)
                throttle_power = 0;
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
        //outputCSV("steering");
        outputCarStateCSV("steering");
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
    public boolean maintainOrientation(double orient, boolean overwrite){
        boolean retVal = false;
        //outputCSV("orient maintain");
        
        if(true || redundant_orient>=REDUNDANT_MSG_RESEND){
            maintain_orient = orient;
            if(overwrite)
                temp_orient = maintain_orient;
            double orientUncalib = (orient + xAxisCalib)%360;
            int orientInt = (int)((360-orientUncalib)*5759)/360;
            //System.out.println("Uncalib = " + orientUncalib + "\tInt = " + orientInt);
            byte[] orientdata = ByteBuffer.allocate(2).putShort((short)(orientInt)).array();
            byte[] id = ByteBuffer.allocate(carIDLen).putShort((short)(carID)).array();
            byte[] message = new byte[carIDLen + minMessageLen +3];            
            System.arraycopy(id, 0, message, 0, id.length);
            message[carIDLen] = minMessageLen+2;
            message[carIDLen+1] = SET_ORIENT;
            System.arraycopy(orientdata, 0, message, carIDLen + minMessageLen, orientdata.length);
            if(overwrite)
                message[carIDLen + minMessageLen + 2] = (byte)0xff;
            else
                message[carIDLen + minMessageLen + 2] = 0;
            pozyx.sendCarCommand(message, false);
            redundant_orient = 0;
            retVal = true;
        }
        else
            redundant_orient++;
        
        outputCarStateCSV("orient maintain");
        return retVal;
        
    }
    private int redundant_orient_timed = 0;
    public boolean maintainOrientationTimed(double orient, double time){
        boolean retVal = false;
        //outputCSV("orient timed");
        if(true || redundant_orient_timed>=REDUNDANT_MSG_RESEND){
            temp_orient = orient;
            double orientUncalib = (orient + xAxisCalib)%360;
            int orientInt = (int)((360-orientUncalib)*5759)/360;
            //System.out.println("Uncalib = " + orientUncalib + "\tInt = " + orientInt);
            byte[] orientdata = ByteBuffer.allocate(2).putShort((short)(orientInt)).array();
            byte[] timedata = ByteBuffer.allocate(4).putInt((int)time).array();
            byte[] id = ByteBuffer.allocate(carIDLen).putShort((short)(carID)).array();
            byte[] message = new byte[carIDLen + minMessageLen +6];            
            System.arraycopy(id, 0, message, 0, id.length);
            message[carIDLen] = minMessageLen+6;
            message[carIDLen+1] = SET_ORIENT_TIMED;
            System.arraycopy(orientdata, 0, message, carIDLen + minMessageLen, orientdata.length);
            System.arraycopy(timedata, 0, message, carIDLen + minMessageLen + 2, timedata.length);
            pozyx.sendCarCommand(message, false);
            redundant_orient_timed = 0;
            retVal = true;
        }
        else
            redundant_orient_timed++;
        retVal = true;
        
        outputCarStateCSV("orient timed");
        
        return retVal;
    }
    public boolean throttleIncrement(){
        if(throttle_power <= 0)
            return adjustThrottle(speedFloor);
        else
            return adjustThrottle(throttle_power + THROTTLE_INCREMENT_STEP);
    }
    public boolean throttleDecrement(){
        if(throttle_power <= speedFloor)
            return adjustThrottle(0);
        else
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
    
    public int getNextRouteDirection(){
        if(routeCount > 0)
            return routeDirections[0];
        else
            return STRAIGHT;
    }
    public boolean addRouteDirection(int dir){
        if(routeCount >= routeDirections.length)
            return false;
        routeDirections[routeCount] = dir;
        routeCount++;
        return true;
    }
    public void advanceRoute(){
        for(int i=0; i<routeCount; i++){
            routeDirections[i] = routeDirections[i+1];
        }
        routeCount--;
        if(routeCount < 0)
            routeCount = 0;
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
    public void setDataHistory(int[] xHist, int[] yHist, double[] orientHist, double[] timeHist){
        xLocHistory = Arrays.copyOf(xHist, historyLength);
        yLocHistory = Arrays.copyOf(yHist, historyLength);
        orientHistory = Arrays.copyOf(orientHist, historyLength);
        timeStampHist = Arrays.copyOf(timeHist, historyLength);
        if(verbose){
            System.out.println("x history: " + Arrays.toString(xLocHistory));
            System.out.println("y history: " + Arrays.toString(yLocHistory));
            System.out.println("orient history: " + Arrays.toString(orientHistory));
            System.out.println("time history: " + Arrays.toString(timeStampHist));
        }
    }
    public int getXDimension(){
        return xdimen;
    }
    public int getYDimension(){
        return ydimen;
    }
    public void setLocation(int x, int y, double time){
        xloc = x;
        yloc = y;
    }
    public void printCarAttributes(){
        //if(verbose){
            System.out.print("ID: " + Integer.toHexString(getID()));
            System.out.print("\tUpdated: " + isUpdated());
            System.out.print("\tTimeStamp: " + (int)getLastTimeStamp());
            System.out.print("\tX = " + getXLocation());
            System.out.print("\tY = " + getYLocation());
            System.out.print("\tOrient: " + (int)getOrientation());
            System.out.print("\tOut of Bounds: " + outOfBounds);
            System.out.print("\tThrottle: " + getThrottlePower());
            System.out.print("\tMaintain: " + (int)getMaintainOrient());
            System.out.println("\tTemp: " + (int)getTempOrient());
        //}
    }
    FileWriter writer = null;
    public void setFileWriter(FileWriter fw){
        writer = fw;
        try{
        writer.append("Car ID,");
        writer.append("Source of change");
        writer.append("Updated,");
        writer.append("Time Stamp,");
        writer.append("X,");
        writer.append("Y,");
        writer.append("Orientation,");
        writer.append("Out of Bounds,");
        writer.append("Throttle Power,");
        writer.append("Maintain Orientation,");
        writer.append("Temp Orientation\n");
        
        }catch(Exception e){if(verbose)System.out.println("File Error");};
    }
    
    FileWriter fwCoordinates = null;
    String coordinatesPath = "";
    FileWriter fwCarState = null;
    String carStatePath = "";
    public boolean setCSVOutput(String path){
        File filePath = new File(path);
        if(filePath.isDirectory()){
            try{
                coordinatesPath = path + "\\CoordinatesUpdate";
                carStatePath = path + "\\CarState";
                File coorPath = new File(coordinatesPath);
                File carPath = new File(carStatePath);
                carPath.mkdirs();
                coorPath.mkdirs();
                
                fwCoordinates = new FileWriter(coordinatesPath + "\\0x" + Integer.toHexString(carID) + "_CoordinatesUpdate.csv");
                initCSVCoorHeaders();
                fwCarState = new FileWriter(carStatePath + "\\0x" + Integer.toHexString(carID) + "_CarState.csv");
                initCSVCarStateHeaders();
                return true;
            }catch(Exception e){
                return false;
            }
        }
        else{
            if(verbose)
                System.out.println("Car: ERROR! File path for csv files does not exist");
            return false;
        }
    }
    public void outputCSV(String source){
        if(writer != null){
            try{
            writer.append(Integer.toHexString(getID())+",");
            writer.append(source + ",");
            writer.append(Boolean.toString(isUpdated()) + ",");
            writer.append(Integer.toString((int)getLastTimeStamp()) + ",");
            writer.append(Integer.toString(getXLocation()) + ",");
            writer.append(Integer.toString(getYLocation()) + ",");
            writer.append(Integer.toString((int)getOrientation()) + ",");
            writer.append(Boolean.toString(outOfBounds) + ",");
            writer.append(Integer.toString(getThrottlePower()) + ",");
            writer.append(Integer.toString((int)getMaintainOrient()) + ",");
            writer.append(Integer.toString((int)getTempOrient()) + "\n");
            writer.flush();
            }catch(Exception e){
                if(verbose){
                    System.out.println("Car ERROR: ID: " + getID() +" Failed to print csv");
                }
            }
        }
    }
    private void initCSVCoorHeaders(){
        if(fwCoordinates != null){
            try{
                fwCoordinates.append("Car ID");
                fwCoordinates.append(",Time Stamp");
                fwCoordinates.append(",X");
                fwCoordinates.append(",Y");
                fwCoordinates.append(",Z");
                fwCoordinates.append(",Euler Angle[0]");
                fwCoordinates.append(",Euler Angle[1]");
                fwCoordinates.append(",Euler Angle[2]\n");
                fwCoordinates.flush();
            }catch(Exception e){}
        }
        else if(verbose)
            System.out.println("Car: ERROR! Coordinates file writer NULL");
    }
    private void outputCoordinatesCSV(Coordinates coor){
        if(fwCoordinates != null && coor!= null){
            try{
                
                fwCoordinates.append(Integer.toHexString(coor.ID));
                fwCoordinates.append("," + Double.toString(coor.timeStamp));
                fwCoordinates.append("," + Double.toString(coor.x));
                fwCoordinates.append("," + Double.toString(coor.y));
                fwCoordinates.append("," + Double.toString(coor.z));
                fwCoordinates.append("," + Double.toString(coor.eulerAngles[0]));
                fwCoordinates.append("," + Double.toString(coor.eulerAngles[1]));
                fwCoordinates.append("," + Double.toString(coor.eulerAngles[2]) + "\n");
                fwCoordinates.flush();
                //fwCoordinates.close();
            }catch(Exception e){
                if(verbose)
                    System.out.println("Car: ERROR! Failed to write Coordinates csv");
            }
        }
        else if(verbose)
            System.out.println("Car: ERROR! Coordinates file writer NULL");
    }
    private void initCSVCarStateHeaders(){
        try{
        fwCarState.append("Local Time,");
        fwCarState.append("Car ID,");
        fwCarState.append("Source of change,");
        fwCarState.append("Updated,");
        fwCarState.append("Time Stamp,");
        fwCarState.append("X,");
        fwCarState.append("Y,");
        fwCarState.append("Orientation,");
        fwCarState.append("Out of Bounds,");
        fwCarState.append("Throttle Power,");
        fwCarState.append("Maintain Orientation,");
        fwCarState.append("Temp Orientation\n");
        
        fwCarState.flush();
        }catch(Exception e){if(verbose)System.out.println("Car: ERROR! Car State file writer NULL");};
    }
    private void outputCarStateCSV(String source){
        try{
        fwCarState.append((LocalTime.now(Clock.systemDefaultZone())).toString() + ",");
        fwCarState.append(Integer.toHexString(getID())+",");
        fwCarState.append(source + ",");
        fwCarState.append(Boolean.toString(isUpdated()) + ",");
        fwCarState.append(Integer.toString((int)getLastTimeStamp()) + ",");
        fwCarState.append(Integer.toString(getXLocation()) + ",");
        fwCarState.append(Integer.toString(getYLocation()) + ",");
        fwCarState.append(Integer.toString((int)getOrientation()) + ",");
        fwCarState.append(Boolean.toString(outOfBounds) + ",");
        fwCarState.append(Integer.toString(getThrottlePower()) + ",");
        fwCarState.append(Integer.toString((int)getMaintainOrient()) + ",");
        fwCarState.append(Integer.toString((int)getTempOrient()) + "\n");
        
        fwCarState.flush();
        
        }catch(Exception e){if(verbose)System.out.println("Car: ERROR! Car State file writer NULL");};
    }
    
}
