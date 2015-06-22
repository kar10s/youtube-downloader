package com.cristianrgreco.ytdl;

@FunctionalInterface
public interface DownloadProgressUpdateEvent {
    void callback(DownloadProgress downloadProgress);
}
