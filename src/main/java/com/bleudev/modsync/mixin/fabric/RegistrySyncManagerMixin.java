package com.bleudev.modsync.mixin.fabric;

import com.bleudev.modsync.ModSyncHttpServer;
import com.bleudev.modsync.custom.ModSyncPackets;
import com.bleudev.modsync.custom.packet.payload.ModSyncInfo;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(RegistrySyncManager.class)
public class RegistrySyncManagerMixin {
    @Inject(method = "configureClient", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/registry/sync/RegistrySyncManager;createAndPopulateRegistryMap()Ljava/util/Map;", shift = At.Shift.BEFORE))
    private static void sendPortPacket(ServerConfigurationPacketListenerImpl handler, MinecraftServer server, CallbackInfo ci) {
//        if (server.isDedicatedServer()) {
//            ModSyncHttpServer.Properties properties = ModSyncHttpServer.Properties.fromFile(
//                FabricLoader.getInstance().getConfigDir().resolve("modsync").resolve("server.config.json")
//            );
//            if (ServerConfigurationNetworking.canSend(handler, ModSyncPackets.MOD_SYNC_INFO)) {
//                System.out.println("Trying to send mod sync info packet");
//                handler.addTask(new ModSyncTask(properties));
//            }
//        }
    }

    @Mixin(RegistrySyncManager.SyncConfigurationTask.class)
    public static class SyncConfigurationTaskMixin {
        @Shadow
        @Final
        private ServerConfigurationPacketListenerImpl handler;

        @Inject(method = "start", at = @At("HEAD"))
        private void modSync(Consumer<Packet<?>> sender, CallbackInfo ci) {
            if (ServerConfigurationNetworking.canSend(handler, ModSyncPackets.MOD_SYNC_INFO)) {
                System.out.println("Trying to send mod sync info packet");
                ModSyncHttpServer.Properties properties = ModSyncHttpServer.Properties.fromFile(
                    FabricLoader.getInstance().getConfigDir().resolve("modsync").resolve("server.config.json")
                );
                sender.accept(ServerConfigurationNetworking.createClientboundPacket(new ModSyncInfo(properties.getPort())));
            }
        }
    }
}
