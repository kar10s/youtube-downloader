package com.cristianrgreco.ytdl;

@FunctionalInterface
public interface StateChangeEvent {
    abstract void submit(State state);
}
