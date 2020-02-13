/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accord;
import com.fazecast.jSerialComm.*;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.Arrays;
/**
 *
 * @author Paolo
 */
public class PozyxSerialComm {
    private boolean verboseOutput = false;
    
    public static final int baudRate = 19200;
    private static final int ARDUINO_RESET_WAIT = 3000;
    private static final int ackWaitAttempts = 10;
    private static final int WAIT_FOR_BYTES_DELAY = 10;
    private static final int waitForDataAttempts = 20;
    private static final int MAX_FRAME_LENGTH = 32;
    private static final int POZYX_POSITIONING_DELAY = 20;
    private static final int WAIT_TO_FLUSH_DELAY = ((32 * 8 * 1000) / baudRate) + POZYX_POSITIONING_DELAY;
            
    private static final byte[] frameHeader = {(byte)0xF0, (byte)0xF0, (byte)0xF0};
    private static final int frameHeaderLen = frameHeader.length + 1;
    private static final int minFrameLength = frameHeaderLen + 1; //including message type
    public SerialPort comPort;
    public SerialPort comPortBLE;
    private byte[] RXBuffer = new byte[256];
    
    public static final byte SEND_CAR_COMMAND = (byte)1;
    public static final byte COORDINATES_GET = (byte)2;
    public static final byte COORDINATES_MESSAGE = (byte)3;
    public static final byte COORDINATES_DATA_LENGTH = (byte) 20;
    public static final byte ADD_ANCHOR = (byte)129;
    public static final byte ADD_TAG = (byte)130;
    public static final byte FINALIZE_DEVICE_LIST = (byte)131;
    
    public static final byte ACK = (byte)255;
    
    
    PozyxSerialComm(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Select POZYX Com port:");
        SerialPort[] ports = SerialPort.getCommPorts();
        for(int i=0; i<ports.length; i++){
            System.out.println(i+". " + ports[i].getSystemPortName());
        }
        
        int selectport = 0;
        do{
            System.out.print(">");
            selectport=sc.nextInt();
        }while(selectport<0 && selectport>=ports.length);
        
        int selectBlePort = 0;
        System.out.println("Select BLE Com Port:");
        do{
            System.out.print(">");
            selectBlePort=sc.nextInt();
        }while(selectport<0 && selectport>=ports.length);
        System.out.println("Starting Serial Ports...");
        comPort = ports[selectport];
        boolean succPozyx = comPort.openPort();
        comPort.setBaudRate(baudRate);
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 500, 0);
        
