package com.cristianrgreco.ytdl;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class YouTubeDownloaderAdapter implements BaseYouTubeDownloaderAdapter {
    private static final String VIDEO_FORMAT = "mp4";
    private static final String AUDIO_FORMAT = "mp3";
    private static final String OUTPUT_FORMAT = "%(title)s_%(id)s.%(ext)s";

    private final URL targetUrl;

    private final String getTitleCommand;
    private final String getFilenameCommand;
    private final String downloadVideoCommand;
    private final String downloadAudioCommand;

    private State currentState = State.NONE;

    public YouTubeDownloaderAdapter(URL targetUrl, File destinationDirectory, BaseBinaryConfiguration binaryConfiguration) {
        this.targetUrl = targetUrl;

        String commandBase = binaryConfiguration.getYouTubeDlBinary().getAbsolutePath();
        this.getTitleCommand = String.format(
                "%s --get-title --no-part --no-playlist",
                commandBase);
        this.getFilenameCommand = String.format(
                "%s -o %s --get-filename --format %s --no-part --no-playlist",
                commandBase, OUTPUT_FORMAT, VIDEO_FORMAT);
        this.downloadVideoCommand = String.format(
                "%s -o %s%c%s --format %s --no-part --no-playlist",
                commandBase, destinationDirectory, File.separatorChar, OUTPUT_FORMAT, VIDEO_FORMAT);
        this.downloadAudioCommand = String.format(
                "%s -o %s%c%s --format %s --extract-audio --audio-format %s --ffmpeg-location %s --no-part --no-playlist",
                commandBase, destinationDirectory, File.separatorChar, OUTPUT_FORMAT, VIDEO_FORMAT, AUDIO_FORMAT,
                binaryConfiguration.getFfmpegBinary().getAbsolutePath());
    }

    @Override
    public String getTitle() throws DownloadException {
        return this.lastLineOfOutput(this.getTitleCommand + " " + this.targetUrl.toString());
    }

    @Override
    public String getFilename() throws DownloadException {
        return this.lastLineOfOutput(this.getFilenameCommand + " " + this.targetUrl.toString());
    }

    private String lastLineOfOutput(String command) throws DownloadException {
        try {
            Process process = Runtime.getRuntime().exec(command);
            Optional<List<String>> outputMessages = this.getOutputMessages(process);
            Optional<List<String>> errorMessages = this.getErrorMessages(process);
            if (errorMessages.isPresent()) {
                throw new DownloadException(this.errorMessagesToString(errorMessages.get()));
            }
            return outputMessages.get().get(0);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void downloadVideo(
            Optional<StateChangeEvent> stateChangeCallback,
            Optional<DownloadProgressUpdateEvent> progressUpdateCallback) throws DownloadException {
        this.download(this.downloadVideoCommand + " " + this.targetUrl.toString(), stateChangeCallback, progressUpdateCallback);
    }

    @Override
    public void downloadAudio(
            Optional<StateChangeEvent> stateChangeCallback,
            Optional<DownloadProgressUpdateEvent> progressUpdateCallback) throws DownloadException {
        this.download(this.downloadAudioCommand + " " + this.targetUrl.toString(), stateChangeCallback, progressUpdateCallback);
    }

    private void download(
            String command,
            Optional<StateChangeEvent> stateChangeCallback,
            Optional<DownloadProgressUpdateEvent> progressUpdateCallback) throws DownloadException {
        try {
            Process process = Runtime.getRuntime().exec(command);

            if (stateChangeCallback.isPresent() || progressUpdateCallback.isPresent()) {
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                try {
                    while ((line = input.readLine()) != null) {
                        if (line.trim().isEmpty()) {
                            continue;
                        }
                        if (stateChangeCallback.isPresent()) {
                            if (State.isValidStateMessage(line)) {
                                State newState = State.parse(line);
                                if (this.currentState != newState) {
                                    this.currentState = newState;
                                    stateChangeCallback.get().callback(this.currentState);
                                }
                            }
                        }
                        if (progressUpdateCallback.isPresent()) {
                            if (DownloadProgress.isValidProgressMessage(line)) {
                                progressUpdateCallback.get().callback(DownloadProgress.parse(line));
                            }
                        }
                    }
                    if (stateChangeCallback.isPresent()) {
                        this.currentState = State.COMPLETE;
                        stateChangeCallback.get().callback(this.currentState);
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }

            Optional<List<String>> errorMessages = this.getErrorMessages(process);
            if (errorMessages.isPresent()) {
                throw new DownloadException(this.errorMessagesToString(errorMessages.get()));
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private Optional<List<String>> getOutputMessages(Process process) {
        return this.getMessages(process.getInputStream());
    }

    private Optional<List<String>> getErrorMessages(Process process) {
        return this.getMessages(process.getErrorStream());
    }

    private Optional<List<String>> getMessages(InputStream is) {
        List<String> messages = new ArrayList<>();

        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String line;
        try {
            while ((line = input.readLine()) != null) {
                messages.add(line);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        if (messages.size() > 0) {
            return Optional.of(messages);
        } else {
            return Optional.empty();
        }
    }

    private String errorMessagesToString(List<String> errorMessages) {
        return errorMessages.stream()
                .map(message -> {
                    if (message.startsWith("ERROR: ")) {
                        message = message.substring(7);
                    }
                    return message;
                })
                .reduce((a, b) -> a + "; " + b).get();
    }
}
