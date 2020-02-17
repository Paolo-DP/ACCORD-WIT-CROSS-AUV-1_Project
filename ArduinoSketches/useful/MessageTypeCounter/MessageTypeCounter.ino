byte RXBuffer[64];
byte const headerBytes[] = {0xF0, 0xF0, 0xF0};
int const headerLength = sizeof(headerBytes) + 1; //header leading bytes + length byte
int const minFrameLength = headerLength + 1; //full header plus message type byte
void setup() {
  Serial.begin(115200);
  delay(500);
}
int commsloop = 500;
void loop() {
  // put your main code here, to run repeatedly:
  for(int i=0; i<commsloop; i++)
    comms();
  printMsgCount();
}

void comms(){
  while(messageIncoming()){
    processMessage();
  }
}

boolean messageIncoming(){
  if(Serial.available()<headerLength){
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
  uint32_t now;
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
  countMsgType((uint16_t)frameType); 
}
uint16_t messageTypes[64][2];
uint16_t uniqueTypes = 0;
void countMsgType(uint16_t msgType){
  int index = 0;
  while(index<uniqueTypes){
    if(messageTypes[index][0] == msgType){
      messageTypes[index][1]++;
      return;
    }
    index++;
  }
  if(index>=uniqueTypes){
    messageTypes[uniqueTypes][0] = msgType;
    messageTypes[uniqueTypes][1] = 1;
    uniqueTypes++;
  }
}

void printMsgCount(){
  Serial.println("==MESSAGE COUNT==");
  for(int i=0; i<uniqueTypes; i++){
    Serial.print(i+1);
    Serial.print(". #");
    Serial.print(messageTypes[i][0]);
    Serial.print("\tCount: ");
    Serial.println(messageTypes[i][1]);
  }
}

