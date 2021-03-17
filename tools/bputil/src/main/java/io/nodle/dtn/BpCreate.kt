package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.bpsec.addEd25519Signature
import io.nodle.dtn.bpv7.extensions.ageBlock
import io.nodle.dtn.crypto.toEd25519PrivateKey
import io.nodle.dtn.utils.hexToBa
import picocli.CommandLine
import java.net.URI
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.ArrayList

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
class BpCreate : Callable<Void> {
    @CommandLine.Option(names = ["-d", "--destination"], description = ["destination eid "])
    private var destination = "dtn://destination/"

    @CommandLine.Option(names = ["-s", "--source"], description = ["destination eid "])
    private var source = "dtn://source/"

    @CommandLine.Option(names = ["-r", "--report"], description = ["report-to eid"])
    private var report = "dtn://report/"

    @CommandLine.Option(names = ["-f", "--flags"], description = ["flags"])
    private var flags: Long = 0

    @CommandLine.Option(names = ["-l", "--lifetime"], description = ["lifetime of the bundle"])
    private var lifetime: Long = 0

    @CommandLine.Option(names = ["--age"], description = ["add an age block (set timestamp to 0)"])
    private var age: Long = 0

    @CommandLine.Option(names = ["--sign"], arity = "0..*", description = ["sign blocks (require --key to be set)"])
    private var targets: List<Int> = ArrayList()

    @CommandLine.Option(names = ["--key"], description = ["ed25519 key (in hex prefixed with 0x)"])
    private var hexKey: String = ""

    @CommandLine.Option(names = ["--crc-16"], description = ["use crc-16"])
    private var crc16 = false

    @CommandLine.Option(names = ["--crc-32"], description = ["use crc-32"])
    private var crc32 = false

    @CommandLine.Option(names = ["--armor"], description = ["Base64 representation"])
    private var armor = false

    override fun call(): Void? {
        val crc = if (crc16) {
            CRCType.CRC16
        } else {
            CRCType.CRC32
        }

        val bundle = PrimaryBlock()
                .destination(URI.create(destination))
                .source(URI.create(source))
                .reportTo(URI.create(report))
                .procV7Flags(flags)
                .crcType(crc)
                .lifetime(lifetime)
                .makeBundle()
                .addBlock(payloadBlock(System.`in`.readBytes()).crcType(crc))

        if (age > 0L) {
            bundle.primaryBlock.creationTimestamp(0)
            bundle.addBlock(ageBlock(age))
        }

        if (targets.isNotEmpty()) {
            if (hexKey == "") {
                println("the --key paraneter must be supplied with --sign")
            }

            try {
                val key = hexKey.hexToBa().toEd25519PrivateKey()
                bundle.addEd25519Signature(key, targets)
            } catch (e: Exception) {
                println("supplied key is not an ed25519 private key")
            }
        }

        val buf = bundle.cborMarshal()
        if(armor) {
            val bufB64 = Base64.getEncoder().encodeToString(buf)
            System.`out`.write(bufB64.toByteArray())
        } else {
            bundle.cborMarshal(System.`out`)
        }
        System.`out`.flush()

        return null
    }
}