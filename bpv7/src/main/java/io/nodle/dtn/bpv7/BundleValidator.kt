package io.nodle.dtn.bpv7

import io.nodle.dtn.bpv7.bpsec.*
import io.nodle.dtn.bpv7.eid.checkValidEid
import io.nodle.dtn.bpv7.eid.isNullEid
import io.nodle.dtn.bpv7.extensions.BundleAgeBlockData

/**
 * @author Lucien Loiseau on 14/02/21.
 */

class ValidationException(msg: String) : Exception(msg)

fun Bundle.isValid(): Boolean {
    return try {
        checkValid()
        true
    } catch(e:Exception) {
        false
    }
}

@Throws(ValidationException::class)
fun Bundle.checkValid() {
    primaryBlock.checkValidPrimaryBlock(this)

    for (block in canonicalBlocks) {
        block.checkValidCanonicalBlock(this)
    }

    if (!primaryBlock.hasCRC() && !isSignedWithEd25519(0)) {
        throw ValidationException("bundle:${ID()} - primary block must have a CRC or being the target of a BIB")
    }

    if (!canonicalBlocks.map { it.blockNumber }.contains(BlockType.PayloadBlock.code)) {
        throw ValidationException("bundle:${ID()} - the bundle must have a payload block")
    }

    if (canonicalBlocks.last().blockType != BlockType.PayloadBlock.code) {
        throw ValidationException("bundle:${ID()} - the last block must be a payload block")
    }

    if (canonicalBlocks.map { it.blockNumber }.distinct().size < canonicalBlocks.size) {
        throw ValidationException("bundle:${ID()} - multiple blocks with same blockNumber")
    }
}


@Throws(ValidationException::class)
fun PrimaryBlock.checkValidPrimaryBlock(bundle : Bundle) {
    if (isFlagSet(BundleV7Flags.IsFragment) && isFlagSet(BundleV7Flags.MustNotFragment)) {
        throw ValidationException("bundle:${ID()} - primary: bundle is a fragment but must_no_fragment flag is set")
    }

    destination.checkValidEid()
    source.checkValidEid()
    reportTo.checkValidEid()

    if(source.isNullEid() && !(isFlagSet(BundleV7Flags.MustNotFragment)
        && !isFlagSet(BundleV7Flags.StatusRequestReception)
        && !isFlagSet(BundleV7Flags.StatusRequestForward)
        && !isFlagSet(BundleV7Flags.StatusRequestDelivery)
        && !isFlagSet(BundleV7Flags.StatusRequestDeletion))) {
        throw ValidationException("bundle:${ID()} - primary: anonymous bundle shall not be fragmented and status report not delivered")
    }

    checkLifetimeExceeded(bundle)
}

@Throws(ValidationException::class)
fun PrimaryBlock.checkLifetimeExceeded(bundle: Bundle) {
    if ((creationTimestamp == 0L) && !bundle.hasBlockType(BlockType.BundleAgeBlock)) {
        throw ValidationException("bundle:${ID()} - primary: creatimeTimestamp is zero but bundle has no age block")
    }

    val now = System.currentTimeMillis()
    if (creationTimestamp == 0L) {
        if ((bundle.getBlockType(BlockType.BundleAgeBlock.code)?.data as BundleAgeBlockData).age > lifetime) {
            throw ValidationException("bundle:${ID()} - primary: bundle has expired")
        }
    } else if (now > creationTimestamp + lifetime){
        throw ValidationException("bundle:${ID()} - primary: bundle has expired")
    }
}

@Throws(ValidationException::class)
fun CanonicalBlock.checkValidCanonicalBlock(bundle: Bundle) {
    if (blockNumber == 0) {
        throw ValidationException("bundle:${bundle.ID()} - canonical: block number 0 forbidden for non-primary block")
    }
    when (blockType) {
        BlockType.BlockIntegrityBlock.code -> checkValidBIB(bundle)
    }
}
