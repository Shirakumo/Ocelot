package org.shirakumo.lichat;
import org.shirakumo.lichat.updates.*;
import java.util.*;

public class Test extends HandlerAdapter{
    public static void main(String[] args) throws Exception{
        switch(args.length){
        case 4: new Test(args[0], args[1], args[2], Integer.parseInt(args[3])); break;
        case 3: new Test(args[0], args[1], args[2], Client.DEFAULT_PORT); break;
        case 2: new Test(args[0], args[1], "localhost", Client.DEFAULT_PORT); break;
        case 1: new Test(args[0], null, "localhost", Client.DEFAULT_PORT); break;
        case 0: System.out.println("Usage: username [password] [hostname] [port]");
        }
    }

    private Client client;
    private String channel;

    public Test(String user, String password, String host, int port) throws Exception{
        client = new Client(user, password, host, port);
        client.addHandler(this);
        client.connect();
        handleInput();
        client.disconnect();
    }

    public void handleInput(){
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine() && client.isConnected()){
            String line = sc.nextLine();
            if(line.indexOf('/') == 0){
                if(line.indexOf("/join") == 0){
                    client.s("JOIN", "channel", line.substring("/join ".length()));
                }else if(line.indexOf("/leave") == 0){
                    if(line.length() > "/leave".length()){
                        client.s("LEAVE", "channel", line.substring("/leave ".length()));
                    }else{
                        client.s("LEAVE", "channel", channel);
                    }
                }else if(line.indexOf("/create") == 0){
                    if(line.length() > "/leave".length()){
                        client.s("CREATE", "channel", line.substring("/create ".length()));
                    }else{
                        client.s("CREATE", "channel", null);
                    }
                }else if(line.indexOf("/disconnect") == 0){
                    client.disconnect();
                }
            }else{
                client.s("MESSAGE", "channel", channel, "text", line);
            }
        }
    }

    public void handle(Disconnect disconnect){
        System.out.println("[SYSTEM] Disconnected.");
    }

    public void handle(Failure update){
        System.out.println("[SYSTEM] Failure: "+update.text);
    }

    public void handle(Join update){
        System.out.println("["+update.channel+"] "+update.from+" ** joined");
        if(update.from.equals(client.username)) channel = update.channel;
    }

    public void handle(Leave update){
        System.out.println("["+update.channel+"] "+update.from+" ** left");
        if(update.from.equals(client.username)) channel = client.channels.get(0);
    }

    public void handle(Message update){
        System.out.println("["+update.channel+"] "+update.from+": "+update.text);
    }
}
