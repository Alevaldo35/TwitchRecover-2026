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

/** One gallery item: a video that was watched (streamed) and/or downloaded. */
class LibraryEntry {
    String url;         // Source Twitch URL (acts as the unique key).
    String title;       // Friendly name.
    boolean clip;       // Clip vs video.
    boolean downloaded; // True once a local file exists.
    String filePath;    // Local file path when downloaded.
    String playLink;    // A playable link (m3u8 source / clip mp4) for "Play".
    String thumbnailUrl;// Twitch preview thumbnail.
    long ts;            // Last update time (epoch millis).
}
