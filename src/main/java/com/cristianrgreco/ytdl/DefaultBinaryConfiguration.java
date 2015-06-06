package com.cristianrgreco.ytdl;

import java.io.File;

public class DefaultBinaryConfiguration implements BinaryConfiguration {
    private final File youTubeDlBinary;
    private final File ffmpegBinary;

    public DefaultBinaryConfiguration(File youTubeDlBinary, File ffmpegBinary) {
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
        return "com.cristianrgreco.ytdl.DefaultBinaryConfiguration{" +
                "youTubeDlBinary=" + youTubeDlBinary +
                ", ffmpegBinary=" + ffmpegBinary +
                '}';
    }
}
