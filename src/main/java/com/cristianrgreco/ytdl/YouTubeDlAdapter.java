package com.cristianrgreco.ytdl;

import java.util.Optional;

public interface YouTubeDlAdapter {
    String getTitle() throws YouTubeDlAdapterException;

    String getFilename() throws YouTubeDlAdapterException;

    void downloadVideo(Optional<YouTubeDlProgressEvent> progressCallback,
                       Optional<YouTubeDlEvent> outputCallback,
                       Optional<YouTubeDlEvent> errorCallback) throws YouTubeDlAdapterException;

    void downloadAudio(Optional<YouTubeDlProgressEvent> progressCallback,
                       Optional<YouTubeDlEvent> outputCallback,
                       Optional<YouTubeDlEvent> errorCallback) throws YouTubeDlAdapterException;
}
