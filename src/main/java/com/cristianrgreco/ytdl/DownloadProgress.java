package com.cristianrgreco.ytdl;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadProgress {
    private static final Pattern PERCENTAGE_COMPLETE = Pattern.compile(".*?([0-9.]+)");
    private static final Pattern FILE_SIZE = Pattern.compile(".*of.*?([A-z0-9].+?) ");
    private static final Pattern DOWNLOAD_SPEED = Pattern.compile(".*at.*?(([A-z0-9].+?)|(Unknown)) ");
    private static final Pattern ETA = Pattern.compile(".*ETA.*?(([0-9:]+)|(Unknown))");
    private static final Pattern VALID_PROGRESS_MESSAGE = Pattern.compile(
            PERCENTAGE_COMPLETE.toString() + FILE_SIZE.toString() + DOWNLOAD_SPEED.toString() + ETA.toString());

    private final BigDecimal percentageComplete;
    private final String fileSize;
    private final String downloadSpeed;
    private final String eta;

    private DownloadProgress(BigDecimal percentageComplete, String fileSize, String downloadSpeed, String eta) {
        this.percentageComplete = percentageComplete;
        this.fileSize = fileSize;
        this.downloadSpeed = downloadSpeed;
        this.eta = eta;
    }

    public static DownloadProgress parse(String progressMessage) {
        Matcher percentageCompleteMatcher = PERCENTAGE_COMPLETE.matcher(progressMessage);
        Matcher fileSizeMatcher = FILE_SIZE.matcher(progressMessage);
        Matcher downloadSpeedMatcher = DOWNLOAD_SPEED.matcher(progressMessage);
        Matcher etaMatcher = ETA.matcher(progressMessage);

        if (!isValidProgressMessage(progressMessage) ||
                !percentageCompleteMatcher.find() ||
                !fileSizeMatcher.find() ||
                !downloadSpeedMatcher.find() ||
                !etaMatcher.find()) {
            throw new IllegalArgumentException("Unable to parse: " + progressMessage);
        }

        BigDecimal percentageComplete = new BigDecimal(percentageCompleteMatcher.group(1));
        String fileSize = fileSizeMatcher.group(1);
        String downloadSpeed = downloadSpeedMatcher.group(1);
        String eta = etaMatcher.group(1);

        return new DownloadProgress(percentageComplete, fileSize, downloadSpeed, eta);
    }

    public static boolean isValidProgressMessage(String progressMessage) {
        return VALID_PROGRESS_MESSAGE.matcher(progressMessage).find();
    }

    public BigDecimal getPercentageComplete() {
        return percentageComplete;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getDownloadSpeed() {
        return downloadSpeed;
    }

    public String getEta() {
        return eta;
    }

    @Override
    public String toString() {
        return "DownloadProgress{" +
                "percentageComplete=" + percentageComplete +
                ", fileSize='" + fileSize + '\'' +
                ", downloadSpeed='" + downloadSpeed + '\'' +
                ", eta='" + eta + '\'' +
                '}';
    }
}
