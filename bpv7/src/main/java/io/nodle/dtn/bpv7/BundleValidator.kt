package io.nodle.dtn.bpv7

import io.nodle.dtn.bpv7.security.AbstractSecurityBlockData
import io.nodle.dtn.bpv7.security.Ed25519SignatureParameter
import io.nodle.dtn.bpv7.security.Ed25519SignatureResult
import io.nodle.dtn.bpv7.security.SecurityContext
import io.nodle.dtn.crypto.Ed25519SignerCheckStream
import io.nodle.dtn.crypto.toEd25519PublicKey
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters

/**
 * @author Lucien Loiseau on 14/02/21.
 */

class ValidationException(msg: String) : Exception(msg)

@Throws(ValidationException::class)
fun Bundle.validate() {
    for (block in canonicalBlocks) {
        block.validate(this)
    }

    if (canonicalBlocks.map { it.blockNumber }.distinct().size < canonicalBlocks.size) {
        throw ValidationException("bundle: multiple blocks with same blockNumber")
    }

    if (!canonicalBlocks.map { it.blockNumber }.contains(BlockType.PayloadBlock.code)) {
        throw ValidationException("bundle: the bundle must have a payload block")
    }

    if (canonicalBlocks.last().blockType != BlockType.PayloadBlock.code) {
        throw ValidationException("bundle: the last block must be a payload block")
    }
}

@Throws(ValidationException::class)
fun CanonicalBlock.validate(bundle: Bundle) {
    if (blockNumber == 0) {
        throw ValidationException("canonical: block number 0 forbidden for non-primary block")
    }
    when (blockType) {
        BlockType.BlockIntegrityBlock.code -> validateBib(bundle)
    }
}

@Throws(ValidationException::class)
fun CanonicalBlock.validateBib(bundle: Bundle) {
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
        if (target != 0 && !bundle.hasBlock(target)) {
            throw ValidationException("asb: target block doesn't exist")
        }
    }

    when (asb.securityContext) {
        SecurityContext.Ed25519BlockSignature.id -> asb.validateEd25519Signatures(bundle)
    }
}


@Throws(ValidationException::class)
fun AbstractSecurityBlockData.validateEd25519Signatures(bundle: Bundle) {
    if (securityContextParameters.size != 1) {
        throw ValidationException("asb-ed25519: missing parameter")
    }

    if (securityContextParameters[0].id != Ed25519SignatureParameter.Ed25519PublicKey.id) {
        throw ValidationException("asb-ed25519: missing ed25519 key parameter")
    }


    val pubkey : Ed25519PublicKeyParameters
    try {
        pubkey = securityContextParameters[0].parameter.toEd25519PublicKey()
    } catch (e: Exception) {
        throw ValidationException("asb-ed25519: not an ed25519 key")
    }

    for ((i, target) in securityTargets.withIndex()) {
        val result = securityResults[i]
        if (result.size != 1) {
            throw ValidationException("asb-ed25519: there should be only one result per target")
        }
        if (result[0].id != Ed25519SignatureResult.Signature.id) {
            throw ValidationException("asb-ed25519: result expected an ed25519 Signature but got ${result[0].id}")
        }
        val signature = result[0].result

        val signer = Ed25519SignerCheckStream(pubkey)
        if (target == 0) {
            bundle.primaryBlock.cborMarshal(signer)
        } else {
            bundle.getBlock(target).cborMarshal(signer)
        }

        if (!signer.done(signature)) {
            throw ValidationException("asb-ed25519: signature verification failed on block target $target")
        }
    }
}