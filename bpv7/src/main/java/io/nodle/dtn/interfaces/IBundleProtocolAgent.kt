package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.Bundle

interface IBundleProtocolAgent {

    suspend fun transmitADU(bundle: Bundle)

    suspend fun receivePDU(bundle: Bundle)

}