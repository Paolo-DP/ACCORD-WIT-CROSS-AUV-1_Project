/*
 * The MIT License
 *
 * Copyright 2019 Paolo.
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

/**
 *
 * @author Paolo
 */
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JFrame;

public class Visualizer extends Canvas{
    private int window_height = 900;
    private int window_width = 1600;
    private float track_scale = 1;
    private float aspect_ratio = 16/9;
    private Track track = null;
    private ArrayList <Car> carList = null;
    
    private JFrame frame;
    
    private boolean verbose = false;
    
    Visualizer() {
        frame = new JFrame("ACCORD Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(window_width, window_height);
        frame.add(this);
        frame.pack();
        frame.setVisible(true);
    }
    Visualizer(int width, int height){
        JFrame frame = new JFrame("ACCORD Visualizer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window_width = width;
        window_height = height;
        setSize(window_height, window_height);
        frame.add(this);
        frame.pack();
        frame.setVisible(true);
    }
    public void setTrack(Track tr){
        track = tr;
        int[][] bounds = track.getTrackBounds();
        track_scale = window_height / (bounds[1][1] - bounds[0][1]);
        aspect_ratio = (float)(bounds[1][0] - bounds[0][0]) / (float)(bounds[1][1] - bounds[0][1]);
        resizeFrame();
    }
    public void setCarList(ArrayList <Car> cList){
        carList = cList;
    }
    
    public void paint(Graphics g) {
        this.setBackground(Color.GRAY);
        drawTrack(g);
        drawCars(g);
    }
    private void drawTrack(Graphics g){
        if(track!=null){
            TrackSegment seg;
            for(int i=0; i<track.getNSegments(); i++){
                seg = track.getSegment(i);
                switch(seg.getSegShape()){
                    case TrackSegment.SEGSHAPE_LINEAR:
                        g.drawLine(
                            (int)(seg.getXLocation() * track_scale),
                            window_height - (int)(seg.getYLocation() * track_scale),
                            (int)(seg.getExitXLocation() * track_scale),
                            window_height - (int)(seg.getExitYLocation() * track_scale));
                        break;
                    case TrackSegment.SEGSHAPE_90DEGTURN:
                        g.fillArc(
                            (int)(seg.getCircleCenterX() * track_scale), 
                            window_height - (int)(seg.getCircleCenterY() * track_scale),
                            (int)(seg.getRadius() * track_scale), 
                            (int)(seg.getRadius() * track_scale), 
                            (int)seg.getSegDirection() - 90, 
                            (int)seg.getSegDirection() - 90 + (90 * seg.getCurveDirection()));
                            
                        break;
                    default:
                        break;
                }
            }
        }
        else{
            if(verbose)
                System.out.println("Visualizer: track object is NULL");
        }
    }
    private void resizeFrame(){
        if(track != null){
            window_width = (int)((float)window_height*aspect_ratio);
            setSize(window_width,window_height);
            frame.pack();
        }
    }
    
    private void drawCars(Graphics g){
        
    }
    
    public void setVerboseOutput(boolean verbose){
        this.verbose = verbose;
    }
}
