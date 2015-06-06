package com.cristianrgreco.ytdl;

import java.util.Optional;

public interface YouTubeDlAdapter {
    String getTitle() throws YouTubeDlAdapterException;

    String getFilename() throws YouTubeDlAdapterException;

    void downloadVideo(Optional<YouTubeDlStateEvent> stateChangeCallback, Optional<YouTubeDlProgressEvent> progressCallback) throws YouTubeDlAdapterException;

    void downloadAudio(Optional<YouTubeDlStateEvent> stateChangeCallback, Optional<YouTubeDlProgressEvent> progressCallback) throws YouTubeDlAdapterException;
}
