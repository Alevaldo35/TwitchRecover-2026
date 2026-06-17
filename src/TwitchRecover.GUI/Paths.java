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

/** Where downloads and the gallery library live: C:\Users\<user>\Videos\TWITCH. */
final class Paths {
    private Paths() {}

    static File twitchDir() {
        File dir = new File(System.getProperty("user.home"), "Videos" + File.separator + "TWITCH");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    /** Folder path with a trailing separator, ready to prepend to a file name. */
    static String twitchDirWithSep() {
        return twitchDir().getAbsolutePath() + File.separator;
    }

    static File libraryFile() {
        return new File(twitchDir(), "library.json");
    }
}
