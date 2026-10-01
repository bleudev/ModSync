# ModSync

[![Downloads](https://img.shields.io/modrinth/dt/modsyncer)](https://modrinth.com/mod/modsyncer)
[![Latest version](https://img.shields.io/badge/Latest_version-0.1-orange)](https://modrinth.com/mod/modsyncer/versions)
[![Supported Minecraft versions](https://img.shields.io/badge/Supported_Minecraft_Versions-26.2--26.3--snapshot--9-green)](https://modrinth.com/mod/modsyncer)
[![Environment](https://img.shields.io/badge/Environment-Client%2BServer-blue)](https://modrinth.com/mod/modsyncer)
[![GitHub contributors](https://img.shields.io/github/contributors/bleudev/ModSync)](https://github.com/bleudev/ModSync/graphs/contributors)
[![GitHub commit activity](https://img.shields.io/github/commit-activity/w/bleudev/ModSync)](https://github.com/bleudev/ModSync/activity)
[![GitHub last commit](https://img.shields.io/github/last-commit/bleudev/ModSync)](https://github.com/bleudev/ModSync/activity)

A mod that automatically synchronizes Client and Server mods upon connection.

![Only for Fabric](https://wsrv.nl/?url=https%3A%2F%2Fi.ibb.co%2FyphNcXz%2Ffabric-only-banner.png&n=-1)

### Supported Minecraft versions and minimal requirements
| Minecraft version | Max Mod Sync version | Fabric Language Kotlin    | Fabric API  | YACL      | Fabric Loader |
|-------------------|----------------------|:--------------------------|:------------|:----------|---------------|
| 26.3-snapshot-9   | ✅ 0.1               | \>= 1.13.13+kotlin.2.4.10 | \>= 0.158.0 | \>= 3.9.6 | \>= 0.19.3    |
| 26.2              | ✅ 0.1               | \>= 1.13.13+kotlin.2.4.10 | \>= 0.158.0 | \>= 3.9.6 | \>= 0.19.3    |

## Configuration

For the mod to work, it must be installed on both the server and the client.
- Open a different port than the one the server is running on.
- Start the server
- Specify this port in the resulting `config/modsync/server.json5` (in the `port` field)
- Specify the IDs of the mods you want to sync (in the `mod_ids` field)
- Configure additional settings (see below)

### Example config

```json5
{
    // The port on which the file sharing server will run.
    // Must be free and open
    // Default: 8000
    "port": 1234,
    // IDs of mods that need to be synced
    // Jar files of all specified mods must be present on the server in the mods folder.
    // Default: []
    "mod_ids": [
      "nine_lifes",
      "modsync"
    ],
    // Require the client to have a mod when joining the server
    // Default: false
    "require_modsync_to_join": true
}
```
