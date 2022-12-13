package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import java.net.URI

fun makeBundle(data: String, dest: URI, source: URI): Bundle =
    PrimaryBlock()
        .destination(dest)
        .source(source)
        .reportTo(source)
        .setProcV7Flags(BundleV7Flags.StatusRequestDelivery)
        .setProcV7Flags(BundleV7Flags.StatusRequestForward)
        .crcType(CRCType.CRC32)
        .lifetime(120000)
        .makeBundle()
        .addBlock(payloadBlock(data.toByteArray()).crcType(CRCType.CRC32))