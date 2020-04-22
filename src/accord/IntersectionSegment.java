/*
 * The MIT License
 *
 * Copyright 2020 Paolo.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package accord;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalTime;
import java.util.ArrayList;
import reservation.manager.*;
import tiles.Intersection;
import simulator.SimulationConstants;
import vehicle.VehicleProperty;
/**
 *
 * @author Paolo
 */
public class IntersectionSegment extends TrackSegment implements SimulationConstants{
    private int dimensionSize = 0;
    private int resolution = 2;
    public int timeBaseNs = 10 * 1000000;
    private int absoluteXLoc = 0;
    private int absoluteYLoc = 0;
    
    private int boundaryMargin = 10;
    private int xHitBox1 = 0;
    private int yHitBox1 = 0;
    private int xHitBox2 = 0;
    private int yHitBox2 = 0;
    
    ReservationManager resMan = new ReservationManager();
    TrackSegment[] entrance = new TrackSegment[4];
    /**
     * [0] starts with 0 degrees (eastbound)
     * [1] 90 degrees (northbound)
     * [2] 180 degrees (westbound)
     * [3] 270 degrees (southbound)
     */
    TrackSegment[] exit = new TrackSegment[4];
    
    
    /* 
        index [0][*] starting at 0 degrees (West entrance]
        index 0 left, 1 straight, 2 right
    */
    TrackSegment[][][] intersectSegs = new TrackSegment[4][3][];
    
    ArrayList<IntersectionSlot> slots = new ArrayList<>();
    
    Intersection sect;
    
    public static final String strLEFTTURN = "LEFT TURN";
    public static final String strRIGHTTURN = "RIGHT TURN";
    public static final String strSTRAIGHT = "STRAIGHT";
    
