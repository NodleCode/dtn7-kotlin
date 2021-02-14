package io.nodle.dtn

import io.nodle.dtn.bpv7.*
import io.nodle.dtn.crypto.Ed25519Util
import io.nodle.dtn.crypto.ed25519PrivateKey
import io.nodle.dtn.crypto.ed25519PublicKey
import io.nodle.dtn.crypto.toEd25519PrivateKey
import io.nodle.dtn.utils.hexToBa
import io.nodle.dtn.utils.toHex
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable


/**
 * @author Lucien Loiseau on 14/02/21.
 */
@CommandLine.Command(
    name = "key",
    mixinStandardHelpOptions = true,
    description = ["", "manage ed25519 keys"],
    optionListHeading = "@|bold %nOptions|@:%n",
    footer = [""],
    subcommands = [BpKeyCreate::class, BpKeyShow::class]
)
class BpKey : Callable<Void> {
    @Throws(Exception::class)
    override fun call(): Void? {
        return null
    }
}

@CommandLine.Command(
    name = "create",
    mixinStandardHelpOptions = true,
    description = ["", "create an ed25519 key pair"],
    optionListHeading = "@|bold %nOptions|@:%n",
    footer = [""]
)
class BpKeyCreate : Callable<Void> {
    @Throws(Exception::class)
    override fun call(): Void? {
        val key = Ed25519Util.generateEd25519KeyPair()
        println("ed25519 " +
                "priv=0x${key.ed25519PrivateKey().encoded.toHex()}" +
                "  pub=0x${key.ed25519PublicKey().encoded.toHex()}")
        return null
    }
}

@CommandLine.Command(
    name = "show",
    mixinStandardHelpOptions = true,
    description = ["", "show an ed25519 key"],
    optionListHeading = "@|bold %nOptions|@:%n",
    footer = [""]
)
class BpKeyShow : Callable<Void> {
    @CommandLine.Parameters(index = "0", description = ["the key to show"])
    private var hexKey : String = ""

    @Throws(Exception::class)
    override fun call(): Void? {
        try {
            val key = hexKey.hexToBa().toEd25519PrivateKey()
            println("ed25519 priv=0x${key.encoded.toHex()}  pub=0x${key.generatePublicKey().encoded.toHex()}")
        } catch(e : Exception) {
            println("not an ed25519 key")
        }
        return null
    }
}