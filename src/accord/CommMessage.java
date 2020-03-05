/*
 * The MIT License
 *
 * Copyright 2020 Paolo.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package accord;

/**
 *
 * @author Paolo
 */
public class CommMessage {
    private double timeStamp = 0; //in ms
    private int dataLength = 0;
    private byte messageType = 0;
    private byte[] frame  = null;
    
    CommMessage(double timeStamp, byte messageType, byte[] frame){
        this.timeStamp = timeStamp;
        this.messageType = messageType;
        this.frame = frame;
        if(frame == null)
            dataLength = 0;
        else
            dataLength = frame.length;
    }
    public double getTimeStamp(){
        return timeStamp;
    }
    public byte getMessageType(){
        return messageType;
    }
    public byte[] getData(){
        return frame;
    }
    public int getMessageLength(){
        return dataLength;
    }
    public String getMessageTypeString(){
        switch(messageType){
            
        }
        return "";
    }
}
