package org.shirakumo.ocelot;
import android.os.Binder;
import org.shirakumo.lichat.Client;

public class ServiceBinder extends Binder{
    public final Service service;

    public ServiceBinder(Service service){
        this.service = service;
    }
}
