## 1.8.19 — Fabric Loader compatibility

- Stop imposing the 26.x Loader requirement on older Minecraft builds. Minimums are now 0.16.14 for 1.20–1.21.8, 0.17.0 for 1.21.9–1.21.10, 0.17.3 for 1.21.11, 0.18.4 for 26.1–26.2 and 0.19.3 for 26.3.
- Check bundled Fabric modules against the advertised Loader minimum during packaging, so updates cannot silently introduce a stricter dependency.
- Keep the bundled MixinExtras 0.5.5 compatibility fix for Loader 0.19.3.
- Test actual release files without rewriting their Loader requirement in the isolated client test.

Install one JAR matching your exact Minecraft version. Other mods may require newer Loader versions independently.
