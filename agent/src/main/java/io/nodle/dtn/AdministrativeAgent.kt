package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.administrative.StatusAssertion
import io.nodle.dtn.bpv7.administrative.StatusReportReason
import io.nodle.dtn.bpv7.administrative.cborMarshalData
import io.nodle.dtn.bpv7.administrative.statusRecord
import io.nodle.dtn.interfaces.BundleDescriptor
import io.nodle.dtn.interfaces.IAdministrativeAgent
import io.nodle.dtn.interfaces.IAgent
import io.nodle.dtn.interfaces.ID
import io.nodle.dtn.utils.hour
import org.slf4j.LoggerFactory

/**
 * @author Lucien Loiseau on 18/02/21.
 */
class AdministrativeAgent : IAdministrativeAgent {

    private val log = LoggerFactory.getLogger("AdministrativeAgent")

    /* 6.2 */
    override suspend fun sendStatusReport(
            core: IAgent,
            desc: BundleDescriptor,
            assertion: StatusAssertion,
            reason: StatusReportReason) {

        // do not respond to other administrative record
        if (desc.bundle.isFlagSet(BundleV7Flags.AdministrativeRecordPayload)) {
            log.debug("bundle:${desc.ID()}, " +
                    "status=${assertion}, " +
                    "reason=${reason} - do not respond to adm record")
            return
        }

        /* step 1 */
        log.debug("bundle:${desc.ID()}, " +
                "status=${assertion}, " +
                "reason=${reason} - sending a status record")
        val report = statusRecord(desc.bundle, assertion, reason, System.currentTimeMillis())
                .cborMarshalData()

        val adm = PrimaryBlock()
                .procV7Flags(BundleV7Flags.AdministrativeRecordPayload)
                .source(core.nodeId())
                .destination(desc.bundle.primaryBlock.reportTo)
                .creationTimestamp(System.currentTimeMillis())
                .lifetime(1 * hour)
                .makeBundle()
                .addBlock(payloadBlock(report))

        /* step 2 */
        core.transmit(adm)
    }
}