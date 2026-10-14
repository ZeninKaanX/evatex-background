# Live background catalogue

EVATEX 1.8.18 and later check this GitHub catalogue automatically when Store opens and every five minutes while it remains open. No new mod jar is needed for catalogue additions. GitHub/CDN caching can delay visibility by a few minutes. Offline clients keep their last valid catalogue. Refresh requests an immediate check.

## Add a background

1. Add one JSON file to `catalog/backgrounds/`, following an existing entry. Give it a unique `id`, title, category, media kind, public HTTPS thumbnail and direct media URL, dimensions, creator and usage terms.
2. Include the exact file size and SHA-256 for a download you have permission to distribute (`redistributionAllowed: true`). A source web page is not a direct media URL. Do not upload media into the mod jar.
3. Commit to `main`. The **Publish background catalogue** workflow validates the entries and updates `catalog/v1.json`. Invalid metadata is rejected; the published catalogue remains unchanged.

Available categories: `anime`, `astral`, `nature`, `games`, `minimal`. Kinds: `image`, `gif`, `video`. Supported downloads are limited to 512 MiB per item. Existing pinned personal collection sources retain their original artists' rights; they do not grant redistribution permission.

Removing a JSON file removes that catalogue listing, but never deletes a user's downloaded file. Keep identifiers stable when editing a listing. Test locally with `python3 scripts/compile_catalog.py`; use `--bundled` only when preparing a mod release to update its offline seed too. The workflow deliberately updates online metadata alone, so adding wallpapers does not rebuild every Minecraft version.
