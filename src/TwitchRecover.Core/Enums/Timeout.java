/*
 * Copyright (c) 2020, 2021 Daylam Tayari <daylam@tayari.gg>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License version 3as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not see http://www.gnu.org/licenses/ or write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *  @author Daylam Tayari daylam@tayari.gg https://github.com/daylamtayari
 *  @version 2.0aH     2.0a Hotfix
 *  Github project home page: https://github.com/TwitchRecover
 *  Twitch Recover repository: https://github.com/TwitchRecover/TwitchRecover
 */

package TwitchRecover.Core.Enums;

public enum Timeout {
    // These are per-segment timeouts, NOT a limit on the total video length or
    // size: a VOD of any duration downloads fully. They only abort a single
    // segment if the connection stalls (no data) for this long, after which it
    // is retried. Values raised so downloads are never cut on slow connections.
    CONNECT(120000),    //2 minutes to establish a connection.
    READ(600000);       //10 minutes of inactivity before a stalled segment is retried.

    public int time;    //Timeout time in milliseconds.
    Timeout(int m){
        time=m;
    }
}