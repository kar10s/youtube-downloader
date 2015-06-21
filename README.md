# YouTube Downloader

*This project serves as a Java wrapper for the [youtube-dl](https://rg3.github.io/youtube-dl/) Python binary.*

## Usage

```
YouTubeDownloaderAdapter ytdl = new YouTubeDownloaderAdapter(
    "https://www.youtube.com/watch?v=lWA2pjMjpBs",
    new File("/Users/<username>/Desktop"),
    new BinaryConfiguration(/* ... */));

ytdl.downloadAudio(Optional.of(System.out::println), Optional.empty());
```

## Setup

A total of **3** binaries are required: **youtube-dl**, **ffmpeg** and **ffprobe**; each of which can be downloaded from their 
respective vendor websites. Once downloaded, update `config.properties` to reflect the absolute path of these binaries.

Run all the tests for the project which include a suite of integration tests to ensure that the binaries work as expected.
