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
    private int resolution = 64;
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
                        intersectSegs[ent][turn][1] = new TrackSegment();
                        intersectSegs[ent][turn][1].create90DegTurn((int)(dimensionSize/4), true, dimensionSize/2, currDirection);
                        intersectSegs[ent][turn][2] = new TrackSegment();
                        intersectSegs[ent][turn][2].createLineSegment(dimensionSize/2, dimensionSize/2, currDirection + 90);
                        break;
                    case 1:
                        intersectSegs[ent][turn] = new TrackSegment[1];
                        intersectSegs[ent][turn][0].createLineSegment(dimensionSize, dimensionSize/2, currDirection);
                        break;
                    case 2:
                        intersectSegs[ent][turn] = new TrackSegment[1];
                        intersectSegs[ent][turn][0].create90DegTurn((int)(dimensionSize/4), false, dimensionSize/2, currDirection);
                        break;
                }
                
            }
            currDirection+=90;
            currDirection%=360;
        }
        setInternalSegmentLocations(absoluteXLoc,absoluteYLoc);
        sect = new Intersection(dimensionSize, resolution);
        
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
        for(int ent=0; ent<entrances.length; ent++){
            for(int turn=0; turn<3; turn++){
                
            }
        }
    }
    @Override
    public int distFromCenterLine(Car c){
        return distFromCenterLine(c.getXLocation(), c.getYLocation());
    }
    
    @Override
    public double idealDirection(Car c){
        return idealDirection(c.getXLocation(), c.getYLocation());
    }
    
    @Override
    public boolean isWithinBounds(Car c){
        int x = c.getXLocation();
        int y = c.getYLocation();
        return isWithinBounds(c.getXLocation(), c.getYLocation());
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
    
    public boolean isApproachingIntersection(TrackSegment segToCheck){
        for(int i=0; i<entrance.length; i++){
            if(entrance[i] == segToCheck)
                return true;
        }
        return false;
    }
    public boolean reserve(Car car, TrackSegment entrance, int turn){
        VehicleProperty vp = car.getVehicleProperty();
        int heading = NORTH;
        int direction = STRAIGHT;
        resMan.reserve(sect, LocalTime.now(), vp, car.getSpeed(), 0, heading, direction, car.getID(), timeBaseNs);
        return true;
    }
    public boolean releaseReservation(Car car){
        return resMan.remove(car.getID());
    }
    
    public void setVerbose(boolean verb){
        verbose = verb;
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