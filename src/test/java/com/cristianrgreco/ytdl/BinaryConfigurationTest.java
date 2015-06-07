package com.cristianrgreco.ytdl;

import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.is;

public class BinaryConfigurationTest {
    private File youtubeDlBinary;
    private File ffmpegBinary;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws IOException {
        this.youtubeDlBinary = File.createTempFile("youtubedl", "tmp");
        this.ffmpegBinary = File.createTempFile("ffmpeg", "tmp");
    }

    @After
    public void tearDown() {
        if (this.youtubeDlBinary.exists()) {
            this.youtubeDlBinary.delete();
        }
        if (this.ffmpegBinary.exists()) {
            this.ffmpegBinary.delete();
        }
    }

    @Test
    public void correctlyCreatesModelIfBinariesExist() {
        BinaryConfiguration binaryConfiguration = new BinaryConfiguration(this.youtubeDlBinary, this.ffmpegBinary);

        Assert.assertThat("YouTubeDl binary is the same as that provided", binaryConfiguration.getYouTubeDlBinary(), is(this.youtubeDlBinary));
        Assert.assertThat("Ffmpeg binary is the same as that provided", binaryConfiguration.getFfmpegBinary(), is(this.ffmpegBinary));
    }

    @Test
    public void throwsExceptionIfYouTubeDlBinaryDoesNotExist() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("YouTubeDl and Ffmpeg binaries must exist"));

        this.youtubeDlBinary.delete();
        new BinaryConfiguration(this.youtubeDlBinary, this.ffmpegBinary);
    }

    @Test
    public void throwsExceptionIfFfmpegBinaryDoesNotExist() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("YouTubeDl and Ffmpeg binaries must exist"));

        this.ffmpegBinary.delete();
        new BinaryConfiguration(this.youtubeDlBinary, this.ffmpegBinary);
    }
}
