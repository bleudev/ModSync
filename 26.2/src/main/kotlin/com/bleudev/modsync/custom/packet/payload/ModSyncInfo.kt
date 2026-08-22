package com.bleudev.modsync.custom.packet.payload

import com.bleudev.modsync.custom.ModSyncPackets
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

class ModSyncInfo(val port: Int) : CustomPacketPayload {
    companion object {
        val CODEC: StreamCodec<FriendlyByteBuf, ModSyncInfo> = StreamCodec.composite(
            ByteBufCodecs.INT, ModSyncInfo::port,
            ::ModSyncInfo
        )
        val TYPE = CustomPacketPayload.Type<ModSyncInfo>(ModSyncPackets.MOD_SYNC_INFO)
    }
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE
}