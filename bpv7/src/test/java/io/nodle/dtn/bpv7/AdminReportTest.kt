package io.nodle.dtn.bpv7

import io.nodle.dtn.bpv7.administrative.*
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.MockitoRule
import java.net.URI

/**
 * @author Niki Izvorski on 13/08/21.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(MockitoJUnitRunner.Silent::class)
class AdminReportTest {

    @get:Rule
    var initRule: MockitoRule = MockitoJUnit.rule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun test01_unMarshalBundleAdministrationRecord() {
        /* Given */
        val admRecord = AdministrativeRecord(
                recordTypeCode = RecordTypeCode.StatusRecordType.code,
                data = StatusReport()
                        .assert(StatusAssertion.ReceivedBundle, true, 1613607271)
                        .assert(StatusAssertion.ForwardedBundle, true, 1613897271)
                        .reason(StatusReportReason.NoInformation)
                        .source(URI.create("dtn://test-sdk/")))

        val bundle = PrimaryBlock()
                .destination(URI.create("dtn://nodle/dtn-router"))
                .source(URI.create("dtn://test-sdk/"))
                .procV7Flags(BundleV7Flags.AdministrativeRecordPayload)
                .makeBundle()
                .addBlock(payloadBlock(admRecord.cborMarshalData()).crcType(CRCType.CRC32))

        /* Then */
        try {
            Assert.assertEquals(bundle, cborUnmarshalBundle(bundle.cborMarshal()))
        } catch (e: CborEncodingException) {
            Assert.fail()
        }
    }

    @Test
    fun test02_readAdministrationRecordMarshasl() {
        /* Given */
        val admRecord = AdministrativeRecord(
            recordTypeCode = RecordTypeCode.StatusRecordType.code,
            data = StatusReport()
                .assert(StatusAssertion.ReceivedBundle, true, 1613607271)
                .assert(StatusAssertion.ForwardedBundle, true, 1613897271)
                .reason(StatusReportReason.NoInformation)
                .source(URI.create("dtn://test-sdk/")))

        /* Then */
        Assert.assertEquals(admRecord, cborUnmarshalAdmnistrativeRecord(admRecord.cborMarshalData()))
    }

    @Test(expected = CborParsingException::class)
    fun test03_AdministrationRecordMarshaslUnsupportedFail() {
        /* Given */
        val admRecord = AdministrativeRecord(
            recordTypeCode = 0,
            data = StatusReport()
                .assert(StatusAssertion.ReceivedBundle, true, 1613607271)
                .assert(StatusAssertion.ForwardedBundle, true, 1613897271)
                .reason(StatusReportReason.NoInformation)
                .source(URI.create("dtn://test-sdk/")))

        /* Then */
        Assert.assertEquals(admRecord, cborUnmarshalAdmnistrativeRecord(admRecord.cborMarshalData()))
    }

    @Test
    fun test04_readAdministrationRecordAssert() {
        /* Given */
        val admRecord = AdministrativeRecord(
            recordTypeCode = RecordTypeCode.StatusRecordType.code,
            data = StatusReport()
                .assert(5, true, 1613607241)
                .reason(StatusReportReason.NoInformation)
                .source(URI.create("dtn://test-sdk/")))

        /* Then */
        Assert.assertNotEquals(admRecord, cborUnmarshalAdmnistrativeRecord(admRecord.cborMarshalData()))
    }

    @Test
    fun test06_checkStatusReportAssertedDeleted() {
        /* Given */
        val report = StatusReport()
            .assert(StatusAssertion.DeletedBundle, true, 1613607271)
            .reason(StatusReportReason.NoInformation)
            .source(URI.create("dtn://test-sdk/"))

        /* Then */
        Assert.assertTrue(report.isAsserted(StatusAssertion.DeletedBundle))
    }

    @Test
    fun test07_checkStatusReportAssertedReceived() {
        /* Given */
        val report = StatusReport()
            .assert(StatusAssertion.ReceivedBundle, true, 1613607271)
            .reason(StatusReportReason.NoInformation)
            .source(URI.create("dtn://test-sdk/"))

        /* Then */
        Assert.assertTrue(report.isAsserted(StatusAssertion.ReceivedBundle))
    }

    @Test
    fun test08_checkStatusReportAssertedForward() {
        /* Given */
        val report = StatusReport()
            .assert(StatusAssertion.ForwardedBundle, true, 1613607271)
            .reason(StatusReportReason.NoInformation)
            .source(URI.create("dtn://test-sdk/"))

        /* Then */
        Assert.assertTrue(report.isAsserted(StatusAssertion.ForwardedBundle))
    }

    @Test
    fun test09_checkStatusReportAssertedDelivered() {
        /* Given */
        val report = StatusReport()
            .assert(StatusAssertion.DeliveredBundle, true, 1613607271)
            .reason(StatusReportReason.NoInformation)
            .source(URI.create("dtn://test-sdk/"))

        /* Then */
        Assert.assertTrue(report.isAsserted(StatusAssertion.DeliveredBundle))
    }

    @Test
    fun test10_checkStatusReportAssertedUknown() {
        /* Given */
        val report = StatusReport()
            .assert(1, true, 1613607271)
            .reason(StatusReportReason.NoInformation)
            .source(URI.create("dtn://test-sdk/"))

        /* Then */
        Assert.assertTrue(report.isAsserted(1))
    }

    @Test
    fun test11_setTimeStamp() {
        /* Given */
        val report = StatusItem(2, true, 1613607271)

        /* When */
        report.timestamp = 1613607271

        /* Then */
        Assert.assertEquals(report.timestamp, 1613607271)
    }

    @Test
    fun test12_assertionStatus() {
        /* Given */
        val report = StatusItem(2, true, 1613607271)

        /* When */
        report.asserted = true

        /* Then */
        Assert.assertTrue(report.asserted)
    }

    @Test
    fun test13_statusAssertion() {
        /* Given */
        val report = StatusItem(2, true, 1613607271)

        /* When */
        report.statusAssertion = 2

        /* Then */
        Assert.assertEquals(report.statusAssertion, 2)
    }

    @Test
    fun test14_statusItemDefault() {
        /* Given */
        val report = StatusItem(2)

        /* Then */
        Assert.assertNotNull(report)
    }
}
