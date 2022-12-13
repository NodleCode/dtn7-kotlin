package io.nodle.dtn.bpv7


import io.nodle.dtn.bpv7.bpsec.addEd25519Signature
import io.nodle.dtn.crypto.Ed25519Util
import io.nodle.dtn.utils.encodeToBase64
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.junit.Assert
import org.junit.Test
import kotlin.random.Random.Default.nextBytes

class FragmentationTest {

    private val keyPair = Ed25519Util.generateEd25519KeyPair()
    private val bundle = MockBundle.bundle()
        .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

    @Test
    fun testNoFragmentation() {
        val reassembled = bundle.fragment(20000).reassemble()
        Assert.assertEquals(reassembled.cborMarshal().encodeToBase64(), bundle.cborMarshal().encodeToBase64())
    }

    @Test
    fun testSimpleFragmentation() {
        val reassembled = bundle.fragment(1000).reassemble()
        Assert.assertEquals(reassembled.cborMarshal().encodeToBase64(), bundle.cborMarshal().encodeToBase64())
    }

    @Test
    fun testSimpleFragmentation2() {
        val reassembled = bundle.fragment(666).reassemble()
        Assert.assertEquals(reassembled.cborMarshal().encodeToBase64(), bundle.cborMarshal().encodeToBase64())
    }

    @Test
    fun testSubFragmentation() {
        val fragmented = bundle.fragment(666).flatMap { it.fragment(300) }
        val reassembled = fragmented.shuffled().reassemble()
        Assert.assertEquals(reassembled.cborMarshal().encodeToBase64(), bundle.cborMarshal().encodeToBase64())
    }
}
