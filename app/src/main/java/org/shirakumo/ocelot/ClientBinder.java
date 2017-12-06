package org.shirakumo.ocelot;
import android.os.Binder;
import org.shirakumo.lichat.Client;

public class ClientBinder extends Binder{
    public final Client client;

    public ClientBinder(Client client){
        this.client = client;
    }
}