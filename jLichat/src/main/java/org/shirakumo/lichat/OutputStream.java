package org.shirakumo.lichat;
import org.shirakumo.lichat.conditions.*;
import java.io.OutputStreamWriter;
import java.io.IOException;

public class OutputStream{
    private final OutputStreamWriter writer;

    public OutputStream(java.io.OutputStream os){
        OutputStreamWriter writer = null;
        try{
            writer = new OutputStreamWriter(os, "utf8");
        }catch(Exception ex){
            throw new EncodingUnsupported("utf8");
        }
        this.writer = writer;
    }

    public void write(int c){
        try{
            writer.write(c);
        }catch(Exception ex){
            throw new WriteError(ex);
        }
    }

    public void write(String s){
        try{
            writer.write(s, 0, s.length());
        }catch(Exception ex){
            throw new WriteError(ex);
        }
    }

    public void flush(){
        try{
            writer.flush();
        }catch(Exception ex){
            throw new WriteError(ex);
        }
    }

    public void close(){
        try{
            writer.close();
        }catch(IOException ex){}
    }
}
