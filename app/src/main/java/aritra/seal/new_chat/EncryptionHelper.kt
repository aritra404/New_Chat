package aritra.seal.new_chat

import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

// Generate AES Secret Key
fun generateAESKey(): SecretKey {
    val keyGen = KeyGenerator.getInstance("AES")
    keyGen.init(256)
    return keyGen.generateKey()
}

// Encrypt a message using AES
fun encryptMessageAES(message: String, secretKey: SecretKey): String {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    val encryptedBytes = cipher.doFinal(message.toByteArray())
    return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
}

// Decrypt a message using AES
fun decryptMessageAES(encryptedMessage: String, secretKey: SecretKey): String {
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.DECRYPT_MODE, secretKey)
    val decodedBytes = Base64.decode(encryptedMessage, Base64.DEFAULT)
    return String(cipher.doFinal(decodedBytes))
}

// Encrypt AES key using RSA
fun encryptAESKeyWithRSA(secretKey: SecretKey, publicKey: PublicKey): String {
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    val encryptedKey = cipher.doFinal(secretKey.encoded)
    return Base64.encodeToString(encryptedKey, Base64.DEFAULT)
}

// Decrypt AES key using RSA
fun decryptAESKeyWithRSA(encryptedKey: String, privateKey: PrivateKey): SecretKey {
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.DECRYPT_MODE, privateKey)
    val decodedKey = Base64.decode(encryptedKey, Base64.DEFAULT)
    return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
}
