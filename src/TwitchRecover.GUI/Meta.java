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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Fetches human-friendly metadata (channel name, title, thumbnail) for a Twitch
 * video or clip via the public GQL endpoint.
 */
class Meta {
    String channel = "";
    String title = "";
    String thumbnailUrl = "";
    long lengthSeconds = 0;

    /** Best-effort: returns metadata, or a mostly-empty object on failure. */
    static Meta fetch(String url) {
        if (Resolver.isClip(url)) return fetchClip(clipSlug(url));
        return fetchVideo(videoId(url));
    }

    static Meta fetchVideo(String id) {
        Meta m = new Meta();
        if (id == null || id.isEmpty()) return m;
        String q = "{\"query\":\"query($id: ID!){ video(id: $id){ title lengthSeconds previewThumbnailURL(height: 180 width: 320) owner{ displayName } } }\",\"variables\":{\"id\":\"" + id + "\"}}";
        try {
            JSONObject data = post(q);
            JSONObject v = data.optJSONObject("video");
            if (v != null) {
                m.title = v.optString("title", "");
                m.lengthSeconds = v.optLong("lengthSeconds", 0);
                m.thumbnailUrl = v.optString("previewThumbnailURL", "");
                JSONObject o = v.optJSONObject("owner");
                if (o != null) m.channel = o.optString("displayName", "");
            }
        } catch (Exception ignored) {}
        return m;
    }

    static Meta fetchClip(String slug) {
        Meta m = new Meta();
        if (slug == null || slug.isEmpty()) return m;
        String q = "{\"query\":\"query($slug: ID!){ clip(slug: $slug){ title durationSeconds thumbnailURL broadcaster{ displayName } } }\",\"variables\":{\"slug\":\"" + slug + "\"}}";
        try {
            JSONObject data = post(q);
            JSONObject c = data.optJSONObject("clip");
            if (c != null) {
                m.title = c.optString("title", "");
                m.lengthSeconds = c.optLong("durationSeconds", 0);
                m.thumbnailUrl = c.optString("thumbnailURL", "");
                JSONObject b = c.optJSONObject("broadcaster");
                if (b != null) m.channel = b.optString("displayName", "");
            }
        } catch (Exception ignored) {}
        return m;
    }

    /** A nice display title like "Channel — Title", falling back to the id. */
    String display(String fallback) {
        String t = title == null ? "" : title.trim();
        String c = channel == null ? "" : channel.trim();
        if (!c.isEmpty() && !t.isEmpty()) return c + " — " + t;
        if (!t.isEmpty()) return t;
        if (!c.isEmpty()) return c;
        return fallback;
    }

    private static JSONObject post(String body) throws Exception {
        URL u = new URL("https://gql.twitch.tv/gql");
        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Client-ID", "kimne78kx3ncx6brgo4mv6wki5h1ko");
        con.setRequestProperty("Content-Type", "application/json");
        con.setConnectTimeout(15000);
        con.setReadTimeout(15000);
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(body.getBytes(StandardCharsets.UTF_8));
        os.close();
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();
        JSONObject root = new JSONObject(sb.toString());
        return root.optJSONObject("data");
    }

    static String videoId(String url) {
        String best = "";
        StringBuilder run = new StringBuilder();
        for (int i = 0; i < url.length(); i++) {
            char ch = url.charAt(i);
            if (ch >= '0' && ch <= '9') run.append(ch);
            else { if (run.length() > best.length()) best = run.toString(); run.setLength(0); }
        }
        if (run.length() > best.length()) best = run.toString();
        return best;
    }

    static String clipSlug(String url) {
        String s = url;
        int q = s.indexOf('?');
        if (q > 0) s = s.substring(0, q);
        if (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        int slash = s.lastIndexOf('/');
        return slash >= 0 ? s.substring(slash + 1) : s;
    }
}
