package com.cristianrgreco.ytdl;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MessageTest {
    private static final String WARNING_MESSAGE = "WARNING: Unable to remove downloaded original file";
    private static final String ERROR_MESSAGE = "ERROR: 'test' is not a valid URL";
    private static final String UNKNOWN_MESSAGE = "Traceback for error: _compile()";

    @Test
    public void createsMessageOfWarningType() {
        Message message = Message.from(WARNING_MESSAGE);

        assertThat("Message type is Warning", message.getType(), is(Message.Type.WARNING));
        assertThat("Message text is correct", message.getMessage(), is("Unable to remove downloaded original file"));
    }

    @Test
    public void createsMessageOfErrorType() {
        Message message = Message.from(ERROR_MESSAGE);

        assertThat("Message type is Error", message.getType(), is(Message.Type.ERROR));
        assertThat("Message text is correct", message.getMessage(), is("'test' is not a valid URL"));
    }

    @Test
    public void createsMessageOfErrorTypeForUnknownText() {
        Message message = Message.from(UNKNOWN_MESSAGE);

        assertThat("Message type is Error", message.getType(), is(Message.Type.ERROR));
        assertThat("Message text is correct", message.getMessage(), is("Traceback for error: _compile()"));
    }
}
