package com.cristianrgreco.ytdl;

@FunctionalInterface
public interface StateChangeEvent {
    abstract void callback(State state);
}
