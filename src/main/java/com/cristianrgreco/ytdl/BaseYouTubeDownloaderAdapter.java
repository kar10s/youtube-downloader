package com.cristianrgreco.ytdl;

import java.util.Optional;

public interface BaseYouTubeDownloaderAdapter {
    String getTitle() throws DownloadException;

    String getFilename() throws DownloadException;

    void downloadVideo(
            Optional<StateChangeEvent> stateChangeCallback,
            Optional<DownloadProgressUpdateEvent> progressUpdateCallback) throws DownloadException;

    void downloadAudio(
            Optional<StateChangeEvent> stateChangeCallback,
            Optional<DownloadProgressUpdateEvent> progressUpdateCallback) throws DownloadException;
}
