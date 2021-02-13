package io.nodle.dtn

import io.nodle.dtn.bpv7.cborUnmarshalBundle
import io.nodle.dtn.bpv7.getPayload
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

    override fun call(): Void? {
        try {
            val bundle = cborUnmarshalBundle(System.`in`)
            if (payload) {
                print(String(bundle.getPayload().buffer))
            } else {
                print(bundle)
            }
        } catch (e: Exception) {
            println("invalid bundle: ${e.message}")
        }
        return null
    }
}