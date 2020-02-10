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
byte data[64]; 
byte const headerBytes[] = {0xF0, 0xF0, 0xF0};
byte test[] = {0xF0, 0xF0, 0xF0};
//test[0], test[1], test[2] = 0xF0;
//test[3], test[4], test[5], test[6] = 0xF0;
word destination;
int const headerLength = sizeof(headerBytes) + 1; //header leading bytes + length byte
byte lengthbyte;
byte message[32];


void setup(){
  Serial.begin(115200);
  mySeriala.begin(9600);
  delay(1000);
  mySerialb.begin(9600);
  delay(1000);
  mySeriala.write("AT+CON508CB165DE33");
  delay(100);
  mySerialb.write("AT+CON508CB165E1CA");
}

void loop(){

  if(messageIncoming()){
      lengthbyte = Serial.read();
      //Serial.write(lengthbyte);
      Serial.readBytes(data, lengthbyte - headerLength);
      int messageindex = 3;
      int messageLength = lengthbyte-headerLength-3;
      /*data[0] = 0xF0;
      data[1] = 0x01;
      data[2] = 0x6a;
      data[3] = 0x3f;
      data[4] = 0x03;
      data[5] = 0x02;
      data[6] = 0x01;*/
      //Serial.print(data[1]);
      destination = word(data[1], data[2]);
      
      //message[0] = 0xF0, message[1] = 0xF0, message[2] = 0xF0;
      //message[3] = data[3], message[4] = data[4]; message[5] = data[5];
      //Serial.write(data[1]);
      //Serial.write(data[2]);
      switch(data[0]){
       
      case 1:
          switch(destination){
            
              case 0x6a1a:
                
                //delay(50);
                mySerialb.write(headerBytes, headerLength-1);
                mySerialb.write(data+messageindex, messageLength);
                //delay(50);
                //mySeriala.write("AT");
                break;

              case 0x6a3f:
                //Serial.write(headerBytes, headerLength-1);
                //Serial.write(data+messageindex, messageLength);
                mySeriala.write(headerBytes, headerLength-1);
                mySeriala.write(data+messageindex, messageLength);
                /*Serial.println("Message: ");
                Serial.println(message[0]);
                Serial.println(message[1]);
                Serial.println(message[2]);
                Serial.println(message[3]);
                Serial.println(message[4]);
                Serial.println(message[5]);*/
                //delay(50);
                //mySeriala.write("AT");
                break;
              
              case 0x6a40:
                mySerialb.write(message, 6);
                /*Serial.println("Message: ");
                Serial.println(message[0]);
                Serial.println(message[1]);
                Serial.println(message[2]);
                Serial.println(message[3]);
                Serial.println(message[4]);
                Serial.println(message[5]);*/
                //delay(50);
                //mySeriala.write("AT");
                break;

              case 0x6a5e:
                mySeriala.begin(9600);
                delay(50);
                mySeriala.write("AT+");
                delay(50);
                mySeriala.write(message, 6);
                delay(50);
                mySeriala.write("AT");
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

boolean messageIncoming(){
  if(Serial.available()<headerLength){
    return false;
  }
  byte b;
  for(int i=0; i<headerLength-1; i++){
    //b = Serial.read();
    b = Serial.read();
    if(b!=headerBytes[i]){
      //Serial.println("invalid header");
      return false;
    }
  }
  return true;
}
