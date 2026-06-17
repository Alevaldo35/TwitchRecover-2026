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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Persistent gallery library, stored as JSON in the TWITCH folder. Tracks every
 * video the user has watched (streamed) and/or downloaded.
 */
class Library {
    private static final Library INSTANCE = new Library();
    static Library get() { return INSTANCE; }

    private final List<LibraryEntry> entries = new ArrayList<LibraryEntry>();

    private Library() { load(); }

    synchronized void addStreamed(String url, String title, boolean clip, String playLink, String thumbnailUrl) {
        LibraryEntry e = find(url);
        if (e == null) { e = new LibraryEntry(); e.url = url; entries.add(e); }
        e.title = title;
        e.clip = clip;
        if (playLink != null && !playLink.isEmpty()) e.playLink = playLink;
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) e.thumbnailUrl = thumbnailUrl;
        e.ts = System.currentTimeMillis();
        save();
    }

    synchronized void addDownloaded(String url, String title, boolean clip, String filePath, String thumbnailUrl) {
        LibraryEntry e = find(url);
        if (e == null) { e = new LibraryEntry(); e.url = url; entries.add(e); }
        e.title = title;
        e.clip = clip;
        e.downloaded = true;
        e.filePath = filePath;
        if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) e.thumbnailUrl = thumbnailUrl;
        e.ts = System.currentTimeMillis();
        save();
    }

    synchronized List<LibraryEntry> snapshot() {
        ArrayList<LibraryEntry> copy = new ArrayList<LibraryEntry>(entries);
        Collections.sort(copy, new Comparator<LibraryEntry>() {
            public int compare(LibraryEntry a, LibraryEntry b) { return Long.compare(b.ts, a.ts); }
        });
        return copy;
    }

    synchronized void clear() {
        entries.clear();
        save();
    }

    synchronized void remove(String url) {
        LibraryEntry e = find(url);
        if (e != null) { entries.remove(e); save(); }
    }

    private LibraryEntry find(String url) {
        for (LibraryEntry e : entries) if (e.url.equals(url)) return e;
        return null;
    }

    private void load() {
        try {
            File f = Paths.libraryFile();
            if (!f.exists()) return;
            String json = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                LibraryEntry e = new LibraryEntry();
                e.url = o.optString("url", "");
                e.title = o.optString("title", e.url);
                e.clip = o.optBoolean("clip", false);
                e.downloaded = o.optBoolean("downloaded", false);
                e.filePath = o.optString("filePath", null);
                e.playLink = o.optString("playLink", null);
                e.thumbnailUrl = o.optString("thumbnailUrl", null);
                e.ts = o.optLong("ts", 0);
                if (!e.url.isEmpty()) entries.add(e);
            }
        } catch (Exception ignored) {}
    }

    private void save() {
        try {
            JSONArray arr = new JSONArray();
            for (LibraryEntry e : entries) {
                JSONObject o = new JSONObject();
                o.put("url", e.url);
                o.put("title", e.title);
                o.put("clip", e.clip);
                o.put("downloaded", e.downloaded);
                if (e.filePath != null) o.put("filePath", e.filePath);
                if (e.playLink != null) o.put("playLink", e.playLink);
                if (e.thumbnailUrl != null) o.put("thumbnailUrl", e.thumbnailUrl);
                o.put("ts", e.ts);
                arr.put(o);
            }
            Files.write(Paths.libraryFile().toPath(), arr.toString(2).getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {}
    }
}
