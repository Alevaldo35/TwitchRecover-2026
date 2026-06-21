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

import java.util.List;

/** Lets panels navigate: open the Download page pre-filled, or open the internal player. */
interface Nav {
    void openDownload(String url);

    /** Open the internal player with selectable qualities. */
    void openPlayer(String title, List<String> labels, List<String> urls, int startIndex);

    /** Stop the internal player and release any file it has open (needed before deleting a file). */
    void stopPlayer();
}
