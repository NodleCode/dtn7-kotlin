package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.extensions.BundleAgeBlockData
import io.nodle.dtn.bpv7.extensions.getAgeBlockData

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
    Delivered("delivered"),
    Forwarded("forwarded")
}

data class BundleDescriptor(
    var bundle: Bundle,
    var created: Long = dtnTimeNow(),
    var constraints: MutableList<String> = mutableListOf(),
    var tags: MutableList<String> = mutableListOf()
)

fun BundleDescriptor.ID() = bundle.ID()

fun BundleDescriptor.fragmentedID() = bundle.fragmentedID()

fun BundleDescriptor.expireAt() = bundle.expireAt()