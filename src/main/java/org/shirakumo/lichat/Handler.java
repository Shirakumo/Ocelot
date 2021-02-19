package org.shirakumo.lichat;
import org.shirakumo.lichat.updates.*;

@FunctionalInterface
public interface Handler{
    public void handle(Update update);
}
