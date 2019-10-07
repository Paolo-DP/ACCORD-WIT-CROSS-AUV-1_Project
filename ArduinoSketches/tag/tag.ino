#include <Pozyx.h>
#include <Pozyx_definitions.h>
#include <Wire.h>

uint16_t source_id;                 // the network id of this device  
uint16_t destination_id = 0;        // the destination network id. 0 means the message is broadcasted to every device in range
String inputString = "";            // a string to hold incoming data
boolean stringComplete = false;     // whether the string is complete
int status;
uint8_t throttle;
uint8_t steering;
boolean pause;
void setup(){

  Serial.begin(115200);
  pinMode(8, OUTPUT);//M1DIR
  pinMode(9, OUTPUT);//M1RUN
  pinMode(10, OUTPUT);//M2RUN
  pinMode(11, OUTPUT);//M2DIR
  throttle = 0;
  steering = 0;
  pause = false;
  // initialize Pozyx
  if(! Pozyx.begin(false, MODE_INTERRUPT, POZYX_INT_MASK_RX_DATA, 0)){
    abort();
  }

  // read the network id of this device
  Pozyx.regRead(POZYX_NETWORK_ID, (uint8_t*)&source_id, 2);

  // reserve 100 bytes for the inputString:
  inputString.reserve(100);
}

void loop(){

  // we wait up to 50ms to see if we have received an incoming message (if so we receive an RX_DATA interrupt)
  if(Pozyx.waitForFlag(POZYX_INT_STATUS_RX_DATA,50))
  {
    // we have received a message!

    uint8_t length = 0;
    uint16_t messenger = 0x00;
    delay(1);
    // Let's read out some information about the message (i.e., how many bytes did we receive and who sent the message)
    Pozyx.getLastDataLength(&length);
    Pozyx.getLastNetworkId(&messenger);

    uint8_t data[length];

    // read the contents of the receive (RX) buffer, this is the message that was sent to this device
    Pozyx.readRXBufferData(data, length);
    Serial.print("Ox");
    Serial.print(messenger, HEX);
    Serial.print(": ");
    uint8_t receive[2];
    uint8_t check[2];
    receive[1]= source_id & 0xff;
    receive[0]=(source_id >> 8);
    switch(data[1]){

      case 0://get car id
          //receive[0]=0xff;
          Pozyx.writeTXBufferData(receive, 2);
          Pozyx.sendTXBufferData(messenger); 
          break;
      
      case 1://get throttle
          check[0] = throttle;
          Pozyx.writeTXBufferData(check,1);
          Pozyx.sendTXBufferData(messenger); 
          break;
          
       case 2://set throttle
          if(data[2] <= 127 && data[2] > 0){
            data[2] = (data[2]/2) + 63;
            throttle = data[2]*2;
            if(pause == false){
            digitalWrite(8, 0); 
            analogWrite(9, throttle);
            }
          }
          else if(data[2] > 127){
            //throttle = 255 - data[2]; 
            throttle = (-0.5*data[2]) + 191;
            throttle = throttle*2;
            if(pause == false){
            digitalWrite(9, 0); 
            analogWrite(8, throttle);
            }
          }
          else if(data[2] == 0){
            digitalWrite(8, 0); 
            analogWrite(9, 0);
          }
          check[0] = 2;
          Pozyx.writeTXBufferData(check,1);
          Pozyx.sendTXBufferData(messenger); 
          break;

       case 3://get steering
          check[0] = steering;
          Pozyx.writeTXBufferData(check,1);
          Pozyx.sendTXBufferData(messenger); 
          break;
          
       case 4://set steering
          if(data[2] <= 127){
            steering = data[2]*2;
            if(pause == false){
              digitalWrite(10, 0); 
              analogWrite(11, steering);
            }
          }
          else if(data[2] > 127){
            steering = 255 - data[2];
            steering = steering*2; 
            if(pause == false){
              digitalWrite(11, 0); 
              analogWrite(10, steering);
            }
          }
          check[0] = 4;
          Pozyx.writeTXBufferData(check,1);
          Pozyx.sendTXBufferData(messenger); 
          break;
          
       case 5://pause
          pause = true;
          digitalWrite(8, 0); 
          analogWrite(9, 0);
          analogWrite(10, 0);
          digitalWrite(11, 0);
          check[0] = 5;
          Pozyx.writeTXBufferData(check,1);
          Pozyx.sendTXBufferData(messenger); 
          break;
          
       case 6://go
          pause = false;
          if((throttle/2) <= 127){
            digitalWrite(8, 0); 
            analogWrite(9, throttle);
          }
          else if((throttle/2) > 127){ 
            digitalWrite(9, 0); 
            analogWrite(8, throttle);
          }
          if((steering/2) <= 127){
              digitalWrite(10, 0); 
              analogWrite(11, steering);
          }
          else if((steering/2) > 127){
              digitalWrite(11, 0); 
              analogWrite(10, steering);
          }
          check[0] = 6;
          Pozyx.writeTXBufferData(check,1);
          Pozyx.sendTXBufferData(messenger); 
          break;
          
       default:
       
          break;
    }
    
  }
  
}


