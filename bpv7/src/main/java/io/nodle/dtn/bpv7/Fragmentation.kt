package io.nodle.dtn.bpv7

import io.nodle.dtn.utils.setFlag
import java.lang.Long.min


class FragmentationError(val msg: String) : Exception(msg)
class ReassemblyError(val msg: String) : Exception(msg)

typealias ReassemblingBundle = Bundle

@Throws(FragmentationError::class)
fun Bundle.fragment(aduSize: Int): List<Bundle> {
    if (isFlagSet(BundleV7Flags.MustNotFragment)) {
        throw FragmentationError("bundle control flag forbids fragmentation")
    }

    val fragmentDataLen = getPayloadSize()
    if (aduSize >= fragmentDataLen) {
        return listOf(this)
    }

    return (0..fragmentDataLen.floorDiv(aduSize))
        .map { it * aduSize }
        .filter { it < fragmentDataLen }
        .map { offset ->
            PrimaryBlock(
                procV7Flags = primaryBlock.procV7Flags.setFlag(BundleV7Flags.IsFragment.offset),
                crcType = primaryBlock.crcType,
                destination = primaryBlock.destination,
                source = primaryBlock.source,
                reportTo = primaryBlock.reportTo,
                creationTimestamp = primaryBlock.creationTimestamp,
                sequenceNumber = primaryBlock.sequenceNumber,
                lifetime = primaryBlock.lifetime,
                fragmentOffset = primaryBlock.fragmentOffset + offset,
                appDataLength = if (isFlagSet(BundleV7Flags.IsFragment)) {
                    primaryBlock.appDataLength
                } else {
                    fragmentDataLen
                }
            ).makeBundle().also { fragment ->
                canonicalBlocks.forEach {
                    if (it.blockType == BlockType.PayloadBlock.code) {
                        fragment.addBlock(
                            payloadBlock(
                                (it.data as PayloadBlockData).buffer.copyOfRange(
                                    offset.toInt(),
                                    min(offset + aduSize, fragmentDataLen).toInt()
                                )
                            )
                        )
                    }
                    if (it.isFlagSet(BlockV7Flags.ReplicateInEveryFragment) || (fragment.primaryBlock.fragmentOffset == 0L)) {
                        fragment.addBlock(cborUnmarshalCanonicalBlock(it.cborMarshal()), false)
                    }
                }

                try {
                    fragment.checkValid()
                } catch (v: ValidationException) {
                    throw FragmentationError("fragment is not valid: ${v.msg}")
                }
            }
        }
}

fun List<Bundle>.reassemble(): Bundle =
    if ((size == 1) && !get(0).isFlagSet(BundleV7Flags.IsFragment)) {
        get(0)
    } else {
        reassembleFragments()
    }

@Throws(ReassemblyError::class)
fun List<Bundle>.reassembleFragments(): Bundle =
    sortedBy { it.primaryBlock.fragmentOffset }
        .fold(null as ReassemblingBundle?) { reassemblingBundle, bundle ->
            reassemblingBundle
                ?.apply {
                    reassemblingBundle.mergeNextFragment(bundle)
                }
                ?: bundle.prepareFirstFragmentForReassembly()
        }
        ?.finishReassembling()
        ?: throw ReassemblyError("fragmentation failed")


@Throws(ReassemblyError::class)
fun Bundle.prepareFirstFragmentForReassembly(): ReassemblingBundle {
    if (!isFlagSet(BundleV7Flags.IsFragment)) {
        throw ReassemblyError("bundle is not a fragment")
    }
    if (primaryBlock.fragmentOffset != 0L) {
        throw ReassemblyError("first fragment should start with offset 0")
    }

    // use the fragment offset as internal counter during reassembly procedure for expected offset
    primaryBlock.fragmentOffset = getPayloadSize()
    return this
}

@Throws(ReassemblyError::class)
fun ReassemblingBundle.mergeNextFragment(fragment: Bundle): ReassemblingBundle {
    if (!isFlagSet(BundleV7Flags.IsFragment)) {
        throw ReassemblyError("bundle is not a fragment")
    }
    if (fragment.primaryBlock.fragmentOffset != primaryBlock.fragmentOffset) {
        throw ReassemblyError("fragment offset is ${fragment.primaryBlock.fragmentOffset} but should be ${primaryBlock.fragmentOffset}")
    }

    getPayloadBlockData().buffer += fragment.getPayloadBlockData().buffer
    primaryBlock.fragmentOffset += fragment.getPayloadSize()
    return this
}

@Throws(ReassemblyError::class)
fun ReassemblingBundle.finishReassembling(): Bundle {
    if (getPayloadSize() != primaryBlock.appDataLength) {
        throw ReassemblyError("reassembled payload is expected to be ${primaryBlock.appDataLength} bytes but is actually ${getPayloadSize()} bytes ")
    }

    primaryBlock.unsetProcV7Flags(BundleV7Flags.IsFragment)
    primaryBlock.fragmentOffset = 0
    primaryBlock.appDataLength = 0

    try {
        checkValid()
    } catch (v: ValidationException) {
        throw ReassemblyError("reassembled bundle is not valid: ${v.msg}")
    }

    return this
}

