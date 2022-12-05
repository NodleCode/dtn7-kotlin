@file:JvmName("Application")

package io.nodle.dtn

import picocli.CommandLine
import java.util.concurrent.Callable


@CommandLine.Command(
    name = "bputil",
    mixinStandardHelpOptions = true,
    version = ["bputil 1.0"],
    description = ["", "bputil is a simple tool to encode and parse bundle"],
    optionListHeading = "@|bold %nOptions|@:%n",
    footer = [""],
    subcommands = [BpStatus::class, BpCreate::class, BpShow::class, BpKey::class]
)
class BpUtil : Callable<Void> {
    @Throws(Exception::class)
    override fun call(): Void? {
        return null
    }
}

fun main(args: Array<String>) {
    CommandLine(BpUtil()).execute(*args)
}