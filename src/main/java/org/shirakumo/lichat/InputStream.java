package org.shirakumo.lichat;
import org.shirakumo.lichat.conditions.*;
import java.io.InputStreamReader;
import java.io.IOException;

public class InputStream{
    private final InputStreamReader reader;
    private int buffer = -1;
    
    public InputStream(java.io.InputStream is){
        InputStreamReader reader = null;
        try{
            reader = new InputStreamReader(is, "utf8");
        }catch(Exception ex){
            throw new EncodingUnsupported("utf8");
        }
        this.reader = reader;
    }

    public boolean hasMore(){
        if(buffer == -1){
            try{
                buffer = reader.read();
            }catch(IOException ex){
                buffer = -1;
            }
            return (buffer != -1);
        }else{
            return true;
        }
    }

    public int peek(){
        int c = peekNoError();
        if(buffer == -1) throw new EndOfStream();
        return c;
    }

    public int peekNoError(){
        if(buffer == -1){
            try{
                buffer = reader.read();
            }catch(IOException ex){
                buffer = -1;
            }
        }
        return buffer;
    }

    public int read(){
        int c = readNoError();
        if(c == -1) throw new EndOfStream();
        return c;
    }

    public int readNoError(){
        if(buffer == -1){
            try{
                return reader.read();
            }catch(IOException ex){
                return -1;
            }
        }else{
            int b = buffer;
            buffer = -1;
            return b;
        }
    }

    public void unread(int c){
        buffer = c;
    }

    public void close(){
        try{
            reader.close();
        }catch(IOException ex){}
    }
}
