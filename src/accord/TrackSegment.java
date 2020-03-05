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
    public String segmentID = "Track Segment";
    public boolean isEnteringIntersection = false;
    private boolean isIntersection = false;
    
    public static final String SEGSHAPE_CIRCULAR = "CIRCULAR";
    public static final String SEGSHAPE_LINEAR = "LINEAR";
    public static final String SEGSHAPE_90DEGTURN = "90TURN";
    public static final String LEFTTURN = "LEFT TURN";
    public static final String RIGHTTURN = "RIGHT TURN";
    public static final String STRAIGHTTURN = "STRAIGHT";
    
    private TrackSegment nextSeg = null;
    private TrackSegment prevSeg = null;
    
    private String segShape = "LINEAR";
    private double direction = 0;
    private double width = 0;
    private double length = 0;
    private double radius = 0;
    private int curveDirection = 0; //1 if left turn, -1 right turn
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
    
    private int boundaryMargin = 10;
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
    
    public void setSegmentID(String id){
        segmentID = id;
    }
    public String getSegmentID(){
        return segmentID;
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
    public int distFromCenterLine(Car c){
        return distFromCenterLine(c.getXLocation(), c.getYLocation());
    }
    public double idealDirection(int xLoc, int yLoc){
        switch(segShape){
            case SEGSHAPE_LINEAR:
                return direction;
            case SEGSHAPE_90DEGTURN:
                double dir;
                double x = xLoc - circleCenterX;
                double y = yLoc - circleCenterY;
                if(x==0)
                    dir=90;
                else
                    dir = Math.toDegrees(Math.atan(Math.abs(y)/Math.abs(x)));
                
                if(x<0 && y>=0)
                    dir = 180 - dir;
                else if(x<0 && y<=0)
                    dir+=180;
                else if(x>0 && y<=0)
                    dir = 360 - dir;
                
                return (dir+(turn90 * 90))%360;
            default:
                return 0;
        }
    }
    public double idealDirection(Car c){
        return idealDirection(c.getXLocation(), c.getYLocation());
    }
    public boolean isWithinBounds(int xLoc, int yLoc){
        return (xLoc>=Math.min(xHitBox1, xHitBox2))&&
                (xLoc<=Math.max(xHitBox1, xHitBox2))&&
                (yLoc>=Math.min(yHitBox1, yHitBox2))&&
                (yLoc<=Math.max(yHitBox1, yHitBox2));
    }
    public boolean isWithinBounds(Car c){
        return isWithinBounds(c.getXLocation(), c.getYLocation());
    }
    
    public void connectNextSegment(TrackSegment next){
        if(next==null)
            return;
        nextSeg = next;
        nextSeg.setAbsoluteLocation(absoluteExitXLoc, absoluteExitYLoc);
        nextSeg.connectPrevSegment(this);
    }
    public void connectPrevSegment(TrackSegment prev){
        if(prev==null)
            return;
        prevSeg = prev;
    }
    public void connectSegments(TrackSegment prev, TrackSegment next){
        prevSeg = prev;
        nextSeg = next;
        /*
        if(prev!=null)
            setAbsoluteLocation(prevSeg.getExitXLocation(), prevSeg.getExitYLocation());
        connectNextSegment(next);
        */
    }
    public void setPrevSegment(TrackSegment prev){prevSeg = prev;}
    public void setNextSegment(TrackSegment next){nextSeg = next;}
        
    public TrackSegment getNextSeg(Car c){
        return nextSeg;
    }
    public TrackSegment getPrevSeg(Car c){
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
    public double getSegDirection(){
        return direction;
    }
    public int getCurveDirection(){
        return curveDirection;
    }
    public double getCircleCenterX(){
        return circleCenterX;
    }
    public double getCircleCenterY(){
        return circleCenterY;
    }
    public double getRadius(){
        return radius;
    }
    public String getSegShape(){
        return segShape;
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
                double dist = Math.sqrt(2)*radius;
                absoluteExitXLoc = (int)(dist * Math.cos(Math.toRadians(direction+(turn90*45)))) + absoluteXLoc;
                absoluteExitYLoc = (int)(dist * Math.sin(Math.toRadians(direction+(turn90*45)))) + absoluteYLoc;
                
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
                    xHitBox1 = (int)(absoluteXLoc - width/2 - boundaryMargin);
                    xHitBox2 = (int)(absoluteExitXLoc + width/2 + boundaryMargin);
                    if(absoluteYLoc<absoluteExitYLoc){
                        yHitBox1 = absoluteYLoc - boundaryMargin;
                        yHitBox2 = absoluteExitYLoc + boundaryMargin;
                    }
                    else{
                        yHitBox2 = absoluteYLoc + boundaryMargin;
                        yHitBox1 = absoluteExitYLoc - boundaryMargin;
                    }
                }
                else{
                    yHitBox1 = (int)(absoluteYLoc - width/2 - boundaryMargin);
                    yHitBox2 = (int)(absoluteExitYLoc + width/2 + boundaryMargin);
                    if(absoluteXLoc<absoluteExitXLoc){
                        xHitBox1 = absoluteXLoc - boundaryMargin;
                        xHitBox2 = absoluteExitXLoc + boundaryMargin;
                    }
                    else{
                        xHitBox2 = absoluteXLoc + boundaryMargin;
                        xHitBox1 = absoluteExitXLoc - boundaryMargin;
                    }
                }
                break;
            
            case SEGSHAPE_90DEGTURN:
                /*
                xHitBox1 = (int) (Math.min(absoluteXLoc, absoluteExitXLoc) - width - boundaryMargin);
                yHitBox1 = (int) (Math.min(absoluteYLoc, absoluteExitYLoc) - width -boundaryMargin);
                xHitBox2 = (int) (Math.max(absoluteXLoc, absoluteExitXLoc) + width + boundaryMargin);
                yHitBox2 = (int) (Math.max(absoluteYLoc, absoluteExitYLoc) + width + boundaryMargin);
                */
                if(turn90 == 1){
                    switch((int)direction){
                        case 0:
                            xHitBox1 = absoluteXLoc - boundaryMargin;
                            yHitBox1 = absoluteYLoc - (int)(width/2) - boundaryMargin;
                            xHitBox2 = absoluteExitXLoc + boundaryMargin;
                            yHitBox2 = absoluteExitYLoc + (int)(width/2) + boundaryMargin;
                            break;
                        case 90:
                            xHitBox1 = absoluteExitXLoc - boundaryMargin;
                            yHitBox1 = (int) (absoluteExitYLoc - radius - boundaryMargin);
                            xHitBox2 = absoluteXLoc + (int)(width/2) + boundaryMargin;
                            yHitBox2 = (int) (absoluteExitYLoc + (width/2) + boundaryMargin);
                            break;
                        case 180:
                            xHitBox1 = absoluteExitXLoc - (int)(width/2) - boundaryMargin;
                            yHitBox1 = absoluteExitYLoc - boundaryMargin;
                            xHitBox2 = absoluteXLoc + boundaryMargin;
                            yHitBox2 = absoluteYLoc + (int)(width/2) + boundaryMargin;
                            break;
                        case 270:
                            xHitBox1 = absoluteXLoc - (int)(width/2) - boundaryMargin;
                            yHitBox1 = absoluteExitYLoc - (int)(width/2) - boundaryMargin;
                            xHitBox2 = absoluteExitXLoc + boundaryMargin;
                            yHitBox2 = (int) (absoluteExitYLoc + radius + boundaryMargin);
                            break;
                    }
                }
                else if(turn90 == -1){
                    switch((int)direction){
                        case 0:
                            xHitBox1 = absoluteXLoc - boundaryMargin;
                            yHitBox1 = (int) (absoluteExitYLoc - boundaryMargin);
                            xHitBox2 = absoluteExitXLoc + (int)(width/2) + boundaryMargin;
                            yHitBox2 = absoluteYLoc + (int)(width/2) + boundaryMargin;
                            break;
                        case 90:
                            xHitBox1 = absoluteXLoc - (int)(width/2) - boundaryMargin;
                            yHitBox1 = absoluteYLoc - boundaryMargin;
                            xHitBox2 = absoluteExitXLoc + boundaryMargin;
                            yHitBox2 = (int) (absoluteExitYLoc + (width/2) + boundaryMargin);
                            break;
                        case 180:
                            xHitBox1 = absoluteExitXLoc - (int)(width/2) - boundaryMargin;
                            yHitBox1 = absoluteYLoc - (int)(width/2) - boundaryMargin;
                            xHitBox2 = absoluteXLoc + boundaryMargin;
                            yHitBox2 = absoluteExitYLoc + boundaryMargin;
                            break;
                        case 270:
                            xHitBox1 = absoluteExitXLoc - boundaryMargin;
                            yHitBox1 = absoluteExitYLoc - (int)(width/2) - boundaryMargin;
                            xHitBox2 = absoluteXLoc + (int)(width/2) + boundaryMargin;
                            yHitBox2 = absoluteExitYLoc + boundaryMargin;
                            break;
                    }
                }
                break;
        }
    }
    public boolean isIntersection(){
        return isIntersection;
    }
    public void setIsIntersection(boolean isInter){
        isIntersection = isInter;
    }
}
