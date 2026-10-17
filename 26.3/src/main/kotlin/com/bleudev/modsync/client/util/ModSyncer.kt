package com.bleudev.modsync.client.util

import com.bleudev.modsync.LOGGER_GENERAL
import com.bleudev.modsync.client.ClientTempStorageManager
import com.bleudev.modsync.http.clientbound.ClientHttpHelper
import com.bleudev.modsync.http.serverbound.serialization.ModSyncMetadata
import kotlinx.io.IOException
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.fabricmc.loader.api.Version
import net.fabricmc.loader.api.metadata.ModMetadata
import java.net.http.HttpConnectTimeoutException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

class ModSyncer private constructor(private val modsDir: Path) {
    fun fetch(address: String): List<ModSyncMetadata.ModMetadata> {
        val h = ClientHttpHelper(address)
        try {
            val mods = h.metadata().mods
            val ans = arrayListOf<ModSyncMetadata.ModMetadata>()
            for (data in mods) {
                val current = FabricLoader.getInstance().getModContainer(data.id)
                    .map(ModContainer::getMetadata)
                    .map(ModMetadata::getVersion)
                    .getOrNull()
                if (current == null || current < Version.parse(data.version)) {
                    ans.add(data)
                }
            }
            return ans.toList()
        } catch (_: HttpConnectTimeoutException) {
            return listOf()
        }
    }

    fun sync(address: String, data: ModSyncMetadata.ModMetadata): Boolean {
        val ff = FabricLoader.getInstance().getModContainer(data.id).map { it.origin.paths }.getOrDefault(listOf())
        val h = ClientHttpHelper(address)
        try {
            h.mod(modsDir, data) { downloaded, total ->
                ClientTempStorageManager.getInstance().downloadReadBytes = downloaded
                ClientTempStorageManager.getInstance().downloadTotalBytes = total
            }
        } catch (_: HttpConnectTimeoutException) {
            return false
        }
        for (f in ff) {
            try {
                Files.deleteIfExists(f)
            }
            catch (_: IOException) { // Some error with deleting
                try {
                    val os = System.getProperty("os.name")
                    // Windows support
                    if (os.lowercase().startsWith("windows")) {
                        winScheduleDelete(f)
                    }
                    // Other systems support
                    else {
                        f.toFile().deleteOnExit()
                        LOGGER_GENERAL.warn("Could not delete mod JAR; scheduled deletion on JVM exit: $f")
                    }
                } catch (e: Exception) {
                    LOGGER_GENERAL.error(
                        "Failed to schedule deletion of mod JAR: $f",
                        e
                    )
                }
                return true
            }
        }
        return true
    }

    private fun winScheduleDelete(file: Path) {
        val pid = ProcessHandle.current().pid()
        val path = file.toAbsolutePath().toString().replace("'", "''")
        val script = $$"""
        $targetPid = $$pid
        $targetPath = '$$path'
        while (Get-Process -Id $targetPid `
                -ErrorAction SilentlyContinue) {
            Start-Sleep -Milliseconds 500
        }
        for ($i = 0; $i -lt 40; $i++) {
            try {
                if (-not (Test-Path -LiteralPath $targetPath)) {
                    exit 0
                }
                Remove-Item -LiteralPath $targetPath `
                    -Force -ErrorAction Stop
                exit 0
            } catch {
                Start-Sleep -Milliseconds 500
            }
        }
        exit 1
        """.trimIndent()

        ProcessBuilder(
            "powershell.exe",
            "-NoProfile",
            "-NonInteractive",
            "-WindowStyle", "Hidden",
            "-Command", script
        )
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.DISCARD)
            .start()
        LOGGER_GENERAL.info(
            "Could not delete mod JAR; scheduled mod JAR deletion after Minecraft exits: $file"
        )
    }

    companion object {
        fun getInstance(): ModSyncer = ModSyncer(FabricLoader.getInstance().gameDir.resolve("mods"))
    }
}