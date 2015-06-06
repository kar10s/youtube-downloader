package com.cristianrgreco.ytdl;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DefaultYouTubeDlAdapterTest {
    private static final String VIDEO_URL_SHORT = "https://www.youtube.com/watch?gl=GB&hl=en-GB&v=oHg5SJYRHA0";
    private static final String VIDEO_URL_SPECIAL_ENCODING = "https://www.youtube.com/watch?v=z-wi-HyaASc";
    private static final String VIDEO_URL_PLAYLIST = "https://www.youtube.com/watch?v=YANRGTqELow&list=RDYANRGTqELow";
    private static final String VIDEO_URL_INVALID = "https://www.youtube.com/watch?v=INVALIDURL";

    private Mockery context;
    private File destinationDirectory;
    private BinaryConfiguration binaryConfiguration;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        this.context = new Mockery();
        this.destinationDirectory = new File(".");
        this.binaryConfiguration = new DefaultBinaryConfiguration(new File("youtube-dl.exe"), new File("ffmpeg.exe"));
    }

    @After
    public void tearDown() {
        List<File> tempFiles = Arrays.asList(
                new File("RickRoll'D_oHg5SJYRHA0.mp4"),
                new File("RickRoll'D_oHg5SJYRHA0.mp3"),
                new File("PSY-GANGNAM STYLE(English Lyrics_subtitle) Emoticon   _z-wi-HyaASc.mp4"),
                new File("Coleccionista de canciones - Camila (Letra)_YANRGTqELow.mp4")
        );
        tempFiles.parallelStream().filter(File::exists).forEach(File::delete);
    }

    @Test
    public void retrievesTheTitle() throws IOException, YouTubeDlAdapterException {
        DefaultYouTubeDlAdapter target = new DefaultYouTubeDlAdapter(new URL(VIDEO_URL_SHORT), this.destinationDirectory, this.binaryConfiguration);

        assertThat("Title is correct", target.getTitle(), is("RickRoll'D"));
    }

    @Test
    public void retrievesTheFilename() throws IOException, YouTubeDlAdapterException {
        DefaultYouTubeDlAdapter target = new DefaultYouTubeDlAdapter(new URL(VIDEO_URL_SHORT), this.destinationDirectory, this.binaryConfiguration);

        assertThat("Filename is correct", target.getFilename(), is("RickRoll'D_oHg5SJYRHA0.mp4"));
    }

    @Test
    public void downloadsVideoFile() throws IOException, InterruptedException, YouTubeDlAdapterException {
        DefaultYouTubeDlAdapter target = new DefaultYouTubeDlAdapter(new URL(VIDEO_URL_SHORT), this.destinationDirectory, this.binaryConfiguration);

        target.downloadVideo(Optional.empty(), Optional.empty());

        File videoFile = new File("RickRoll'D_oHg5SJYRHA0.mp4");
        assertThat("Video file exists", videoFile.exists(), is(true));
    }

    @Test
    public void downloadsAudioFile() throws IOException, InterruptedException, YouTubeDlAdapterException {
        DefaultYouTubeDlAdapter target = new DefaultYouTubeDlAdapter(new URL(VIDEO_URL_SHORT), this.destinationDirectory, this.binaryConfiguration);

        target.downloadAudio(Optional.empty(), Optional.empty());

        File audioFile = new File("RickRoll'D_oHg5SJYRHA0.mp3");
        assertThat("Audio file exists", audioFile.exists(), is(true));
        File videoFile = new File("RickRoll'D_oHg5SJYRHA0.mp4");
        assertThat("Video file does not exist when downloading audio", videoFile.exists(), is(false));
    }

    @Test
    public void downloadsFileWithDifferentFilenameEncoding() throws IOException, InterruptedException, YouTubeDlAdapterException {
        DefaultYouTubeDlAdapter target = new DefaultYouTubeDlAdapter(new URL(VIDEO_URL_SPECIAL_ENCODING), this.destinationDirectory, this.binaryConfiguration);

        target.downloadVideo(Optional.empty(), Optional.empty());

        File videoFile = new File("PSY-GANGNAM STYLE(English Lyrics_subtitle) Emoticon   _z-wi-HyaASc.mp4");
        assertThat("Video file exists", videoFile.exists(), is(true));
    }

    @Test
    public void downloadsSingleEntryFromPlaylistInsteadOfAll() throws IOException, InterruptedException, YouTubeDlAdapterException {
        DefaultYouTubeDlAdapter target = new DefaultYouTubeDlAdapter(new URL(VIDEO_URL_PLAYLIST), this.destinationDirectory, this.binaryConfiguration);

        target.downloadVideo(Optional.empty(), Optional.empty());

        File videoFile = new File("Coleccionista de canciones - Camila (Letra)_YANRGTqELow.mp4");
        assertThat("Video file exists", videoFile.exists(), is(true));
    }

    @Test
    public void firesEventsOnStateChange() throws MalformedURLException, YouTubeDlAdapterException {
        YouTubeDlStateEvent stateChangeCallback = this.context.mock(YouTubeDlStateEvent.class);
        Sequence sequence = context.sequence("STATE_CHANGE_ORDER");
        this.context.checking(new Expectations() {{
            oneOf(stateChangeCallback).submit(DownloadState.RESOLVING);
            inSequence(sequence);
            oneOf(stateChangeCallback).submit(DownloadState.DOWNLOADING);
            inSequence(sequence);
            oneOf(stateChangeCallback).submit(DownloadState.CONVERTING);
            inSequence(sequence);
            oneOf(stateChangeCallback).submit(DownloadState.COMPLETE);
            inSequence(sequence);
        }});
        DefaultYouTubeDlAdapter target = new DefaultYouTubeDlAdapter(new URL(VIDEO_URL_SHORT), this.destinationDirectory, this.binaryConfiguration);

        target.downloadAudio(Optional.of(stateChangeCallback), Optional.empty());

        this.context.assertIsSatisfied();
    }

    @Test
    public void firesEventsOnProgressOutput() throws MalformedURLException, YouTubeDlAdapterException {
        YouTubeDlProgressEvent progressCallback = this.context.mock(YouTubeDlProgressEvent.class);
        this.context.checking(new Expectations() {{
            atLeast(1).of(progressCallback).submit(with(any(DownloadProgress.class)));
        }});
        DefaultYouTubeDlAdapter target = new DefaultYouTubeDlAdapter(new URL(VIDEO_URL_SHORT), this.destinationDirectory, this.binaryConfiguration);

        target.downloadVideo(Optional.empty(), Optional.of(progressCallback));

        this.context.assertIsSatisfied();
    }

    @Test
    public void throwsAnExceptionIfThereIsErrorOutput() throws IOException, YouTubeDlAdapterException {
        this.expectedException.expect(YouTubeDlAdapterException.class);
        this.expectedException.expectMessage("ERROR: Incomplete YouTube ID INVALIDURL. URL " + VIDEO_URL_INVALID + " looks truncated.");
        DefaultYouTubeDlAdapter target = new DefaultYouTubeDlAdapter(new URL(VIDEO_URL_INVALID), this.destinationDirectory, this.binaryConfiguration);

        target.downloadVideo(Optional.empty(), Optional.empty());
    }
}
