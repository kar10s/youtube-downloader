package com.cristianrgreco.ytdl;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class YouTubeDownloaderAdapter implements BaseYouTubeDownloaderAdapter {
    private static final String VIDEO_FORMAT = "mp4";
    private static final String AUDIO_FORMAT = "mp3";
    private static final String OUTPUT_FORMAT = "%(title)s_%(id)s.%(ext)s";

    private final URL targetUrl;

    private final List<String> getTitleCommand;
    private final List<String> getFilenameCommand;
    private final List<String> downloadVideoCommand;
    private final List<String> downloadAudioCommand;

    private State currentState = State.NONE;

    public YouTubeDownloaderAdapter(URL targetUrl, File destinationDirectory, BaseBinaryConfiguration binaryConfiguration) {
        this.targetUrl = targetUrl;

        String commandBase = binaryConfiguration.getYouTubeDlBinary().getAbsolutePath();
        this.getTitleCommand = new ArrayList<>(Arrays.asList(
                commandBase,
                "--get-title",
                "--encoding", "UTF-8",
                "--no-part",
                "--no-playlist"
        ));
        this.getFilenameCommand = new ArrayList<>(Arrays.asList(
                commandBase,
                "-o", OUTPUT_FORMAT,
                "--get-filename",
                "--format", VIDEO_FORMAT,
                "--no-part",
                "--no-playlist"
        ));
        this.downloadVideoCommand = new ArrayList<>(Arrays.asList(
                commandBase,
                "-o", destinationDirectory + File.separator + OUTPUT_FORMAT,
                "--format", VIDEO_FORMAT,
                "--no-part",
                "--no-playlist"
        ));
        this.downloadAudioCommand = new ArrayList<>(Arrays.asList(
                commandBase,
                "-o", destinationDirectory + File.separator + OUTPUT_FORMAT,
                "--format", VIDEO_FORMAT,
                "--extract-audio",
                "--audio-format", AUDIO_FORMAT,
                "--ffmpeg-location", binaryConfiguration.getFfmpegBinary().getAbsolutePath(),
                "--no-part",
                "--no-playlist"
        ));
    }

    @Override
    public String getTitle() throws DownloadException {
        List<String> command = new ArrayList<String>() {{
            addAll(getTitleCommand);
            add(targetUrl.toString());
        }};
        return this.lastLineOfOutput(new ProcessBuilder(command));
    }

    @Override
    public String getFilename() throws DownloadException {
        List<String> command = new ArrayList<String>() {{
            addAll(getFilenameCommand);
            add(targetUrl.toString());
        }};
        return this.lastLineOfOutput(new ProcessBuilder(command));
    }

    private String lastLineOfOutput(ProcessBuilder command) throws DownloadException {
        try {
            Process process = command.start();
            Optional<List<String>> outputMessages = this.getOutputMessages(process);
            Optional<List<String>> errorMessages = this.getErrorMessages(process);
            if (errorMessages.isPresent()) {
                throw this.createExceptionForErrorMessages(errorMessages.get());
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
        List<String> command = new ArrayList<String>() {{
            addAll(downloadVideoCommand);
            add(targetUrl.toString());
        }};
        this.download(new ProcessBuilder(command), stateChangeCallback, progressUpdateCallback);
    }

    @Override
    public void downloadAudio(
            Optional<StateChangeEvent> stateChangeCallback,
            Optional<DownloadProgressUpdateEvent> progressUpdateCallback) throws DownloadException {
        List<String> command = new ArrayList<String>() {{
            addAll(downloadAudioCommand);
            add(targetUrl.toString());
        }};
        this.download(new ProcessBuilder(command), stateChangeCallback, progressUpdateCallback);
    }

    private void download(
            ProcessBuilder command,
            Optional<StateChangeEvent> stateChangeCallback,
            Optional<DownloadProgressUpdateEvent> progressUpdateCallback) throws DownloadException {
        try {
            Process process = command.start();

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
                throw this.createExceptionForErrorMessages(errorMessages.get());
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

    DownloadException createExceptionForErrorMessages(List<String> errorMessages) {
        return errorMessages.stream()
                .map(errorMessage -> new DownloadException(Message.from(errorMessage)))
                .reduce((prev, current) -> new DownloadException(current.getErrorMessage(), prev)).get();
    }
}
