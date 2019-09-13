/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accord;

import java.util.*;

/**
 *
 * @author Paolo
 */
public class TrackSegment {
    private static final String SEGSHAPE_CIRCULAR = "CIRCULAR";
    private static final String SEGSHAPE_LINEAR = "LINEAR";
    private static final String SEGSHAPE_90DEGTURN = "90TURN";
    private static final String LEFTTURN = "LEFT TURN";
    private static final String RIGHTTURN = "RIGHT TURN";
    
    private TrackSegment nextSeg = null;
    private TrackSegment prevSeg = null;
    
    private String segShape = "LINEAR";
    private double direction = 0;
    private double width = 0;
    private double length = 0;
    private double radius = 0;
    private int curveDirection = 0;
    private int turn90 = 0;
    private double circleCenterX = 0;
    private double circleCenterY = 0;
    
    //Constants to describe segment as an equation
    private double lineM = 0, 
            lineA = 0,
            lineB = 0,
            lineC = 0; 
            
    //location of the entrace and exit of segment;
    private int absoluteXLoc = 0;
    private int absoluteYLoc = 0;
    private int absoluteExitXLoc = 0;
    private int absoluteExitYLoc = 0;
    
    private int boundaryMargin = 0;
    private int xHitBox1 = 0;
    private int yHitBox1 = 0;
    private int xHitBox2 = 0;
    private int yHitBox2 = 0;
    
    public void createCircleSegment(int length, int width, int radius, double direction, int curved){
        this.length = length;
        this.width = width;
        this.radius = radius;
        this.direction = direction % 360;
        this.segShape = SEGSHAPE_CIRCULAR;
        curveDirection = curved;
    }
    /**
     * Creates a new track segment in the shape of a Line.
     * @param length length of the segment
     * @param width width of the segment
     * @param direction direction of travel from the origin to the exit in degrees
     */
    public void createLineSegment(int length, int width, double direction){
        this.length = length;
        this.width = width;
        this.direction = (direction % 360);
        this.segShape = SEGSHAPE_LINEAR;
        lineM = (double)Math.tan(Math.toRadians(this.direction));
        computeOtherParameters();
    }
    /**
     * Creates a new Line Segment using two points.
     * 
     * @param startX starting point X position
     * @param startY starting point Y position
     * @param endX exit point X position
     * @param endY exit point Y position
     * @param absoluteLoc indicates if the given positions are absolute or not. This will set the segments absolute position on the track
     */
    public void createLineSegment(int startX, int startY, int endX, int endY, int width, boolean absoluteLoc){
        this.segShape = SEGSHAPE_LINEAR;
        if(absoluteLoc){
            this.absoluteXLoc = startX;
            this.absoluteYLoc = startY;
        }
        else{
            absoluteXLoc = 0;
            absoluteYLoc = 0;
        }
        this.length = Math.sqrt((startX-endX)*(startX-endX) + (startY-endY)*(startY-endY));
        this.width = width;
        if(startX == endX){
            lineM = Integer.MAX_VALUE;
            if(endY>=startY)
                direction = 90;
            else
                direction = 270;
        }
        else{
            lineM = ((double)(endY-startY))/((double)(endX-startX));
            direction = (double)Math.atan(lineM);
            if(endX<startX)
                direction = 180 + direction;
            else if(endY<startY)
                direction = (360 + direction)%360;
        }
        computeOtherParameters();
        
    }
    /**
     * Creates a 90 turn segment
     * @param radius
     * @param positive >0 will result in left turn. otherwise right turn
     * @param entryDirection 
     */
    public void create90DegTurn(int radius, boolean leftTurn, int width, double entryDirection){
        segShape = SEGSHAPE_90DEGTURN;
        this.radius = radius;
        if(leftTurn)
            turn90=1;
        else
            turn90=-1;
        this.width = width;
        direction = entryDirection;
        
        computeOtherParameters();
    }
    
    public void setAbsoluteLocation(int x, int y){
        this.absoluteXLoc = x;
        this.absoluteYLoc = y;
        computeOtherParameters();
    }
    public void setBoundaryMargin(int margin){
        boundaryMargin = margin;
    }
    
    public int distFromCenterLine(int xLoc, int yLoc){
        int centerDistance = 0;
        
        switch(segShape){
            case SEGSHAPE_LINEAR:
                centerDistance = distFromCenterLineLinear(xLoc, yLoc);
                break;
            case SEGSHAPE_CIRCULAR:
                centerDistance = distFromCenterLineCircular(xLoc, yLoc);
                break;
            case SEGSHAPE_90DEGTURN:
                centerDistance = distFromCenterLine90DegTurn(xLoc, yLoc);
                break;
        }
        return centerDistance;
    }
    public double idealDirection(int xLoc, int yLoc){
        switch(segShape){
            case SEGSHAPE_LINEAR:
                return direction;
            case SEGSHAPE_90DEGTURN:
                double dir;
                double x = xLoc - circleCenterX;
                double y = yLoc - circleCenterX;
                if(x==0)
                    dir=90;
                else
                    dir = Math.atan(Math.abs(y)/Math.abs(x));
                
                if(x<0 && y>=0)
                    dir+=90;
                else if(x<0 && y<=0)
                    dir+=180;
                else if(x>0 && y<=0)
                    dir+=270;
                
                return (dir+(turn90 * 90))%360;
            default:
                return 0;
        }
    }
    public boolean isWithinBounds(int xLoc, int yLoc){
        return (xLoc>=Math.min(xHitBox1, xHitBox2))&&
                (xLoc<=Math.max(xHitBox1, xHitBox2))&&
                (yLoc>=Math.min(yHitBox1, yHitBox2))&&
                (yLoc<=Math.max(yHitBox1, yHitBox2));
    }
    
