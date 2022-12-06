package io.nodle.dtn.bpv7.bpsec

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.eid.nullDtnEid
import io.nodle.dtn.crypto.Ed25519SignerCheckStream
import io.nodle.dtn.crypto.Ed25519SignerStream
import io.nodle.dtn.utils.hexToBa
import io.nodle.dtn.utils.isFlagSet
import io.nodle.dtn.utils.setFlag
import io.nodle.dtn.utils.toHex
import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import java.io.DataOutputStream
import java.net.URI

/**
 * This security context creates an Ed25519 signature for each target block.
 * The signed data is performed over the entire target block represented
 * as a CBOR byte string. Each signature takes two parameters, the public key
 * and a timestamp.
 *
 * @author Lucien Loiseau on 14/02/21.
 */

data class Ed25519SecurityParameter(
    val ed25519PublicKey: Ed25519PublicKeyParameters,
    val timestamp: Long
) : AbstractSecurityParameter() {
    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other !is Ed25519SecurityParameter) {
            return false
        }
        return ed25519PublicKey.encoded.contentEquals(other.ed25519PublicKey.encoded)
                && timestamp == other.timestamp
    }

    override fun hashCode(): Int {
        return (ed25519PublicKey.encoded.toHex()+timestamp).hashCode()
    }
}

data class Ed25519SecurityResult(
    val signature: String
) : AbstractSecurityResult()

fun Bundle.isSignedWithEd25519(targetBlock: Number): Boolean {
    return canonicalBlocks
        .filter { it.blockType == BlockType.BlockIntegrityBlock.code }
        .any { (it.data as AbstractSecurityBlockData).securityTargets.contains(targetBlock) }
}

fun Bundle.getEd25519SignatureKey(targetBlock: Number) =
    canonicalBlocks
        .filter { it.blockType == BlockType.BlockIntegrityBlock.code }
        .map { it.data as AbstractSecurityBlockData }
        .filter { it.securityTargets.contains(targetBlock) }
        .filter { it.securityContext == SecurityContext.Ed25519BlockSignature.id }
        .filter { it.securityBlockV7Flags.isFlagSet(SecurityBlockV7Flags.ContextParameterPresent.offset) }
        .map { it.securityContextParameters as Ed25519SecurityParameter }
        .map { it.ed25519PublicKey }
        .firstOrNull()

@Throws(NoSuchElementException::class)
fun Bundle.signWithEd25519(
    key: Ed25519PrivateKeyParameters,
    time: Long,
    targets: List<Int>,
    author: URI = nullDtnEid()
): CanonicalBlock {
    val asb = AbstractSecurityBlockData(
        securityContext = SecurityContext.Ed25519BlockSignature.id,
        securitySource = author,
        securityBlockV7Flags = 0.toLong()
            .setFlag(SecurityBlockV7Flags.ContextParameterPresent.offset),
        securityContextParameters = Ed25519SecurityParameter(key.generatePublicKey(), time)
    )

    for (target in targets) {
        val signer = Ed25519SignerStream(key)
        val out = DataOutputStream(signer)
        if (target == 0) {
            primaryBlock.cborMarshal(out)
        } else {
            getBlockNumber(target)
                ?.cborMarshal(signer)
                ?: throw NoSuchElementException("bundle has no block number $target")
        }
        out.writeLong(time)
        out.flush()
        asb.securityTargets.add(target)
        asb.securityResults.add(Ed25519SecurityResult(signer.done().toHex()))
    }

    return CanonicalBlock(
        blockType = BlockType.BlockIntegrityBlock.code,
        data = asb
    )
}

// todo: unsafe cast
fun Bundle.addEd25519Signature(
    key: AsymmetricCipherKeyPair,
    targets: List<Int>,
    author: URI = nullDtnEid()
) =
    addBlock(
        this.signWithEd25519(
            key.private as Ed25519PrivateKeyParameters,
            System.currentTimeMillis(),
            targets,
            author
        )
    )

fun Bundle.addEd25519Signature(
    key: Ed25519PrivateKeyParameters,
    targets: List<Int>,
    author: URI = nullDtnEid()
) =
    addBlock(this.signWithEd25519(key, System.currentTimeMillis(), targets, author))

fun Bundle.addEd25519Signature(
    key: AsymmetricCipherKeyPair,
    time: Long,
    targets: List<Int>,
    author: URI = nullDtnEid()
) =
    addBlock(
        this.signWithEd25519(
            key.private as Ed25519PrivateKeyParameters,
            time,
            targets,
            author
        )
    )

fun Bundle.addEd25519Signature(
    key: Ed25519PrivateKeyParameters,
    time: Long,
    targets: List<Int>,
    author: URI = nullDtnEid()
) =
    addBlock(this.signWithEd25519(key, time, targets, author))

@Throws(ValidationException::class)
fun AbstractSecurityBlockData.checkValidEd25519Signatures(bundle: Bundle) {
    val param = securityContextParameters as Ed25519SecurityParameter

    for ((i, target) in securityTargets.withIndex()) {
        val result = securityResults[i] as Ed25519SecurityResult
        val signer = Ed25519SignerCheckStream(param.ed25519PublicKey)
        val out = DataOutputStream(signer)
        if (target == 0) {
            bundle.primaryBlock.cborMarshal(out)
        } else {
            bundle.getBlockNumber(target)
                ?.cborMarshal(out)
                ?: throw ValidationException("asb-ed25519: bundle has no block number $target")
        }
        out.writeLong(param.timestamp)
        out.flush()
        if (!signer.done(result.signature.hexToBa())) {
            throw ValidationException("asb-ed25519: signature verification failed on block target $target")
        }
    }
}

