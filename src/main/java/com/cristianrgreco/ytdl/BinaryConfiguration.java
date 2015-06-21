package com.cristianrgreco.ytdl;

import java.io.File;

public class BinaryConfiguration implements BaseBinaryConfiguration {
    private final File youTubeDlBinary;
    private final File ffmpegBinary;
    private final File ffprobe;

    public BinaryConfiguration(File youTubeDlBinary, File ffmpegBinary, File ffprobe) {
        if (!youTubeDlBinary.exists() || !ffmpegBinary.exists() || !ffprobe.exists()) {
            throw new IllegalArgumentException("YouTubeDl, Ffmpeg and Ffprobe binaries must exist");
        }
        this.youTubeDlBinary = youTubeDlBinary;
        this.ffmpegBinary = ffmpegBinary;
        this.ffprobe = ffprobe;
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
        return this.ffprobe;
    }

    @Override
    public String toString() {
        return "BinaryConfiguration{" +
                "youTubeDlBinary=" + youTubeDlBinary +
                ", ffmpegBinary=" + ffmpegBinary +
                ", ffprobe=" + ffprobe +
                '}';
    }
}
