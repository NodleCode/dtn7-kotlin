package io.nodle.dtn.bpv7

import io.nodle.dtn.bpv7.bpsec.addEd25519Signature
import io.nodle.dtn.bpv7.eid.createDtnEid
import io.nodle.dtn.crypto.Ed25519Util
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import java.net.URI
import kotlin.random.Random

/**
 * @author Lucien Loiseau on 18/02/21.
 */
public object MockBundle {
        fun bundle(s: Int = 10000) = PrimaryBlock()
                .destination(createDtnEid("test-destination"))
                .source(createDtnEid("test-source"))
                .reportTo(createDtnEid("test-report-to"))
                .creationTimestamp(dtnTimeNow())
                .lifetime(10000)
                .crcType(CRCType.CRC32)
                .makeBundle()
                .addBlock(payloadBlock(Random.nextBytes(array = ByteArray(s))))

        fun bundles(c: Int, s: Int = 10000) = (0 until c).map { bundle(s) }
}