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

public class YouTubeDownloaderAdapterTest {
    private static final URL VIDEO_URL_SHORT;
    private static final URL VIDEO_URL_SPECIAL_ENCODING;
    private static final URL VIDEO_URL_PLAYLIST;
    private static final URL VIDEO_URL_INVALID;

    static {
        try {
            VIDEO_URL_SHORT = new URL("https://www.youtube.com/watch?gl=GB&hl=en-GB&v=oHg5SJYRHA0");
            VIDEO_URL_SPECIAL_ENCODING = new URL("https://www.youtube.com/watch?v=z-wi-HyaASc");
            VIDEO_URL_PLAYLIST = new URL("https://www.youtube.com/watch?v=YANRGTqELow&list=RDYANRGTqELow");
            VIDEO_URL_INVALID = new URL("https://www.youtube.com/watch?v=INVALIDURL");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private Mockery context;
    private File destinationDirectory;
    private BaseBinaryConfiguration binaryConfiguration;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        this.context = new Mockery();
        this.destinationDirectory = new File(".");
        this.binaryConfiguration = new BinaryConfiguration(
                new File("C:\\Users\\crgreco\\Desktop\\youtube-dl.exe"),
                new File("C:\\Users\\crgreco\\Desktop\\ffmpeg.exe"));
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
    public void retrievesTheTitle() throws IOException, DownloadException {
        YouTubeDownloaderAdapter target = new YouTubeDownloaderAdapter(VIDEO_URL_SHORT, this.destinationDirectory, this.binaryConfiguration);

        assertThat("Title is correct", target.getTitle(), is("RickRoll'D"));
    }

    @Test
    public void retrievesTheFilename() throws IOException, DownloadException {
        YouTubeDownloaderAdapter target = new YouTubeDownloaderAdapter(VIDEO_URL_SHORT, this.destinationDirectory, this.binaryConfiguration);

        assertThat("Filename is correct", target.getFilename(), is("RickRoll'D_oHg5SJYRHA0.mp4"));
    }

    @Test
    public void downloadsVideoFile() throws IOException, InterruptedException, DownloadException {
        YouTubeDownloaderAdapter target = new YouTubeDownloaderAdapter(VIDEO_URL_SHORT, this.destinationDirectory, this.binaryConfiguration);

        target.downloadVideo(Optional.empty(), Optional.empty());

        File videoFile = new File("RickRoll'D_oHg5SJYRHA0.mp4");
        assertThat("Video file exists", videoFile.exists(), is(true));
    }

    @Test
    public void downloadsAudioFile() throws IOException, InterruptedException, DownloadException {
        YouTubeDownloaderAdapter target = new YouTubeDownloaderAdapter(VIDEO_URL_SHORT, this.destinationDirectory, this.binaryConfiguration);

        target.downloadAudio(Optional.empty(), Optional.empty());

        File audioFile = new File("RickRoll'D_oHg5SJYRHA0.mp3");
        assertThat("Audio file exists", audioFile.exists(), is(true));
        File videoFile = new File("RickRoll'D_oHg5SJYRHA0.mp4");
        assertThat("Video file does not exist when downloading audio", videoFile.exists(), is(false));
    }

    @Test
    public void downloadsFileWithDifferentFilenameEncoding() throws IOException, InterruptedException, DownloadException {
        YouTubeDownloaderAdapter target = new YouTubeDownloaderAdapter(VIDEO_URL_SPECIAL_ENCODING, this.destinationDirectory, this.binaryConfiguration);

        target.downloadVideo(Optional.empty(), Optional.empty());

        File videoFile = new File("PSY-GANGNAM STYLE(English Lyrics_subtitle) Emoticon   _z-wi-HyaASc.mp4");
        assertThat("Video file exists", videoFile.exists(), is(true));
    }

    @Test
    public void downloadsSingleEntryFromPlaylistInsteadOfAll() throws IOException, InterruptedException, DownloadException {
        YouTubeDownloaderAdapter target = new YouTubeDownloaderAdapter(VIDEO_URL_PLAYLIST, this.destinationDirectory, this.binaryConfiguration);

        target.downloadVideo(Optional.empty(), Optional.empty());

        File videoFile = new File("Coleccionista de canciones - Camila (Letra)_YANRGTqELow.mp4");
        assertThat("Video file exists", videoFile.exists(), is(true));
    }

    @Test
    public void firesEventsOnStateChange() throws MalformedURLException, DownloadException {
        StateChangeEvent stateChangeCallback = this.context.mock(StateChangeEvent.class);
        Sequence sequence = context.sequence("STATE_CHANGE_ORDER");
        this.context.checking(new Expectations() {{
            oneOf(stateChangeCallback).callback(State.RESOLVING);
            inSequence(sequence);
            oneOf(stateChangeCallback).callback(State.DOWNLOADING);
            inSequence(sequence);
            oneOf(stateChangeCallback).callback(State.CONVERTING);
            inSequence(sequence);
            oneOf(stateChangeCallback).callback(State.COMPLETE);
            inSequence(sequence);
        }});
        YouTubeDownloaderAdapter target = new YouTubeDownloaderAdapter(VIDEO_URL_SHORT, this.destinationDirectory, this.binaryConfiguration);

        target.downloadAudio(Optional.of(stateChangeCallback), Optional.empty());

        this.context.assertIsSatisfied();
    }

    @Test
    public void firesEventsOnProgressOutput() throws MalformedURLException, DownloadException {
        DownloadProgressUpdateEvent progressUpdateCallback = this.context.mock(DownloadProgressUpdateEvent.class);
        this.context.checking(new Expectations() {{
            atLeast(1).of(progressUpdateCallback).callback(with(any(DownloadProgress.class)));
        }});
        YouTubeDownloaderAdapter target = new YouTubeDownloaderAdapter(VIDEO_URL_SHORT, this.destinationDirectory, this.binaryConfiguration);

        target.downloadVideo(Optional.empty(), Optional.of(progressUpdateCallback));

        this.context.assertIsSatisfied();
    }

    @Test
    public void throwsAnExceptionIfThereIsErrorOutput() throws IOException, DownloadException {
        this.expectedException.expect(DownloadException.class);
        this.expectedException.expectMessage(is("ERROR: Incomplete YouTube ID INVALIDURL. URL " + VIDEO_URL_INVALID + " looks truncated."));
        YouTubeDownloaderAdapter target = new YouTubeDownloaderAdapter(VIDEO_URL_INVALID, this.destinationDirectory, this.binaryConfiguration);

        target.downloadVideo(Optional.empty(), Optional.empty());
    }
}
