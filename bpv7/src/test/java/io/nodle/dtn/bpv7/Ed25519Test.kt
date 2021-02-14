package io.nodle.dtn.bpv7

import io.nodle.dtn.crypto.*
import org.junit.Assert
import org.junit.Test

/**
 * @author Lucien Loiseau on 14/02/21.
 */
class Ed25519Test {

    @Test
    fun testEd25519Signature() {
        val key = Ed25519Util.generateEd25519KeyPair()
        val plaintext = "this is a simple message to sign".toByteArray()
        val signature = key.ed25519PrivateKey().signMsg(plaintext)
        Assert.assertEquals(true, key.ed25519PublicKey().checkSignature(plaintext, signature))
    }

}