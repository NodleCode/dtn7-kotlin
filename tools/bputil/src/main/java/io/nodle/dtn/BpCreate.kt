package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import picocli.CommandLine
import java.net.URI
import java.util.concurrent.Callable

/**
 * @author Lucien Loiseau on 13/02/21.
 */
@CommandLine.Command(
    name = "create",
    mixinStandardHelpOptions = true,
    description = ["", "create a bundle"],
    optionListHeading = "@|bold %nOptions|@:%n",
    footer = [""]
)
class BpCreate: Callable<Void> {
    @CommandLine.Option(names = ["-d", "--destination"], description = ["destination eid "])
    private var destination = "dtn://destination/"

    @CommandLine.Option(names = ["-s", "--source"], description = ["destination eid "])
    private var source = "dtn://source/"

    @CommandLine.Option(names = ["-r", "--report"], description = ["report-to eid"])
    private var report = "dtn://report/"

    @CommandLine.Option(names = ["-l", "--lifetime"], description = ["lifetime of the bundle"])
    private var lifetime : Long = 0

    @CommandLine.Option(names = ["--crc-16"], description = ["use crc-16"])
    private var crc16 = false

    @CommandLine.Option(names = ["--crc-32"], description = ["use crc-32"])
    private var crc32 = false

    override fun call(): Void? {
        val crc = if(crc16) { CRCType.CRC16}else{ CRCType.CRC32}

        newPrimaryBlock()
            .destination(URI.create(destination))
            .source(URI.create(source))
            .reportTo(URI.create(report))
            .crcType(crc)
            .lifetime(lifetime)
            .makeBundle()
            .addBlock(PayloadBlock(System.`in`.readBytes()).crcType(crc))
            .cborMarshal(System.`out`)

        return null
    }
}