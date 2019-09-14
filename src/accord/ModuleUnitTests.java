/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accord;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Scanner;
/**
 *
 * @author Paolo
 */
public class ModuleUnitTests {
    public static void testLinearTrackSegment(){
        TrackSegment testSegment = null;
        System.out.println("Testing Track Segment Class...\n");
        double[][] testLineSegments = { //length, width, direction
            {10, 5, 0},
            {10, 5, 90},
            {10, 5, 180},
            {10, 5, 270},
            {10, 5, 360},
            {10, 5, 450},
            {1000, 5, 36.8699},
            {1000, 5, 126.8699},
            {1000, 5, 216.8699},
            {1000, 5, 306.8699},
        };
        for(int i=0; i<testLineSegments.length; i++){
            System.out.println("Creating Line Segment with paramaters:");
            System.out.println("Length = " + testLineSegments[i][0]);
            System.out.println("Width = " + testLineSegments[i][1]);
            System.out.println("Direction = " + testLineSegments[i][2]);
            testSegment = new TrackSegment();
            testSegment.createLineSegment((int) testLineSegments[i][0], (int) testLineSegments[i][1], testLineSegments[i][2]);
            System.out.println("Resulting Segment astributes:");
            System.out.print("Exit Location: ");
            System.out.println(testSegment.getExitXLocation()+", "+testSegment.getExitYLocation());
            System.out.println("Length: "+ testSegment.getSegLength());
            System.out.println("Distance from point (5,5): " + testSegment.distFromCenterLine(5, 5));
            System.out.println("(2,3) Within bounds: " + testSegment.isWithinBounds(2, 3));
            System.out.print("\n\n");
        }
        
        System.out.println("Testing Track Segment Class with start and end points...\n");
        int[][] testStartEndLines = { //startx, starty, endx, endy, width, abs
            {0,0,10,0,5},
            {0,0,0,10,5},
            {0,0,-10,0,5},
            {0,0,0,-10,5}
        };
        for(int i=0; i<testStartEndLines.length; i++){
            System.out.println("Creating Line Segment with paramaters:");
            System.out.println("Start = " + testStartEndLines[i][0] + ", " + testStartEndLines[i][1]);
            System.out.println("End = " + testStartEndLines[i][2] + ", " + testStartEndLines[i][3]);
            System.out.println("Width = " + testStartEndLines[i][4]);
            testSegment = new TrackSegment();
            testSegment.createLineSegment(
                    testStartEndLines[i][0],
                    testStartEndLines[i][1],
                    testStartEndLines[i][2],
                    testStartEndLines[i][3],
                    testStartEndLines[i][4],
                    false);
            System.out.println("Resulting Segment astributes:");
            System.out.print("Exit Location: ");
            System.out.println(testSegment.getExitXLocation()+", "+testSegment.getExitYLocation());
            System.out.println("Length: "+ testSegment.getSegLength());
            System.out.println("Distance from point (5,5): " + testSegment.distFromCenterLine(5, 5));
            System.out.print("\n\n");
        }
    }
    public static void testSimpleOvalTrack(){
        Track tr = createSimpleOvalTrack(1500, 500, 100, false);
        int[][] testdata = {
            //xLoc, yLoc, expected Direction, expected dist from cetnerline
            //straight 1
            {0,0,0,0},
            {10, 5, 0, -5},
            //turn 1
            {1000, 0, 0, 0},
            {1100, 0, 22, 19},
            //turn 2
            {1250, 350, 112, 19},
            {1250, 0, 45, 104},
            //straight 2
            {0,500,180,0},
            //turn 3
            {-100,500,202,19},
            //turn 4
            {-100,0,338,19},
            {-1,-1,0,0}
        };
        System.out.println("Testing sample Oval Track");
        for(int i=0; i<testdata.length; i++){
            System.out.println("\nPoint ("
                +testdata[i][0]+", "
                +testdata[i][1]+")");
            System.out.println("Direction Deviation"
                +"\nExpected:\t"+ testdata[i][2]
                +"\nResult:\t\t" + (int)tr.idealDirection(testdata[i][0],testdata[i][1]));
            System.out.println("Distance from Center Line"
                +"\nExpected:\t"+ testdata[i][3]
                +"\nResult:\t\t" + tr.distfromCenterLine(testdata[i][0],testdata[i][1]));
                
        }
    }
    public static Track createSimpleOvalTrack(int length, int width, int roadWidth, boolean troubleshoot){
        Track tr = new Track();
        
        TrackSegment seg = new TrackSegment();
        seg.createLineSegment(length-width, roadWidth, 0);
        if(troubleshoot){
            System.out.println("Segment 1");
            System.out.println("Entry Point: (" + 
                    seg.getXLocation() + ", " + 
                    seg.getYLocation());
            System.out.println("Exit Point: (" + 
                    seg.getExitXLocation() + ", " + 
                    seg.getExitYLocation());
        }
        tr.addTrackSegment(seg);
       
        seg = new TrackSegment();
        seg.create90DegTurn(width/2, true, roadWidth, 0);
        tr.addTrackSegment(seg);
        if(troubleshoot){
            System.out.println("Segment 2");
            System.out.println("Entry Point: (" + 
                    seg.getXLocation() + ", " + 
                    seg.getYLocation());
            System.out.println("Exit Point: (" + 
                    seg.getExitXLocation() + ", " + 
                    seg.getExitYLocation());
        }
       
        seg = new TrackSegment();
        seg.create90DegTurn(width/2, true, roadWidth, 90);
        tr.addTrackSegment(seg);
        if(troubleshoot){
            System.out.println("Segment 3");
            System.out.println("Entry Point: (" + 
                    seg.getXLocation() + ", " + 
                    seg.getYLocation());
            System.out.println("Exit Point: (" + 
                    seg.getExitXLocation() + ", " + 
                    seg.getExitYLocation());
        }
       
        seg = new TrackSegment();
        seg.createLineSegment(length-width, roadWidth, 180);
        tr.addTrackSegment(seg);
        if(troubleshoot){
            System.out.println("Segment 4");
            System.out.println("Entry Point: (" + 
                    seg.getXLocation() + ", " + 
                    seg.getYLocation());
            System.out.println("Exit Point: (" + 
                    seg.getExitXLocation() + ", " + 
                    seg.getExitYLocation());
        }
       
        seg = new TrackSegment();
        seg.create90DegTurn(width/2, true, roadWidth, 180);
        tr.addTrackSegment(seg);
        if(troubleshoot){
            System.out.println("Segment 5");
            System.out.println("Entry Point: (" + 
                    seg.getXLocation() + ", " + 
                    seg.getYLocation());
            System.out.println("Exit Point: (" + 
                    seg.getExitXLocation() + ", " + 
                    seg.getExitYLocation());
        }
       
        seg = new TrackSegment();
        seg.create90DegTurn(width/2, true, roadWidth, 270);
        tr.addTrackSegment(seg);
        if(troubleshoot){
            System.out.println("Segment 6");
            System.out.println("Entry Point: (" + 
                    seg.getXLocation() + ", " + 
                    seg.getYLocation());
            System.out.println("Exit Point: (" + 
                    seg.getExitXLocation() + ", " + 
                    seg.getExitYLocation());
        }
        tr.complete();
        return tr;
    }
    public static void testCarDetails(){
        CarDetails dets = new CarDetails();
        System.out.println("xloc = "+dets.xloc);
        dets.xloc = 69;
        System.out.println("xloc = "+dets.xloc);
    }
    public static void testSerialComm(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Select Com port:");
        SerialPort[] ports = SerialPort.getCommPorts();
        for(int i=0; i<ports.length; i++){
            System.out.println(i+". " + ports[i].getSystemPortName());
        }
        System.out.print(">");
        int selectport = 0;
        do{
            selectport=sc.nextInt();
        }while(selectport<0 && selectport>=ports.length);
        
        SerialPort comPort = ports[selectport];
        if(comPort.openPort())
            System.out.println(comPort.getSystemPortName() + "Port Open Successful with Baud: " + comPort.getBaudRate());
        
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 100, 0);
        
        int byteCount;
        String test = "test string";
        //System.out.println("Writing bytes: " + test);
        //byteCount = comPort.writeBytes(test.getBytes(), test.length());
        //System.out.println(byteCount + " bytes written");
        sc.nextLine();
        /*
        try{
            while (comPort.bytesAvailable() == 0)
                Thread.sleep(20);
            byte[] buffer = new byte[1024];
            
            byteCount = comPort.readBytes(buffer, 1);
            String echo = new String(buffer);
            System.out.println(byteCount + " bytes recieved");
            System.out.println("bytes recieved: " + echo);
            
        }catch(Exception e){ e.printStackTrace();}
        */
        try {
            while (true)
            {
                System.out.println("waiting for bytes");
               while (comPort.bytesAvailable() == 0)
                  Thread.sleep(20);

               byte[] readBuffer = new byte[comPort.bytesAvailable()];
               int numRead = comPort.readBytes(readBuffer, readBuffer.length);
               System.out.println("Read " + numRead + " bytes.");
               String output = new String(readBuffer);
               System.out.println(output);
            }
        } catch (Exception e) { e.printStackTrace(); }    
        comPort.closePort();
    }
}