    private boolean verbose = false;
    private LocalTime verboseTimer = LocalTime.now();
    IntersectionSegment(int size_mm){
        setIsIntersection(true);
        dimensionSize = size_mm;
        double currDirection = 0;
        for(int ent=0; ent<intersectSegs.length; ent++){
            for(int turn=0; turn<intersectSegs[ent].length; turn++){
                switch(turn){
                    case 0:
                        intersectSegs[ent][turn] = new TrackSegment[3];
                        intersectSegs[ent][turn][0] = new TrackSegment();
                        intersectSegs[ent][turn][0].createLineSegment(dimensionSize/2, dimensionSize/2, currDirection);
                        intersectSegs[ent][turn][0].setSegmentID("Intersection "+ent+turn+0+"\t direction: " + currDirection);
                        intersectSegs[ent][turn][1] = new TrackSegment();
                        intersectSegs[ent][turn][1].create90DegTurn((int)(dimensionSize/4), true, dimensionSize/2, currDirection);
                        intersectSegs[ent][turn][1].setSegmentID("Intersection "+ent+turn+1+"\t direction: " + currDirection);
                        intersectSegs[ent][turn][2] = new TrackSegment();
                        intersectSegs[ent][turn][2].createLineSegment(dimensionSize/2, dimensionSize/2, currDirection + 90);
                        intersectSegs[ent][turn][2].setSegmentID("Intersection "+ent+turn+2+"\t direction: " + currDirection);
                        break;
                    case 1:
                        intersectSegs[ent][turn] = new TrackSegment[1];
                        intersectSegs[ent][turn][0] = new TrackSegment();
                        intersectSegs[ent][turn][0].createLineSegment(dimensionSize, dimensionSize/2, currDirection);
                        intersectSegs[ent][turn][0].setSegmentID("Intersection "+ent+turn+0+"\t direction: " + currDirection);
                        break;
                    case 2:
                        intersectSegs[ent][turn] = new TrackSegment[1];
                        intersectSegs[ent][turn][0] = new TrackSegment();
                        intersectSegs[ent][turn][0].create90DegTurn((int)(dimensionSize/4), false, dimensionSize/2, currDirection);
                        intersectSegs[ent][turn][0].setSegmentID("Intersection "+ent+turn+0+"\t direction: " + currDirection);
                        break;
                }
                
            }
            currDirection+=90;
            currDirection%=360;
        }
        setInternalSegmentLocations(absoluteXLoc,absoluteYLoc);
        sect = new Intersection(dimensionSize, resolution);
        
    }
    private String segmentID = "";
    @Override
    public void setSegmentID(String id){
        segmentID = id;
        for(int i=0; i<intersectSegs.length; i++){
            for(int j=0; j<intersectSegs[i].length; j++){
                for(int k=0; k<intersectSegs[i][j].length; k++){
                    intersectSegs[i][j][k].setSegmentID(segmentID + " - " + intersectSegs[i][j][k].getSegmentID());
                }
            }
        }
    }
    @Override
    public String getSegmentID(){
        return segmentID;
    }
    @Override
    public void setAbsoluteLocation(int x, int y){
        absoluteXLoc = x;
        absoluteYLoc = y;
        setInternalSegmentLocations(x, y);
        
        xHitBox1 = x - boundaryMargin;
        yHitBox1 = y - boundaryMargin;
        xHitBox2 = x + dimensionSize + boundaryMargin;
        yHitBox2 = y + dimensionSize + boundaryMargin;
        
    }
    @Override
    public int getXLocation(){
        return absoluteXLoc;
    }
    @Override
    public int getYLocation(){
        return absoluteYLoc;
    }
    private void setInternalSegmentLocations(int x, int y){
        //eastbound left
        intersectSegs[0][0][0].setAbsoluteLocation(x, y + dimensionSize/4);
        intersectSegs[0][0][1].setAbsoluteLocation(x + dimensionSize/2, y + dimensionSize/4);
        intersectSegs[0][0][2].setAbsoluteLocation(x + (int)(dimensionSize*.75), y + dimensionSize/2);
        //eastbound straight
        intersectSegs[0][1][0].setAbsoluteLocation(x, y + dimensionSize/4);
        //eastbound right
        intersectSegs[0][2][0].setAbsoluteLocation(x, y + dimensionSize/4);
        
        //northbound left
        intersectSegs[1][0][0].setAbsoluteLocation(x + (int)(dimensionSize*.75), y);
        intersectSegs[1][0][1].setAbsoluteLocation(x + (int)(dimensionSize*.75), y + dimensionSize/2);
        intersectSegs[1][0][2].setAbsoluteLocation(x + dimensionSize/2, y + (int)(dimensionSize*.75));
        //northbound straight
        intersectSegs[1][1][0].setAbsoluteLocation(x + (int)(dimensionSize*.75), y);
        //northbound right
        intersectSegs[1][2][0].setAbsoluteLocation(x + (int)(dimensionSize*.75), y);
        
        //westbound left
        intersectSegs[2][0][0].setAbsoluteLocation(x + dimensionSize, y + (int)(dimensionSize*.75));
        intersectSegs[2][0][1].setAbsoluteLocation(x + dimensionSize/2, y + (int)(dimensionSize*.75));
        intersectSegs[2][0][2].setAbsoluteLocation(x + dimensionSize/4, y + dimensionSize/2);
        //westbound straight
        intersectSegs[2][1][0].setAbsoluteLocation(x + dimensionSize, y + (int)(dimensionSize*.75));
        //westbound right
        intersectSegs[2][2][0].setAbsoluteLocation(x + dimensionSize, y + (int)(dimensionSize*.75));
        
        //southbound left
        intersectSegs[3][0][0].setAbsoluteLocation(x + dimensionSize/4, y + dimensionSize);
        intersectSegs[3][0][1].setAbsoluteLocation(x + dimensionSize/4, y + dimensionSize/2);
        intersectSegs[3][0][2].setAbsoluteLocation(x + dimensionSize/2, y + dimensionSize/4);
        //southbound straight
        intersectSegs[3][1][0].setAbsoluteLocation(x + dimensionSize/4, y + dimensionSize);
        //southbound right
        intersectSegs[3][2][0].setAbsoluteLocation(x + dimensionSize/4, y + dimensionSize);
                
    }
    @Override
    public void setBoundaryMargin(int margin){
        boundaryMargin = margin;
        for(int i=0; i<intersectSegs.length; i++){
            for(int j = 0; j<intersectSegs[i].length; j++){
                for(int k=0; k < intersectSegs[i][j].length; k++){
                    intersectSegs[i][j][k].setBoundaryMargin(margin);
                }
            }
        }
        xHitBox1 = absoluteXLoc - boundaryMargin;
        yHitBox1 = absoluteYLoc - boundaryMargin;
        xHitBox2 = absoluteXLoc + dimensionSize + boundaryMargin;
        yHitBox2 = absoluteYLoc + dimensionSize + boundaryMargin;
    }
    @Override
    public void connectNextSegment(TrackSegment next){}
    @Override
    public void connectPrevSegment(TrackSegment prev){}
    @Override
    public void connectSegments(TrackSegment prev, TrackSegment next){}
    public void connectEntranceExits(TrackSegment[] entrances, TrackSegment[] exits){
        entrance = entrances;
        exit = exits;
        
    }
    public TrackSegment[] getEntrances(){
        return entrance;
    }
    public TrackSegment[] getExits(){
        return exit;
    }
    public TrackSegment[] getRoute(int heading, int direction){
        int headIndex=-1;
        int dirIndex=-1;
        switch(heading){
            case EAST:
                headIndex = 0;
                break;
            case NORTH:
                headIndex = 1;
                break;
            case WEST:
                headIndex = 2;
                break;
            case SOUTH:
                headIndex = 3;
                break;
        }
        switch(direction){
            case LEFT_TURN:
                dirIndex = 0;
                break;
            case STRAIGHT:
                dirIndex = 1;
                break;
            case RIGHT_TURN:
                dirIndex = 2;
                break;
        }
        if(headIndex<=0 || dirIndex<=0)
            return null;
        
        return intersectSegs[headIndex][dirIndex];
    }
    public TrackSegment getExitTo(TrackSegment enterFrom, int direction){
        int segIndex = headingSegIndex(getEntranceHeading(enterFrom));
        //System.out.println("heading index = " + segIndex);
        if(segIndex < 0)
            return null;
        switch(direction){
            case LEFT_TURN:
                segIndex++;
                break;
            case RIGHT_TURN:
                if(segIndex <= 0)
                    segIndex = 3;
                else
                    segIndex--;
                break;
            case STRAIGHT:
                break;
            default:
                return null;
        }
        segIndex = (segIndex)%4;
        //System.out.println("Exit Index = " + segIndex);
        //System.out.println("Exit: " + exit[segIndex].segmentID);
        return exit[segIndex];
    }
    public TrackSegment getCurrentInternalSegment(Car c){
        IntersectionSlot slot = findSlot(c);
        if(slot==null)
            return null;
        for(int i=0; i<slot.route.length; i++){
            if(slot.route[i].isWithinBounds(c))
                return slot.route[i];
        }
        return null;
    }
    @Override
    public int distFromCenterLine(Car c){
       int dist = Integer.MAX_VALUE;
       IntersectionSlot sl = findSlot(c);
       if(sl==null)
           return Integer.MAX_VALUE;
       int maxIndex = 0;
       switch(sl.direction){
           case LEFT_TURN:
               maxIndex = 3;
               break;
           case STRAIGHT:
               maxIndex = 1;
               break;
           case RIGHT_TURN:
               maxIndex = 1;
               break;
       }
       for(int i=0; i<maxIndex; i++){
           
           if(sl.route[i].isWithinBounds(c))
               return sl.route[i].distFromCenterLine(c);
       }
       return dist;
    }
    
