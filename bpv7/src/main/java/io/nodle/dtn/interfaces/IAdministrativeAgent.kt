package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.administrative.StatusAssertion
import io.nodle.dtn.bpv7.administrative.StatusReportReason
import io.nodle.dtn.interfaces.BundleDescriptor

/**
 * @author Lucien Loiseau on 18/02/21.
 */
interface IAdministrativeAgent {

    suspend fun sendStatusReport(
            core: IAgent,
            desc: BundleDescriptor,
            assertion: StatusAssertion,
            reason: StatusReportReason)

}
