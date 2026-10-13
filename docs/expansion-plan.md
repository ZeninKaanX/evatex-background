# Distribution and loader expansion

The current release remains stable and Fabric-only. A volunteer platform-test issue template collects physical Windows/macOS/Linux results. A separate opt-in preview channel should only be enabled for an actual preview build with stated changes; stable users should not be migrated to beta.

CurseForge publication requires a project under the maintainer's account, platform access and a deliberate decision about release mirroring. No CurseForge upload is claimed.

NeoForge is the first loader to investigate. The rendering hooks, native-widget access, resource entrypoints and 24 version adapters need a small proof of concept before estimating a port. Sharing decoder/config/media-pipe code is feasible, but Architectury or Stonecutter does not remove those API differences. Forge follows only after the NeoForge prototype and target-version decision. No loader compatibility or numerical cost estimate is claimed without that prototype.

The repository currently has a versioned name. All current source/issues links should point to the same active repository. Renaming it is optional and needs a migration of remotes, release links, CI, metadata and historical references; no repository rename is required to correct the broken source link.

## Preliminary effort estimate

This is a planning estimate from source inspection, not a quote or a measured port. The main source tree contains 22 mixin files. Rendering/font pipelines, menu lifecycle and compatibility fallbacks are the principal uncertainties.

| Work | Preliminary effort for one experienced maintainer |
| --- | --- |
| One-version NeoForge proof of concept: entrypoint, one title-screen video and configuration | 3–5 developer days |
| Complete first supported NeoForge target: menus, typography, diagnostics, packaging and regression checks | 2–4 developer weeks including the prototype |
| Windows/macOS volunteer validation and release review | Depends on available testers; not included as a promised deadline |
| Forge after the NeoForge prototype | Estimate again after the target Minecraft version and actual shared-code boundary are known |
| Broad historical-version coverage | Estimate per available loader/game target after the prototype; the 24 Fabric builds are not 24 guaranteed NeoForge targets |

The estimate assumes reuse of the media engine and one initial Minecraft target, without redesigning the interface. It excludes external hosting fees and legal review. No monetary cost can be calculated without a rate/budget and chosen scope.
