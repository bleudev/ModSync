package com.bleudev.modsync.server

import com.bleudev.modsync.ModSyncHttpServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.DedicatedServerModInitializer

class ModSyncServer : DedicatedServerModInitializer {
    override fun onInitializeServer() {
        val properties = ModSyncHttpServer.Properties.fromDefaultFile()
        val server = ModSyncHttpServer(properties)
        runBlocking {
            launch(Dispatchers.IO) {
                server.run()
            }
        }
    }
}