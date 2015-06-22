package com.cristianrgreco.ytdl;

public class Message {
    private static final String WARNING_PREFIX = "WARNING:";
    private static final String ERROR_PREFIX = "ERROR:";

    private String message;
    private Type type;

    private Message(String message, Type type) {
        this.message = message;
        this.type = type;
    }

    public static Message from(String text) {
        if (text.startsWith(WARNING_PREFIX)) {
            String messageText = text.substring(WARNING_PREFIX.length()).trim();
            return new Message(messageText, Type.WARNING);
        } else if (text.startsWith(ERROR_PREFIX)) {
            String messageText = text.substring(ERROR_PREFIX.length()).trim();
            return new Message(messageText, Type.ERROR);
        } else {
            return new Message(text, Type.ERROR);
        }
    }

    public enum Type {
        WARNING,
        ERROR
    }

    public String getMessage() {
        return message;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", type=" + type +
                '}';
    }
}
