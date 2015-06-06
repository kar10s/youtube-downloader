package com.cristianrgreco.ytdl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DownloadProgressTest {
    private static final String PROGRESS_MESSAGE = "[download]  10.8% of 9.22MiB at  5.68MiB/s ETA 00:01";
    private static final String PROGRESS_MESSAGE_UNKNOWN = "[download]  21.7% of 9.22MiB at Unknown speed ETA Unknown ETA";
    private static final String INVALID_PROGRESS_MESSAGE = "[download]  abc of 9.22MiBat  5.68MiB/s ETA 00:01";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void parsePercentageCompleteFromProgressMessage() {
        DownloadProgress downloadProgress = DownloadProgress.parse(PROGRESS_MESSAGE);

        assertThat("Percentage complete is correct", downloadProgress.getPercentageComplete(), is(BigDecimal.valueOf(10.8)));
    }

    @Test
    public void parseFileSizeFromProgressMessage() {
        DownloadProgress downloadProgress = DownloadProgress.parse(PROGRESS_MESSAGE);

        assertThat("File size is correct", downloadProgress.getFileSize(), is("9.22MiB"));
    }

    @Test
    public void parseDownloadSpeedFromProgressMessage() {
        DownloadProgress downloadProgress = DownloadProgress.parse(PROGRESS_MESSAGE);

        assertThat("Download speed is correct", downloadProgress.getDownloadSpeed(), is("5.68MiB/s"));
    }

    @Test
    public void parseDownloadSpeedFromProgressMessageWhenUnknown() {
        DownloadProgress downloadProgress = DownloadProgress.parse(PROGRESS_MESSAGE_UNKNOWN);

        assertThat("Download speed is unknown", downloadProgress.getDownloadSpeed(), is("Unknown"));
    }

    @Test
    public void parseEtaFromProgressMessage() {
        DownloadProgress downloadProgress = DownloadProgress.parse(PROGRESS_MESSAGE);

        assertThat("ETA is correct", downloadProgress.getEta(), is("00:01"));
    }

    @Test
    public void parseEtaFromProgressMessageWhenUnknown() {
        DownloadProgress downloadProgress = DownloadProgress.parse(PROGRESS_MESSAGE_UNKNOWN);

        assertThat("ETA is unknown", downloadProgress.getEta(), is("Unknown"));
    }

    @Test
    public void throwsExceptionIfAttemptToParseInvalidProgressMessage() {
        this.expectedException.expect(IllegalArgumentException.class);

        DownloadProgress.parse(INVALID_PROGRESS_MESSAGE);
    }

    @Test
    public void validatesCorrectProgressMessage() {
        assertThat("Process message is correct", DownloadProgress.isValidProgressMessage(PROGRESS_MESSAGE), is(true));
    }

    @Test
    public void invalidatesIncorrectProgressMessage() {
        assertThat("Progress message is incorrect", DownloadProgress.isValidProgressMessage(INVALID_PROGRESS_MESSAGE), is(false));
    }
}
