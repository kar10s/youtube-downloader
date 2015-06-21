package com.cristianrgreco.ytdl;

import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;

public class BinaryConfigurationTest {
    private File youtubeDlBinary;
    private File ffmpegBinary;
    private File ffprobeBinary;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws IOException {
        this.youtubeDlBinary = File.createTempFile("youtubedl", "tmp");
        this.ffmpegBinary = File.createTempFile("ffmpeg", "tmp");
        this.ffprobeBinary = File.createTempFile("ffprobe", "tmp");
    }

    @After
    public void tearDown() {
        new ArrayList<>(Arrays.asList(this.youtubeDlBinary, this.ffmpegBinary, this.ffprobeBinary))
                .parallelStream()
                .filter(File::exists)
                .forEach(File::delete);
    }

    @Test
    public void correctlyCreatesModelIfBinariesExist() {
        BinaryConfiguration binaryConfiguration = new BinaryConfiguration(this.youtubeDlBinary, this.ffmpegBinary, this.ffprobeBinary);

        Assert.assertThat("YouTubeDl binary is the same as that provided", binaryConfiguration.getYouTubeDlBinary(), is(this.youtubeDlBinary));
        Assert.assertThat("Ffmpeg binary is the same as that provided", binaryConfiguration.getFfmpegBinary(), is(this.ffmpegBinary));
        Assert.assertThat("Ffprobe binary is the same as that provided", binaryConfiguration.getFfprobeBinary(), is(this.ffprobeBinary));
    }

    @Test
    public void throwsExceptionIfYouTubeDlBinaryDoesNotExist() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("YouTubeDl, Ffmpeg and Ffprobe binaries must exist"));

        this.youtubeDlBinary.delete();
        new BinaryConfiguration(this.youtubeDlBinary, this.ffmpegBinary, this.ffprobeBinary);
    }

    @Test
    public void throwsExceptionIfFfmpegBinaryDoesNotExist() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("YouTubeDl, Ffmpeg and Ffprobe binaries must exist"));

        this.ffmpegBinary.delete();
        new BinaryConfiguration(this.youtubeDlBinary, this.ffmpegBinary, this.ffprobeBinary);
    }

    @Test
    public void throwsExceptionIfFfprobeBinaryDoesNotExist() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("YouTubeDl, Ffmpeg and Ffprobe binaries must exist"));

        this.ffprobeBinary.delete();
        new BinaryConfiguration(this.youtubeDlBinary, this.ffmpegBinary, this.ffprobeBinary);
    }
}
