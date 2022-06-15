package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.administrative.*
import io.nodle.dtn.bpv7.bpsec.addEd25519Signature
import io.nodle.dtn.bpv7.extensions.ageBlock
import io.nodle.dtn.bpv7.extensions.hopCountBlockData
import io.nodle.dtn.bpv7.extensions.previousNodeBlock
import io.nodle.dtn.crypto.Ed25519Util
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import java.net.URI

/**
 * @author Lucien Loiseau on 18/02/21.
 */
public object MockBundle {
    val localNodeId = URI.create("dtn://test/")
    val remoteNodeId = URI.create("dtn://nodle/dtn-router")
    val keyPair = Ed25519Util.generateEd25519KeyPair()

    val inBundle1 = PrimaryBlock()
        .destination(localNodeId)
        .source(remoteNodeId)
        .reportTo(remoteNodeId)
        .crcType(CRCType.CRC32)
        .makeBundle()
        .addBlock(payloadBlock(ByteArray(10000)))
        .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

    val inBundle2 = PrimaryBlock()
        .destination(localNodeId)
        .source(remoteNodeId)
        .reportTo(remoteNodeId)
        .procV7Flags(BundleV7Flags.StatusRequestDelivery)
        .crcType(CRCType.CRC32)
        .makeBundle()
        .addBlock(payloadBlock(ByteArray(10000)))
        .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

    val outBundle1 = PrimaryBlock()
        .destination(remoteNodeId)
        .source(localNodeId)
        .reportTo(remoteNodeId)
        .crcType(CRCType.CRC32)
        .makeBundle()
        .addBlock(payloadBlock(ByteArray(10000)))
        .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

    val outBundle2 = PrimaryBlock()
        .destination(remoteNodeId)
        .source(localNodeId)
        .reportTo(remoteNodeId)
        .crcType(CRCType.CRC32)
        .procV7Flags(BundleV7Flags.StatusRequestForward)
        .makeBundle()
        .addBlock(payloadBlock(ByteArray(10000)))
        .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

    val outBundle3 = PrimaryBlock()
        .destination(remoteNodeId)
        .source(localNodeId)
        .reportTo(remoteNodeId)
        .crcType(CRCType.CRC32)
        .procV7Flags(BundleV7Flags.StatusRequestForward)
        .procV7Flags(BundleV7Flags.StatusRequestReception)
        .makeBundle()
        .addBlock(payloadBlock(ByteArray(10000)))
        .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

    val outBundle4 = PrimaryBlock()
        .destination(remoteNodeId)
        .source(localNodeId)
        .reportTo(remoteNodeId)
        .creationTimestamp(0L)
        .lifetime(3000)
        .crcType(CRCType.CRC32)
        .procV7Flags(BundleV7Flags.StatusRequestForward)
        .procV7Flags(BundleV7Flags.StatusRequestReception)
        .makeBundle()
        .addBlock(payloadBlock(ByteArray(10000)))
        .addBlock(ageBlock(3000))
        .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

    val outBundle5 = PrimaryBlock()
        .destination(remoteNodeId)
        .source(localNodeId)
        .reportTo(remoteNodeId)
        .crcType(CRCType.CRC32)
        .procV7Flags(BundleV7Flags.StatusRequestForward)
        .makeBundle()
        .addBlock(payloadBlock(ByteArray(10000)))
        .addBlock(hopCountBlockData(5, 1))
        .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

    val outBundle6 = PrimaryBlock()
        .destination(remoteNodeId)
        .source(localNodeId)
        .reportTo(remoteNodeId)
        .crcType(CRCType.CRC32)
        .procV7Flags(BundleV7Flags.StatusRequestForward)
        .makeBundle()
        .addBlock(payloadBlock(ByteArray(10000)))
        .addBlock(hopCountBlockData(5, 7))
        .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

    val outBundle7 = PrimaryBlock()
        .destination(remoteNodeId)
        .source(localNodeId)
        .reportTo(remoteNodeId)
        .crcType(CRCType.CRC32)
        .procV7Flags(BundleV7Flags.StatusRequestForward)
        .makeBundle()
        .addBlock(payloadBlock(ByteArray(10000)))
        .addBlock(previousNodeBlock(localNodeId))
        .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

    val outBundle8 = PrimaryBlock()
        .destination(remoteNodeId)
        .source(localNodeId)
        .reportTo(remoteNodeId)
        .crcType(CRCType.CRC32)
        .procV7Flags(BundleV7Flags.StatusRequestForward)
        .makeBundle()
        .addBlock(payloadBlock(ByteArray(10000)))
        .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

    val outBundle9 = PrimaryBlock()
        .destination(remoteNodeId)
        .source(localNodeId)
        .reportTo(remoteNodeId)
        .crcType(CRCType.CRC32)
        .procV7Flags(BundleV7Flags.StatusRequestForward)
        .makeBundle()
        .addBlock(payloadBlock(ByteArray(10000)))
        .addEd25519Signature(keyPair.private as Ed25519PrivateKeyParameters, listOf(0, 1))

    val admRecord = AdministrativeRecord(
        recordTypeCode = RecordTypeCode.StatusRecordType.code,
        data = StatusReport()
            .assert(StatusAssertion.ReceivedBundle, true, 1613607271)
            .assert(StatusAssertion.ForwardedBundle, true, 1613897271)
            .reason(StatusReportReason.NoInformation)
            .source(URI.create("dtn://test-sdk/")))

    val bundleAdm = PrimaryBlock()
        .procV7Flags(BundleV7Flags.AdministrativeRecordPayload)
        .destination(remoteNodeId)
        .source(localNodeId)
        .reportTo(remoteNodeId)
        .makeBundle()
        .addBlock(payloadBlock(admRecord.cborMarshalData()).crcType(CRCType.CRC32))
}