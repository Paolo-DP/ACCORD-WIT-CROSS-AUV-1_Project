
package accord;

import java.util.Scanner;
import com.fazecast.jSerialComm.*;

/**
 *
 * @author Paolo
 */
public class ACCORD {
    
    /**
     * @param args the command line arguments
     */
    static Scanner sc = null;
    static boolean testModules = true;
    
    

    public static void main(String[] args) {
        sc = new Scanner(System.in);
        if(testModules)
            runTestModules();
        setup();
    }
    
    private static void setup(){
        initPozyx();
    }
    private static void initPozyx(){
        System.out.println("Initializing Pozyx...\nSelect Com port:");
        SerialPort[] ports = SerialPort.getCommPorts();
        for(int i=0; i<ports.length; i++){
            System.out.println(i+". " + ports[i].getSystemPortName());
        }
        int selectport = 0;
        do{
            System.out.print(">");
            selectport=sc.nextInt();
        }while(selectport<0 && selectport>=ports.length);
        SerialPort comPort = ports[selectport];
        if(comPort.openPort())
            System.out.println(comPort.getSystemPortName() 
                    + "Port Open Successful with Baud: " 
                    + comPort.getBaudRate());
        
        
    }
    
    private static void runTestModules(){
        System.out.println("Running Module Tests...");
        //testCarDetails();
        testSerialComm();
    }
    
    private static void testCarDetails(){
        CarDetails dets = new CarDetails();
        System.out.println("xloc = "+dets.xloc);
        dets.xloc = 69;
        System.out.println("xloc = "+dets.xloc);
    }
    
    private static void testSerialComm(){
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
