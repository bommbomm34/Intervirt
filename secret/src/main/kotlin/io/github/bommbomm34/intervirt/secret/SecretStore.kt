package io.github.bommbomm34.intervirt.secret

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uniffi.secret.deleteKey
import uniffi.secret.loadKey
import uniffi.secret.saveKey

class SecretStore(
    val id: String,
    val initialized: Boolean = false
) {
    private var key: AES.IvAuthenticatedCipher? = null

    suspend fun init(): Result<Unit> = catching {
        val provider = CryptographyProvider.Default
        val aesGcm = provider.get(AES.GCM)

        if (initialized) {
            val loadedKey = loadKey(id)
            withContext(Dispatchers.Default){
                val decoded = aesGcm.keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, loadedKey)
                key = decoded.cipher()
            }
        } else {
            withContext(Dispatchers.Default){
                val keyGenerator = aesGcm.keyGenerator(keySize = AES.Key.Size.B256)
                val newKey = keyGenerator.generateKey()
                val bytes = newKey.encodeToByteArray(AES.Key.Format.RAW)
                val cipher = newKey.cipher()
                saveKey(id, bytes)
                bytes.zeroize()
                key = cipher
            }
        }
    }

    suspend fun encrypt(content: ByteArray) = getKey().encrypt(content)

    suspend fun decrypt(ciphertext: ByteArray) = getKey().decrypt(ciphertext)

    suspend fun wipe(): Result<Unit> = catching {
        deleteKey(id)
        key = null
    }

    fun getKey(): AES.IvAuthenticatedCipher {
        check(key != null) { "SecretStore isn't initialized" }
        return key!!
    }
}