package io.nodle.dtn.bpv7

import io.nodle.dtn.bpv7.bpsec.*
import io.nodle.dtn.bpv7.eid.checkValidEid
import io.nodle.dtn.bpv7.eid.isNullEid
import io.nodle.dtn.bpv7.extensions.BundleAgeBlockData
import io.nodle.dtn.utils.isFlagSet

/**
 * @author Lucien Loiseau on 14/02/21.
 */

class ValidationException(msg: String) : Exception(msg)

@Throws(ValidationException::class)
fun Bundle.checkValid() {
    primaryBlock.checkValidPrimaryBlock(this)

    for (block in canonicalBlocks) {
        block.checkValidCanonicalBlock(this)
    }

    if (!primaryBlock.hasCRC() && !hasBIB(0)) {
        throw ValidationException("bundle: primary block must have a CRC or being the target of a BIB")
    }

    if (!canonicalBlocks.map { it.blockNumber }.contains(BlockType.PayloadBlock.code)) {
        throw ValidationException("bundle: the bundle must have a payload block")
    }

    if (canonicalBlocks.last().blockType != BlockType.PayloadBlock.code) {
        throw ValidationException("bundle: the last block must be a payload block")
    }

    if (canonicalBlocks.map { it.blockNumber }.distinct().size < canonicalBlocks.size) {
        throw ValidationException("bundle: multiple blocks with same blockNumber")
    }
}


@Throws(ValidationException::class)
fun PrimaryBlock.checkValidPrimaryBlock(bundle : Bundle) {
    if (procV7Flags.isFlagSet(BundleV7Flags.IsFragment.offset) && procV7Flags.isFlagSet(BundleV7Flags.MustNotFragment.offset)) {
        throw ValidationException("primary: bundle is a fragment but must_no_fragment flag is set")
    }

    destination.checkValidEid()
    source.checkValidEid()
    reportTo.checkValidEid()

    if(source.isNullEid() && !(procV7Flags.isFlagSet(BundleV7Flags.MustNotFragment.offset)
        && !procV7Flags.isFlagSet(BundleV7Flags.StatusRequestReception.offset)
        && !procV7Flags.isFlagSet(BundleV7Flags.StatusRequestForward.offset)
        && !procV7Flags.isFlagSet(BundleV7Flags.StatusRequestDelivery.offset)
        && !procV7Flags.isFlagSet(BundleV7Flags.StatusRequestDeletion.offset))) {
        throw ValidationException("primary: anonymous bundle shall not be fragmented and status report not delivered")
    }

    checkLifetimeExceeded(bundle)
}

@Throws(ValidationException::class)
fun PrimaryBlock.checkLifetimeExceeded(bundle: Bundle) {
    if ((creationTimestamp == 0L) && !bundle.hasBlockType(BlockType.BundleAgeBlock.code)) {
        throw ValidationException("primary: creatimeTimestamp is zero but bundle has no age block")
    }

    val now = System.currentTimeMillis()
    if (creationTimestamp == 0L) {
        if ((bundle.getBlockType(BlockType.BundleAgeBlock.code).data as BundleAgeBlockData).age > lifetime) {
            throw ValidationException("primary: bundle has expired")
        }
    } else if (now > creationTimestamp + lifetime){
        throw ValidationException("primary: bundle has expired")
    }
}

@Throws(ValidationException::class)
fun CanonicalBlock.checkValidCanonicalBlock(bundle: Bundle) {
    if (blockNumber == 0) {
        throw ValidationException("canonical: block number 0 forbidden for non-primary block")
    }
    when (blockType) {
        BlockType.BlockIntegrityBlock.code -> checkValidBIB(bundle)
    }
}
