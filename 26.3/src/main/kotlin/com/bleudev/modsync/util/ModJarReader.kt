package com.bleudev.modsync.util

import com.bleudev.modsync.ModSyncHttpServer
import net.fabricmc.loader.impl.lib.gson.JsonReader
import net.fabricmc.loader.impl.lib.gson.JsonToken
import net.fabricmc.loader.impl.metadata.ParseMetadataException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.zip.ZipFile

class ModJarReader(val jarPath: Path) {
    private fun read(name: String): InputStream? {
        try {
            ZipFile(jarPath.toFile()).use { zipFile ->
                val stream = zipFile.getInputStream(zipFile.getEntry(name) ?: return null)
                return stream
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private var metadata: ModSyncHttpServer.ModCachedData? = null

    fun getModData(): ModSyncHttpServer.ModCachedData? {
        if (metadata == null) {
            val metadataStream = read("fabric.mod.json") ?: return null

            var id: String? = null
            var version: String? = null

            JsonReader(InputStreamReader(metadataStream, StandardCharsets.UTF_8)).use { reader ->
                if (reader.peek() != JsonToken.BEGIN_OBJECT) {
                    throw ParseMetadataException("Root of \"fabric.mod.json\" must be an object", reader)
                }
                reader.beginObject()
                while (reader.hasNext()) {
                    val key = reader.nextName()

                    when (key) {
                        "id" -> {
                            if (reader.peek() != JsonToken.STRING) {
                                throw ParseMetadataException("Mod id must be a non-empty string with a length of 3-64 characters.", reader)
                            }
                            id = reader.nextString()
                        }
                        "version" -> {
                            if (reader.peek() != JsonToken.STRING) {
                                throw ParseMetadataException("Version must be a non-empty string", reader)
                            }
                            version = reader.nextString()
                        }
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
            }

            metadata = ModSyncHttpServer.ModCachedData(id!!, version!!, jarPath.toFile())
        }

        return metadata
    }
}