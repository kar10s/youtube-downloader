package com.cristianrgreco.ytdl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DefaultYouTubeDlAdapter implements YouTubeDlAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultYouTubeDlAdapter.class);

    private static final String VIDEO_FORMAT = "mp4";
    private static final String AUDIO_FORMAT = "mp3";
    private static final String OUTPUT_FORMAT = "%(title)s_%(id)s.%(ext)s";

    private final URL targetUrl;
    private final File destinationDirectory;

    private final String getTitleCommand;
    private final String getFilenameCommand;
    private final String downloadVideoCommand;
    private final String downloadAudioCommand;

    private DownloadState currentState = DownloadState.NONE;

    public DefaultYouTubeDlAdapter(URL targetUrl, File destinationDirectory, BinaryConfiguration binaryConfiguration) {
        this.targetUrl = targetUrl;
        this.destinationDirectory = destinationDirectory;

        String commandBase = binaryConfiguration.getYouTubeDlBinary().getAbsolutePath();
        this.getTitleCommand = String.format(
                "%s --get-title --no-part --no-playlist",
                commandBase);
        this.getFilenameCommand = String.format(
                "%s -o %s --get-filename --format %s --no-part --no-playlist",
                commandBase, OUTPUT_FORMAT, VIDEO_FORMAT);
        this.downloadVideoCommand = String.format(
                "%s -o %s%c%s --format %s --no-part --no-playlist",
                commandBase, this.destinationDirectory, File.separatorChar, OUTPUT_FORMAT, VIDEO_FORMAT);
        this.downloadAudioCommand = String.format(
                "%s -o %s%c%s --format %s --extract-audio --audio-format %s --ffmpeg-location %s --no-part --no-playlist",
                commandBase, this.destinationDirectory, File.separatorChar, OUTPUT_FORMAT, VIDEO_FORMAT, AUDIO_FORMAT, binaryConfiguration.getFfmpegBinary().getAbsolutePath());
    }

    @Override
    public String getTitle() throws YouTubeDlAdapterException {
        try {
            Process process = Runtime.getRuntime().exec(this.getTitleCommand + " " + this.targetUrl.toString());
            Optional<List<String>> outputMessages = getOutputMessages(process);
            Optional<List<String>> errorMessages = getErrorMessages(process);
            if (errorMessages.isPresent()) {
                throw new YouTubeDlAdapterException(errorMessages.get().toString());
            }
            return outputMessages.get().get(0);
        } catch (IOException e) {
            LOGGER.error(null, e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getFilename() throws YouTubeDlAdapterException {
        try {
            Process process = Runtime.getRuntime().exec(this.getFilenameCommand + " " + this.targetUrl.toString());
            Optional<List<String>> outputMessages = getOutputMessages(process);
            Optional<List<String>> errorMessages = getErrorMessages(process);
            if (errorMessages.isPresent()) {
                throw new YouTubeDlAdapterException(errorMessages.get().toString());
            }
            return outputMessages.get().get(0);
        } catch (IOException e) {
            LOGGER.error(null, e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void downloadVideo(Optional<YouTubeDlStateEvent> stateChangeCallback, Optional<YouTubeDlProgressEvent> progressCallback, Optional<YouTubeDlEvent> outputCallback, Optional<YouTubeDlEvent> errorCallback) throws YouTubeDlAdapterException {
        try {
            Process process = Runtime.getRuntime().exec(this.downloadVideoCommand + " " + this.targetUrl.toString());

            if (stateChangeCallback.isPresent() || outputCallback.isPresent() || progressCallback.isPresent()) {
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                try {
                    while ((line = input.readLine()) != null) {
                        if (line.trim().isEmpty()) {
                            continue;
                        }
                        if (stateChangeCallback.isPresent()) {
                            if (DownloadState.isValidStateMessage(line)) {
                                DownloadState newState = DownloadState.parse(line);
                                if (this.currentState != newState) {
                                    this.currentState = newState;
                                    stateChangeCallback.get().submit(this.currentState);
                                }
                            }
                        }
                        if (outputCallback.isPresent()) {
                            outputCallback.get().submit(line);
                        }
                        if (progressCallback.isPresent()) {
                            if (DownloadProgress.isValidProgressMessage(line)) {
                                progressCallback.get().submit(DownloadProgress.parse(line));
                            }
                        }
                    }
                    if (stateChangeCallback.isPresent()) {
                        this.currentState = DownloadState.COMPLETE;
                        stateChangeCallback.get().submit(this.currentState);
                    }
                } catch (IOException e) {
                    LOGGER.error(null, e);
                    throw new IllegalStateException(e);
                }
            }

            if (errorCallback.isPresent()) {
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                try {
                    while ((line = input.readLine()) != null) {
                        errorCallback.get().submit(line);
                    }
                } catch (IOException e) {
                    LOGGER.error(null, e);
                    throw new IllegalStateException(e);
                }
            }

            Optional<List<String>> errorMessages = getErrorMessages(process);
            if (errorMessages.isPresent()) {
                throw new YouTubeDlAdapterException(errorMessages.get().toString());
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            LOGGER.error(null, e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void downloadAudio(Optional<YouTubeDlStateEvent> stateChangeCallback, Optional<YouTubeDlProgressEvent> progressCallback, Optional<YouTubeDlEvent> outputCallback, Optional<YouTubeDlEvent> errorCallback) throws YouTubeDlAdapterException {
        try {
            Process process = Runtime.getRuntime().exec(this.downloadAudioCommand + " " + this.targetUrl.toString());

            if (stateChangeCallback.isPresent() || outputCallback.isPresent() || progressCallback.isPresent()) {
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                try {
                    while ((line = input.readLine()) != null) {
                        if (line.trim().isEmpty()) {
                            continue;
                        }
                        if (stateChangeCallback.isPresent()) {
                            if (DownloadState.isValidStateMessage(line)) {
                                DownloadState newState = DownloadState.parse(line);
                                if (this.currentState != newState) {
                                    this.currentState = newState;
                                    stateChangeCallback.get().submit(this.currentState);
                                }
                            }
                        }
                        if (outputCallback.isPresent()) {
                            outputCallback.get().submit(line);
                        }
                        if (progressCallback.isPresent()) {
                            if (DownloadProgress.isValidProgressMessage(line)) {
                                progressCallback.get().submit(DownloadProgress.parse(line));
                            }
                        }
                    }
                    if (stateChangeCallback.isPresent()) {
                        this.currentState = DownloadState.COMPLETE;
                        stateChangeCallback.get().submit(this.currentState);
                    }
                } catch (IOException e) {
                    LOGGER.error(null, e);
                    throw new IllegalStateException(e);
                }
            }

            if (errorCallback.isPresent()) {
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String line;
                try {
                    while ((line = input.readLine()) != null) {
                        errorCallback.get().submit(line);
                    }
                } catch (IOException e) {
                    LOGGER.error(null, e);
                    throw new IllegalStateException(e);
                }
            }

            Optional<List<String>> errorMessages = getErrorMessages(process);
            if (errorMessages.isPresent()) {
                throw new YouTubeDlAdapterException(errorMessages.get().toString());
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            LOGGER.error(null, e);
            throw new IllegalStateException(e);
        }
    }

    private static Optional<List<String>> getOutputMessages(Process process) {
        List<String> outputMessages = new ArrayList<>();

        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        try {
            while ((line = input.readLine()) != null) {
                outputMessages.add(line);
            }
        } catch (IOException e) {
            LOGGER.error(null, e);
            throw new IllegalStateException(e);
        }

        if (outputMessages.size() > 0) {
            return Optional.of(outputMessages);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<List<String>> getErrorMessages(Process process) {
        List<String> errorMessages = new ArrayList<>();

        BufferedReader input = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line;
        try {
            while ((line = input.readLine()) != null) {
                errorMessages.add(line);
            }
        } catch (IOException e) {
            LOGGER.error(null, e);
            throw new IllegalStateException(e);
        }

        if (errorMessages.size() > 0) {
            return Optional.of(errorMessages);
        } else {
            return Optional.empty();
        }
    }
}
