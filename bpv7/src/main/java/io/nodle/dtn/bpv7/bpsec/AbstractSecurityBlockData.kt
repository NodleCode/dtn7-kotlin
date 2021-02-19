package io.nodle.dtn.bpv7.bpsec

import io.nodle.dtn.bpv7.ExtensionBlockData
import io.nodle.dtn.bpv7.eid.nullDtnEid
import io.nodle.dtn.utils.isFlagSet
import java.net.URI

/**
 * @author Lucien Loiseau on 14/02/21.
 */

enum class SecurityContext(val id: Int) {
    Ed25519BlockSignature(1),
}

enum class SecurityBlockV7Flags(val offset: Int) {
    /* CanonicalBlock Processing Control Flags
        Bit 0  : (the least-significant bit, 0x01) Security Context Parameters Present Flag.
        Bit >0 : Reserved
    */
    ContextParameterPresent(0)
}

data class AbstractSecurityBlockData(
        var securityTargets: MutableList<Int> = ArrayList(),
        var securityContext: Int = 0,
        var securityBlockV7Flags: Long = 0,
        var securitySource: URI = nullDtnEid(),
        var securityContextParameters: AbstractSecurityParameter = AbstractSecurityParameter(),
        var securityResults: MutableList<AbstractSecurityResult> = mutableListOf()
) : ExtensionBlockData

fun AbstractSecurityBlockData.hasSecurityParam() : Boolean {
    return securityBlockV7Flags.isFlagSet(SecurityBlockV7Flags.ContextParameterPresent.offset)
}

open class AbstractSecurityParameter

open class AbstractSecurityResult