    @Override
    public double idealDirection(Car c){
        int dist = Integer.MAX_VALUE;
       IntersectionSlot sl = findSlot(c);
       if(sl==null)
           return Integer.MAX_VALUE;
       for(int i=0; i<sl.route.length; i++){
           if(sl.route[i].isWithinBounds(c))
               return sl.route[i].idealDirection(c);
       }
       return dist;
    }
    
    @Override
    public boolean isWithinBounds(Car c){
        int x = c.getXLocation();
        int y = c.getYLocation();
        return x >= xHitBox1 && x <= xHitBox2 &&
                y >= yHitBox1 && y <= yHitBox2;
    }
    @Override
    public boolean isWithinBounds(int xLoc, int yLoc){
        return xLoc >= xHitBox1 && xLoc <= xHitBox2 &&
                yLoc >= yHitBox1 && yLoc <= yHitBox2;
    }
    
    public TrackSegment getNextSeg(Car c){
        IntersectionSlot sl = findSlot(c);
        if(sl != null)
            return sl.exitTo;
        else
            return null;
    }
    public TrackSegment getPrevSeg(Car c){
        IntersectionSlot sl = findSlot(c);
        if(sl != null)
            return sl.enterFrom;
        else
            return null;
    }
    private IntersectionSlot findSlot(Car c){
        for(int i=0; i<slots.size(); i++){
            if(slots.get(i).car == c)
                return slots.get(i);
        }
        return null;
    }
    private void addSlot(Car c, int heading, int direction){
        if(c == null)
            return;
        
        IntersectionSlot sl = new IntersectionSlot();
        
        sl.car = c;
        sl.heading = heading;
        sl.direction = direction;
        sl.isReserved = true;
        
        switch(heading){
            case EAST:
                sl.enterFrom = entrance[0];
                switch(direction){
                    case LEFT_TURN:
                        sl.exitTo = exit[1];
                        sl.route = intersectSegs[0][0];
                        break;
                    case STRAIGHT:
                        sl.exitTo = exit[0];
                        sl.route = intersectSegs[0][1];
                        break;
                    case RIGHT_TURN:
                        sl.exitTo = exit[3];
                        sl.route = intersectSegs[0][2];
                        break;
                }
                break;
            case NORTH:
                sl.enterFrom = entrance[1];
                switch(direction){
                    case LEFT_TURN:
                        sl.exitTo = exit[2];
                        sl.route = intersectSegs[1][0];
                        break;
                    case STRAIGHT:
                        sl.exitTo = exit[1];
                        sl.route = intersectSegs[1][1];
                        break;
                    case RIGHT_TURN:
                        sl.exitTo = exit[0];
                        sl.route = intersectSegs[1][2];
                        break;
                }
                break;
            case WEST:
                sl.enterFrom = entrance[2];
                switch(direction){
                    case LEFT_TURN:
                        sl.exitTo = exit[3];
                        sl.route = intersectSegs[2][0];
                        break;
                    case STRAIGHT:
                        sl.exitTo = exit[2];
                        sl.route = intersectSegs[2][1];
                        break;
                    case RIGHT_TURN:
                        sl.exitTo = exit[1];
                        sl.route = intersectSegs[2][2];
                        break;
                }
                break;
            case SOUTH:
                sl.enterFrom = entrance[3];
                switch(direction){
                    case LEFT_TURN:
                        sl.exitTo = exit[0];
                        sl.route = intersectSegs[3][0];
                        break;
                    case STRAIGHT:
                        sl.exitTo = exit[3];
                        sl.route = intersectSegs[3][1];
                        break;
                    case RIGHT_TURN:
                        sl.exitTo = exit[2];
                        sl.route = intersectSegs[3][2];
                        break;
                }
                break;
        }
        slots.add(sl);
        if(verbose){
            System.out.print("Intersection Segment: new slot added \tCar: 0x" + Integer.toHexString(sl.car.getID()) + "\tEntrance: " + sl.enterFrom.getSegmentID() + 
                    "\tRoute " + sl.route.length + ":");
            for(int i=0; i<sl.route.length; i++){
                System.out.print("\t" + sl.route[i].getSegmentID() + ",");
            }
            System.out.println("\tExit: "+sl.exitTo.getSegmentID());
        }
    }
    
