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

import reservation.manager.*;
/**
 *
 * @author Paolo
 */
public class IntersectionSegment {
    private int dimensionSize = 0;
    
    ReservationManager resMan = new ReservationManager();
    TrackSegment[] entrance = null;
    TrackSegment[] exit = null;
    /* 
        index [0][*] starting at 0 degrees (West entrance]
        intex [*][0] starts from left to right
    */
    TrackSegment[][] intersectSegs = new TrackSegment[4][3]; 
    
    public static final String LEFTTURN = "LEFT TURN";
    public static final String RIGHTTURN = "RIGHT TURN";
    public static final String STRAIGHT = "STRAIGHT";
    
    IntersectionSegment(int size_mm){
        dimensionSize = size_mm;
        double currDirection = 0;
        for(int ent=0; ent<intersectSegs.length; ent++){
            for(int turn=0; turn<intersectSegs[ent].length; turn++){
                intersectSegs[ent][turn] = new TrackSegment();
                intersectSegs[ent][0].create90DegTurn((int)(dimensionSize*0.75), true, dimensionSize/2, currDirection);
                intersectSegs[ent][1].createLineSegment(dimensionSize, dimensionSize/2, currDirection);
                intersectSegs[ent][0].create90DegTurn((int)(dimensionSize/4), false, dimensionSize/2, currDirection);
            }
            currDirection+=90;
            currDirection%=360;
        }
            
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
        boolean res = false;
        
        return res;
    }
}