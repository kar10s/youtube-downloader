package com.cristianrgreco.ytdl;

import java.io.File;

public class BinaryConfiguration implements BaseBinaryConfiguration {
    private final File youTubeDlBinary;
    private final File ffmpegBinary;

    public BinaryConfiguration(File youTubeDlBinary, File ffmpegBinary) {
        this.youTubeDlBinary = youTubeDlBinary;
        this.ffmpegBinary = ffmpegBinary;
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
    public String toString() {
        return "BinaryConfiguration{" +
                "youTubeDlBinary=" + youTubeDlBinary +
                ", ffmpegBinary=" + ffmpegBinary +
                '}';
    }
}
