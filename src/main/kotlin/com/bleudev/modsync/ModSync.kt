package com.bleudev.modsync

import com.bleudev.modsync.custom.ModSyncPackets
import kotlinx.serialization.json.Json
import net.fabricmc.api.ModInitializer
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
    }
}
