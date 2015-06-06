package com.cristianrgreco.ytdl;

@FunctionalInterface
public interface YouTubeDlProgressEvent {
    abstract void submit(DownloadProgress downloadProgress);
}
