import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.bpsec.addEd25519Signature
import io.nodle.dtn.crypto.Ed25519Util
import io.nodle.dtn.interfaces.IActiveRegistration
import io.nodle.dtn.interfaces.IApplicationAgent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import java.net.URI

@ExperimentalCoroutinesApi
class MockApplicationAgent: IApplicationAgent {
    private val localNodeId = URI.create("dtn://test/")
    private val remoteNodeId = URI.create("dtn://nodle/dtn-router")
    private val keyPair = Ed25519Util.generateEd25519KeyPair()
    val outBundle1 = PrimaryBlock()
        .destination(remoteNodeId)
        .source(localNodeId)
        .reportTo(remoteNodeId)
        .crcType(CRCType.CRC32)
        .makeBundle()
        .addBlock(payloadBlock(ByteArray(10000)))
        .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

    override fun onRegistrationActive(active: IActiveRegistration) {
        runBlockingTest {
            println("Sending Bundle")
            active.sendBundle(outBundle1)

            println("Unregister")
            active.unregister()
        }
    }

    override suspend fun deliver(bundle: Bundle): Boolean {
        println("Delivered Bundle")
        return true
    }
}