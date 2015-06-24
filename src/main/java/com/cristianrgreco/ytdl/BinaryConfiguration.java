package com.cristianrgreco.ytdl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BinaryConfiguration implements BaseBinaryConfiguration {
    private final File youTubeDlBinary;
    private final File ffmpegBinary;
    private final File ffprobeBinary;

    public BinaryConfiguration(File youTubeDlBinary, File ffmpegBinary, File ffprobeBinary) {
        if (!youTubeDlBinary.exists() || !ffmpegBinary.exists() || !ffprobeBinary.exists()) {
            throw new IllegalArgumentException("YouTubeDl, Ffmpeg and Ffprobe binaries not found");
        }
        if (!areBinariesFunctioningCorrectly(youTubeDlBinary, ffmpegBinary, ffprobeBinary)) {
            throw new IllegalArgumentException("YouTubeDl, Ffmpeg and Ffprobe binaries not functioning as expected");
        }
        this.youTubeDlBinary = youTubeDlBinary;
        this.ffmpegBinary = ffmpegBinary;
        this.ffprobeBinary = ffprobeBinary;
    }

    private static boolean areBinariesFunctioningCorrectly(File youTubeDlBinary, File ffmpegBinary, File ffprobeBinary) {
        try {
            List<Process> processes = Arrays.asList(
                    new ProcessBuilder(Arrays.asList(youTubeDlBinary.getAbsolutePath(), "--version")).start(),
                    new ProcessBuilder(Arrays.asList(ffmpegBinary.getAbsolutePath(), "-version")).start(),
                    new ProcessBuilder(Arrays.asList(ffprobeBinary.getAbsolutePath(), "-version")).start());
            return processes.parallelStream()
                    .map(process -> {
                        try {
                            process.waitFor();
                            return process.exitValue();
                        } catch (InterruptedException e) {
                            return -1;
                        }
                    })
                    .allMatch(status -> status == 0);
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public File getYouTubeDlBinary() {
        return this.youTubeDlBinary;
    }

    @Override
    public File getFfmpegBinary() {
        return this.ffmpegBinary;
    }

    @Override
    public File getFfprobeBinary() {
        return this.ffprobeBinary;
    }

    @Override
    public String toString() {
        return "BinaryConfiguration{" +
                "youTubeDlBinary=" + youTubeDlBinary +
                ", ffmpegBinary=" + ffmpegBinary +
                ", ffprobeBinary=" + ffprobeBinary +
                '}';
    }
}
