package com.cristianrgreco.ytdl;

@FunctionalInterface
public interface YouTubeDlStateEvent {
    abstract void submit(DownloadState downloadState);
}
