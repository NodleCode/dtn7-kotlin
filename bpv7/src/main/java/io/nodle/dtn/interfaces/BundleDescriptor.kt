package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.extensions.BundleAgeBlockData
import io.nodle.dtn.bpv7.extensions.getAgeBlockData
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * @author Lucien Loiseau on 17/02/21.
 */

enum class BundleConstraint(val code: String) {
    DispatchPending("dispatch_pending"),
    ForwardPending("forward_pending"),
    ReassemblyPending("reassembly_pending"),
    Contraindicated("contraindicated"),
}

enum class BundleTag(val code: String) {
    OriginCLA("origin_cla"),
    OriginLocal("origin_local"),
    OriginStorage("origin_storage"),
    Delivered("delivered"),
    Forwarded("forwarded"),
    Deleted("deleted")
}

data class BundleDescriptor(
    var bundle: Bundle,
    var created: Long = dtnTimeNow(),
    var constraints: MutableList<String> = mutableListOf(),
    var tags: MutableList<String> = mutableListOf(),
)

data class PrimaryBlockDescriptor(
    var primaryBlock: PrimaryBlock,
    var created: Long = dtnTimeNow(),
    var constraints: MutableList<String> = mutableListOf(),
    var tags: MutableList<String> = mutableListOf(),
    val payloadSize: Long,
    val expireAt: Long
)

fun BundleDescriptor.ID() = bundle.ID()
fun PrimaryBlockDescriptor.ID() = primaryBlock.ID()

fun BundleDescriptor.fragmentedID() = bundle.fragmentedID()
fun PrimaryBlockDescriptor.fragmentedID() = primaryBlock.fragmentedID()

fun BundleDescriptor.expireAt() = bundle.expireAt()
fun PrimaryBlockDescriptor.expireAt() = expireAt