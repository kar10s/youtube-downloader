package com.cristianrgreco.ytdl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class BinaryConfiguration implements BaseBinaryConfiguration {
    private final File youTubeDlBinary;
    private final File ffmpegBinary;
    private final File ffprobeBinary;

    public BinaryConfiguration(File youTubeDlBinary, File ffmpegBinary, File ffprobeBinary) {
        if (!youTubeDlBinary.exists() || !ffmpegBinary.exists() || !ffprobeBinary.exists()) {
            throw new IllegalArgumentException("YouTubeDl, Ffmpeg and Ffprobe binaries must exist");
        }
        if (!areBinariesFunctioningCorrectly(youTubeDlBinary, ffmpegBinary, ffprobeBinary)) {
            throw new IllegalArgumentException("YouTubeDl, Ffmpeg and Ffprobe binaries must function correctly");
        }
        this.youTubeDlBinary = youTubeDlBinary;
        this.ffmpegBinary = ffmpegBinary;
        this.ffprobeBinary = ffprobeBinary;
    }

    private static boolean areBinariesFunctioningCorrectly(File youTubeDlBinary, File ffmpegBinary, File ffprobeBinary) {
        try {
            return new ArrayList<>(Arrays.asList(
                    Runtime.getRuntime().exec(String.format("%s --version", youTubeDlBinary.getAbsolutePath())),
                    Runtime.getRuntime().exec(String.format("%s -version", ffmpegBinary.getAbsolutePath())),
                    Runtime.getRuntime().exec(String.format("%s -version", ffprobeBinary.getAbsolutePath())))
            ).parallelStream().map(process -> {
                try {
                    process.waitFor();
                    return process.exitValue();
                } catch (InterruptedException e) {
                    return -1;
                }
            }).allMatch(status -> status == 0);
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
