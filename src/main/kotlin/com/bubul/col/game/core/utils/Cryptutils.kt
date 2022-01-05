package com.bubul.col.game.core.utils

import java.io.IOException
import java.security.InvalidKeyException
import java.security.Key
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.SecretKeySpec

class CryptoException(msg: String, ex: Throwable) : Exception(msg, ex) {

}

class Cryptutils {

    companion object {
        private val ALGORITHM = "AES"
        private val TRANSFORMATION = "AES"

        @Throws(CryptoException::class)
        fun decrypt(key: ByteArray, data: String): String {
            try {
                return String(doCrypto(Cipher.DECRYPT_MODE, key, Base64.getDecoder().decode(data)))
            } catch (e: IllegalArgumentException) {
                throw CryptoException("Error encrypting/decrypting file", e)
            }

        }

        @Throws(CryptoException::class)
        fun encrypt(key: ByteArray, data: String): String {
            try {
                return Base64.getEncoder().encodeToString(doCrypto(Cipher.ENCRYPT_MODE, key, data.toByteArray()))
            } catch (e: IllegalArgumentException) {
                throw CryptoException("Error encrypting/decrypting file", e)
            }
        }

        @Throws(CryptoException::class)
        fun parseAesKey(pem: String): ByteArray {
            try {
                return Base64.getDecoder().decode(pem.replace("\n", "").replace("\r", ""))
            } catch (e: IllegalArgumentException) {
                throw CryptoException("Error encrypting/decrypting file", e)
            }
        }

        @Throws(CryptoException::class)
        private fun doCrypto(cipherMode: Int, key: ByteArray, input: ByteArray): ByteArray {
            try {
                val secretKey: Key = SecretKeySpec(key, ALGORITHM)
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(cipherMode, secretKey)
                return cipher.doFinal(input)
            } catch (ex: NoSuchPaddingException) {
                throw CryptoException("Error encrypting/decrypting file", ex)
            } catch (ex: NoSuchAlgorithmException) {
                throw CryptoException("Error encrypting/decrypting file", ex)
            } catch (ex: InvalidKeyException) {
                throw CryptoException("Error encrypting/decrypting file", ex)
            } catch (ex: BadPaddingException) {
                throw CryptoException("Error encrypting/decrypting file", ex)
            } catch (ex: IllegalBlockSizeException) {
                throw CryptoException("Error encrypting/decrypting file", ex)
            } catch (ex: IOException) {
                throw CryptoException("Error encrypting/decrypting file", ex)
            }
        }

    }
}