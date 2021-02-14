package io.nodle.dtn.bpv7.bpsec

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.eid.nullDtnEid
import io.nodle.dtn.crypto.Ed25519SignerCheckStream
import io.nodle.dtn.crypto.Ed25519SignerStream
import io.nodle.dtn.crypto.toEd25519PublicKey
import io.nodle.dtn.utils.setFlag
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import java.net.URI

/**
 * @author Lucien Loiseau on 14/02/21.
 */

/**
 * This security context creates an Ed25519 signature for each target block.
 * The signed data is performed over the entire target block represented
 * as a CBOR byte string.
 */
enum class Ed25519SignatureParameter(val id: Int) {
    Ed25519PublicKey(0)
}

enum class Ed25519SignatureResult(val id: Int) {
    Signature(0)
}

fun Bundle.hasBIB(targetBlock : Number) : Boolean {
    return canonicalBlocks
        .filter { it.blockType == BlockType.BlockIntegrityBlock.code }
        .any { (it.data as AbstractSecurityBlockData).securityTargets.contains(targetBlock) }
}

@Throws(NoSuchElementException::class)
fun Bundle.signWithEd25519(key: Ed25519PrivateKeyParameters, targets: List<Int>, author: URI = nullDtnEid()) : CanonicalBlock {
    val asb = AbstractSecurityBlockData(
        securityContext = SecurityContext.Ed25519BlockSignature.id,
        securitySource = author,
        securityBlockV7Flags = 0.toLong().setFlag(SecurityBlockV7Flags.CONTEXT_PARAMETERS_PRESENT.offset),
        securityContextParameters = arrayListOf(
            SecurityContextParameter(Ed25519SignatureParameter.Ed25519PublicKey.id, key.generatePublicKey().encoded)))

    for (target in targets) {
        val signer = Ed25519SignerStream(key)
        if(target == 0) {
            primaryBlock.cborMarshal(signer)
        } else {
            getBlockNumber(target).cborMarshal(signer)
        }
        asb.securityTargets.add(target)
        asb.securityResults.add(arrayListOf(SecurityResult(Ed25519SignatureResult.Signature.id, signer.done())))
    }

    return CanonicalBlock(
        blockType = BlockType.BlockIntegrityBlock.code,
        data = asb)
}

fun Bundle.addEd25519Signature(key: Ed25519PrivateKeyParameters, targets: List<Int>, author: URI = nullDtnEid()) =
    addBlock(this.signWithEd25519(key,targets, author))

@Throws(ValidationException::class)
fun AbstractSecurityBlockData.checkValidEd25519Signatures(bundle: Bundle) {
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
            bundle.getBlockNumber(target).cborMarshal(signer)
        }

        if (!signer.done(signature)) {
            throw ValidationException("asb-ed25519: signature verification failed on block target $target")
        }
    }
}