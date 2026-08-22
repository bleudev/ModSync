package com.bleudev.modsync.mixin.client;

import com.bleudev.modsync.client.ClientTempStorageManager;
import com.bleudev.modsync.i.mixin.client.IConnectScreen;
import io.netty.channel.ChannelFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ConnectScreen.class)
public class ConnectScreenMixin extends Screen implements IConnectScreen {
    @Shadow
    private volatile boolean aborted;

    @Shadow
    private @Nullable ChannelFuture channelFuture;

    @Shadow
    private volatile @Nullable Connection connection;

    @Shadow
    @Final
    public static Component ABORT_CONNECTION;

    @Shadow
    @Final
    private Screen parent;

    protected ConnectScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "connect", at = @At("HEAD"))
    private void saveServerHost(Minecraft minecraft, ServerAddress hostAndPort, ServerData server, TransferState transferState, CallbackInfo ci) {
        ClientTempStorageManager.getInstance().setServerHost(hostAndPort.getHost());
        ClientTempStorageManager.getInstance().setServerPort(hostAndPort.getPort());
    }

    @Override
    public void modsync$cancel() {
        synchronized(this) {
            this.aborted = true;
            if (this.channelFuture != null) {
                this.channelFuture.cancel(true);
                this.channelFuture = null;
            }

            if (this.connection != null) {
                this.connection.disconnect(ABORT_CONNECTION);
            }
        }

        this.minecraft.gui.setScreen(this.parent);
    }
}
