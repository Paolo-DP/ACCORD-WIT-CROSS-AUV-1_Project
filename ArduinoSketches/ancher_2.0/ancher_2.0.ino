/**
  The pozyx chat demo
  please check out https://www.pozyx.io/Documentation/Tutorials/getting_started

  This demo requires at least two pozyx shields and an equal number of Arduino's.
  It demonstrates the wireless messaging capabilities of the pozyx device.

  This demo creates a chat room. Text written in the Serial monitor will be broadcasted to all other pozyx devices
  within range. They will see your message appear in their Serial monitor.
*/

#include <Wire.h>
#include <SoftwareSerial.h>

SoftwareSerial mySeriald(9, 8); // RX, TX
SoftwareSerial mySerialc(7, 6); // RX, TX
SoftwareSerial mySerialb(5, 4); // RX, TX
SoftwareSerial mySeriala(3, 2); // RX, TX

int length = 4;
uint8_t buffer[4];
byte *data;
byte dataArrays[4][64];
int dataArraysIndex = 0; 
byte const headerBytes[] = {0xF0, 0xF0, 0xF0};
byte test[] = {0xF0, 0xF0, 0xF0};
word destination;
int const headerLength = sizeof(headerBytes) + 1; //header leading bytes + length byte
byte lengthbyte;
byte message[32];


void setup(){
  Serial.begin(115200);
  mySeriala.begin(9600);
  //delay(500);
  mySerialb.begin(9600);
  mySerialc.begin(9600);
  mySeriald.begin(9600);
  delay(100);
  mySeriala.write("AT+CON508CB165DE33");
  delay(100);
  mySerialb.write("AT+CON508CB165E1CA");
  delay(100);
  mySerialc.write("AT+CON508CB169C7DA");
  delay(100);
  mySeriald.write("AT+CON508CB16A3D16");
}

void loop(){

  if(messageIncoming()){
    dataArraysIndex++;
    dataArraysIndex%=4;
    data = dataArrays[dataArraysIndex];
      lengthbyte = Serial.read();
      Serial.readBytes(data, lengthbyte - headerLength);
      int messageindex = 3;
      int messageLength = lengthbyte-headerLength-3;
      destination = word(data[1], data[2]);
      for(int i=0; i<lengthbyte - headerLength; i++){
        Serial.write(data[i]);
        //Serial.print(" ");
      }
      //Serial.println();
      
      switch(data[0]){
       
      case 1:
          switch(destination){

              case 0x6a3f:
                mySeriala.write(headerBytes, headerLength-1);
                mySeriala.write(data+messageindex, messageLength);
                break;

              case 0x6743:
                mySerialb.write(headerBytes, headerLength-1);
                mySerialb.write(data+messageindex, messageLength);
                break;
                
              case 0x6a1a:        
                mySerialc.write(headerBytes, headerLength-1);
                mySerialc.write(data+messageindex, messageLength);
                break;
              
              case 0x6a40:
                mySeriald.write(headerBytes, headerLength-1);
                mySeriald.write(data+messageindex, messageLength);
                break;

              default:

                break;
          }
         
          break;
            
       default:
       
          break;
    }
    
    }
}
int headerCount = 0;
boolean messageIncoming(){
  if(Serial.available()<headerLength){
    return false;
  }
  byte b;
  boolean messageRec = false
  while(Serial.available()<headerLength){
    for(int i=headerCount; i<headerLength-1; i++){
      //b = Serial.read();
      b = Serial.read();
      if(b!=headerBytes[i]){
        break;
      }
    }
    if(messageRec)
      return true;
  }
  return messageRec;
}

