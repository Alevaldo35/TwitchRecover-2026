/*
 * Copyright (c) 2020, 2021 Daylam Tayari <daylam@tayari.gg>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not see http://www.gnu.org/licenses/ or write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  Original project by Daylam Tayari - https://github.com/TwitchRecover/TwitchRecover
 *  Clean GUI added in this fork by Alevaldo35 - https://github.com/Alevaldo35
 */

package TwitchRecover.GUI;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Entry point for the graphical (GUI) version of Twitch Recover.
 * Run with "--cli" to start the original command-line interface instead.
 */
public class App {
    public static void main(String[] args) {
        for (String a : args) {
            if (a.equalsIgnoreCase("--cli") || a.equalsIgnoreCase("-cli")) {
                TwitchRecover.CLI.CLI.main(new String[0]);
                return;
            }
        }

        // Keep ALL temporary download segments inside Videos\TWITCH\.cache so the app
        // never writes large temp files anywhere else on the disk.
        TwitchRecover.Core.Downloader.Download.setTempBaseDir(Paths.cacheDir());

        // Clean up any leftover temporary download segments so the app never
        // silently eats disk space (e.g. after a previous crash or force-quit).
        Cache.purge();

        // If a portable VLC ships next to the app (folder "vlc/"), point the
        // native binding at it so the internal player works with nothing installed.
        // Otherwise vlcj auto-discovers a system-installed VLC.
        java.io.File vlcDir = new java.io.File("vlc");
        if (new java.io.File(vlcDir, "libvlc.dll").exists()) {
            System.setProperty("jna.library.path", vlcDir.getAbsolutePath());
        }

        // Clean, flat, Apple-like look and feel.
        UIManager.put("Button.arc", 14);
        UIManager.put("Component.arc", 14);
        UIManager.put("TextComponent.arc", 14);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.accentColor", Ui.ACCENT);
        FlatLightLaf.setup();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }
}
