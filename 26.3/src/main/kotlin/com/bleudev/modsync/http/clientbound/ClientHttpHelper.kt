package com.bleudev.modsync.http.clientbound

import com.bleudev.modsync.ModSync.Companion.JSON
import com.bleudev.modsync.http.serverbound.serialization.ModSyncMetadata
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path

class ClientHttpHelper(val fullAddress: String) {
    typealias ProgressFunction = (downloaded: Long, total: Long?) -> Unit

    private fun <T> get(endpoint: String, bodyHandler: HttpResponse.BodyHandler<T>, additionalAction: (HttpResponse<T>) -> Unit = {}): HttpResponse<T> {
        HttpClient.newHttpClient().use { client ->
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$fullAddress$endpoint"))
                .GET()
                .build()
            val r = client.send(request, bodyHandler)
            additionalAction(r)
            return r
        }
    }

    private fun getString(endpoint: String): String = get(endpoint, HttpResponse.BodyHandlers.ofString()).body()
    private fun getFile(endpoint: String, path: Path, onProgress: ProgressFunction = { _, _ -> }) {
        get(endpoint, HttpResponse.BodyHandlers.ofInputStream()) { response ->
            response.body().use { input ->
                Files.createDirectories(path.parent)
                Files.newOutputStream(path).use { output ->
                    val total = response.headers()
                        .firstValueAsLong("Content-Length")
                        .orElse(-1L)
                        .takeIf { it >= 0 }

                    val buffer = ByteArray(8192)
                    var downloaded = 0L
                    var read: Int

                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read

                        onProgress(downloaded, total)
                    }
                }
            }
        }
    }

    internal fun metadata(): ModSyncMetadata = JSON
        .decodeFromString<ModSyncMetadata>(getString("/"))
    internal fun mod(modsDir: Path, data: ModSyncMetadata.ModMetadata, onProgress: ProgressFunction = { _, _ -> }) {
        getFile("/${data.id}", modsDir.resolve(data.fileName), onProgress)
    }
}