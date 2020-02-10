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
    
    private TrackSegment nextSeg = new TrackSegment();
    private TrackSegment prevSeg = new TrackSegment();
    
    /* 
        index [0][*] starting at 0 degrees (West entrance]
        intex 1, straight, left, right
    */
    TrackSegment[][] intersectSegs = new TrackSegment[4][3];
    
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
                intersectSegs[ent][turn] = new TrackSegment();
                switch(turn){
                    case 0:
                        intersectSegs[ent][0].create90DegTurn((int)(dimensionSize*0.75), true, dimensionSize/2, currDirection);
                        break;
                    case 1:
                        intersectSegs[ent][1].createLineSegment(dimensionSize, dimensionSize/2, currDirection);
                        break;
                    case 2:
                        intersectSegs[ent][2].create90DegTurn((int)(dimensionSize/4), false, dimensionSize/2, currDirection);
                        break;
                }
                
            }
            currDirection+=90;
            currDirection%=360;
        }
        sect = new Intersection(dimensionSize, resolution);
        
    }
    @Override
    public void connectNextSegment(TrackSegment next){
        if(next==null)
            return;
        
        nextSeg.connectPrevSegment(this);
    }
    @Override
    public void connectPrevSegment(TrackSegment prev){
        if(prev==null)
            return;
        prevSeg = prev;
    }
    @Override
    public void connectSegments(TrackSegment prev, TrackSegment next){
        prevSeg = prev;
        if(prev!=null)
            setAbsoluteLocation(prevSeg.getExitXLocation(), prevSeg.getExitYLocation());
        connectNextSegment(next);
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
        return isWithinBounds(c.getXLocation(), c.getYLocation());
    }
    
    public TrackSegment getNextSeg(Car c){
        return null;
    }
    public TrackSegment getPrevSeg(Car c){
        return null;
    }
    
    public void connectSegments(TrackSegment[] entrances, TrackSegment[] exits){
    
    }
    public boolean isApproachingIntersection(TrackSegment segToCheck){
        for(int i=0; i<entrance.length; i++){
            if(entrance[i] == segToCheck)
                return true;
        }
        return false;
    }
    public boolean reserve(Car car, TrackSegment entrance, String turn){
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
}