    public boolean isApproachingIntersection(TrackSegment segToCheck){
        for(int i=0; i<entrance.length; i++){
            if(entrance[i] == segToCheck)
                return true;
        }
        return false;
    }
    public boolean reserve(Car car, TrackSegment entrance, int turn){
        if(car == null || entrance == null)
            return false;
        if(findSlot(car) != null)
            return true;
        LocalTime reservationmade = LocalTime.now();
        LocalTime arrival = LocalTime.now();
        VehicleProperty vp = car.getVehicleProperty();
        int heading = getEntranceHeading(entrance);
        int direction = turn;
        boolean res = false;
        if(car.getSpeed() > 0){
            arrival = LocalTime.now().plusNanos((long)(timeToEntrance(car, entrance) * 1000000000));
            //LocalTime arrival = LocalTime.NOON;
            if(verbose)
                System.out.println("Intersection Segment: (" + LocalTime.now() + ") Processing Reservation...");
            res =  resMan.reserve(sect, arrival, vp, car.getSpeed(), 0, heading, direction, car.getID(), timeBaseNs);
            //boolean res = true;
        }
        else
            res = false;
        if(verbose)
            System.out.println("Intersection Segment: (" + LocalTime.now() + ") DONE!\tResult: " + res);
        
        if(res)
            addSlot(car, heading, direction);
        if(willOutputCSV)
            printToCSV(reservationmade, car.getID(), res, arrival, car.getXLocation(), car.getYLocation(), car.getSpeed(), car.getThrottlePower(), car.getOrientation(), car.getNextRouteDirection());
            
        return res;
        //return true;
    }
    public boolean releaseReservation(Car car){
        return resMan.remove(car.getID());
    }
    public boolean isReserved(CarTracker ct){        
        return findSlot(ct.car) != null;
    }
    public boolean isReserved(Car c){
        
        return findSlot(c) != null;
    }
    
