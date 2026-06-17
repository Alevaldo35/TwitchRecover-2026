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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * Locates and runs ffmpeg to remux the merged .ts file into a real container
 * (mp4/mov/mkv…). This is what makes the downloaded recording actually playable.
 */
final class Ffmpeg {
    private Ffmpeg() {}

    private static String cached;
    private static boolean searched;

    /** Returns an ffmpeg command/path, or null if none is available. */
    static synchronized String find() {
        if (searched) return cached;
        searched = true;
        String[] fileCandidates = {
            "ffmpeg.exe",
            "bin" + File.separator + "ffmpeg.exe",
            "lib" + File.separator + "ffmpeg.exe",
            "src" + File.separator + "TwitchRecover.Core" + File.separator + "Libraries" + File.separator + "ffmpeg.exe"
        };
        for (String c : fileCandidates) {
            File f = new File(c);
            if (f.exists()) { cached = f.getAbsolutePath(); return cached; }
        }
        // Try ffmpeg on the system PATH.
        try {
            Process p = new ProcessBuilder("ffmpeg", "-version").redirectErrorStream(true).start();
            p.getInputStream().close();
            p.waitFor();
            cached = "ffmpeg";
        } catch (Exception e) {
            cached = null;
        }
        return cached;
    }

    static boolean available() { return find() != null; }

    /**
     * Remux (no re-encode) the input file into the output container.
     * @return true on success and the output file exists.
     */
    static boolean remux(String input, String output) {
        String ff = find();
        if (ff == null) return false;
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    ff, "-y", "-i", input, "-c", "copy", "-bsf:a", "aac_adtstoasc", output);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while (br.readLine() != null) { /* drain output so ffmpeg does not block */ }
            br.close();
            int code = p.waitFor();
            return code == 0 && new File(output).exists() && new File(output).length() > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
