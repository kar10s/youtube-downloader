package com.cristianrgreco.ytdl;

@FunctionalInterface
public interface YouTubeDlEvent {
    abstract void submit(String message);
}
