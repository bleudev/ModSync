package com.bleudev.modsync.custom

import com.bleudev.modsync.custom.packet.payload.ModSyncInfo
import com.bleudev.modsync.resolveIdentifier
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry

object ModSyncPackets {
    val MOD_SYNC_INFO = resolveIdentifier("packet/mod_sync_info")
    fun initialize() {
        PayloadTypeRegistry.clientboundPlay().register(ModSyncInfo.TYPE, ModSyncInfo.CODEC)
    }
}