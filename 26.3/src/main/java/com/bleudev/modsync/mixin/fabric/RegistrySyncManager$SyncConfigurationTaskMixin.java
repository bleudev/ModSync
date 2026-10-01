package com.bleudev.modsync.mixin.fabric;

import com.bleudev.modsync.config.server.ModSyncConfig;
import com.bleudev.modsync.custom.ModSyncPackets;
import com.bleudev.modsync.custom.packet.payload.ModSyncInfo;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@SuppressWarnings("UnstableApiUsage")
@Mixin(RegistrySyncManager.SyncConfigurationTask.class)
public class RegistrySyncManager$SyncConfigurationTaskMixin {
    @Shadow
    @Final
    private ServerConfigurationPacketListenerImpl handler;

    @Inject(method = "start", at = @At("HEAD"), cancellable = true)
    private void modSync(Consumer<Packet<?>> sender, CallbackInfo ci) {
        if (ServerConfigurationNetworking.canSend(handler, ModSyncPackets.MOD_SYNC_INFO)) {
            sender.accept(ServerConfigurationNetworking.createClientboundPacket(new ModSyncInfo(ModSyncConfig.getInstance().port)));
        } else if (ModSyncConfig.getInstance().require_modsync_to_join) {
            ci.cancel();
            handler.disconnect(Component.literal("ModSync is not installed!"));
        }
    }
}
