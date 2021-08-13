package io.nodle.dtn.bpv7

import io.nodle.dtn.crypto.*
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule

/**
 * @author Lucien Loiseau on 14/02/21.
 */

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.Silent::class)
class Ed25519Test {

    @get:Rule
    var initRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun test1_Ed25519Signature() {
        /* Given */
        val key = Ed25519Util.generateEd25519KeyPair()
        val plaintext = "this is a simple message to sign".toByteArray()
        val signature = key.ed25519PrivateKey().signMsg(plaintext)

        /* Then */
        Assert.assertEquals(true, key.ed25519PublicKey().checkSignature(plaintext, signature))
    }

}