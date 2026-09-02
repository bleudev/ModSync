package com.bleudev.modsync.client

import com.bleudev.modsync.ModSyncHttpServer

class ClientTempStorageManager {
    var serverHost: String? = null
    var serverPort: Int? = null
    val serverAddress: String? get() {
        val h = serverHost ?: return null
        val p = serverPort ?: return null
        return "$h:$p"
    }

    internal var updateData: List<ModSyncHttpServer.ModSyncMetadata.ModMetadata> = listOf()
    internal var updateAddress: String = ""
    internal var updating: Boolean = false
    internal var requireRestartAddresses: ArrayList<String> = arrayListOf()

    companion object {
        private var instance: ClientTempStorageManager? = null

        @JvmStatic
        fun getInstance(): ClientTempStorageManager {
            if (instance == null) {
                instance = ClientTempStorageManager()
            }
            return instance!!
        }
    }
}