
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
        //Visualizer vs = new Visualizer();
        runTestModules();
        
        //setup();
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
        //ModuleUnitTests.testLinearTrackSegment();
        //ModuleUnitTests.testSimpleOvalTrack();
        //ModuleUnitTests.testPozyxSerialComm();
        //ModuleUnitTests.testPozyxIncommingFrame();
        //ModuleUnitTests.testPozyxAck();
        //ModuleUnitTests.testPozyxLocalization();
        //ModuleUnitTests.testCoordinatesPolling();
        //ModuleUnitTests.testCarLocationPolling();
        ModuleUnitTests.testCarSimulationLine();
        //ModuleUnitTests.testCarSimulationOval();
        //ModuleUnitTests.testCarCommand();
        //ModuleUnitTests.testVisualizer();
    }
}
