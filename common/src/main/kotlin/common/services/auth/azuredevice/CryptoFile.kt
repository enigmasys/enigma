package common.services.auth.azuredevice

import java.io.*
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CryptoFile(private val secretKey: SecretKey, cipher: String?) {
    private val cipher: Cipher

    init {
        this.cipher = Cipher.getInstance(cipher)
    }

    @Throws(InvalidKeyException::class, IOException::class)
    fun encrypt(content: String, fileName: String?) {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        if (fileName != null) {
            FileOutputStream(fileName).use { fileOut ->
                CipherOutputStream(fileOut, cipher).use { cipherOut ->
                    fileOut.write(iv)
                    cipherOut.write(content.toByteArray())
                }
            }
        }
    }

    @Throws(InvalidAlgorithmParameterException::class, InvalidKeyException::class, IOException::class)
    fun decrypt(fileName: String?): String {
        var content: String = ""
        if (fileName != null) {
            FileInputStream(fileName).use { fileIn ->
                val fileIv = ByteArray(16)
                fileIn.read(fileIv)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(fileIv))
                CipherInputStream(fileIn, cipher).use { cipherIn ->
                    InputStreamReader(cipherIn).use { inputReader ->
                        BufferedReader(inputReader).use { reader ->
                            val sb = StringBuilder()
                            var line: String?
                            while (reader.readLine().also { line = it } != null) {
                                sb.append(line)
                            }
                            content = sb.toString()
                        }
                    }
                }
            }
        }
        return content
    }
}