package com.cristianrgreco.ytdl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class YouTubeDownloaderAdapter implements BaseYouTubeDownloaderAdapter {
    private static final String VIDEO_FORMAT = "mp4";
    private static final String AUDIO_FORMAT = "mp3";
    //private static final String OUTPUT_FORMAT = "%(title)s_%(id)s.%(ext)s";
    private static final String OUTPUT_FORMAT = "%(title)s.%(ext)s";

    private final ProcessBuilder getTitleProcess;
    private final ProcessBuilder getFilenameProcess;
    private final ProcessBuilder downloadVideoProcess;
    private final ProcessBuilder downloadAudioProcess;

    private State currentState = State.NONE;

    public YouTubeDownloaderAdapter(URL targetUrl, File destinationDirectory, BaseBinaryConfiguration binaryConfiguration) {
        String commandBase = binaryConfiguration.getYouTubeDlBinary().getAbsolutePath();
        this.getTitleProcess = new ProcessBuilder(Arrays.asList(
                commandBase,
                "--get-title",
                "--encoding", "UTF-8",
                "--no-part",
                "--no-playlist",
                targetUrl.toString()
        ));
        this.getFilenameProcess = new ProcessBuilder(Arrays.asList(
                commandBase,
                "-o", OUTPUT_FORMAT,
                "--get-filename",
                "--format", VIDEO_FORMAT,
                "--no-part",
                "--no-playlist",
                targetUrl.toString()
        ));
        this.downloadVideoProcess = new ProcessBuilder(Arrays.asList(
                commandBase,
                "-o", destinationDirectory + File.separator + OUTPUT_FORMAT,
                "--format", VIDEO_FORMAT,
                "--no-part",
                "--no-playlist",
                targetUrl.toString()
        ));
        this.downloadAudioProcess = new ProcessBuilder(Arrays.asList(
                commandBase,
                "-o", destinationDirectory + File.separator + OUTPUT_FORMAT,
                "--format", VIDEO_FORMAT,
                "--extract-audio",
                "--audio-format", AUDIO_FORMAT,
                "--audio-quality","0",
                "--ffmpeg-location", binaryConfiguration.getFfmpegBinary().getAbsolutePath(),
                "--no-part",
                "--no-playlist",
                targetUrl.toString()
        ));
    }

    @Override
    public String getTitle() throws DownloadException {
        return this.lastLineOfOutput(this.getTitleProcess);
    }

    @Override
    public String getFilename() throws DownloadException {
        return this.lastLineOfOutput(this.getFilenameProcess);
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
        this.download(this.downloadVideoProcess, stateChangeCallback, progressUpdateCallback);
    }

    @Override
    public void downloadAudio(
            Optional<StateChangeEvent> stateChangeCallback,
            Optional<DownloadProgressUpdateEvent> progressUpdateCallback) throws DownloadException {
        this.download(this.downloadAudioProcess, stateChangeCallback, progressUpdateCallback);
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
