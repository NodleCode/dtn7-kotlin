package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.extensions.BundleAgeBlockData

/**
 * @author Lucien Loiseau on 17/02/21.
 */

enum class BundleConstraint(val code: String) {
    DispatchPending("dispatch_pending"),
    ForwardPending("forward_pending"),
    Contraindicated("contraindicated"),
    LocalEndpoint("local_endpoint"),
}

enum class BundleTag(val code: String) {
    OriginCLA("origin_cla"),
    OriginLocal("origin_local"),
    OriginStorage("origin_storage"),
    Delivered("delivered"),
    Forwarded("forwarded")
}

data class BundleDescriptor(
        var bundle: Bundle,
        var created: Long = System.currentTimeMillis(),
        var constraints: MutableList<String> = mutableListOf(),
        var tags: MutableList<String> = mutableListOf()
)

fun BundleDescriptor.expireAt(): Long {
    return if (bundle.primaryBlock.creationTimestamp == 0L) {
        bundle.getBlockType(BlockType.BundleAgeBlock.code)?.data?.run {
            bundle.primaryBlock.lifetime - (this as BundleAgeBlockData).age + created
        } ?: 0L
    } else {
        bundle.primaryBlock.creationTimestamp + bundle.primaryBlock.lifetime
    }
}

fun BundleDescriptor.updateAgeBlock() {
    bundle.getBlockType(BlockType.BundleAgeBlock.code)?.apply {
        val localTimeSpent = System.currentTimeMillis() - created
        (this.data as BundleAgeBlockData).age += localTimeSpent
    }
}

fun BundleDescriptor.ID() = bundle.ID()
fun BundleDescriptor.fragmentedID() = bundle.fragmentedID()