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
    private static int[] tags = {0x6a19};
    private static int[] anchorIDs = {0x6e38, 0x6717, 0x6735, 0x6e3C};
    private static int[] anchorX = {0, 1700, 200, 1600};
    private static int[] anchorY = {0, 0, 1600, 1600};
    private static int[] anchorZ = {0, 0, 0, 0};
    
    
    //Track and Track Segment Tests
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
        Track tr = createSimpleOvalTrack(1500, 500, 100, false,0,0);
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
    public static Track createSimpleOvalTrack(int length, int width, int roadWidth, boolean troubleshoot, int xOffset, int yOffset){
        Track tr = new Track();
        
        TrackSegment seg = new TrackSegment();
        seg.createLineSegment(length-width, roadWidth, 0);
        seg.setAbsoluteLocation(xOffset, yOffset);
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
    //PozyxSerialComm Tests
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
    public static void testPozyxSerialComm(){
        PozyxSerialComm poz = new PozyxSerialComm();
        byte[] message = {
        (byte)0x01,
        (byte)0x00,
        (byte)0x00,
        (byte)0x03,
        (byte)0x02,
        (byte)0x7F
        };
        byte[] frame = {
            (byte)0xF0,
            (byte)0x08,
        (byte)0x01,
        (byte)0x00,
        (byte)0x00,
        (byte)0x03,
        (byte)0x02,
        (byte)0x7F
        };
        System.out.print("Input message: ");
        for(byte b : message)
            System.out.println(Integer.toHexString(b));
        byte[] ack = poz.sendCarCommand(message, true);
        if(ack != null){
            System.out.println("Ack: ");
            for(byte b : ack)
                System.out.println(Integer.toHexString(b));
        }
        else{
            System.out.print("No ack Recieved");
        }
        System.out.println("Manual serial write");
        
        poz.comPort.writeBytes(frame, frame.length);
        while(poz.comPort.bytesAvailable()<4){
            try{
                Thread.sleep(20);
                System.out.println("bytes available: " + poz.comPort.bytesAvailable());
            }catch(Exception e){}
        }
        byte[] readBuffer = new byte[4];
        poz.comPort.readBytes(readBuffer, 4);
        for(byte by : readBuffer)
            System.out.println(Integer.toHexString(by));
        poz.closeComm();
    }
    public static void testPozyxIncommingFrame(){
        PozyxSerialComm pozyx =  new PozyxSerialComm();
        byte[][] sendme = {
            {(byte)0xf0, 10},
            {0,0,0,0,0,0, (byte)0xf0, 5}
        };
        int[] expected = {
            8,
            3
        };
        for(int i=0; i<sendme.length; i++){
            System.out.print("Sending Bytes: ");
            for(int b=0; b<sendme[i].length; b++){
                System.out.print(Integer.toHexString(sendme[i][b]) + " ");
            }
            System.out.println();
            pozyx.sendBytes(sendme[i]);
            int incomef = pozyx.incomingFrame();
            System.out.println("Expected: " + expected[i] + "\tResult: " + incomef);
        }
    }
    public static void testPozyxAck(){
        PozyxSerialComm pozyx =  new PozyxSerialComm();
        pozyx.setVerboseOutput(true);
        byte[][] sendme = {
            {(byte)0xf0, (byte)0x04, (byte)0xff, (byte)PozyxSerialComm.ADD_ANCHOR},
            {(byte)0xf0, (byte)0x04, (byte)0xff, (byte)PozyxSerialComm.ADD_TAG},
            {(byte)0xf0, (byte)0x04, (byte)0xff, (byte)PozyxSerialComm.FINALIZE_DEVICE_LIST},
            {(byte)0xf0, (byte)0x04, (byte)0xff, (byte)PozyxSerialComm.COORDINATES_GET},
            {(byte)0xf0, (byte)0x04, (byte)0xff, (byte)PozyxSerialComm.COORDINATES_MESSAGE},
            
        };
        int[] message_type = {
            (byte)PozyxSerialComm.ADD_ANCHOR,
            (byte)PozyxSerialComm.ADD_TAG,
            (byte)PozyxSerialComm.FINALIZE_DEVICE_LIST,
            (byte)PozyxSerialComm.COORDINATES_GET,
            (byte)PozyxSerialComm.COORDINATES_MESSAGE,
        };
        boolean[] expected = {
            true,
            true,
            true,
            true,
            true
        };
        for(int i=0; i<sendme.length; i++){
            System.out.print("Sending Bytes: ");
            for(int b=0; b<sendme[i].length; b++){
                System.out.print(Integer.toHexString(sendme[i][b]) + " ");
            }
            System.out.println();
            pozyx.sendBytes(sendme[i]);
            boolean ackRec = pozyx.ACKRecieved((byte)message_type[i]);
            System.out.println("Expected: " + expected[i] + "\tResult: " + Boolean.toString(ackRec));
        }
    }
    public static void testPozyxLocalization(){
        PozyxSerialComm pozyx = new PozyxSerialComm();
        pozyx.setVerboseOutput(true);
        int[] tags = {0x6a19};
        for(int i=0; i<anchorIDs.length; i++){
            pozyx.addAnchor(anchorIDs[i], anchorX[i], anchorY[i], anchorZ[i]);
        }
        for(int i=0; i<tags.length; i++){
            pozyx.addTag(tags[i]);
        }
        pozyx.finalizeDeviceList();
        Coordinates coor;
        //while(true){
            coor = pozyx.getCoordinates(tags[0]);
            if(coor!=null){
                System.out.println("Time: " + coor.timeStamp);
                System.out.println("X: "+coor.x+"\t Y: "+coor.y+"\n");
            }
        //}
    }
    //Car Simulation Tests
    public static void testCarSimulationLine(){
        Track tr = new Track();
        TrackSegment seg = new TrackSegment();
        seg.createLineSegment(1700, 1000, 0);
        seg.setAbsoluteLocation(0, 800);
        tr.addTrackSegment(seg);
        tr.complete();
        CarSimulator carSim = new CarSimulator();
        carSim.setTrack(tr);
        PozyxSerialComm pozyx = new PozyxSerialComm();
        for(int i=0; i<anchorIDs.length; i++){
            pozyx.addAnchor(anchorIDs[i], anchorX[i], anchorY[i], anchorZ[i]);
        }
        
        Car c = new Car(0x6a40, pozyx);
        carSim.addCar(c);
        pozyx.addTag(c.getID());
        pozyx.finalizeDeviceList();
        while(true){
            
            carSim.simulate();
            System.out.print("X = " + c.getXLocation());
            System.out.print("\tY = " + c.getYLocation());
            System.out.println("\tSteer: " + c.getSteeringPower());
        }
        
    }
    public static void testCarSimulationOval(){
        Track tr = createSimpleOvalTrack(3000, 2000, 150, false, 500, 150);
        PozyxSerialComm pozyx = new PozyxSerialComm();
        CarSimulator carSim = new CarSimulator();
        carSim.setTrack(tr);
        
        for(int i=0; i<anchorIDs.length; i++){
            pozyx.addAnchor(anchorIDs[i], anchorX[i], anchorY[i], anchorZ[i]);
        }
        
        /*Car c = new Car(0x6a3f, pozyx);
        c.adjustSpeed(0x3f);
        carSim.addCar(c);
        pozyx.addTag(c.getID());*/
        Car c = new Car(0x6A3F, pozyx);
        //c.adjustSpeed(0x3f);
        carSim.addCar(c);
        pozyx.addTag(c.getID());
        /*c = new Car(0x6a40, pozyx);
        c.adjustSpeed(0x3f);
        carSim.addCar(c);
        pozyx.addTag(c.getID());
        */
        pozyx.addTag(c.getID());
        pozyx.finalizeDeviceList();
        while(true){
            
            carSim.simulate();
            System.out.print("X = " + c.getXLocation());
            System.out.print("\tY = " + c.getYLocation());
            System.out.println("\tSteer: " + c.getSteeringPower());
        }
        
    }
    public static void testCarCommand(){
        PozyxSerialComm poz = new PozyxSerialComm();
        byte[] frame1 = {(byte)0xF0, (byte)0xF0, (byte)0xF0, (byte)0x0A, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x03, (byte)0x02, (byte)0x3F};
        byte[] frame2 = {(byte)0xF0, (byte)0xF0, (byte)0xF0, (byte)0x0A, (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x03, (byte)0x02, (byte)0x00};
        
        byte[] message1 = {(byte)0x6A, (byte)0x3F, (byte)0x03, (byte)0x02, (byte)0x80};
        byte[] message2 = {(byte)0x6A, (byte)0x3F, (byte)0x03, (byte)0x02, (byte)0x00};
        Car c = new Car(0x6A3F, poz);
        try{
            System.out.println("GO");
            //poz.sendBytes(frame1);
            //poz.sendCarCommand(message1, true);
            c.adjustSpeed(100);
            //c.adjustSteering(100);
            Thread.sleep(1000);
            //poz.sendBytes(frame2);
            //poz.sendCarCommand(message2, true);
            c.adjustSpeed(0);
            //c.adjustSteering(0);
        }catch(Exception e){};
    }
    public static void testCoordinatesPolling(){
        
        int carID = 0x6a5e;
        PozyxSerialComm pozyx = new PozyxSerialComm();
        pozyx.setVerboseOutput(false);
        for(int i=0; i<anchorIDs.length; i++){
            pozyx.addAnchor(anchorIDs[i], anchorX[i], anchorY[i], anchorZ[i]);
        }
        pozyx.addTag(carID);
        pozyx.finalizeDeviceList();
        Coordinates coor;
        while(true){
            coor = pozyx.getCoordinates(carID);
            if(coor==null){
                System.out.println("Coor return null");
                continue;
            }
            System.out.print("Car ID: 0x" + Integer.toHexString(coor.ID)+ 
                    "\tTime Stamp: " + coor.timeStamp);
            System.out.println("\t(" + coor.x + ", " + coor.y + ")");
        }
        
    }
    
    public static void testVisualizer(){
        Track tr = createSimpleOvalTrack(1000, 500, 100, false, 300, 300);
        int[][] bounds = tr.getTrackBounds();
        System.out.println("Oval Track Bounds: ("+bounds[0][0]+","
                + bounds[0][1] + "), ("
                + bounds[1][0] + ","
                + bounds[1][1] + ")");
        Visualizer vs = new Visualizer();
        vs.setVerboseOutput(true);
        vs.setTrack(tr);
    }
}
