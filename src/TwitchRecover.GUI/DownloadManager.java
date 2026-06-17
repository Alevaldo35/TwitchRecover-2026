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

import TwitchRecover.Core.Clips;
import TwitchRecover.Core.Downloader.Download;
import TwitchRecover.Core.Downloader.ProgressListener;
import TwitchRecover.Core.Feeds;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * App-wide download queue. Tasks are processed one at a time on a background
 * thread (each video itself downloads its segments in parallel), so the user
 * can queue many VODs at once and they all download in turn.
 */
class DownloadManager {
    private static final DownloadManager INSTANCE = new DownloadManager();
    static DownloadManager get() { return INSTANCE; }

    private final List<DownloadTask> tasks = Collections.synchronizedList(new ArrayList<DownloadTask>());
    private final BlockingQueue<DownloadTask> queue = new LinkedBlockingQueue<DownloadTask>();
    private Thread worker;

    private DownloadManager() {}

    void add(DownloadTask t) {
        tasks.add(t);
        queue.offer(t);
        ensureWorker();
    }

    /** Snapshot of all tasks (most recent first) for the UI to render. */
    List<DownloadTask> snapshot() {
        synchronized (tasks) {
            ArrayList<DownloadTask> copy = new ArrayList<DownloadTask>(tasks);
            Collections.reverse(copy);
            return copy;
        }
    }

    void clearFinished() {
        synchronized (tasks) {
            ArrayList<DownloadTask> keep = new ArrayList<DownloadTask>();
            for (DownloadTask t : tasks) {
                if (t.state != DownloadTask.State.DONE && t.state != DownloadTask.State.FAILED) {
                    keep.add(t);
                }
            }
            tasks.clear();
            tasks.addAll(keep);
        }
    }

    private synchronized void ensureWorker() {
        if (worker != null && worker.isAlive()) return;
        worker = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    DownloadTask t;
                    try {
                        t = queue.take();
                    } catch (InterruptedException e) {
                        return;
                    }
                    process(t);
                }
            }
        }, "TR-download-worker");
        worker.setDaemon(true);
        worker.start();
    }

    private void process(final DownloadTask t) {
        t.state = DownloadTask.State.DOWNLOADING;
        try {
            boolean clip = Resolver.isClip(t.sourceUrl);
            if (clip) {
                Clips c = new Clips();
                String link = c.retrieveURL(t.sourceUrl, false);
                if (link == null || link.isEmpty()) throw new Exception("Clip not found");
                t.outputPath = Download.download(link, t.destDir + baseName(t, true));
            } else {
                Feeds f = Resolver.videoFeeds(t.sourceUrl);
                if (f == null || f.getFeeds().isEmpty()) throw new Exception("No playable link found");
                String feed = Resolver.pickFeed(f, t.quality);
                String base = t.destDir + baseName(t, false);
                String tsPath = base + ".ts";
                Download.m3u8Download(feed, tsPath, new ProgressListener() {
                    public void update(int completed, int total) {
                        t.done = completed;
                        t.total = total;
                    }
                });
                t.outputPath = finalize(tsPath, base, t.formatExt);
            }
            Meta m = Meta.fetch(t.sourceUrl);
            String nice = m.display(t.title);
            t.title = nice;
            t.state = DownloadTask.State.DONE;
            Library.get().addDownloaded(t.sourceUrl, nice, clip, t.outputPath, m.thumbnailUrl);
        } catch (Throwable ex) {
            t.error = ex.getMessage() == null ? ex.toString() : ex.getMessage();
            t.state = DownloadTask.State.FAILED;
        } finally {
            // Never leave temporary segments behind (they can be many GB).
            Cache.purge();
        }
    }

    /**
     * Turn the merged .ts into the requested container. Uses ffmpeg to remux
     * (so the file actually plays); falls back to renaming if ffmpeg is missing.
     * @return the final output file path.
     */
    private String finalize(String tsPath, String base, String formatExt) {
        String fmt = (formatExt == null || formatExt.isEmpty()) ? "mov" : formatExt.toLowerCase();
        if (fmt.equals("ts")) return tsPath;
        String out = base + "." + fmt;
        if (Ffmpeg.remux(tsPath, out)) {
            new File(tsPath).delete();
            return out;
        }
        // ffmpeg unavailable or failed: keep the working .ts (most compatible).
        return tsPath;
    }

    /** A filesystem-safe base file name (no extension). */
    private String baseName(DownloadTask t, boolean clip) {
        return "TwitchRecover-" + (clip ? "clip" : "video") + "-" + idFromUrl(t.sourceUrl);
    }

    private String idFromUrl(String url) {
        String best = "";
        StringBuilder run = new StringBuilder();
        for (int i = 0; i < url.length(); i++) {
            char ch = url.charAt(i);
            if (ch >= '0' && ch <= '9') {
                run.append(ch);
            } else {
                if (run.length() > best.length()) best = run.toString();
                run.setLength(0);
            }
        }
        if (run.length() > best.length()) best = run.toString();
        if (best.isEmpty()) {
            String s = url.replaceAll("[^a-zA-Z0-9]", "");
            best = s.length() > 12 ? s.substring(s.length() - 12) : s;
        }
        if (best.isEmpty()) best = String.valueOf(System.currentTimeMillis());
        return best;
    }
}
