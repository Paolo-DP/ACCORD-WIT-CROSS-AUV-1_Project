/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accord;
import com.fazecast.jSerialComm.*;
import java.util.Scanner;
/**
 *
 * @author Paolo
 */
public class PozyxSerialComm {
    private static byte frameHeader = (byte)0xF0;
    private SerialPort comPort;
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
    
    public boolean sendCarCommand(byte[] frame){
        boolean success = true;
        
        return success;
    }    
    public boolean sendBytes(byte[] sendme){
        boolean success = true;
        
        return success;
    }
    
}
