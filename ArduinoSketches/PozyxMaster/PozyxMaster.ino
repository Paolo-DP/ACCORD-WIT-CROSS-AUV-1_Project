#include <Pozyx.h>
#include <Pozyx_definitions.h>
#include <Wire.h>

boolean const degug = true;
boolean devicesReady = false;
uint8_t currentTag = 0;

uint16_t master_id = 0x6000;                            // set this to the ID of the remote device
bool remote = false;

const int numAnchors = 4;
uint8_t lastAnchor = 0;
uint16_t anchors[numAnchors] = {0, 0, 0, 0};
int32_t anchors_x[numAnchors] = {0, 0, 0, 0};
int32_t anchors_y[numAnchors] = {0, 0, 0, 0};
uint8_t algorithm = POZYX_POS_ALG_UWB_ONLY;             // positioning algorithm to use. try POZYX_POS_ALG_TRACKING for fast moving objects.
uint8_t dimension = POZYX_2D;  
uint8_t height = 0;

const short numTags = 8;
uint8_t lastTag = 0;
uint16_t tags[numTags] = {0, 0, 0, 0, 0, 0, 0, 0};
int32_t tags_x[numTags] = {0, 0, 0, 0, 0, 0, 0, 0};
int32_t tags_y[numTags] = {0, 0, 0, 0, 0, 0, 0, 0};
int32_t tags_z[numTags] = {0, 0, 0, 0, 0, 0, 0, 0};
int16_t tags_angle[numTags] = {0, 0, 0, 0, 0, 0, 0, 0};
int16_t tags_angle_calib[numTags] = {0, 0, 0, 0, 0, 0, 0, 0};
int32_t updateTimeStamp[8] = {0, 0, 0, 0, 0, 0, 0, 0};

byte const headerBytes[] = {0xF0};
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
    tags_x[tagIndex] = position.x;
    tags_y[tagIndex] = position.y;
    tags_z[tagIndex] = position.z;
    tags_angle[tagIndex] = sensor_raw.euler_angles[0];
    updateTimeStamp[tagIndex] = t;
    return true;
  }
  else
    return false;
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
  sendAck(frameType);
  
  switch(frameType){
    case 1: //send message to Pozyx Device
      uint16_t destID;
      destID = ((uint16_t)RXBuffer[1])<<8 + (uint16_t)RXBuffer[2];
      dataBuffer = (uint8_t *)(RXBuffer+3);
      dataLength = frameLength - minFrameLength;
      int status;
      status = Pozyx.writeTXBufferData(dataBuffer, dataLength);
      status = Pozyx.sendTXBufferData(destID);
      break;
      
    case 2: //retrieve coordinate data of a specific Tag
      uint16_t tagID;
      tagID = ((uint16_t)RXBuffer[1])<<8 + (uint16_t)RXBuffer[2];
      uint8_t tagIndex;
      tagIndex = indexOfTag(tagID);
      if(tagIndex!=-1){
        byte coordinatesFrame[minFrameLength + 18];
        for(int i = 0; i<headerLength-1; i++)
          coordinatesFrame[i] = headerBytes[i];
        coordinatesFrame[headerLength-1] = minFrameLength + 18;
        coordinatesFrame[headerLength] = 3;
        getCoordinatesDataBytes(coordinatesFrame+minFrameLength, tagIndex);
        Serial.write(coordinatesFrame, headerLength + 18);
      }
      
      break;
    case 129: //add new Anchor Device
      if(lastAnchor>=numAnchors)
        break;
      anchors[lastAnchor] = ((uint16_t)RXBuffer[1])<<8 + (uint16_t)RXBuffer[2];
      anchors_x[lastAnchor] = 
        ((uint32_t)RXBuffer[3])<<24 +
        ((uint32_t)RXBuffer[4])<<16 +
        ((uint32_t)RXBuffer[5])<<8 +
        (uint32_t)RXBuffer[6];
      anchors_x[lastAnchor] = 
        ((uint32_t)RXBuffer[7])<<24 +
        ((uint32_t)RXBuffer[8])<<16 +
        ((uint32_t)RXBuffer[9])<<8 +
        (uint32_t)RXBuffer[10];
      anchors_x[lastAnchor] = 
        ((uint32_t)RXBuffer[11])<<24 +
        ((uint32_t)RXBuffer[12])<<16 +
        ((uint32_t)RXBuffer[13])<<8 +
        (uint32_t)RXBuffer[14];
      lastAnchor++;
      break;
    case 130:
      tags[lastTag] = ((uint16_t)RXBuffer[1])<<8 + (uint16_t)RXBuffer[2];
      break;
    case 131:
      finalizeDeviceList();
      break;
    default:
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
  data[0] = tags_x[tagIndex]>>24;
  data[1] = tags_x[tagIndex]>>16;
  data[2] = tags_x[tagIndex]>>8;
  data[3] = tags_x[tagIndex];
  data[4] = tags_y[tagIndex]>>24;
  data[5] = tags_y[tagIndex]>>16;
  data[6] = tags_y[tagIndex]>>8;
  data[7] = tags_y[tagIndex];
  data[8] = tags_z[tagIndex]>>24;
  data[9] = tags_z[tagIndex]>>16;
  data[10] = tags_z[tagIndex]>>8;
  data[11] = tags_z[tagIndex];
  data[12] = tags_angle[tagIndex]>>8;
  data[13] = tags_angle[tagIndex];
  data[14] = updateTimeStamp[tagIndex]>>24;
  data[15] = updateTimeStamp[tagIndex]>>16;
  data[16] = updateTimeStamp[tagIndex]>>8;
  data[17] = updateTimeStamp[tagIndex];
}

void finalizeDeviceList(){
  for(int i = 0; i < lastAnchor; i++){
    device_coordinates_t anchor;
    anchor.network_id = anchors[i];
    anchor.flag = 0x1;
    anchor.pos.x = anchors_x[i];
    anchor.pos.y = anchors_y[i];
    anchor.pos.z = height;
    for(int n = 0; n < lastTag; n++){
      Pozyx.addDevice(anchor, tags[n]);
    }
  }
  if (numAnchors > 4){
    for(int n = 0; n < lastTag; n++){
      Pozyx.setSelectionOfAnchors(POZYX_ANCHOR_SEL_AUTO, numAnchors, tags[n]);
    }
  }
}

int indexOfTag(uint16_t tagID){
  for(int i=0; i<numTags; i++){
    if(tagID==tags[i])
      return i;
  }
  return -1;
}
