package com.bleudev.modsync.client

class ClientTempStorageManager {
    var serverHost: String? = null
    var serverPort: Int? = null
    val serverAddress: String? get() {
        val h = serverHost ?: return null
        val p = serverPort ?: return null
        return "$h:$p"
    }

    var downloadReadBytes: Long = 0
    var downloadTotalBytes: Long? = null

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