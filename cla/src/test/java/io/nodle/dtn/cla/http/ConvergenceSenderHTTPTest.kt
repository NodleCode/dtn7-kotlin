package io.nodle.dtn.cla.http

import com.sun.net.httpserver.HttpServer
import io.nodle.dtn.bpv7.*
import io.nodle.dtn.bpv7.eid.createDtnEid
import io.nodle.dtn.interfaces.IBundleNode
import io.nodle.dtn.interfaces.IBundleProtocolAgent
import io.nodle.dtn.utils.encodeToBase64
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.net.InetSocketAddress
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.random.Random


@ExperimentalCoroutinesApi
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.Silent::class)
class ConvergenceSenderHTTPTest {

    private fun http(str: String, hdl : (Bundle) -> Bundle?) : HttpServer {
        val address = InetSocketAddress(0)
        val server = HttpServer.create(address, 0)
        server.createContext(str) {
            val response = hdl(cborUnmarshalBundle(it.requestBody))?.cborMarshal()
            it.sendResponseHeaders(200,response?.size?.toLong()?:0)
            it.responseBody.write(response?: byteArrayOf())
        }
        server.start()
        println("server is started on port: "+server.address.port+" (${server.address.hostName})")
        return server
    }

    private fun bundle(s: Int = 10000) = PrimaryBlock()
        .destination(createDtnEid("test-destination"))
        .source(createDtnEid("test-source"))
        .reportTo(createDtnEid("test-report-to"))
        .lifetime(1000)
        .crcType(CRCType.CRC32)
        .makeBundle()
        .addBlock(payloadBlock(Random.nextBytes(array = ByteArray(s))))

    private fun bundles(c: Int, s: Int = 10000) = (0 until c).map { bundle(s) }

    private fun assertEquals(b1: Bundle, b2: Bundle) =
        assertEquals(b1.cborMarshal().encodeToBase64(), b2.cborMarshal().encodeToBase64())

    private fun mockAgent(hdl : (Bundle) -> Unit = {}): IBundleNode  {
        val bpa = object : IBundleProtocolAgent {
            override suspend fun transmitADU(bundle: Bundle) = TODO()
            override suspend fun receivePDU(bundle: Bundle) = hdl(bundle)
        }
        return mock<IBundleNode> {
            on {mock.bpa} doReturn bpa
        }
    }

    @Test
    fun test1_sendBundle() {
        val bundle = bundle()
        val latch = CountDownLatch(1)
        val server = http("/") {
            println("received a bundle")
            assertEquals(bundle,it)
            latch.countDown()
            null
        }

        /* Check */
        runBlockingTest {
            val uri = URI.create("http://127.0.0.1:${server.address.port}")
            val cla = ConvergenceSenderHTTP(mockAgent(), uri)
            cla.sendBundle(bundle)
            assertEquals(true, latch.await(1, TimeUnit.SECONDS))
        }
    }

    @Test
    fun test1_receiveBundle() {
        val bundle = bundle()
        val latch = CountDownLatch(1)
        val server = http("/") {
            println("endpoint serve bundle")
            bundle
        }
        val agent = mockAgent {
            println("agent receive bundle")
            assertEquals(bundle,it)
            latch.countDown()
        }

        /* Check */
        runBlockingTest {
            val uri = URI.create("http://127.0.0.1:${server.address.port}")
            val cla = ConvergenceSenderHTTP(agent, uri)
            cla.sendBundle(bundle)
            assertEquals(true, latch.await(2, TimeUnit.SECONDS))
        }
    }
}
