#include <Pozyx.h>
#include <Pozyx_definitions.h>
#include <Wire.h>

uint16_t master_id = 0x6000;                            // set this to the ID of the remote device
bool remote = false;
int numAnchors = 0;

byte const headerByte = 0xF0;
byte const minFrameLength = 3;
byte RXBuffer[255];

void setup() {
  Serial.begin(115200);
  while(!Serial);
  if(Pozyx.begin() == POZYX_FAILURE){
    Serial.println(F("ERROR: Unable to connect to POZYX shield"));
    Serial.println(F("Reset required"));
    delay(100);
    abort();
  }

}

void loop() {
  if(Serial.available()){
    int headerChk = Serial.read();
    if(headerChk == headerByte){
      processMessage();
    }
  }
}

void processMessage(){
  int frameLength = 0;
  int timeOut=50;
  while(!Serial.available()&&timeOut>0){
    timeOut--;
  }
  frameLength = Serial.read();
  if(frameLength<=minFrameLength)
    return;
  timeOut=50;
  while(!Serial.available()<frameLength-2 && timeOut>0){
    timeOut--;
  }
  Serial.readBytes(RXBuffer, frameLength-2);
  int frameType = RXBuffer[0];
  
  switch(frameType){
    case 129: //add new Anchor Device
      device_coordinates_t newAnchor;
      newAnchor.network_id = ((uint16_t)RXBuffer[1])<<8 + (uint16_t)RXBuffer[2];
      newAnchor.pos.x = 
        ((uint32_t)RXBuffer[3])<<24 +
        ((uint32_t)RXBuffer[4])<<16 +
        ((uint32_t)RXBuffer[5])<<8 +
        (uint32_t)RXBuffer[6];
      newAnchor.pos.y = 
        ((uint32_t)RXBuffer[7])<<24 +
        ((uint32_t)RXBuffer[8])<<16 +
        ((uint32_t)RXBuffer[9])<<8 +
        (uint32_t)RXBuffer[10];
      newAnchor.pos.z = 
        ((uint32_t)RXBuffer[11])<<24 +
        ((uint32_t)RXBuffer[12])<<16 +
        ((uint32_t)RXBuffer[13])<<8 +
        (uint32_t)RXBuffer[14];
      newAnchor.flag = 0x1;

      Pozyx.addDevice(newAnchor, master_id);
      numAnchors++;
      if (numAnchors > 4){
        Pozyx.setSelectionOfAnchors(POZYX_ANCHOR_SEL_AUTO, numAnchors, master_id);
      }
      break;
      
    default:
      break;
  }
}
