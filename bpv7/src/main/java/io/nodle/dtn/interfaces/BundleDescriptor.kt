package io.nodle.dtn

import io.nodle.dtn.bpv7.Bundle

/**
 * @author Lucien Loiseau on 17/02/21.
 */

enum class BundleConstraint(val code : String) {
    DispatchPending("dispatch_pending"),
    ForwardPending("forward_pending"),
    Contraindicated("contraindicated")
}

class BundleDescriptor(val bundle : Bundle) {
    val tags = HashMap<Int, String>()

    fun tag(bundleTag : BundleConstraint) = tag(bundleTag.code)

    fun tag(bundleTag : String) {
        tags[hashCode()] = bundleTag
    }
}
