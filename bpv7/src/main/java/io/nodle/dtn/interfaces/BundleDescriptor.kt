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
    Incoming("incoming"),
    Outgoing("outgoing")
}

data class BundleDescriptor(
        var bundle: Bundle,
        var created: Long = System.currentTimeMillis(),
        var constraints: MutableList<String> = mutableListOf(),
        var tags: MutableList<String> = mutableListOf()
) {
    fun addConstraint(c: BundleConstraint) = addConstraint(c.code)
    fun addConstraint(c: String) {
        constraints.add(c)
    }

    fun hasConstraint(c: BundleConstraint) = hasConstraint(c.code)
    fun hasConstraint(c: String): Boolean =
        constraints.contains(c)

    fun removeConstraint(c: BundleConstraint) = removeConstraint(c.code)
    fun removeConstraint(c: String) {
        constraints.remove(c)
    }

    fun purgeConstraints() {
        constraints = mutableListOf()
    }

    fun addTag(t: BundleTag) = addTag(t.code)
    fun addTag(t: String) {
        tags.add(t)
    }
}

fun BundleDescriptor.expireAt(): Long {
    val now = System.currentTimeMillis()
    return if (bundle.primaryBlock.creationTimestamp == 0L) {
        if (!bundle.hasBlockType(BlockType.BundleAgeBlock.code)) {
            0 // expire already
        } else {
            val localTimeSpent = now - created
            val bundleAge = (bundle.getBlockType(BlockType.BundleAgeBlock.code).data as BundleAgeBlockData).age + localTimeSpent
            now + bundle.primaryBlock.lifetime - bundleAge
        }
    } else {
        bundle.primaryBlock.creationTimestamp + bundle.primaryBlock.lifetime
    }
}

fun BundleDescriptor.ID() = bundle.ID()