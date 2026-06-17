# Patches — unofficial fork

This file documents **only** the changes made in this fork. All original code is
the work of [Daylam Tayari](https://github.com/daylamtayari) — original project:
**https://github.com/TwitchRecover/TwitchRecover**

Patches by **[Alevaldo35](https://github.com/Alevaldo35)**, applied to the `2.0 Alpha (2.0aH)`
codebase so it builds and runs again in 2026. Distributed under the same GPLv3 license.

---

## What was broken

The 2.0 Alpha no longer compiled or ran on modern setups, and VOD link retrieval
crashed because Twitch removed an API it depended on.

## Changes

### 1. Build / runtime fixes
- **Removed the Lombok dependency.** Lombok `1.18.16` is incompatible with modern
  JDKs (tested on JDK 22). It was only used once (`@Cleanup` in
  `TwitchRecover.Core/Downloader/FileHandler.java`); replaced with a standard
  `try-with-resources` block.
- Added committed dependencies in `lib/` and build/launch helpers
  (`build.sh`, `build.bat`, `run.bat`) so the project builds and runs without
  Maven/Gradle or an IDE.

### 2. Dead Twitch API replaced (the main crash)
File: `TwitchRecover.Core/API/VideoAPI.java` — `getSubVODFeeds()`

- The old endpoint `https://api.twitch.tv/kraken/videos/<id>` (Kraken v5) was
  **shut down by Twitch and now returns HTTP 404**, producing an empty response
  and the crash:
  `org.json.JSONException: A JSONObject text must begin with '{'`.
- Replaced it with the current **Twitch GQL** query
  `VideoPlayer_VODSeekbarPreviewVideo` (POST `https://gql.twitch.tv/gql`) to
  obtain `seekPreviewsURL`.

### 3. Sub-only VOD link reconstruction
File: `TwitchRecover.Core/API/VideoAPI.java`

- Instead of fuzzing an outdated hard-coded list of CDN domains (which failed for
  hosts like `*.cloudfront.net` not in the list → `IndexOutOfBoundsException`),
  the actual CDN host/path is now derived **directly from `seekPreviewsURL`**,
  which already contains it. M3U8 feed URLs are built from that base.

### 4. Robustness / no more hard crashes on missing VODs
File: `TwitchRecover.Core/API/API.java` — `parseToken()`, and
`TwitchRecover.Core/API/VideoAPI.java` — `getVODFeeds()` / `getSubVODFeeds()`

- `parseToken()` now returns `null` (instead of throwing) when the GQL response
  has no `videoPlaybackAccessToken` / `streamPlaybackAccessToken` (e.g. the VOD
  does not exist or is unavailable).
- Callers now bail out gracefully (return empty feeds) on a `null` token or a
  missing/empty API response, instead of crashing with an unhandled exception.

## Verified

- Project compiles cleanly with `javac --release 8` (JDK 22).
- Public VOD link retrieval (option 3) works and returns valid M3U8 links.
- Sub-only VOD link retrieval works (resolved links return HTTP 200).
- Non-existent VOD IDs no longer crash the program.
