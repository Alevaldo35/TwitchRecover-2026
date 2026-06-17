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

import TwitchRecover.Core.Enums.Quality;
import TwitchRecover.Core.Feeds;
import TwitchRecover.Core.Highlights;
import TwitchRecover.Core.VOD;

import java.util.List;

/**
 * Figures out what a pasted Twitch link is and retrieves its links, so the user
 * never has to choose between VOD / highlight / clip.
 */
final class Resolver {
    private Resolver() {}

    static boolean isClip(String url) {
        String u = url.toLowerCase();
        return u.contains("clips.twitch.tv") || u.contains("twitch.tv/clips") || u.contains("/clip/");
    }

    /**
     * Retrieve the feeds of a video link, transparently trying both the VOD and
     * the highlight paths (a /videos/ link can be either).
     * @return Feeds (possibly empty) or null.
     */
    static Feeds videoFeeds(String url) {
        VOD v = new VOD(false);
        v.retrieveID(url);
        Feeds f = v.getVODFeeds();
        if (f != null && !f.getFeeds().isEmpty()) return f;
        // Fall back to the highlight playlist layout (highlight-<id>.m3u8).
        Highlights h = new Highlights(false);
        h.retrieveID(url);
        return h.getHighlightFeeds();
    }

    /**
     * Picks the feed URL matching a quality preference
     * ("source", "1080", "720", "480", "360", "160", "audio"), with sensible
     * fallbacks to source / first feed.
     */
    static String pickFeed(Feeds f, String pref) {
        if (f == null || f.getFeeds().isEmpty()) return null;
        List<Quality> qs = f.getQualities();
        if (pref == null || pref.equals("source")) return sourceOrFirst(f, qs);
        if (pref.equals("audio")) {
            for (int i = 0; i < qs.size(); i++) if (qs.get(i) == Quality.AUDIO) return f.getFeed(i);
            return sourceOrFirst(f, qs);
        }
        for (int i = 0; i < qs.size(); i++) {
            Quality q = qs.get(i);
            if (q != null && q.text.startsWith(pref)) return f.getFeed(i);
        }
        return sourceOrFirst(f, qs);
    }

    private static String sourceOrFirst(Feeds f, List<Quality> qs) {
        for (int i = 0; i < qs.size(); i++) if (qs.get(i) == Quality.Source) return f.getFeed(i);
        return f.getFeed(0);
    }
}
