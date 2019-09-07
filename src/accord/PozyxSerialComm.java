/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accord;
import com.fazecast.jSerialComm.*;
/**
 *
 * @author Paolo
 */
public class PozyxSerialComm {
    public CarDetails getLocation(int carID){
        CarDetails locationDet = new CarDetails();
        
        return locationDet;
    }
    public boolean sendCarCommand(String cmdType, int value){
        boolean success = true;
        
        return success;
    }
    
    public boolean sendBytes(byte[] sendme){
        boolean success = true;
        
        return success;
    }
}
