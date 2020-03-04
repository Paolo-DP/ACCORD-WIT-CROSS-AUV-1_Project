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

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

/**
 *
 * @author Paolo
 */
public class CommMessageScheduler {
    Queue<CommMessage> messages = new ArrayDeque<>();
    private double timeZero = 0;
    Instant instantZero = null;
    Instant instantCurrent = null;
    
    public int size(){
        return messages.size();
    }
    
    public void addMessage(double timeStamp, byte messageType, byte[] data){
        CommMessage message = new CommMessage(timeStamp, messageType, data);
        messages.add(message);
    }
    
    public CommMessage peekNextMessage(){
        return messages.peek();
    }
    public CommMessage pollNextMessage(){
        return messages.poll();
    }
    public boolean exportCSV(){
        
        return true;
    }
    
    //ACCORD Specific methods
    
    
}
