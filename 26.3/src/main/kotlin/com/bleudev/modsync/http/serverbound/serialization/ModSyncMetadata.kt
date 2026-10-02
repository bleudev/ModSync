package com.bleudev.modsync.http.serverbound.serialization

import com.bleudev.modsync.http.serverbound.ModSyncHttpServer
import kotlinx.serialization.Serializable

@Serializable
data class ModSyncMetadata(val mods: List<ModMetadata>) {
    @Serializable
    data class ModMetadata(val id: String, val version: String, val fileName: String)

    companion object {
        fun from(modsCachedData: List<ModSyncHttpServer.ModCachedData>): ModSyncMetadata =
            ModSyncMetadata(modsCachedData.map {
                ModMetadata(
                    it.id,
                    it.version,
                    it.file.name
                )
            })
    }
}