    public void connectNextSegment(TrackSegment next){
        if(next==null)
            return;
        nextSeg = next;
        nextSeg.setAbsoluteLocation(absoluteExitXLoc, absoluteExitYLoc);
        nextSeg.connectPrevSegment(this);
    }
    private void connectPrevSegment(TrackSegment prev){
        if(prev==null)
            return;
        prevSeg = prev;
    }
    public void connectSegments(TrackSegment prev, TrackSegment next){
        prevSeg = prev;
        if(prev!=null)
            setAbsoluteLocation(prevSeg.getExitXLocation(), prevSeg.getExitYLocation());
        connectNextSegment(next);
    }
    public TrackSegment getNextSeg(){
        return nextSeg;
    }
    public TrackSegment getPrevSeg(){
        return prevSeg;
    }
    
    public int getXLocation(){
        return this.absoluteXLoc;
    }
    public int getYLocation(){
        return this.absoluteYLoc;
    }
    public int getExitXLocation(){
        return this.absoluteExitXLoc;
    }
    public int getExitYLocation(){
        return this.absoluteExitYLoc;
    }
    public int getSegLength(){
        return (int)length;
    }
    
    private int distFromCenterLineLinear(int xLoc, int yLoc){
        int x = xLoc-absoluteXLoc;
        int y = yLoc-absoluteYLoc;
        
        int centerDistance = 0;
        if(direction==0)
            centerDistance = -y;
        else if(direction==90)
            centerDistance = x;
        else if(direction==180)
            centerDistance = y;
        else if(direction==270)
            centerDistance = -x;
        else{
            centerDistance = (int) ((Math.abs(lineM*x - y))/Math.sqrt(lineM*2 + 1));
            int centerY = (int) lineM*x;
            if(((direction>270 || direction<90) && centerY < y) || 
                    (direction<270 && direction>90 && centerY > y))
                centerDistance *= -1;
        }
        
        return centerDistance;
    }
    private int distFromCenterLineCircular(int xLoc, int yLoc){
        int centerDistance = 0;
        
        return centerDistance;
    }
    private int distFromCenterLine90DegTurn(int xLoc, int yLoc){
        return (int)(Math.sqrt((xLoc-circleCenterX)*(xLoc-circleCenterX) 
                + (yLoc-circleCenterY)*(yLoc-circleCenterY)) - radius);
    }
    private void computeOtherParameters(){
        switch(segShape){
            case SEGSHAPE_LINEAR:
                //if(direction==90 || direction == 270 || lineM == Integer.MAX_VALUE)
                //    absoluteExitXLoc = absoluteXLoc;
                //else
                absoluteExitXLoc = (int)(length * Math.cos(Math.toRadians(direction))) + absoluteXLoc;
                absoluteExitYLoc = (int)(length * Math.sin(Math.toRadians(direction))) + absoluteYLoc;
                break;
            case SEGSHAPE_CIRCULAR:
                
                break;
            case SEGSHAPE_90DEGTURN:
                double dist = Math.sqrt(2*radius*radius);
                absoluteExitXLoc = (int)(dist * Math.cos(Math.toRadians(direction+(turn90*45)))) + absoluteXLoc;
                absoluteExitXLoc = (int)(dist * Math.sin(Math.toRadians(direction+(turn90*45)))) + absoluteYLoc;
                
                circleCenterX = (double)(radius * Math.cos(Math.toRadians(90 + direction))) + absoluteXLoc;
                circleCenterY = (double)(radius * Math.sin(Math.toRadians(90 + direction))) + absoluteYLoc;
                break;
        }
        simpleBoundsCheck();
    }
    /**
     * Checks if a point is within this track Segment
     * this method assumes that the segment is aligned with the x or y axis
     * @param xLoc
     * @param yLoc
     * @return 
     */
    private void simpleBoundsCheck(){
        switch(segShape){
            case SEGSHAPE_LINEAR:
                if(direction==90 || direction==270){
                    xHitBox1 = (int)(absoluteXLoc + width + boundaryMargin);
                    xHitBox2 = (int)(absoluteExitXLoc - width - boundaryMargin);
                    if(absoluteYLoc<absoluteExitYLoc){
                        yHitBox1 = absoluteYLoc - boundaryMargin;
                        yHitBox2 = absoluteExitYLoc + boundaryMargin;
                    }
                    else{
                        yHitBox1 = absoluteYLoc + boundaryMargin;
                        yHitBox2 = absoluteExitYLoc - boundaryMargin;
                    }
                }
                else{
                    yHitBox1 = (int)(absoluteYLoc + width + boundaryMargin);
                    yHitBox2 = (int)(absoluteExitYLoc - width - boundaryMargin);
                    if(absoluteXLoc<absoluteExitXLoc){
                        xHitBox1 = absoluteXLoc - boundaryMargin;
                        xHitBox2 = absoluteExitXLoc + boundaryMargin;
                    }
                    else{
                        xHitBox1 = absoluteXLoc + boundaryMargin;
                        xHitBox2 = absoluteExitXLoc - boundaryMargin;
                    }
                }
                break;
            
            case SEGSHAPE_90DEGTURN:
                xHitBox1 = absoluteXLoc;
                yHitBox1 = absoluteYLoc;
                xHitBox2 = absoluteExitXLoc;
                yHitBox2 = absoluteExitYLoc;
                break;
        }
    }
}
