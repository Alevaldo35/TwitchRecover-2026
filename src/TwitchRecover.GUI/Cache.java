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

import java.io.File;
import java.io.FilenameFilter;

/**
 * Removes the temporary segment folders/playlists the downloader leaves in the
 * system temp directory, so the app never silently eats disk space.
 */
final class Cache {
    private Cache() {}

    /** Delete every "TwitchRecover-*" item from the system temp folder. */
    static void purge() {
        try {
            File tmp = new File(System.getProperty("java.io.tmpdir"));
            File[] items = tmp.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) { return name.startsWith("TwitchRecover-"); }
            });
            if (items != null) {
                for (File f : items) deleteRecursively(f);
            }
        } catch (Exception ignored) {}
    }

    private static void deleteRecursively(File f) {
        if (f == null) return;
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids != null) for (File k : kids) deleteRecursively(k);
        }
        try { f.delete(); } catch (Exception ignored) {}
    }
}
