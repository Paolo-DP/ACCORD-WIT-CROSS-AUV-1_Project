void setup() {
  // put your setup code here, to run once:
  Serial.begin(115200);
  delay(1000);
}

void loop() {
  if(Serial.available()){
    Serial.write(Serial.read());
  }
}