        comPortBLE = ports[selectBlePort];
        boolean succBLE = comPortBLE.openPort();
        comPortBLE.setBaudRate(baudRate);
        comPortBLE.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 500, 0);
        
        if(succPozyx && succBLE){
            System.out.println("Serial Ports openned successfully!");
        }
        else
            System.out.println("ERROr: Failed to open Serial ports");
        
        try{
            System.out.println("Waiting for Arduinos to initialize...");
            Thread.sleep(ARDUINO_RESET_WAIT); //wait for Arduino to Reset
        }catch(Exception e){}
        System.out.println("Commuications READY!");
    }
    public boolean setSerialPortPozyx(SerialPort port){
        comPort.closePort();
        comPort = port;
        boolean open = comPort.openPort();
        comPort.setBaudRate(baudRate);
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 500, 0);
        return open;
    }
    public boolean setSerialPortBLE(SerialPort port){
        comPortBLE.closePort();
        comPortBLE = port;
        boolean open = comPortBLE.openPort();
        comPortBLE.setBaudRate(baudRate);
        comPortBLE.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 500, 0);
        return open;
    }
    
    public boolean addAnchor(int deviceID, int xLoc, int yLoc, int zLoc){
        byte[] frame = new byte[minFrameLength + 14];
        System.arraycopy(frameHeader, 0, frame, 0, frameHeader.length);
        frame[frameHeader.length] = (byte)frame.length;
        frame[frameHeaderLen] = (byte)ADD_ANCHOR;
        byte[] id = ByteBuffer.allocate(2).putShort((short)deviceID).array();
        byte[] x = ByteBuffer.allocate(4).putInt(xLoc).array();
        byte[] y = ByteBuffer.allocate(4).putInt(yLoc).array();
        byte[] z = ByteBuffer.allocate(4).putInt(zLoc).array();
        System.arraycopy(id, 0, frame, minFrameLength, id.length);
        System.arraycopy(x, 0, frame, minFrameLength+id.length, x.length);
        System.arraycopy(y, 0, frame, minFrameLength+id.length + x.length, y.length);
        System.arraycopy(z, 0, frame, minFrameLength + id.length + x.length + y.length, z.length);
        if(comPort.isOpen()){
            comPort.writeBytes(frame, frame.length);
        }
        return ACKRecieved(ADD_ANCHOR);
    }
    public boolean addTag(int deviceID){
        byte[] frame = new byte[minFrameLength + 2];
        System.arraycopy(frameHeader, 0, frame, 0, frameHeader.length);
        frame[frameHeader.length] = (byte)frame.length;
        frame[frameHeaderLen] = (byte)ADD_TAG;
        byte[] id = ByteBuffer.allocate(2).putShort((short)deviceID).array();
        System.arraycopy(id, 0, frame, minFrameLength, id.length);
        if(comPort.isOpen()){
            comPort.writeBytes(frame, frame.length);
        }
        return ACKRecieved(ADD_TAG);
    }
    public boolean finalizeDeviceList(){
        byte[] frame = new byte[minFrameLength];
        System.arraycopy(frameHeader, 0, frame, 0, frameHeader.length);
        frame[frameHeaderLen-1] = (byte)frame.length;
        frame[frameHeaderLen] = (byte)FINALIZE_DEVICE_LIST;
        if(comPort.isOpen())
            comPort.writeBytes(frame, frame.length);
        try{
            Thread.sleep(1000);
        }catch(Exception e){};
        return ACKRecieved(FINALIZE_DEVICE_LIST);
    }
    
    public byte[] sendCarCommand(byte[] message, boolean waitAck){
        byte[] frame = new byte[minFrameLength + message.length];
        System.arraycopy(frameHeader, 0, frame, 0, frameHeader.length);
        System.arraycopy(message, 0, frame, minFrameLength, message.length);
        frame[frameHeader.length] = (byte)frame.length;
        frame[minFrameLength-1] = SEND_CAR_COMMAND;
        if(comPortBLE.isOpen()){
            try{
                int success = comPortBLE.writeBytes(frame, frame.length);
                //comPort.closePort();
                if(success==-1)
                    return null;
                if(waitAck){
                    int ackLen = incomingFrame();
                    if(ackLen<=0)
                        return null;
                    
                    byte[] ack = new byte[ackLen];
                    comPortBLE.readBytes(ack, ackLen);
                    return ack;
                }
                return null;
            }catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
        else
            return null;
    }    
    public Coordinates getCoordinates(int carID){        
        Coordinates coor = new Coordinates();
        byte[] frame = new byte[minFrameLength+2];
        System.arraycopy(frameHeader, 0, frame, 0, frameHeader.length);
        frame[frameHeader.length] = (byte)frame.length;
        frame[minFrameLength-1] = COORDINATES_GET;
        byte[] carIDb = ByteBuffer.allocate(2).putShort((short)carID).array();
        System.arraycopy(carIDb, 0, frame, minFrameLength, carIDb.length);
        
        if(comPort.isOpen()){
            try{
                flushRX(false);
                int success = comPort.writeBytes(frame, frame.length);
                if(ACKRecieved(frame[minFrameLength-1])){
                    for(int i=0; i<waitForDataAttempts; i++){
                        int dataLen = incomingFrame();
                        if(dataLen==COORDINATES_DATA_LENGTH+1){
                            byte[] message = getMessage(dataLen);
                            if(message[0] == COORDINATES_MESSAGE){
                                coor.ID = ByteBuffer.wrap(
                                        Arrays.copyOfRange(message, 1, 3)).getShort();
                                coor.x = ByteBuffer.wrap(
                                        Arrays.copyOfRange(message, 3, 7)).getInt();
                                coor.y = ByteBuffer.wrap(
                                        Arrays.copyOfRange(message, 7, 11)).getInt();
                                coor.z = ByteBuffer.wrap(
                                        Arrays.copyOfRange(message, 11, 15)).getInt();
                                coor.eulerAngles[0] = ByteBuffer.wrap(
                                        Arrays.copyOfRange(message, 15, 17)).getShort();
                                coor.timeStamp = ByteBuffer.wrap(
                                        Arrays.copyOfRange(message, 17, 21)).getInt();
                                if(verboseOutput){
                                    System.out.println("PozyxSerialComm - getCoordinates(): SUCCESS");
                                }
                                return coor;
                            }
                        }
                        else
                            continue;
                    }
                    return coor;
                }
                else{
                    if(verboseOutput)
                        System.out.println("PoxyzSerialComm: COORDINATES ERROR - "
                                + "No Ack Recieved\t"
                                + "Car ID: "+Integer.toHexString(carID));
                    return null;
                }
            }catch(Exception e){e.printStackTrace(); return null;}
        }
        else{
            if(verboseOutput)
                System.out.println("PoxyzSerialComm: COORDINATES ERROR - "
                        + "ComPort not Open\t"
                        + "Car ID: "+Integer.toHexString(carID));
            return null;
        }
    }
    
    public boolean sendBytes(byte[] sendme){
        boolean success = true;
        comPort.writeBytes(sendme, sendme.length);
        return success;
    }
    private boolean sendTrailingZeroes(int n){
        if(!comPort.isOpen())
            return false;
        byte[] zeroes = new byte[n];
        for(int i=0; i<zeroes.length; i++){
            zeroes[i] = 0;
        }
        return true;
    }
    public void closeComm(){
        comPort.closePort();
    }
    public void flushRX(boolean immediately){
        try{
            if(comPort!=null && comPort.isOpen()){
                if(!immediately)
                    Thread.sleep(WAIT_TO_FLUSH_DELAY);
                if(comPort.bytesAvailable()>0)
                    comPort.readBytes(RXBuffer, comPort.bytesAvailable());
            }
        }catch(Exception e){};
    }
    public int incomingFrame(){
        //comPort.openPort();
        byte[] headchk = new byte[frameHeader.length];
        int frameLength = -1;
        for(int i = 0; i<ackWaitAttempts; i++){
            if(comPort.bytesAvailable()<frameHeader.length){
                try{
                    Thread.sleep(WAIT_FOR_BYTES_DELAY);
                }catch(InterruptedException e){}
                continue;
            }
            /*` 
            frameLength = comPort.readBytes(headchk, frameHeader.length);
            if(Arrays.equals(headchk, frameHeader)){
                //System.out.println("Frame Recieved");
                //System.out.println("length: " + frameLength);
                comPort.readBytes(headchk,1);
                frameLength = (int)headchk[0] - frameHeaderLen;
                break;
            }
            else{
                //System.out.println("NO Frame Recieved");
                //System.out.println("length: " + frameLength);
                //System.out.println("Byte buffer" + comPort.bytesAvailable());
                //for(byte b : headchk)
                //    System.out.println(Integer.toHexString(b));
            }*/
            while(comPort.bytesAvailable()>=frameHeader.length){
                for(int n = 0; n<frameHeader.length; n++){
                    comPort.readBytes(RXBuffer, 1);
                    if(RXBuffer[0] != frameHeader[n])
                        break;
                    else if(n==frameHeader.length-1){
                        comPort.readBytes(RXBuffer, 1);
                        frameLength = (int)RXBuffer[0] - frameHeaderLen;
                        if(verboseOutput){
                            System.out.println("PozyxSerialComm - IncommingFrame: "
                                    + "Message Recieved with Length = " + frameLength);
                        }
                        return frameLength;
                    }
                }
            }
            if(frameLength>=0)
                break;
        }
        if(verboseOutput){
            if(frameLength==-1)
                System.out.println("PozyxSerialComm: No frame detected");
        }
        return frameLength;
    }
    public boolean ACKRecieved(byte message){
        int framelen=-1;
        for(int i=0; i<ackWaitAttempts; i++){
            while(framelen<0 || comPort.bytesAvailable()>frameHeader.length){
                framelen= incomingFrame();

                if(framelen!=2){
                    if(verboseOutput)
                        System.out.println("PozyxSerialComm-Ack Error: Incorrect message length");
                    return false;
                }
                else{
                    comPort.readBytes(RXBuffer, framelen);
                    //System.out.println(Integer.toHexString(RXBuffer[0]) + Integer.toHexString(RXBuffer[1]));
                    if(RXBuffer[0]==ACK && RXBuffer[1]==message){
                        if(verboseOutput){
                            System.out.println("PozyxSerialComm - ACKRecieved(): Ack Recived for message "
                                    + "0x" + Integer.toHexString(RXBuffer[1]));
                        }
                        return true;
                    }
                    else{
                        if(verboseOutput){
                            System.out.print("PozyxSerialComm: Ack Error. incorrect message");
                            System.out.print("\tmessage = 0x" + Integer.toHexString(message));
                            System.out.println("\tByte 0 = 0x" + Integer.toHexString(RXBuffer[0])
                                    + "\tByte 1 = 0x" + Integer.toHexString(RXBuffer[1]));
                        }
                    }
                }
            }
            if(verboseOutput){
                System.out.println("PozyxSerialComm - ACKRecieved(): No ack Recieved. RX Empty"
                                + "\tmessage = 0x" + Integer.toHexString(message));
            }
            framelen = -1;
            try{
                Thread.sleep(WAIT_FOR_BYTES_DELAY);
            }catch(InterruptedException e){}
        }
        return false;
    }
    public byte[] getMessage(int length){
        if(comPort.isOpen()){
            byte[] message = new byte[length];
            comPort.readBytes(message, length);
            return message;
        }
        else
            return null;
    }
    
    public void setVerboseOutput(boolean output){
        verboseOutput = output;
    }
}
