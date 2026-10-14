# 1.8.17 — Smooth playback and clearer menus

- Pause both video and audio in Backgrounds, Store, My Library and EVATEX Settings. Leaving resumes the existing player at its paused position.
- Render the main EVATEX wordmark as a dedicated antialiased, linearly sampled texture.
- Fix modern server/world entries retaining cached positions after list resizing, which could clip icons and misalign rows. Keep native layouts at smaller sizes.
- Add four verified, directly downloadable MP4 space backgrounds from NASA/Goddard, with visible credits and a dedicated Videos filter. The catalogue now contains 254 entries: 238 photos, 12 GIFs and 4 MP4 videos.
- Reuse the parsed catalogue across menu transitions instead of reading and parsing it repeatedly on the game thread. A successful Refresh replaces this cache.
- Add restrained press feedback and a short, dry interface click.
