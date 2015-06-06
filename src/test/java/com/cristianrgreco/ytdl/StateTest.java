package com.cristianrgreco.ytdl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class StateTest {
    private static final String RESOLVING_MESSAGE = "[youtube] oHg5SJYRHA0: Downloading webpage";
    private static final String DOWNLOADING_MESSAGE = "[download]   0.0% of 9.22MiB at Unknown speed ETA Unknown ETA";
    private static final String CONVERTING_MESSAGE = "[ffmpeg] Destination: RickRoll'D_oHg5SJYRHA0.mp3";

    private static final String PROGRESS_MESSAGE = "[download]   0.0% of 9.22MiB at Unknown speed ETA Unknown ETA";
    private static final String INVALID_PROGRESS_MESSAGE_1 = "[invalid]   0.0% of 9.22MiB at Unknown speed ETA Unknown ETA";
    private static final String INVALID_PROGRESS_MESSAGE_2 = "Deleting original file C:\\Users\\crgreco\\Desktop\\RickRoll'D_oHg5SJYRHA0.mp4";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void parsesResolvingState() {
        assertThat("State is resolving", State.parse(RESOLVING_MESSAGE), is(State.RESOLVING));
    }

    @Test
    public void parsesDownloadingState() {
        assertThat("State is downloading", State.parse(DOWNLOADING_MESSAGE), is(State.DOWNLOADING));
    }

    @Test
    public void parsesConvertingState() {
        assertThat("State is converting", State.parse(CONVERTING_MESSAGE), is(State.CONVERTING));
    }

    @Test
    public void throwsExceptionIfAttemptToParseInvalidStateMessage() {
        this.expectedException.expect(IllegalArgumentException.class);

        State.parse(INVALID_PROGRESS_MESSAGE_1);
    }

    @Test
    public void validatesCorrectStateMessage() {
        assertThat("State message is correct", State.isValidStateMessage(PROGRESS_MESSAGE), is(true));
    }

    @Test
    public void invalidatesIncorrectStateMessage() {
        assertThat("State message is incorrect", State.isValidStateMessage(INVALID_PROGRESS_MESSAGE_1), is(false));
        assertThat("State message is incorrect", State.isValidStateMessage(INVALID_PROGRESS_MESSAGE_2), is(false));
    }
}
