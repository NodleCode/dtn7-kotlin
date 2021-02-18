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
    private var receive = 0L

    @CommandLine.Option(names = ["-f", "--forward"], description = ["forward assertion timestamp"])
    private var forward = 0L

    @CommandLine.Option(names = ["-d", "--delivered"], description = ["delivered assertion timestamp"])
    private var delivered = 0L

    @CommandLine.Option(names = ["-z", "--deleted"], description = ["deleted assertion timestamp"])
    private var deleted = 0L

    @CommandLine.Option(names = ["-o", "--offset"], description = ["fragment offset"])
    private var offset = 0L

    @CommandLine.Option(names = ["-l", "--length"], description = ["app data length"])
    private var length = 0L

    override fun call(): Void? {
        val status = AdministrativeRecord(
                recordTypeCode = RecordTypeCode.StatusRecordType.code,
                data =StatusReport()
                .assert(StatusAssertion.ReceivedBundle, receive>0, receive)
                .assert(StatusAssertion.ForwardedBundle, forward>0, forward)
                .assert(StatusAssertion.DeliveredBundle, delivered>0, delivered)
                .assert(StatusAssertion.DeletedBundle, deleted>0, deleted)
                .source(URI.create(source))
                .offset(offset)
                .appDataLength(length))

        status.cborMarshalData(System.`out`)
        return null
    }
}