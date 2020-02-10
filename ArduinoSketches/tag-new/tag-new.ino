#include <Pozyx.h>
#include <Pozyx_definitions.h>
#include <Wire.h>

uint16_t source_id;                 // the network id of this device  
int status;
int currentsteer;
uint8_t throttle;
uint8_t steering;
boolean pause;
boolean forward;
void setup(){

  Serial.begin(115200);
  pinMode(8, OUTPUT);//M1DIR
  pinMode(9, OUTPUT);//M1RUN
  pinMode(10, OUTPUT);//M2RUN
  pinMode(11, OUTPUT);//M2DIR
  currentsteer = 1;
  throttle = 0;
  steering = 0;
  pause = false;
  forward = true;
  // initialize Pozyx
  if(! Pozyx.begin(false, MODE_INTERRUPT, POZYX_INT_MASK_RX_DATA, 0)){
    abort();
  }

  // read the network id of this device
  Pozyx.regRead(POZYX_NETWORK_ID, (uint8_t*)&source_id, 2);

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
    receive[1]= source_id & 0x0f;
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
          if(data[2] == 0){
            throttle = 0;
            digitalWrite(8, 0); 
            analogWrite(9, 0);
          }
          else if(data[2] <= 127){
            data[2] = (data[2]/2) + 64;
            throttle = data[2]*2;
            if(pause == false){
            digitalWrite(8, 0);
            analogWrite(9, 255);
            delay(50);
            digitalWrite(8, 0); 
            analogWrite(9, throttle);
            forward = true;
            }
          }
          else if(data[2] > 127){ 
            throttle = (-0.5*data[2]) + 191;
            throttle = throttle*2;
            if(pause == false){
            digitalWrite(9, 0); 
            analogWrite(8, 255);
            forward = false;
            }
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
          if(data[2] == 0){
            steering = 0;
            digitalWrite(10, 0); 
            analogWrite(11, 0);
            currentsteer = 1;
          }
          else if(data[2] <= 127 && currentsteer != 2){
            steering = data[2]*2; 
            if(pause == false){
              digitalWrite(11, 0); 
              analogWrite(10, 255);
              delay(50);
              digitalWrite(10, 0); 
              analogWrite(11, 255);
              currentsteer = 2;
            }
          }
          else if(data[2] > 127 && currentsteer != 0){
            steering = 255 - data[2];
            steering = steering*2; 
            if(pause == false){
              digitalWrite(10, 0); 
              analogWrite(11, 255);
              delay(50);
              digitalWrite(11, 0); 
              analogWrite(10, 255);
              currentsteer = 0;
            }
          }
          check[0] = steering;
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
         if(forward == true){
            digitalWrite(8, 0); 
            analogWrite(9, throttle);
          }
          else if(forward == false){ 
            digitalWrite(9, 0); 
            analogWrite(8, throttle);
          }
          if(steering == 0){
            digitalWrite(10, 0); 
            analogWrite(11, 0);
          }
          else if(steering <= 127){
              digitalWrite(11, 0); 
              analogWrite(10, 255);
              delay(50);
              digitalWrite(10, 0); 
              analogWrite(11, 255);
          }
          else if(steering > 127){
              digitalWrite(10, 0); 
              analogWrite(11, 255);
              delay(50);
              digitalWrite(11, 0); 
              analogWrite(10, 255);
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
