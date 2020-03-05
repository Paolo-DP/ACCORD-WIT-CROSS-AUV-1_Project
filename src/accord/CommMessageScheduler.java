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

import com.fazecast.jSerialComm.SerialPort;
import java.io.File;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Scanner;

/**
 *
 * @author Paolo
 */
public class CommMessageScheduler {
    Queue<CommMessage> messages = new ArrayDeque<>();
    //private LocalTime timeZero = LocalTime.now();
    long timeZero = System.nanoTime();
    Instant instantCurrent = null;
    private boolean verbose = true;
    
    public int size(){
        return messages.size();
    }
    
    public void addMessage(double timeStamp, byte messageType, byte[] data){
        CommMessage message = new CommMessage(timeStamp, messageType, data);
        messages.add(message);
    }
    
    public CommMessage peekNextMessage(){
        return messages.peek();
    }
    public CommMessage pollNextMessage(){
        return messages.poll();
    }
    public SerialPort comPort = null;
    public void sendAll(){
        if(comPort == null) return;
        long sendTimeZero = System.nanoTime();
        for(CommMessage msg : messages){
            while(msg.getTimeStamp() > (System.nanoTime() - sendTimeZero)/1000000);
                
            comPort.writeBytes(msg.getData(), msg.getData().length);
        }
    }
    private static final int baudRate = 19200;
    public boolean initComPort(){
        SerialPort[] ports = SerialPort.getCommPorts();
        for(int i=0; i<ports.length; i++){
            System.out.println(i+". " + ports[i].getSystemPortName());
        }
        
        Scanner sc = new Scanner(System.in);
        
        if(ports.length < 2){
            System.out.println("Error! Insufficient number of Comm Ports");
            
        }
        
        int selectBlePort = 0;
        System.out.println("Select BLE Com Port:");
        do{
            System.out.print(">");
            selectBlePort=sc.nextInt();
        }while(selectBlePort<0 && selectBlePort>=ports.length);
        System.out.println("Starting Serial Ports...");
        comPort = ports[selectBlePort];
        boolean succ = comPort.openPort();
        comPort.setBaudRate(baudRate);
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING | SerialPort.TIMEOUT_WRITE_BLOCKING, 500, 0);
        return succ;
    }
    public boolean exportCSV(String path){
        if(fwExport != null)
            writeScheduleCSV();
        return true;
    }
    String exportPath;
    FileWriter fwExport = null;
    public boolean setCSVOutput(String path){
        File filePath = new File(path);
        if(filePath.isDirectory()){
            try{
                exportPath = path + "\\CommMessageScheduler";
                File expPath = new File(exportPath);
                expPath.mkdirs();
                
                fwExport = new FileWriter(exportPath + "\\CommSched.csv");
                initHeaders();
                
                return true;
            }catch(Exception e){
                return false;
            }
        }
        else{
            if(verbose)
                System.out.println("Car: ERROR! File path for csv files does not exist");
            return false;
        }
    }
    private void initHeaders(){
        if(fwExport == null)
            return;
        
        try{
            fwExport.append("Time Stamp");
            fwExport.append(",Length");
            //fwExport.append(",Message Type (String)");
            fwExport.append(",Message Type Byte");
            fwExport.append(",Frame\n");
            fwExport.flush();
        }catch(Exception e){};
    }
    private void writeScheduleCSV(){
        if(fwExport == null)
            return;
        try{
            for(CommMessage commMsg : messages){
                fwExport.append(Double.toString(commMsg.getTimeStamp()) + ",");
                fwExport.append(Integer.toString(commMsg.getMessageLength()) + ",");
                //fwExport.append(Double.toString(commMsg.) + ",");
                fwExport.append(Integer.toString(commMsg.getMessageType()) + ",");
                fwExport.append("0x");
                for(byte b : commMsg.getData()){
                    if(b<16)
                        fwExport.append("0");
                    fwExport.append(Integer.toHexString(b) + " ");
                }
                fwExport.append("\n");
                fwExport.flush();
            }
        }catch(Exception e){};
    }
    
    //ACCORD Specific methods
    private static final byte[] frameHeader = {(byte)0xF0, (byte)0xF0, (byte)0xF0};
    private static final int frameHeaderLen = frameHeader.length + 1;
    private static final int minFrameLength = frameHeaderLen + 1; //including message type
    private static final int carIDLen = 2;
    
    public static final byte SEND_CAR_COMMAND = 1;
    public static final int SEND_CAR_COMMAND_DATA_LEN = 2;
    
    public static final byte SET_THROTTLE_CMD = 2;
    public static final byte SET_ORIENTATION_CMD = 7;
    public static final byte SET_ORIENTATION_TIMED_CMD = 8;
    
    public static final byte SET_THROTTLE_CMD_LENGTH = 3;
    public static final byte SET_ORIENTATION_CMD_LENGTH = 5;
    public static final byte SET_ORIENTATION_TIMED_CMD_LENGTH = 8;
    
    public void cmdGetCarID(int carID){
        
    }
    
    public void cmdSetThrottle(int carID, int throttle){
        
        byte[] frameLength = {(byte) (minFrameLength + SEND_CAR_COMMAND_DATA_LEN + SET_THROTTLE_CMD_LENGTH)};
        byte[] frameType = {SEND_CAR_COMMAND};
        byte[] destination = ByteBuffer.allocate(2).putShort((short)(carID)).array();
        byte[] cmdLength = {(byte) SET_THROTTLE_CMD_LENGTH};
        byte[] type = {SET_THROTTLE_CMD};
        byte[] data = {(byte)throttle};
        
        byte[] frame = new byte[frameLength[0]];
        int index = 0;
        
        System.arraycopy(frameHeader, 0, frame, index, frameHeader.length); index += frameHeader.length;
        System.arraycopy(frameLength, 0, frame, index, frameLength.length); index += frameLength.length;
        System.arraycopy(frameType, 0, frame, index, frameType.length); index += frameType.length;
        System.arraycopy(destination, 0, frame, index, destination.length); index += destination.length;
        System.arraycopy(cmdLength, 0, frame, index, cmdLength.length); index += cmdLength.length;
        System.arraycopy(type, 0, frame, index, type.length); index += type.length;
        System.arraycopy(data, 0, frame, index, data.length); index += data.length;
        
        double timestamp = (double)(System.nanoTime() - timeZero) / 1000000;
        
        messages.add(new CommMessage(timestamp, type[0], frame));
    }
    public void cmdSetSteering(int carID, int steering){
        byte[] frameLength = {(byte) (minFrameLength + SEND_CAR_COMMAND_DATA_LEN + SET_THROTTLE_CMD_LENGTH)};
        byte[] frameType = {SEND_CAR_COMMAND};
        byte[] destination = ByteBuffer.allocate(2).putShort((short)(carID)).array();
        byte[] cmdLength = {(byte) SET_THROTTLE_CMD_LENGTH};
        byte[] type = {SET_THROTTLE_CMD};
        byte[] data = {(byte)steering};
        
        byte[] frame = new byte[frameLength[0]];
        int index = 0;
        
        System.arraycopy(frameHeader, 0, frame, index, frameHeader.length); index += frameHeader.length;
        System.arraycopy(frameLength, 0, frame, index, frameLength.length); index += frameLength.length;
        System.arraycopy(frameType, 0, frame, index, frameType.length); index += frameType.length;
        System.arraycopy(destination, 0, frame, index, destination.length); index += destination.length;
        System.arraycopy(cmdLength, 0, frame, index, cmdLength.length); index += cmdLength.length;
        System.arraycopy(type, 0, frame, index, type.length); index += type.length;
        System.arraycopy(data, 0, frame, index, data.length); index += data.length;
        
        double timestamp =(double)(System.nanoTime() - timeZero) / 1000000;
        
        messages.add(new CommMessage(timestamp, type[0], data));
    }
    public void cmdSetOrientation(int carID, double xAxisCalib, double orient, boolean overwrite){
        double orientUncalib = (orient + xAxisCalib)%360;
        int orientInt = (int)((360-orientUncalib)*5759)/360;
        byte overwriteByte;
        if(overwrite) overwriteByte = (byte)0xff; else overwriteByte = 0;
        
        byte[] frameLength = {(byte) (minFrameLength + SEND_CAR_COMMAND_DATA_LEN + SET_ORIENTATION_CMD_LENGTH)};
        byte[] frameType = {SEND_CAR_COMMAND};
        byte[] destination = ByteBuffer.allocate(2).putShort((short)(carID)).array();
        byte[] cmdLength = {(byte) SET_ORIENTATION_CMD_LENGTH};
        byte[] type = {SET_ORIENTATION_CMD};
        byte[] data1 = ByteBuffer.allocate(2).putShort((short)(orientInt)).array();
        byte[] data2 = {overwriteByte};
        
        byte[] frame = new byte[frameLength[0]];
        int index = 0;
        
        System.arraycopy(frameHeader, 0, frame, index, frameHeader.length); index += frameHeader.length;
        System.arraycopy(frameLength, 0, frame, index, frameLength.length); index += frameLength.length;
        System.arraycopy(frameType, 0, frame, index, frameType.length); index += frameType.length;
        System.arraycopy(destination, 0, frame, index, destination.length); index += destination.length;
        System.arraycopy(cmdLength, 0, frame, index, cmdLength.length); index += cmdLength.length;
        System.arraycopy(type, 0, frame, index, type.length); index += type.length;
        System.arraycopy(data1, 0, frame, index, data1.length); index += data1.length;
        System.arraycopy(data2, 0, frame, index, data2.length); index += data2.length;
        
        double timestamp = (double)(System.nanoTime() - timeZero) / 1000000;
        
        messages.add(new CommMessage(timestamp, type[0], frame));
    }
    public void cmdSetOrientationTimed(int carID, double xAxisCalib, double orient, double time){
        double orientUncalib = (orient + xAxisCalib)%360;
        int orientInt = (int)((360-orientUncalib)*5759)/360;
        
        byte[] frameLength = {(byte) (minFrameLength + SEND_CAR_COMMAND_DATA_LEN + SET_ORIENTATION_TIMED_CMD_LENGTH)};
        byte[] frameType = {SEND_CAR_COMMAND};
        byte[] destination = ByteBuffer.allocate(2).putShort((short)(carID)).array();
        byte[] cmdLength = {(byte) SET_ORIENTATION_TIMED_CMD_LENGTH};
        byte[] type = {SET_ORIENTATION_TIMED_CMD};
        byte[] data1 = ByteBuffer.allocate(2).putShort((short)(orientInt)).array();
        byte[] data2 = ByteBuffer.allocate(4).putInt((int)time).array();
        
        byte[] frame = new byte[frameLength[0]];
        int index = 0;
        
        System.arraycopy(frameHeader, 0, frame, index, frameHeader.length); index += frameHeader.length;
        System.arraycopy(frameLength, 0, frame, index, frameLength.length); index += frameLength.length;
        System.arraycopy(frameType, 0, frame, index, frameType.length); index += frameType.length;
        System.arraycopy(destination, 0, frame, index, destination.length); index += destination.length;
        System.arraycopy(cmdLength, 0, frame, index, cmdLength.length); index += cmdLength.length;
        System.arraycopy(type, 0, frame, index, type.length); index += type.length;
        System.arraycopy(data1, 0, frame, index, data1.length); index += data1.length;
        System.arraycopy(data2, 0, frame, index, data2.length); index += data2.length;
        
        double timestamp = (double)(System.nanoTime() - timeZero) / 1000000;
        
        messages.add(new CommMessage(timestamp, type[0], frame));
    }
    
}
