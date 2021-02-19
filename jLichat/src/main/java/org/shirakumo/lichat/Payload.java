package org.shirakumo.lichat;
import org.shirakumo.lichat.updates.*;

import java.net.URLConnection;
import java.util.*;
import java.io.*;

public class Payload{
    public final String name;
    public final String contentType;
    public final byte[] data;

    public Payload(Emote update){
        this(update.name, update.contentType, update.payload);
    }

    public Payload(Data update){
        this(update.filename, update.contentType, update.payload);
    }

    public Payload(String path) throws IOException{
        this(new File(path));
    }

    public Payload(File path) throws IOException{
        this(path.getName(), URLConnection.guessContentTypeFromName(path.getName()), new FileInputStream(path));
    }

    public Payload(String name, String contentType, String data){
        this(name, contentType, Base64.decode(data));
    }

    public Payload(String name, String contentType, byte[] data){
        this.name = name;
        this.contentType = contentType;
        this.data = data;
    }

    public Payload(String name, String contentType, java.io.InputStream stream) throws IOException{
        this.name = name;
        this.contentType = contentType;
        this.data = CL.readOctetStream(stream);
    }

    public void save(String path) throws IOException{
        save(new File(path));
    }

    public void save(File path) throws IOException{
        FileOutputStream out = new FileOutputStream(path);
        out.write(data);
        out.close();
    }
}
