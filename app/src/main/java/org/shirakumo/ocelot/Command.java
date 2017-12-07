package org.shirakumo.ocelot;

@FunctionalInterface
public interface Command {
    public void execute(Channel channel, String[] args);
}
