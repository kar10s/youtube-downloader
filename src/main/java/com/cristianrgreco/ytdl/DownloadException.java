package com.cristianrgreco.ytdl;

public class DownloadException extends Exception {
    private Message message;
    private DownloadException cause;

    public DownloadException(Message message) {
        super(message.getMessage());
        this.message = message;
    }

    public DownloadException(Message message, DownloadException cause) {
        super(message.getMessage(), cause);
        this.message = message;
        this.cause = cause;
    }

    public boolean hasErrorOccurred() {
        if (this.message.getType() == Message.Type.ERROR) {
            return true;
        }
        DownloadException tail = this.cause;
        while (tail != null) {
            if (tail.message.getType() == Message.Type.ERROR) {
                return true;
            }
            tail = tail.cause;
        }
        return false;
    }

    public Message getErrorMessage() {
        return message;
    }
}
