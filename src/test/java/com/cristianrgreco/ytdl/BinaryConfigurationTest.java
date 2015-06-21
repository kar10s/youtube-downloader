package com.cristianrgreco.ytdl;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.hamcrest.Matchers.is;

public class BinaryConfigurationTest {
    private static final File YOUTUBEDL_BINARY;
    private static final File FFMPEG_BINARY;
    private static final File FFPROBE_BINARY;
    private static final File NON_EXISTENT_FILE = new File("adfasdf");

    static {
        try {
            Properties properties = new Properties();
            properties.load(ClassLoader.class.getResourceAsStream("/config.properties"));
            YOUTUBEDL_BINARY = new File(properties.getProperty("YOUTUBE-DL_BINARY_PATH"));
            FFMPEG_BINARY = new File(properties.getProperty("FFMPEG_BINARY_PATH"));
            FFPROBE_BINARY = new File(properties.getProperty("FFPROBE_BINARY_PATH"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void correctlyCreatesModelIfBinariesExist() {
        BinaryConfiguration binaryConfiguration = new BinaryConfiguration(YOUTUBEDL_BINARY, FFMPEG_BINARY, FFPROBE_BINARY);

        Assert.assertThat("YouTubeDl binary is the same as that provided", binaryConfiguration.getYouTubeDlBinary(), is(YOUTUBEDL_BINARY));
        Assert.assertThat("Ffmpeg binary is the same as that provided", binaryConfiguration.getFfmpegBinary(), is(FFMPEG_BINARY));
        Assert.assertThat("Ffprobe binary is the same as that provided", binaryConfiguration.getFfprobeBinary(), is(FFPROBE_BINARY));
    }

    @Test
    public void throwsExceptionIfYouTubeDlBinaryDoesNotExist() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("YouTubeDl, Ffmpeg and Ffprobe binaries not found"));

        new BinaryConfiguration(NON_EXISTENT_FILE, FFMPEG_BINARY, FFPROBE_BINARY);
    }

    @Test
    public void throwsExceptionIfFfmpegBinaryDoesNotExist() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("YouTubeDl, Ffmpeg and Ffprobe binaries not found"));

        new BinaryConfiguration(YOUTUBEDL_BINARY, NON_EXISTENT_FILE, FFPROBE_BINARY);
    }

    @Test
    public void throwsExceptionIfFfprobeBinaryDoesNotExist() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("YouTubeDl, Ffmpeg and Ffprobe binaries not found"));

        new BinaryConfiguration(YOUTUBEDL_BINARY, FFMPEG_BINARY, NON_EXISTENT_FILE);
    }

    @Test
    public void throwsExceptionIfYouTubeDlBinaryIsInvalid() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("YouTubeDl, Ffmpeg and Ffprobe binaries not functioning as expected"));
        File invalidYouTubeDlBinary = this.createTempFile("youtube-dl");

        new BinaryConfiguration(invalidYouTubeDlBinary, FFMPEG_BINARY, FFPROBE_BINARY);
    }

    @Test
    public void throwsExceptionIfFfmpegBinaryIsInvalid() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("YouTubeDl, Ffmpeg and Ffprobe binaries not functioning as expected"));
        File invalidFfmpegBinary = this.createTempFile("ffmpeg");

        new BinaryConfiguration(YOUTUBEDL_BINARY, invalidFfmpegBinary, FFPROBE_BINARY);
    }

    @Test
    public void throwsExceptionIfFfprobeBinaryIsInvalid() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("YouTubeDl, Ffmpeg and Ffprobe binaries not functioning as expected"));
        File invalidFfprobeBinary = this.createTempFile("ffprobe");

        new BinaryConfiguration(YOUTUBEDL_BINARY, FFMPEG_BINARY, invalidFfprobeBinary);
    }

    private File createTempFile(String filename) {
        try {
            File file = File.createTempFile(filename, "suffix");
            file.deleteOnExit();
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
