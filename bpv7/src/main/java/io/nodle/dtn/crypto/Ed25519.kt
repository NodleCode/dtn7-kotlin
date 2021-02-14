package io.nodle.dtn.crypto

import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator
import org.bouncycastle.crypto.Signer
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.io.OutputStream
import java.security.SecureRandom

/**
 * @author Lucien Loiseau on 14/02/21.
 */

object Ed25519Util {
    private var kpgen: AsymmetricCipherKeyPairGenerator = Ed25519KeyPairGenerator()

    init {
        kpgen.init(Ed25519KeyGenerationParameters(SecureRandom()))
    }

    fun generateEd25519KeyPair(): AsymmetricCipherKeyPair {
        return kpgen.generateKeyPair()
    }
}

fun AsymmetricCipherKeyPair.ed25519PrivateKey() : Ed25519PrivateKeyParameters = private as Ed25519PrivateKeyParameters

fun AsymmetricCipherKeyPair.ed25519PublicKey() : Ed25519PublicKeyParameters = ed25519PrivateKey().generatePublicKey()

fun ByteArray.toEd25519PublicKey() : Ed25519PublicKeyParameters = Ed25519PublicKeyParameters(this, 0)

fun ByteArray.toEd25519PrivateKey() : Ed25519PrivateKeyParameters = Ed25519PrivateKeyParameters(this, 0)

class Ed25519SignerStream(key : Ed25519PrivateKeyParameters) : OutputStream() {
    private val signer: Signer = Ed25519Signer()

    init {
        signer.init(true, key)
    }

    override fun write(b: Int) {
        signer.update(byteArrayOf(b.toByte()), 0, 1)
    }

    fun done(): ByteArray = signer.generateSignature()
}

class Ed25519SignerCheckStream(key : Ed25519PublicKeyParameters) : OutputStream() {
    private val signer: Signer = Ed25519Signer()

    init {
        signer.init(false, key)
    }

    override fun write(b: Int) {
        signer.update(byteArrayOf(b.toByte()), 0, 1)
    }

    fun done(signature : ByteArray): Boolean = signer.verifySignature(signature)
}


fun Ed25519PrivateKeyParameters.signMsg(msg: ByteArray) : ByteArray {
    val signer: Signer = Ed25519Signer()
    signer.init(true, this)
    signer.update(msg, 0, msg.size)
    return signer.generateSignature()
}

fun Ed25519PublicKeyParameters.checkSignature(msg : ByteArray, signature : ByteArray) : Boolean {
    val signer: Signer = Ed25519Signer()
    signer.init(false, this)
    signer.update(msg, 0, msg.size)
    return signer.verifySignature(signature)
}
