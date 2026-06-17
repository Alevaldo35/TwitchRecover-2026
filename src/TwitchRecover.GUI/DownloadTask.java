/*
 * Copyright (c) 2020, 2021 Daylam Tayari <daylam@tayari.gg>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *  Original project by Daylam Tayari - https://github.com/TwitchRecover/TwitchRecover
 *  Clean GUI added in this fork by Alevaldo35 - https://github.com/Alevaldo35
 */

package TwitchRecover.GUI;

/** One queued download. Fields read by the UI are volatile so the refresh timer sees updates. */
class DownloadTask {
    enum State { QUEUED, DOWNLOADING, DONE, FAILED }

    final String sourceUrl;     // Original Twitch URL pasted by the user.
    final String destDir;       // Destination folder (with trailing separator).
    final String quality;       // Preferred quality: source/1080/720/480/360/160/audio.
    final String formatExt;     // Output container without dot: mov/mp4/mkv/ts/avi.
    volatile String title;      // Friendly name shown in the list (refined with metadata).

    volatile State state = State.QUEUED;
    volatile int done = 0;      // Completed segments (m3u8) — for the progress bar.
    volatile int total = 0;     // Total segments; 0 = unknown / indeterminate.
    volatile String outputPath; // Final file path once finished.
    volatile String error;      // Error message if failed.

    DownloadTask(String sourceUrl, String destDir, String quality, String formatExt, String title) {
        this.sourceUrl = sourceUrl;
        this.destDir = destDir;
        this.quality = quality;
        this.formatExt = formatExt;
        this.title = title;
    }

    /** Progress as a percentage 0-100, or -1 when indeterminate. */
    int percent() {
        if (state == State.DONE) return 100;
        if (total <= 0) return -1;
        return (int) Math.min(100, Math.round(done * 100.0 / total));
    }
}
