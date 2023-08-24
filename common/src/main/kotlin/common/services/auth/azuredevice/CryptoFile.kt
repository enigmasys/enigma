package common.services.auth.azuredevice

import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

// Source: https://gist.github.com/kobeumut/17932fd08b5b6dd7ee153a85865f4c54
object CryptHelper {
    private const val SECONDARY_KEY = "RandomInitVector" // 16 bytes IV

    fun encrypt(key: String, value: String): String? {
        try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            val iv = IvParameterSpec(SECONDARY_KEY.toByteArray(charset("UTF-8")))
            val skeySpec = SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")

            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)

            val encrypted = cipher.doFinal(value.toByteArray())
            return String(Base64.getEncoder().encode(encrypted))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    fun decrypt(key: String,  encrypted: String?): String? {
        try {
            val iv = IvParameterSpec(SECONDARY_KEY.toByteArray(charset("UTF-8")))
            val skeySpec = SecretKeySpec(key.toByteArray(charset("UTF-8")), "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)

            val original = cipher.doFinal(Base64.getDecoder().decode(encrypted))

            return String(original)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }
}
