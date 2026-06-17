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

/** Plays a stream link or a local file in VLC (falling back to the system player). */
final class Player {
    private Player() {}

    static boolean play(String target) {
        if (target == null || target.isEmpty()) return false;
        String vlc = findVlc();
        try {
            if (vlc != null) { new ProcessBuilder(vlc, target).start(); return true; }
        } catch (Exception ignored) {}
        try {
            new ProcessBuilder("vlc", target).start(); // VLC on PATH (Linux/macOS).
            return true;
        } catch (Exception ignored) {}
        // Last resort for a local file: open with the default application.
        try {
            File f = new File(target);
            if (f.exists() && Desktop.isDesktopSupported()) { Desktop.getDesktop().open(f); return true; }
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

    private static String findVlc() {
        String[] candidates = {
            "C:\\Program Files\\VideoLAN\\VLC\\vlc.exe",
            "C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe",
            "/Applications/VLC.app/Contents/MacOS/VLC",
            "/usr/bin/vlc"
        };
        for (String c : candidates) {
            if (new File(c).exists()) return c;
        }
        return null;
    }
}
