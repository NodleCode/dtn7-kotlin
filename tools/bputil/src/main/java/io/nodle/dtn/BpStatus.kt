package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.administrative.*
import io.nodle.dtn.bpv7.bpsec.addEd25519Signature
import io.nodle.dtn.bpv7.extensions.ageBlock
import io.nodle.dtn.crypto.toEd25519PrivateKey
import io.nodle.dtn.utils.hexToBa
import picocli.CommandLine
import java.net.URI
import java.util.concurrent.Callable

/**
 * @author Lucien Loiseau on 18/02/21.
 */
@CommandLine.Command(
    name = "status",
    mixinStandardHelpOptions = true,
    description = ["", "create a status report payload"],
    optionListHeading = "@|bold %nOptions|@:%n",
    footer = [""]
)
class BpStatus : Callable<Void> {

    @CommandLine.Option(names = ["-s", "--source"], description = ["bundle source status"])
    private var source = "dtn://source/"

    @CommandLine.Option(names = ["-r", "--receive"], description = ["receive assertion timestamp"])
    private var receive: Boolean = false

    @CommandLine.Option(names = ["-f", "--forward"], description = ["forward assertion timestamp"])
    private var forward: Boolean = false

    @CommandLine.Option(names = ["-d", "--delivered"], description = ["delivered assertion timestamp"])
    private var delivered: Boolean = false

    @CommandLine.Option(names = ["-z", "--deleted"], description = ["deleted assertion timestamp"])
    private var deleted: Boolean = false

    @CommandLine.Option(names = ["-o", "--offset"], description = ["fragment offset"])
    private var offset = 0L

    @CommandLine.Option(names = ["-l", "--length"], description = ["app data length"])
    private var length = 0L

    override fun call(): Void? {
        val status = AdministrativeRecord(
            recordTypeCode = RecordTypeCode.StatusRecordType.code,
            data = StatusReport(
                sourceNodeId = URI.create(source),
                creationTimestamp = dtnTimeNow(),
                sequenceNumber = 0,
                fragmentOffset = offset,
                appDataLength = length
            )
                .assert(StatusAssertion.ReceivedBundle, receive, dtnTimeNow())
                .assert(StatusAssertion.ForwardedBundle, forward, dtnTimeNow())
                .assert(StatusAssertion.DeliveredBundle, delivered, dtnTimeNow())
                .assert(StatusAssertion.DeletedBundle, deleted, dtnTimeNow())
        )

        status.cborMarshalData(System.`out`)
        System.`out`.flush()
        return null
    }
}