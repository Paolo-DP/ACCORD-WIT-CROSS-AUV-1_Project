#include <Pozyx.h>
#include <Pozyx_definitions.h>
#include <Wire.h>

uint16_t remote_id = 0x6000;                            // set this to the ID of the remote device
bool remote = false;    

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
  // put your main code here, to run repeatedly:

}
