package io.nodle.dtn.bpv7

import io.nodle.dtn.bpv7.administrative.StatusReportReason
import io.nodle.dtn.bpv7.bpsec.*
import io.nodle.dtn.bpv7.eid.*
import io.nodle.dtn.bpv7.extensions.getAgeBlockData

/**
 * @author Lucien Loiseau on 14/02/21.
 */
class ValidationException(val msg: String, val status: StatusReportReason = StatusReportReason.NoInformation) : Exception(msg)

fun Bundle.isValid(): Boolean = try {
    checkValid()
    true
} catch (e: ValidationException) {
    false
}

@Throws(ValidationException::class)
fun Bundle.checkValid() {
    checkValidPrimaryBlock()

    for (block in canonicalBlocks) {
        checkValidCanonicalBlock(block)
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


    checkLifetimeExceeded()
}


@Throws(ValidationException::class)
fun Bundle.checkValidPrimaryBlock() {
    if (isFlagSet(BundleV7Flags.IsFragment) && isFlagSet(BundleV7Flags.MustNotFragment)) {
        throw ValidationException("bundle:${ID()} - primary: bundle is a fragment but must_no_fragment flag is set")
    }

    try {
        primaryBlock.source.checkValidEid()
        primaryBlock.reportTo.checkValidEid()
    } catch (e: InvalidEid) {
        throw ValidationException("bundle:${ID()} - primary: eid unintelligible")
    }

    try {
        primaryBlock.destination.checkValidEid()
    } catch (e: InvalidEid) {
        throw ValidationException(
            "bundle:${ID()} - primary: destination eid unintelligible",
            status = StatusReportReason.DestEndpointUnintelligible
        )
    }

    if (primaryBlock.source.isNullEid() && !(isFlagSet(BundleV7Flags.MustNotFragment)
                && !isFlagSet(BundleV7Flags.StatusRequestReception)
                && !isFlagSet(BundleV7Flags.StatusRequestForward)
                && !isFlagSet(BundleV7Flags.StatusRequestDelivery)
                && !isFlagSet(BundleV7Flags.StatusRequestDeletion))
    ) {
        throw ValidationException("bundle:${ID()} - primary: anonymous bundle shall not be fragmented and status report not delivered")
    }
}

@Throws(ValidationException::class)
fun Bundle.checkLifetimeExceeded() {
    if ((this.primaryBlock.creationTimestamp == 0L) && !this.hasBlockType(BlockType.BundleAgeBlock)) {
        throw ValidationException("bundle:${ID()} - primary: creatimeTimestamp is zero but bundle has no age block")
    }

    if (isExpired()) {
        throw ValidationException(
            "bundle:${ID()} - primary: bundle has expired (${(dtnTimeNow() - expireAt())/1000} seconds ago)",
            StatusReportReason.LifetimeExpired
        )
    }
}

fun Bundle.expireAt(): DtnTime =
    if (this.primaryBlock.creationTimestamp == 0L) {
        (getAgeBlockData()!!.age - this.primaryBlock.lifetime) + dtnTimeNow()
    } else {
        (this.primaryBlock.creationTimestamp + this.primaryBlock.lifetime)
    }

fun Bundle.isExpired(now : Long = dtnTimeNow()): Boolean =
    if (this.primaryBlock.creationTimestamp == 0L) {
        getAgeBlockData()!!.age > this.primaryBlock.lifetime
    } else {
        (now > (this.primaryBlock.creationTimestamp + this.primaryBlock.lifetime))
    }

@Throws(ValidationException::class)
fun Bundle.checkValidCanonicalBlock(block: CanonicalBlock) {
    if (block.blockNumber == 0) {
        throw ValidationException("bundle:${ID()} - canonical: block number 0 forbidden for non-primary block")
    }
    when (block.blockType) {
        BlockType.BlockIntegrityBlock.code -> block.checkValidBIB(this)
    }
}
