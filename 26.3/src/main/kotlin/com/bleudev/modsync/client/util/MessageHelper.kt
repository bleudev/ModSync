package com.bleudev.modsync.client.util

import com.bleudev.modsync.ModSyncHttpServer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

object MessageHelper {
    fun getVersionChangeMessage(metadata: ModSyncHttpServer.ModSyncMetadata.ModMetadata, withId: Boolean = false): Component {
        val id = metadata.id
        val newVersion = metadata.version

        var string = if (withId) "${id}: " else ""
        var color: ChatFormatting
        val container = FabricLoader.getInstance().getModContainer(id)
        if (container.isPresent) {
            val currentVersion = container.get().metadata.version.friendlyString
            string += "$currentVersion -> $newVersion"
            color = ChatFormatting.GREEN
        }
        else {
            string += newVersion
            color = ChatFormatting.AQUA
        }

        return Component.literal(string).withStyle(color)
    }
}