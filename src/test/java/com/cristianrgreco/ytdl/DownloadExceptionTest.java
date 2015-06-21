package com.cristianrgreco.ytdl;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DownloadExceptionTest {
    @Test
    public void hasErrorOccurredInExceptionChain() throws DownloadException {
        DownloadException ex1 = new DownloadException(Message.from("WARNING: Warning 1"));
        DownloadException ex2 = new DownloadException(Message.from("ERROR: Error 1"), ex1);
        DownloadException ex3 = new DownloadException(Message.from("WARNING: Warning 2"), ex2);

        assertThat("An error has occurred in the exception chain", ex3.hasErrorOccurred(), is(true));
    }
}
