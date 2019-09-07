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
public class TrackSegment {
    private static final String SEGSHAPE_CIRCULAR = "CIRCULAR";
    private static final String SEGSHAPE_LINEAR = "LINEAR";
    
    private TrackSegment nextSeg = null;
    private TrackSegment prevSeg = null;
    
    private String segShape = "linear";
    private int direction = 0;
    private int width = 0;
    private int length = 0;
    private int radius = 0;
    private int curveDirection = 0;
    
    //location of the entrace of segment;
    public int absoluteXloc = 0;
    public int absoluteYloc = 0;
    
    public TrackSegment(){
        
    }
    
    public void createCircleSegment(int length, int width, int radius, int direction, int curved){
        this.length = length;
        this.width = width;
        this.radius = radius;
        this.direction = direction;
        this.segShape = SEGSHAPE_CIRCULAR;
        curveDirection = curved;
    }
    
    public void createLineSegment(int length, int width, int direction){
        this.length = length;
        this.width = width;
        this.direction = direction;
        this.segShape = SEGSHAPE_LINEAR;
    }
    
    public void setAbsoluteLocation(int x, int y){
        this.absoluteXloc = x;
        this.absoluteYloc = y;
    }
    
    public int distFromCenterLine(int xLoc, int yLoc){
        int centerDistance = 0;
        
        switch(segShape){
            case SEGSHAPE_LINEAR:
                distFromCenterLineLinear(xLoc, yLoc);
                break;
            case SEGSHAPE_CIRCULAR:
                distFromCenterLineCircular(xLoc, yLoc);
                break;
        }
        return centerDistance;
    }
    
    private int distFromCenterLineLinear(int xLoc, int yLoc){
        int centerDistance = 0;
        
        return centerDistance;
    }
    private int distFromCenterLineCircular(int xLoc, int yLoc){
        int centerDistance = 0;
        
        return centerDistance;
    }
    
    public void connectSegments(TrackSegment next){
        nextSeg = next;
    }
    public void connectSegments(TrackSegment prev, TrackSegment next){
        prevSeg = prev;
        nextSeg = next;
    }
    public TrackSegment getNextSeg(){
        return nextSeg;
    }
    public TrackSegment getPrevSeg(){
        return prevSeg;
    }
    
}
