package com.cristianrgreco.ytdl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DownloadStateTest {
    private static final String RESOLVING_MESSAGE = "[youtube] oHg5SJYRHA0: Downloading webpage";
    private static final String DOWNLOADING_MESSAGE = "[download]   0.0% of 9.22MiB at Unknown speed ETA Unknown ETA";
    private static final String CONVERTING_MESSAGE = "[ffmpeg] Destination: RickRoll'D_oHg5SJYRHA0.mp3";

    private static final String PROGRESS_MESSAGE = "[download]   0.0% of 9.22MiB at Unknown speed ETA Unknown ETA";
    private static final String INVALID_PROGRESS_MESSAGE = "Deleting original file C:\\Users\\crgreco\\Desktop\\RickRoll'D_oHg5SJYRHA0.mp4 (pass -k to keep)";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void parsesResolvingState() {
        assertThat("State is resolving", DownloadState.parse(RESOLVING_MESSAGE), is(DownloadState.RESOLVING));
    }

    @Test
    public void parsesDownloadingState() {
        assertThat("State is downloading", DownloadState.parse(DOWNLOADING_MESSAGE), is(DownloadState.DOWNLOADING));
    }

    @Test
    public void parsesConvertingState() {
        assertThat("State is converting", DownloadState.parse(CONVERTING_MESSAGE), is(DownloadState.CONVERTING));
    }

    @Test
    public void throwsExceptionIfAttemptToParseInvalidProgressMessage() {
        this.expectedException.expect(IllegalArgumentException.class);

        DownloadState.parse(INVALID_PROGRESS_MESSAGE);
    }

    @Test
    public void validatesCorrectStateMessage() {
        assertThat("State message is correct", DownloadState.isValidStateMessage(PROGRESS_MESSAGE), is(true));
    }

    @Test
    public void invalidatesIncorrectStateMessage() {
        assertThat("State message is incorrect", DownloadState.isValidStateMessage(INVALID_PROGRESS_MESSAGE), is(false));
    }
}
