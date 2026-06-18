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

import java.awt.Desktop;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Opens a stream link or a local file with the operating system's default player. */
final class Player {
    private Player() {}

    static boolean play(String target) {
        if (target == null || target.isEmpty()) return false;

        // Local file -> open with the OS default application.
        try {
            File f = new File(target);
            if (f.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(f);
                return true;
            }
        } catch (Exception ignored) {}

        // Stream URL -> wrap it in a tiny .m3u8 and open with the OS default player.
        try {
            if (Desktop.isDesktopSupported()) {
                File pl = File.createTempFile("TwitchRecover-play-", ".m3u8");
                pl.deleteOnExit();
                String content = "#EXTM3U\n#EXT-X-STREAM-INF:BANDWIDTH=1\n" + target + "\n";
                Files.write(pl.toPath(), content.getBytes(StandardCharsets.UTF_8));
                Desktop.getDesktop().open(pl);
                return true;
            }
        } catch (Exception ignored) {}

        return false;
    }

    static void openFolder(String filePath) {
        try {
            File f = new File(filePath);
            File dir = f.isDirectory() ? f : f.getParentFile();
            if (dir == null) dir = Paths.twitchDir();
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(dir);
        } catch (Exception ignored) {}
    }
}
