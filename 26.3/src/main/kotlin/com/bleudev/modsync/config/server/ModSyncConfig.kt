package com.bleudev.modsync.config.server

import com.bleudev.modsync.MOD_ID
import com.bleudev.modsync.platform.ModSyncPlatform
import com.bleudev.modsync.resolveIdentifier
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler
import dev.isxander.yacl3.config.v2.api.SerialEntry
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder

@Suppress("PropertyName")
class ModSyncConfig {
    @JvmField
    @SerialEntry(
        comment =
            "The port on which the file sharing server will run.\n" +
            "Must be free and open\n" +
            "Default: 8000"
    )
    var port: Int = 8000
    @JvmField
    @SerialEntry(
        comment =
            "IDs of mods that need to be synced\n" +
            "Jar files of all specified mods must be present on the server in the \"mods\" folder.\n" +
            "If the mod is client side drop it to \"client_mods\" folder.\n" +
            "Default: []"
    )
    var mod_ids: List<String> = listOf()
    @JvmField
    @SerialEntry(
        comment =
            "Require the client to have a mod when joining the server\n" +
            "Default: false"
    )
    var require_modsync_to_join: Boolean = false

    companion object {
        private val HANDLER = ConfigClassHandler.createBuilder(ModSyncConfig::class.java)
            .id(resolveIdentifier("config"))
            .serializer {
                GsonConfigSerializerBuilder.create(it)
                    .setPath(ModSyncPlatform.configDir.resolve(MOD_ID).resolve("server.json5"))
                    .setJson5(true)
                    .build()
            }
            .build()

        @JvmStatic
        fun getInstance(): ModSyncConfig {
            HANDLER.load()
            return HANDLER.instance()
        }
    }
}