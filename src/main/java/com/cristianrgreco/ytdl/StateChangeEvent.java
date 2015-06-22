package com.cristianrgreco.ytdl;

@FunctionalInterface
public interface StateChangeEvent {
    void callback(State state);
}
