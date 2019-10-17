#include <Pozyx.h>
#include <Pozyx_definitions.h>
#include <Wire.h>

boolean const manualSetup = true;
boolean const debug = true;
boolean devicesReady = false;
uint8_t currentTag = 0;

uint16_t master_id = 0x6000;                            // set this to the ID of the remote device
bool remote = false;

const int numAnchors = 4;
uint8_t lastAnchor = 0;
uint16_t anchors[numAnchors] = {1, 2, 3, 4};
int32_t anchors_x[numAnchors] = {1, 2, 3, 4};
int32_t anchors_y[numAnchors] = {1, 2, 3, 4};
int32_t anchors_z[numAnchors] = {1, 2, 3, 4};
uint8_t algorithm = POZYX_POS_ALG_UWB_ONLY;             // positioning algorithm to use. try POZYX_POS_ALG_TRACKING for fast moving objects.
uint8_t dimension = POZYX_2D;  
uint8_t height = 0;

const short numTags = 8;
uint8_t lastTag = 0;
uint16_t tags[numTags] = {1, 2, 3, 4, 5, 6, 7, 8};
int32_t tags_x[numTags] = {1, 2, 3, 4, 5, 6, 7, 8};
int32_t tags_y[numTags] = {1, 2, 3, 4, 5, 6, 7, 8};
int32_t tags_z[numTags] = {1, 2, 3, 4, 5, 6, 7, 8};
int16_t tags_angle[numTags] = {1, 2, 3, 4, 5, 6, 7, 8};
int16_t tags_angle_calib[numTags] = {1, 2, 3, 4, 5, 6, 7, 8};
int32_t updateTimeStamp[8] = {1, 2, 3, 4, 5, 6, 7, 8};

byte const headerBytes[] = {0xF0, 0xF0, 0xF0};
int const headerLength = sizeof(headerBytes) + 1; //header leading bytes + length byte
int const minFrameLength = headerLength + 1; //full header plus message type byte
byte RXBuffer[255];

void setup() {
  Serial.begin(115200);
  while(!Serial);
  if(Pozyx.begin() == POZYX_FAILURE){
    //Serial.println(F("ERROR: Unable to connect to POZYX shield"));
    //Serial.println(F("Reset required"));
    delay(100);
    abort();
  }
}

void loop() {
  if(messageIncoming()){
    processMessage();
  }
  if(devicesReady){
    updatePosition(currentTag);
    currentTag++;
    currentTag%=lastTag;
  }
}

boolean updatePosition(short tagIndex){
  coordinates_t position;
  sensor_raw_t sensor_raw;
  int status1 = Pozyx.doRemotePositioning(tags[tagIndex], &position, dimension, height, algorithm);
  int status2 = Pozyx.getRawSensorData(&sensor_raw, tags[tagIndex]);
  int32_t t = millis();
  if(status1 == POZYX_SUCCESS && status2 == POZYX_SUCCESS){
    tags_x[tagIndex] = (int32_t)position.x;
    tags_y[tagIndex] = (int32_t)position.y;
    tags_z[tagIndex] = (int32_t)position.z;
    tags_angle[tagIndex] = sensor_raw.euler_angles[0];
    updateTimeStamp[tagIndex] = t;
    return true;
  }
  else{
    if(status1 != POZYX_SUCCESS && debug)
      Serial.println("Remote Positioning Error");
    if(status2 != POZYX_SUCCESS && debug)
      Serial.println("Sensor Data Error");
    return false;
  }
}

boolean messageIncoming(){
  if(Serial.available()<headerLength-1){
    return false;
  }
  byte b;
  for(int i=0; i<headerLength-1; i++){
    b = Serial.read();
    if(b!=headerBytes[i]){
      //Serial.println("invalid header");
      return false;
    }
  }
  return true;
}

void processMessage(){
  //delay(100);
  int frameLength = 0;
  int timeOut=50;
  while(!Serial.available()&&timeOut>0){
    timeOut--;
  }
  frameLength = Serial.read();
  if(frameLength<minFrameLength)
    return;
  timeOut=50;
  while(Serial.available()<frameLength-headerLength && timeOut>0){
    timeOut--;
  }
  Serial.readBytes(RXBuffer, frameLength-headerLength);
  byte frameType = RXBuffer[0];
  uint8_t * dataBuffer;
  int dataLength;
  
  
  switch(frameType){
    case 1: //send message to Pozyx Device
      uint16_t destID;
      destID = ((uint16_t)RXBuffer[1])<<8 | (uint16_t)RXBuffer[2];
      dataBuffer = (uint8_t *)(RXBuffer+3);
      dataLength = frameLength - minFrameLength;
      int status;
      status = Pozyx.writeTXBufferData(dataBuffer, dataLength);
      status = Pozyx.sendTXBufferData(destID);
      sendAck(frameType);
      break;
      
    case 2: //retrieve coordinate data of a specific Tag
      sendAck(frameType);
      uint16_t tagID;
      tagID = ((uint16_t)RXBuffer[1])<<8 | (uint16_t)RXBuffer[2];
      uint8_t tagIndex;
      tagIndex = indexOfTag(tagID);
      if(tagIndex!=-1){
        byte coordinatesFrame[minFrameLength + 20];
        for(int i = 0; i<headerLength-1; i++)
          coordinatesFrame[i] = headerBytes[i];
        coordinatesFrame[headerLength-1] = minFrameLength + 20;
        coordinatesFrame[headerLength] = 3;
        getCoordinatesDataBytes(coordinatesFrame+minFrameLength, tagIndex);
        Serial.write(coordinatesFrame, minFrameLength + 20);
      }      
      break;
    case 129: //add new Anchor Device
      if(lastAnchor>=numAnchors)
        break;
      anchors[lastAnchor] = ((uint16_t)RXBuffer[1])<<8 | (uint16_t)RXBuffer[2];
      anchors_x[lastAnchor] = 
        ((uint32_t)RXBuffer[3])<<24 |
        ((uint32_t)RXBuffer[4])<<16 |
        ((uint32_t)RXBuffer[5])<<8 |
        (uint32_t)RXBuffer[6];
      anchors_y[lastAnchor] = 
        ((uint32_t)RXBuffer[7])<<24 |
        ((uint32_t)RXBuffer[8])<<16 |
        ((uint32_t)RXBuffer[9])<<8 |
        (uint32_t)RXBuffer[10];
      anchors_z[lastAnchor] = 
        ((uint32_t)RXBuffer[11])<<24 |
        ((uint32_t)RXBuffer[12])<<16 |
        ((uint32_t)RXBuffer[13])<<8 |
        (uint32_t)RXBuffer[14];
      lastAnchor++;
      sendAck(frameType);
      break;
    case 130:
      tags[lastTag] = ((uint16_t)RXBuffer[1])<<8 | (uint16_t)RXBuffer[2];
      lastTag++;
      sendAck(frameType);
      break;
    case 131:
      finalizeDeviceList();
      sendAck(frameType);
      break;
    case 132:
      for(int i=0; i<lastTag; i++){
        Pozyx.clearDevices(tags[lastTag]);
      }
      lastTag=0;
      lastAnchor=0;
      sendAck(frameType);
      break;
    case 160:
      printAnchors();
      break;
    case 161:
      printTags();
      break;
    case 162:
      printTagData(((uint16_t)RXBuffer[1])<<8 | (uint16_t)RXBuffer[2]);
      break;
    default:
      sendAck(0);
      break;
  }
  
}
void sendAck(int type){
  Serial.write(headerBytes, headerLength-1);
  Serial.write(headerLength+2);
  Serial.write(0xFF);
  Serial.write((byte)type);
}

