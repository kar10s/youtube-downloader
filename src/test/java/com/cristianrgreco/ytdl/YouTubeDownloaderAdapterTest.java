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
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class YouTubeDownloaderAdapterTest {
    private static final File YOUTUBEDL_BINARY;
    private static final File FFMPEG_BINARY;
    private static final File FFPROBE_BINARY;

    private static final URL VIDEO_URL_SHORT;
    private static final URL VIDEO_URL_SPECIAL_ENCODING;
    private static final URL VIDEO_URL_TITLE_SPECIAL_ENCODING;
    private static final URL VIDEO_URL_PLAYLIST;
    private static final URL VIDEO_URL_INVALID;

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

        try {
            VIDEO_URL_SHORT = new URL("https://www.youtube.com/watch?gl=GB&hl=en-GB&v=oHg5SJYRHA0");
            VIDEO_URL_SPECIAL_ENCODING = new URL("https://www.youtube.com/watch?v=z-wi-HyaASc");
            VIDEO_URL_TITLE_SPECIAL_ENCODING = new URL("https://www.youtube.com/watch?v=jcF5HtGvX5I");
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
        this.binaryConfiguration = new BinaryConfiguration(YOUTUBEDL_BINARY, FFMPEG_BINARY, FFPROBE_BINARY);
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
    public void retrievesTheTitle() throws DownloadException {
        YouTubeDownloaderAdapter target = new YouTubeDownloaderAdapter(VIDEO_URL_SHORT, this.destinationDirectory, this.binaryConfiguration);

        assertThat("Title is correct", target.getTitle(), is("RickRoll'D"));
    }

    @Test
    public void retrievesTheTitleWithCorrectEncoding() throws DownloadException {
        YouTubeDownloaderAdapter target = new YouTubeDownloaderAdapter(VIDEO_URL_TITLE_SPECIAL_ENCODING, this.destinationDirectory, this.binaryConfiguration);

        assertThat("Title is with correct encoding", target.getTitle(), is("Beyoncé - Yoncé"));
    }

    @Test
    public void retrievesTheFilename() throws DownloadException {
        YouTubeDownloaderAdapter target = new YouTubeDownloaderAdapter(VIDEO_URL_SHORT, this.destinationDirectory, this.binaryConfiguration);

        assertThat("Filename is correct", target.getFilename(), is("RickRoll'D_oHg5SJYRHA0.mp4"));
    }

    @Test
    public void downloadsVideoFile() throws InterruptedException, DownloadException {
        YouTubeDownloaderAdapter target = new YouTubeDownloaderAdapter(VIDEO_URL_SHORT, this.destinationDirectory, this.binaryConfiguration);

        target.downloadVideo(Optional.empty(), Optional.empty());

        File videoFile = new File("RickRoll'D_oHg5SJYRHA0.mp4");
        assertThat("Video file exists", videoFile.exists(), is(true));
    }

    @Test
    public void downloadsAudioFile() throws InterruptedException, DownloadException {
        YouTubeDownloaderAdapter target = new YouTubeDownloaderAdapter(VIDEO_URL_SHORT, this.destinationDirectory, this.binaryConfiguration);

        target.downloadAudio(Optional.empty(), Optional.empty());

        File audioFile = new File("RickRoll'D_oHg5SJYRHA0.mp3");
        assertThat("Audio file exists", audioFile.exists(), is(true));
        File videoFile = new File("RickRoll'D_oHg5SJYRHA0.mp4");
        assertThat("Video file does not exist when downloading audio", videoFile.exists(), is(false));
    }

    @Test
    public void downloadsFileWithDifferentFilenameEncoding() throws InterruptedException, DownloadException {
        YouTubeDownloaderAdapter target = new YouTubeDownloaderAdapter(VIDEO_URL_SPECIAL_ENCODING, this.destinationDirectory, this.binaryConfiguration);

        target.downloadVideo(Optional.empty(), Optional.empty());

        File videoFile = new File("PSY-GANGNAM STYLE(English Lyrics_subtitle) Emoticon   _z-wi-HyaASc.mp4");
        assertThat("Video file exists", videoFile.exists(), is(true));
    }

    @Test
    public void downloadsSingleEntryFromPlaylistInsteadOfAll() throws InterruptedException, DownloadException {
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
    public void throwsAnExceptionIfThereIsErrorOutput() throws DownloadException {
        this.expectedException.expect(DownloadException.class);
        this.expectedException.expectMessage(is("Incomplete YouTube ID INVALIDURL. URL " + VIDEO_URL_INVALID + " looks truncated."));
        YouTubeDownloaderAdapter target = new YouTubeDownloaderAdapter(VIDEO_URL_INVALID, this.destinationDirectory, this.binaryConfiguration);

        target.downloadVideo(Optional.empty(), Optional.empty());
    }

    @Test
    public void createsAndThrowsExceptionForAllErrorMessages() {
        List<String> errorMessages = new ArrayList<String>() {{
            add("WARNING: Warning 1");
            add("ERROR: Error 1");
            add("WARNING: Warning 2");
            add("WARNING: Warning 3");
        }};
        YouTubeDownloaderAdapter target = new YouTubeDownloaderAdapter(VIDEO_URL_INVALID, this.destinationDirectory, this.binaryConfiguration);

        DownloadException exception = target.createExceptionForErrorMessages(errorMessages);

        assertThat("Exception knows an error has occurred", exception.hasErrorOccurred(), is(true));

        assertThat(exception.getErrorMessage().getMessage(), is("Warning 3"));
        assertThat(exception.getErrorMessage().getType(), is(Message.Type.WARNING));

        DownloadException cause1 = (DownloadException) exception.getCause();
        assertThat(cause1.getErrorMessage().getMessage(), is("Warning 2"));
        assertThat(cause1.getErrorMessage().getType(), is(Message.Type.WARNING));

        DownloadException cause2 = (DownloadException) exception.getCause().getCause();
        assertThat(cause2.getErrorMessage().getMessage(), is("Error 1"));
        assertThat(cause2.getErrorMessage().getType(), is(Message.Type.ERROR));

        DownloadException cause3 = (DownloadException) exception.getCause().getCause().getCause();
        assertThat(cause3.getErrorMessage().getMessage(), is("Warning 1"));
        assertThat(cause3.getErrorMessage().getType(), is(Message.Type.WARNING));

        assertThat(exception.getCause().getCause().getCause().getCause(), nullValue());
    }
}
