package com.cristianrgreco.ytdl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MessageTest {
    private static final String WARNING_MESSAGE = "WARNING: Unable to remove downloaded original file";
    private static final String ERROR_MESSAGE = "ERROR: 'test' is not a valid URL";
    private static final String INVALID_MESSAGE = "Invalid message";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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
    public void throwsExceptionIfAttemptToCreateMessageFromInvalidText() {
        this.expectedException.expect(IllegalArgumentException.class);
        this.expectedException.expectMessage(is("Unable to construct Message from: " + INVALID_MESSAGE));

        Message.from(INVALID_MESSAGE);
    }
}
