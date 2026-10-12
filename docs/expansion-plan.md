# Distribution and loader expansion

The current release remains stable and Fabric-only. A volunteer platform-test issue template collects physical Windows/macOS/Linux results. A separate opt-in preview channel should only be enabled for an actual preview build with stated changes; stable users should not be migrated to beta.

CurseForge publication requires a project under the maintainer's account, platform access and a deliberate decision about release mirroring. No CurseForge upload is claimed.

NeoForge is the first loader to investigate. The rendering hooks, native-widget access, resource entrypoints and 24 version adapters need a small proof of concept before estimating a port. Sharing decoder/config/media-pipe code is feasible, but Architectury or Stonecutter does not remove those API differences. Forge follows only after the NeoForge prototype and target-version decision. No loader compatibility or numerical cost estimate is claimed without that prototype.

The repository currently has a versioned name. All current source/issues links should point to the same active repository. Renaming it is optional and needs a migration of remotes, release links, CI, metadata and historical references; no repository rename is required to correct the broken source link.
