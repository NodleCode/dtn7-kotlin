package io.nodle.dtn.bpv7.security

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.crypto.Ed25519SignerStream
import io.nodle.dtn.utils.setFlag
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters

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

@Throws(NoSuchElementException::class)
fun Bundle.signWithEd25519(key: Ed25519PrivateKeyParameters, targets: List<Int>): CanonicalBlock {
    val asb = AbstractSecurityBlockData(
        securityContext = SecurityContext.Ed25519BlockSignature.id,
        securityBlockV7Flags = 0.toLong().setFlag(SecurityBlockV7Flags.CONTEXT_PARAMETERS_PRESENT.offset),
        securityContextParameters = arrayListOf(
            SecurityContextParameter(Ed25519SignatureParameter.Ed25519PublicKey.id, key.generatePublicKey().encoded)))

    for (target in targets) {
        val signer = Ed25519SignerStream(key)
        if(target == 0) {
            primaryBlock.cborMarshal(signer)
        } else {
            getBlock(target).cborMarshal(signer)
        }
        asb.securityTargets.add(target)
        asb.securityResults.add(arrayListOf(SecurityResult(Ed25519SignatureResult.Signature.id, signer.done())))
    }

    return CanonicalBlock(
        blockType = BlockType.BlockIntegrityBlock.code,
        data = asb)
}

fun Bundle.addEd25519Signature(key: Ed25519PrivateKeyParameters, targets: List<Int>) =
    addBlock(this.signWithEd25519(key,targets))