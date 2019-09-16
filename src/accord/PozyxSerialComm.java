/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accord;
import com.fazecast.jSerialComm.*;
import java.util.Scanner;
import java.util.Arrays;
/**
 *
 * @author Paolo
 */
public class PozyxSerialComm {
    
    public static final int baudRate = 115200;
    private static final int ARDUINO_RESET_WAIT = 3000;
    private static final int ackWaitAttempts = 10;
    private static final byte[] frameHeader = {(byte)0xF0};
    public SerialPort comPort;
    private byte[] RXBuffer = new byte[256];
    PozyxSerialComm(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Select Com port:");
        SerialPort[] ports = SerialPort.getCommPorts();
        for(int i=0; i<ports.length; i++){
            System.out.println(i+". " + ports[i].getSystemPortName());
        }
        
        int selectport = 0;
        do{
            System.out.print(">");
            selectport=sc.nextInt();
        }while(selectport<0 && selectport>=ports.length);
        
        comPort = ports[selectport];
        comPort.openPort();
        comPort.setBaudRate(baudRate);
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 500, 0);
        
        try{
            Thread.sleep(ARDUINO_RESET_WAIT); //wait for Arduino to Reset
        }catch(Exception e){}
        
    }
    
    public void addAnchor(int deviceID){
        
    }
    public void addTag(int deviceID){
        
    }
    public CarDetails getLocation(int carID){
        CarDetails locationDet = new CarDetails();
        
        return locationDet;
    }
    public double getOrientation(int carID){
        
        return 0;
    }
    
    public byte[] sendCarCommand(byte[] message, boolean waitAck){
        byte[] frame = new byte[frameHeader.length + message.length + 1];
        System.arraycopy(frameHeader, 0, frame, 0, frameHeader.length);
        System.arraycopy(message, 0, frame, frameHeader.length+1, message.length);
        frame[frameHeader.length] = (byte)frame.length;
        
        //for(byte b : frame)
        //    System.out.println(Integer.toHexString(b));
        
        if(comPort.isOpen()){
            try{
                int success = comPort.writeBytes(frame, frame.length);
                //comPort.closePort();
                if(success==-1)
                    return null;
                if(waitAck){
                    int ackLen = incomingFrame()-frameHeader.length-1;
                    if(ackLen<=0)
                        return null;
                    
                    byte[] ack = new byte[ackLen];
                    comPort.readBytes(ack, ackLen);
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
    public boolean sendBytes(byte[] sendme){
        boolean success = true;
        
        return success;
    }
    public void closeComm(){
        comPort.closePort();
    }
    private int incomingFrame(){
        //comPort.openPort();
        byte[] headchk = new byte[frameHeader.length];
        int frameLength = -1;
        for(int i = 0; i<ackWaitAttempts; i++){
            if(comPort.bytesAvailable()<frameHeader.length){
                try{
                    Thread.sleep(20);
                }catch(InterruptedException e){}
                continue;
            }
            frameLength = comPort.readBytes(headchk, frameHeader.length);
            if(Arrays.equals(headchk, frameHeader)){
                System.out.println("Frame Recieved");
                System.out.println("length: " + frameLength);
                comPort.readBytes(headchk,1);
                frameLength = (int)headchk[0];
                break;
            }
            else{
                System.out.println("NO Frame Recieved");
                System.out.println("length: " + frameLength);
                System.out.println("Byte buffer" + comPort.bytesAvailable());
                for(byte b : headchk)
                    System.out.println(Integer.toHexString(b));
            }
        }
        return frameLength;
    }
}
