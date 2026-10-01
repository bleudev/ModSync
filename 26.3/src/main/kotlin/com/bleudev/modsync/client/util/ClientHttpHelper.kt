package com.bleudev.modsync.client.util

import com.bleudev.modsync.ModSync.Companion.JSON
import com.bleudev.modsync.ModSyncHttpServer
import kotlinx.atomicfu.AtomicRef
import java.io.FileOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path


class ClientHttpHelper(val fullAddress: String) {
    private fun <T> get(endpoint: String, bodyHandler: HttpResponse.BodyHandler<T>): HttpResponse<T> {
        HttpClient.newHttpClient().use { client ->
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$fullAddress$endpoint"))
                .GET()
                .build()
            return client.send(request, bodyHandler)
        }
    }

    private fun getString(endpoint: String): String = get(endpoint, HttpResponse.BodyHandlers.ofString()).body()
    private fun getFile(endpoint: String, path: Path): Path = get(endpoint, HttpResponse.BodyHandlers.ofFile(path)).body()
    private fun getStreamedFile(endpoint: String, path: Path, progressAtomic: AtomicRef<Float>) {
        val resp = get(endpoint, HttpResponse.BodyHandlers.ofInputStream())
        val contentLengthHeader = resp.headers().firstValueAsLong("content-length")
        val totalBytes = contentLengthHeader.orElse(-1L)
        println("Starting download... $endpoint")

        resp.body().use { inputStream ->
            FileOutputStream(path.toFile()).use { outputStream ->
                val buffer = ByteArray(8192)
                var bytesReadTotal: Long = 0
                var bytesRead: Int
                while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    bytesReadTotal += bytesRead.toLong()
                    progressAtomic.value = if (totalBytes > 0)
                        (bytesReadTotal.toFloat() / totalBytes).coerceIn(0f, 1f)
                        else 0f
                }
            }
        }
    }

    internal fun metadata(): ModSyncHttpServer.ModSyncMetadata = JSON
        .decodeFromString<ModSyncHttpServer.ModSyncMetadata>(getString("/"))
    internal fun mod(modsDir: Path, data: ModSyncHttpServer.ModSyncMetadata.ModMetadata, progressAtomic: AtomicRef<Float>) =
        getStreamedFile("/${data.id}", modsDir.resolve(data.fileName), progressAtomic)
}