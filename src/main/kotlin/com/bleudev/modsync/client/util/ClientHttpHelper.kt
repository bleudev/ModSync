package com.bleudev.modsync.client.util

import com.bleudev.modsync.ModSync.Companion.JSON
import com.bleudev.modsync.ModSyncHttpServer
import net.fabricmc.loader.api.FabricLoader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path

class ClientHttpHelper(val fullAddress: String) {
    private fun <T> get(endpoint: String, bodyHandler: HttpResponse.BodyHandler<T>): HttpResponse<T> {
        HttpClient.newHttpClient().use { client ->
            val request = HttpRequest.newBuilder()
                .uri(URI.create("http://$fullAddress$endpoint"))
                .GET()
                .build()
            return client.send(request, bodyHandler)
        }
    }

    private fun getString(endpoint: String): String = get(endpoint, HttpResponse.BodyHandlers.ofString()).body()
    private fun getFile(endpoint: String, path: Path) = get(endpoint, HttpResponse.BodyHandlers.ofFile(path)).body()

    internal fun metadata(): ModSyncHttpServer.ModSyncMetadata = JSON
        .decodeFromString<ModSyncHttpServer.ModSyncMetadata>(getString("/"))
    internal fun mod(modsDir: Path, id: String, version: String) =
        getFile("/$id", modsDir.resolve("$id-$version.jar"))
}