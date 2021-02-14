package io.nodle.dtn.bpv7.bpsec

import io.nodle.dtn.bpv7.Bundle
import io.nodle.dtn.bpv7.CanonicalBlock
import io.nodle.dtn.bpv7.ValidationException
import io.nodle.dtn.bpv7.hasBlockNumber

/**
 * @author Lucien Loiseau on 14/02/21.
 */
@Throws(ValidationException::class)
fun CanonicalBlock.checkValidBIB(bundle: Bundle) {
    if (data !is AbstractSecurityBlockData) {
        throw ValidationException("asb: security block expected ")
    }
    val asb = data as AbstractSecurityBlockData

    if (asb.securityTargets.size == 0) {
        throw ValidationException("asb: should have at least 1 target")
    }

    if (asb.securityTargets.contains(blockNumber)) {
        throw ValidationException("asb: security block cannot target itself")
    }

    if (asb.securityTargets.distinct().size < asb.securityTargets.size) {
        throw ValidationException("asb: duplicate entries")
    }

    if (asb.securityTargets.size != asb.securityResults.size) {
        throw ValidationException("asb: mismatch between number of target and number of results")
    }

    for (target in asb.securityTargets) {
        if (target != 0 && !bundle.hasBlockNumber(target)) {
            throw ValidationException("asb: target block doesn't exist")
        }
    }

    when (asb.securityContext) {
        SecurityContext.Ed25519BlockSignature.id -> asb.checkValidEd25519Signatures(bundle)
    }
}