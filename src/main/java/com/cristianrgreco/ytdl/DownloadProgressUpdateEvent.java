package com.cristianrgreco.ytdl;

@FunctionalInterface
public interface DownloadProgressUpdateEvent {
    abstract void callback(DownloadProgress downloadProgress);
}
