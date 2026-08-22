package com.bleudev.modsync

import com.bleudev.modsync.ModSync.Companion.JSON
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.fabricmc.loader.api.FabricLoader
import java.io.File
import java.io.IOException
import java.net.BindException
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

class ModSyncHttpServer(private val properties: Properties) {
    internal fun run() {
        val mods = arrayListOf<ModCachedData>()
        for (id in properties.modIds) {
            FabricLoader.getInstance().getModContainer(id).ifPresent {
                mods.add(ModCachedData(id, it.metadata.version.friendlyString, it.origin.paths.first().toFile()))
            }
        }
        try {
            val server = HttpServer.create(InetSocketAddress(properties.port), 0)
            server.createContext("/", RootHandler(mods.toList()))
            server.setExecutor(null)
            server.start()
            LOGGER_GENERAL.info("Server was started!")
        }
        catch (e: IOException) {
            if (e !is BindException) { // Do not start server if running
                throw RuntimeException(e)
            }
        }
    }

    class RootHandler(private val mods: List<ModCachedData>) : HttpHandler {
        override fun handle(t: HttpExchange) {
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
    @Serializable
    data class Properties(
        val port: Int = 8000,
        @SerialName("mod_ids") val modIds: List<String> = listOf(),
        @SerialName("require_modsync_to_join") val requireModsyncToJoin: Boolean = false
    ) {
        companion object {
            @JvmStatic
            fun fromFile(path: Path): Properties {
                try {
                    if (!path.exists()){
                        Files.createDirectories(path.parent)
                        Files.writeString(path, JSON.encodeToString(Properties()))
                    }
                } catch (e: Throwable) {
                    LOGGER_GENERAL.error("Error while initialization properties file: $e")
                }
                val s1 = Files.readString(path)
                val prop = JSON.decodeFromString<Properties>(s1)
                val s2 = JSON.encodeToString(prop)
                if (s1 != s2) {
                    Files.writeString(path, s2)
                }
                return prop
            }
            @JvmStatic
            fun fromDefaultFile(): Properties = fromFile(
                FabricLoader.getInstance().configDir.resolve(MOD_ID).resolve("server.config.json")
            )
        }
    }
}