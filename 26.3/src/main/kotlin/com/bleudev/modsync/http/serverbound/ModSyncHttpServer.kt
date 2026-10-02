package com.bleudev.modsync.http.serverbound

import com.bleudev.modsync.ADDITIONAL_MODS_DIR
import com.bleudev.modsync.LOGGER_DISCOVER
import com.bleudev.modsync.LOGGER_GENERAL
import com.bleudev.modsync.LOGGER_REQUEST
import com.bleudev.modsync.ModSync.Companion.JSON
import com.bleudev.modsync.config.server.ModSyncConfig
import com.bleudev.modsync.util.ModJarReader
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import kotlinx.serialization.Serializable
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.net.BindException
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class ModSyncHttpServer {
    internal fun run() {
        val c = ModSyncConfig.getInstance()
        try {
            val server = HttpServer.create(InetSocketAddress(c.port), 0)
            server.createContext("/", RootHandler())
            server.setExecutor(null)
            server.start()
            LOGGER_GENERAL.info("Server was started!")
        }
        catch (e: BindException) {
            // Do not start server if running
            // And print error
            LOGGER_GENERAL.error("Requested port ${c.port} isn't free. Is this port for Minecraft server? Please change it to free and open port in configuration file and restart the server. For now ModSync isn't working.")
            if (c.require_modsync_to_join) {
                throw e // You have no choice - change port. Because no one could join the server now.
            }
        }
    }

    class RootHandler : HttpHandler {
        private val additionalMods = hashMapOf<String, ModCachedData>()
        private fun discoverAdditionalMods() {
            additionalMods.clear()

            val additionalModsPath = FabricLoader.getInstance().gameDir.resolve(ADDITIONAL_MODS_DIR)
            if (!Files.exists(additionalModsPath)) {
                Files.createDirectories(additionalModsPath)
            }

            for (path in Files.walk(additionalModsPath)) {
                val f = path.toFile()
                // Read only jar files
                if (f.isFile && f.name.endsWith(".jar")) {
                    LOGGER_DISCOVER.info("Discovered additional mod with path: $path")
                    val reader = ModJarReader(path)
                    reader.getModData()?.let {
                        LOGGER_DISCOVER.info("Successfully generated additional mod metadata: $it")
                        additionalMods[it.id] = it
                    }
                }
            }
        }

        private fun getModsData(): List<ModCachedData> {
            val mods = arrayListOf<ModCachedData>()
            for (id in ModSyncConfig.getInstance().mod_ids) {
                FabricLoader.getInstance().getModContainer(id).ifPresentOrElse( {
                    mods.add(ModCachedData(id, it.metadata.version.friendlyString, it.origin.paths.first().toFile()))
                }, {
                    additionalMods[id]?.let { mods.add(it) }
                })
            }
            return mods.toList()
        }

        override fun handle(t: HttpExchange) {
            discoverAdditionalMods()
            val mods = getModsData() // Optimization

            val modId = t.requestURI.path.replace("\\?.*".toRegex(), "").substring(1)
            LOGGER_REQUEST.info("${t.requestMethod} /$modId")
            if (modId.isEmpty()) {
                t.textRespond(JSON.encodeToString(
                    ModSyncMetadata(mods.map { ModSyncMetadata.ModMetadata(it.id, it.version, it.file.name) }),
                ), 200, "application/json")
            } else {
                val file = mods.find { it.id == modId }?.file
                if (file == null || !file.exists()) {
                    t.textRespond("Not found", 404)
                }
                else {
                    t.responseHeaders["Content-Type"] = "application/java-archive"
                    t.responseHeaders["Content-Disposition"] = "attachment; filename=\"${file.name}\""
                    t.sendResponseHeaders(200, file.length())
                    t.responseBody.use { os ->
                        Files.copy(file.toPath(), os)
                    }
                }
            }
        }

        private fun HttpExchange.textRespond(response: String, code: Int, contentType: String = "text/html") {
            val responseBytes = response.toByteArray(StandardCharsets.UTF_8)
            responseHeaders["Content-Type"] = "$contentType; charset=UTF-8"
            sendResponseHeaders(code, responseBytes.size.toLong())
            responseBody.use { it.write(responseBytes) }
        }
    }

    data class ModCachedData(val id: String, val version: String, val file: File)

    @Serializable
    data class ModSyncMetadata(val mods: List<ModMetadata>) {
        @Serializable
        data class ModMetadata(val id: String, val version: String, val fileName: String)
    }
}