void getCoordinatesDataBytes(byte* data, uint8_t tagIndex){
  data[0] = tags[tagIndex]>>8;
  data[1] = tags[tagIndex];
  data[2] = tags_x[tagIndex]>>24;
  data[3] = tags_x[tagIndex]>>16;
  data[4] = tags_x[tagIndex]>>8;
  data[5] = tags_x[tagIndex];
  data[6] = tags_y[tagIndex]>>24;
  data[7] = tags_y[tagIndex]>>16;
  data[8] = tags_y[tagIndex]>>8;
  data[9] = tags_y[tagIndex];
  data[10] = tags_z[tagIndex]>>24;
  data[11] = tags_z[tagIndex]>>16;
  data[12] = tags_z[tagIndex]>>8;
  data[13] = tags_z[tagIndex];
  data[14] = tags_angle[tagIndex]>>8;
  data[15] = tags_angle[tagIndex];
  data[16] = updateTimeStamp[tagIndex]>>24;
  data[17] = updateTimeStamp[tagIndex]>>16;
  data[18] = updateTimeStamp[tagIndex]>>8;
  data[19] = updateTimeStamp[tagIndex];
}

void finalizeDeviceList(){
  
    for(int n = 0; n < lastTag; n++){
      Pozyx.clearDevices(tags[n]);
      //Serial.print("Tag: ");
      //Serial.println(tags[n], HEX);
      for(int i = 0; i < lastAnchor; i++){
        //Serial.print("Anchor: ");
        //Serial.println(anchors[i], HEX);
        device_coordinates_t anchor;
        anchor.network_id = anchors[i];
        anchor.flag = 0x1;
        anchor.pos.x = anchors_x[i];
        anchor.pos.y = anchors_y[i];
        anchor.pos.z = anchors_z[i];
        Pozyx.addDevice(anchor, tags[n]);
      }
    }
  
  if (numAnchors > 4){
    for(int n = 0; n < lastTag; n++){
      Pozyx.setSelectionOfAnchors(POZYX_ANCHOR_SEL_AUTO, numAnchors, tags[n]);
    }
  }
  devicesReady = true;
}

int indexOfTag(uint16_t tagID){
  for(int i=0; i<numTags; i++){
    if(tagID==tags[i])
      return i;
  }
  return -1;
}
void printAnchors(){
  Serial.print("Number of Anchors: ");
  Serial.println(lastAnchor);
  for(int i=0; i<4; i++){
    Serial.print("Anchor ");
    Serial.print(i);
    Serial.print(": 0x");
    Serial.print(anchors[i],HEX);
    Serial.print("\tx,y,z: (");
    Serial.print(anchors_x[i], DEC);
    Serial.print(",");
    Serial.print(anchors_y[i], DEC);
    Serial.println(")");
  }
}
void printTags(){
  Serial.print("Number of Tags: ");
  Serial.println(lastTag);
  for(int i=0; i<4; i++){
    Serial.print("Tag ");
    Serial.print(i);
    Serial.print(": 0x");
    Serial.println(tags[i],HEX);
  }
}
void printTagData(uint16_t id){
  int tagIndex = indexOfTag(id);
  if(tagIndex!=-1){
    Serial.print("Tag 0x Data: ");
    Serial.println(tags[tagIndex],HEX);
    Serial.print("X: ");
    Serial.println(tags_x[tagIndex]);
    Serial.print("Y: ");
    Serial.println(tags_y[tagIndex]);
    Serial.print("Z: ");
    Serial.println(tags_z[tagIndex]);
    Serial.print("Angle: ");
    Serial.println(tags_angle[tagIndex]);
    Serial.print("TimeStamp: ");
    Serial.println(updateTimeStamp[tagIndex], DEC);
  }
  else
    Serial.println("ERROR Tag not Registered");
}
