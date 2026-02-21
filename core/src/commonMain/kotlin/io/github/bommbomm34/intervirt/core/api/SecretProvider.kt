package io.github.bommbomm34.intervirt.core.api

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import eu.anifantakis.lib.ksafe.KSafe
import io.github.bommbomm34.intervirt.core.runSuspendingCatching
import io.github.bommbomm34.intervirt.core.zeroize

object SecretProvider {
    private val ksafe = KSafe()
    private var internalCipher: AES.IvAuthenticatedCipher? = null
    private val cipher: AES.IvAuthenticatedCipher
        get() {
            check(internalCipher != null) { "SecretProvider isn't initialized!" }
            return internalCipher!!
        }
    val initialized: Boolean
        get() = internalCipher != null

    suspend fun init(): Result<Unit> = runSuspendingCatching {
        val masterKey: ByteArray? = ksafe.getEncrypted("master-key", null)
        val provider = CryptographyProvider.Default
        val aesGcm = provider.get(AES.GCM)
        if (masterKey != null) {
            val key = aesGcm
                .keyDecoder()
                .decodeFromByteArray(AES.Key.Format.RAW, masterKey)
            internalCipher = key.cipher()
            masterKey.zeroize()
        } else {
            val key = aesGcm
                .keyGenerator()
                .generateKey()
            val encoded = key.encodeToByteArray(AES.Key.Format.RAW)
            ksafe.putEncrypted("master-key", encoded)
            internalCipher = key.cipher()
            encoded.zeroize()
        }
    }

    suspend fun encrypt(plaintext: ByteArray): String {
        val cipherText = cipher.encrypt(plaintext)
        plaintext.zeroize()
        return cipherText.decodeToString()
    }

    suspend fun decrypt(cipherText: String): ByteArray = cipher.decrypt(cipherText.encodeToByteArray())

    suspend fun wipe(): Result<Unit> = runSuspendingCatching {
        ksafe.delete("master-key")
    }
}