    private int getEntranceHeading(TrackSegment enter){
        if(enter == entrance[0])
            return EAST;
        else if(enter == entrance[1])
            return NORTH;
        else if(enter == entrance[2])
            return WEST;
        else if(enter == entrance[3])
            return SOUTH;
        
        return 0;
    }
    private int headingSegIndex(int heading){
        switch(heading){
            case EAST:
                return 0;
            case NORTH:
                return 1;
            case WEST:
                return 2;
            case SOUTH:
                return 3;
            default:
                return -1;
        }
    }
    private int directionSegIndex(int direction){
        switch(direction){
            case LEFT_TURN:
                return 0;
            case STRAIGHT:
                return 1;
            case RIGHT_TURN:
                return 2;
            default:
                return -1;
        }
    }
    private double timeToEntrance(Car c, TrackSegment entrance){
        double time = 0;
        double enterX = entrance.getExitXLocation();
        double enterY = entrance.getExitYLocation();
        double x = c.getXLocation();
        double y = c.getYLocation();
        double speed = c.getSpeed();
        
        time = Math.sqrt((enterX-x)*(enterX-x) + (enterY-y)*(enterY-y)) / speed;
        
        return time;
    }
    
    public void setVerbose(boolean verb){
        verbose = verb;
    }
    public void printAllSegments(){
        if(verbose){
            for(int i=0; i<intersectSegs.length; i++){
                for(int j=0; j<intersectSegs[i].length; j++){
                    for(int k=0; k<intersectSegs[i][j].length; k++){
                        System.out.println("Segment ("+i+","+j+","+k+")\tID: " + intersectSegs[i][j][k].getSegmentID());
                    }
                }
            }
            
            /*TrackSegment[] route = intersectSegs[0][1];
            for(int i=0; i<route.length; i++){
                System.out.println("Segment (0,0," + i + " )\tID: " + route[i].getSegmentID());
            }*/
        }
    }
    
    private String CSV_path = null;
    private FileWriter fw = null;
    private boolean willOutputCSV = false;
    @Override
    public void setCSVOutput(String path){
        CSV_path = path;
        willOutputCSV = initCSVFile();
    }
    
    private boolean initCSVFile(){
        if(CSV_path == null)
            return false;
        File filePath = new File(CSV_path);
        if(filePath.isDirectory()){
            try{
                
                fw = new FileWriter(CSV_path + "\\IntersectionQueue.csv");
                String headers = "Time Stamp,Car ID,is Reserved,Arrival,X,Y,Speed,Throttle,Orientation,Route\n";
                fw.append(headers);
                fw.flush();
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
    
    private void printToCSV(LocalTime time, int CarID, boolean reserved, LocalTime arrival, int x, int y, double speed, int throttle, double orient, int route){
        if(willOutputCSV && fw != null){
            String data = time + "_" + time.getNano()+ "," 
                    + "0x" + Integer.toHexString(CarID) + ","
                    + Boolean.toString(reserved) + ","
                    + arrival.toString() + "_" + arrival.getNano() + ","
                    + x + ","
                    + y + ","
                    + speed + ","
                    + throttle + ","
                    + orient + ","
                    + route + "\n";
            try{
                fw.append(data);
                fw.flush();
            }catch(Exception e){};
        }
    }
    
    @Override
    public void createCircleSegment(int length, int width, int radius, double direction, int curved){}
    @Override
    public void createLineSegment(int length, int width, double direction){}
    @Override
    public void createLineSegment(int startX, int startY, int endX, int endY, int width, boolean absoluteLoc){}
    @Override
        public void create90DegTurn(int radius, boolean leftTurn, int width, double entryDirection){}
}