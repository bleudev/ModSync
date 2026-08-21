package com.bleudev.modsync

import com.bleudev.modsync.custom.ModSyncPackets
import com.bleudev.modsync.custom.packet.payload.ModSyncInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.resources.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory


val LOGGER: Logger = LoggerFactory.getLogger("ModSync")
const val MOD_ID = "modsync"
fun resolveIdentifier(path: String) : Identifier = Identifier.fromNamespaceAndPath(MOD_ID, path)

class ModSync : ModInitializer {
    companion object {
        internal val JSON: Json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }
    }

    override fun onInitialize() {
        ModSyncPackets.initialize()

        val properties = ModSyncHttpServer.Properties.fromFile(
            FabricLoader.getInstance().configDir.resolve("modsync").resolve("server.config.json")
        )

        ServerPlayerEvents.JOIN.register { player ->
            println(player.level().server.isDedicatedServer)
            ServerPlayNetworking.send(player, ModSyncInfo(properties.port))
        }

        // Server
        val server = ModSyncHttpServer(properties)
        runBlocking {
            launch(Dispatchers.IO) {
                server.run()
            }
        }
    }
}
