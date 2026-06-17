/*
 * Copyright (c) 2020, 2021 Daylam Tayari <daylam@tayari.gg>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *  Original project by Daylam Tayari - https://github.com/TwitchRecover/TwitchRecover
 *  Progress reporting added in this fork by Alevaldo35 - https://github.com/Alevaldo35
 */

package TwitchRecover.Core.Downloader;

/**
 * Simple callback used to report download progress (completed vs total
 * segments) so a UI can show a progress bar.
 */
public interface ProgressListener {
    void update(int completed, int total);
}
