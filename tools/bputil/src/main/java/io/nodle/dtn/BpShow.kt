package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import picocli.CommandLine
import java.util.concurrent.Callable

/**
 * @author Lucien Loiseau on 13/02/21.
 */
@CommandLine.Command(
    name = "show",
    mixinStandardHelpOptions = true,
    description = ["", "show a bundle"],
    optionListHeading = "@|bold %nOptions|@:%n",
    footer = [""]
)
class BpShow : Callable<Void> {
    @CommandLine.Option(
        names = ["-p", "--payload-only"],
        description = ["only dump payload (check mode only)"]
    )
    private var payload = false

    @CommandLine.Option(names = ["--valid"], description = ["use crc-32"])
    private var validate = false

    override fun call(): Void? {
        try {
            val bundle = cborUnmarshalBundle(System.`in`)
            if (payload) {
                print(String((bundle.getPayloadBlock().data as BlobBlockData).buffer))
            } else {
                print(bundle)
            }
            if(validate) {
                bundle.checkValid()
                println("\nbundle is valid!")
            }
        } catch (e: CborParsingException) {
            println("\nerror format bundle: ${e.message}")
        } catch(e : ValidationException) {
            println("\nerror bundle is not valid: ${e.message}")
        }

        return null
    }
}