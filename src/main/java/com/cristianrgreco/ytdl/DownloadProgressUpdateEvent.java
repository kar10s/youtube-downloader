package com.cristianrgreco.ytdl;

@FunctionalInterface
public interface DownloadProgressUpdateEvent {
    abstract void submit(DownloadProgress downloadProgress);
}
