package io.nodle.dtn

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
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

    @CommandLine.Option(names = ["--valid"], description = ["only checks if bundle is valid (ignore -p)"])
    private var validate = false

    override fun call(): Void? {
        try {
            val parser = CBORFactory().createParser(System.`in`)
            while (!parser.isClosed) {
                val bundle = parser.readBundle()
                if (!validate) {
                    if (payload && bundle.hasBlockType(BlockType.PayloadBlock)) {
                        System.`out`.write((bundle.getPayloadBlock()?.data as BlobBlockData).buffer)
                    } else {
                        println(bundle)
                    }
                } else {
                    bundle.checkValid()
                    println("bundle is valid!")
                }
            }
        } catch (e: CborParsingException) {
            if(e.message != "expected start array but got null") {
                println("\nerror format bundle: ${e.message}")
            }
        } catch (e: ValidationException) {
            println("\nerror bundle is not valid: ${e.message}")
        }

        return null
    }
}