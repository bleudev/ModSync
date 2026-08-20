package com.bleudev.modsync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Modsync : ModInitializer {
    companion object {
        val LOGGER: Logger = LoggerFactory.getLogger("ModSync")
    }

    override fun onInitialize() {
        val properties = ModSyncHttpServer.Properties.fromFile(
            FabricLoader.getInstance().configDir.resolve("modsync").resolve("server.config.json")
        )
        val server = ModSyncHttpServer(properties)
        runBlocking {
            launch(Dispatchers.IO) {
                server.run()
            }
        }
    }
}
