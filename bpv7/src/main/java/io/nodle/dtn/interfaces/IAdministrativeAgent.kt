package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.administrative.StatusAssertion
import io.nodle.dtn.bpv7.administrative.StatusReportReason
import java.net.URI

/**
 * @author Lucien Loiseau on 18/02/21.
 */
interface IAdministrativeAgent {

    val administrativeEndpoint : URI

    suspend fun sendStatusReport(
        bpa: IBundleProtocolAgent,
        desc: BundleDescriptor,
        assertion: StatusAssertion,
        reason: StatusReportReason)

}
