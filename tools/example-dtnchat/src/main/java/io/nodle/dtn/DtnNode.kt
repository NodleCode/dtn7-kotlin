@file:JvmName("Application")

package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.administrative.assertion
import io.nodle.dtn.bpv7.administrative.reportedId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.net.URI
import java.util.concurrent.Callable


@CommandLine.Command(
    name = "dtnode",
    mixinStandardHelpOptions = true,
    version = ["dtnode 1.0"],
    description = ["", "bputil is a simple tool to encode and parse bundle"],
    optionListHeading = "@|bold %nOptions|@:%n",
    footer = [""],
)
class DtnNode : Callable<Void> {
    @CommandLine.Option(names = ["-l", "--listen"], description = ["cla http server"])
    private var listen = false

    @Throws(Exception::class)
    override fun call(): Void? {
        var nodeId: URI? = null
        var source: URI? = null
        var dest: URI? = null
        var node: BpNode? = null

        if (listen) {
            nodeId = URI.create("dtn://user1/")
            source = URI.create("dtn://user1/chat")
            dest = URI.create("dtn://user2/chat")
            node = createBpNodeServer(nodeId, "/chat") {
                if(it.isAdminRecord()) {
                    println(">> receive ${it.getStatusReport()!!.assertion()} for ${it.getStatusReport()!!.reportedId()}")
                } else {
                    println("(${it.ID()}): " + String(it.getPayloadBlockData().buffer))
                }
            }
        } else {
            nodeId = URI.create("dtn://user2/")
            source = URI.create("dtn://user2/chat")
            dest = URI.create("dtn://user1/chat")
            node = createBpNodeClient(nodeId, "/chat") {
                if(it.isAdminRecord()) {
                    println(">> receive  ${it.getStatusReport()!!.assertion()} for ${it.getStatusReport()!!.reportedId()}")
                } else {
                    println("(${it.ID()}): " + String(it.getPayloadBlockData().buffer))
                }
            }
        }

        while(true) {
            val input = readLine()!!
            CoroutineScope(Dispatchers.IO).launch {
                node.bpa.receivePDU(makeBundle(input,dest,source))
            }
        }
    }
}

fun main(args: Array<String>) {
    CommandLine(DtnNode()).execute(*args)
}