package io.nodle.dtn.bpv7.bpsec

import io.nodle.dtn.bpv7.ExtensionBlockData
import io.nodle.dtn.bpv7.eid.nullDtnEid
import io.nodle.dtn.utils.isFlagSet
import io.nodle.dtn.utils.toHex
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
    CONTEXT_PARAMETERS_PRESENT(0)
}

data class AbstractSecurityBlockData(
    var securityTargets: MutableList<Int> = ArrayList(),
    var securityContext: Int = 0,
    var securityBlockV7Flags: Long = 0,
    var securitySource: URI = nullDtnEid(),
    var securityContextParameters: MutableList<SecurityContextParameter> = ArrayList(),
    var securityResults: MutableList<MutableList<SecurityResult>> = ArrayList()
) : ExtensionBlockData

fun AbstractSecurityBlockData.hasSecurityParam() : Boolean {
    return securityBlockV7Flags.isFlagSet(SecurityBlockV7Flags.CONTEXT_PARAMETERS_PRESENT.offset)
}

data class SecurityResult(var id: Int, var result : ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SecurityResult) return false

        if (id != other.id) return false
        if (!result.contentEquals(other.result)) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = id
        result1 = 31 * result1 + result.contentHashCode()
        return result1
    }

    override fun toString() = "SecurityResult(id=$id, result=0x${result.toHex()})"
}

data class SecurityContextParameter(var id: Int, var parameter : ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SecurityContextParameter) return false

        if (id != other.id) return false
        if (!parameter.contentEquals(other.parameter)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + parameter.contentHashCode()
        return result
    }

    override fun toString() = "SecurityContextParameter(id=$id, result=0x${parameter.toHex()})"
}