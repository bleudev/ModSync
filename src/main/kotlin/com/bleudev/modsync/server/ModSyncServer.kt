package com.bleudev.modsync.server

import com.bleudev.modsync.ModSyncHttpServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.loader.api.FabricLoader

class ModSyncServer : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        val properties = ModSyncHttpServer.Properties.fromFile(
            FabricLoader.getInstance().configDir.resolve("modsync").resolve("server.config.json")
        )

//        ServerPlayerEvents.JOIN.register { player ->
//            ServerPlayNetworking.send(player, ModSyncInfo(properties.port))
//        }

        // Server
        val server = ModSyncHttpServer(properties)
        runBlocking {
            launch(Dispatchers.IO) {
                server.run()
            }
        }
    }
}