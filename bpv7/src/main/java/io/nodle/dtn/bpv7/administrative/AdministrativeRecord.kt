package io.nodle.dtn.bpv7.administrative

import io.nodle.dtn.bpv7.eid.nullDtnEid
import java.net.URI

/**
 * @author Lucien Loiseau on 17/02/21.
 */
enum class RecordTypeCode(val code: Int) {
    StatusRecordType(1)
}

data class AdministrativeRecord(
        val recordTypeCode : Int,
        var data : AdministrativeData = StatusReport())

sealed class AdministrativeData

data class StatusReport(
        var received: Long = 0,
        var forwarded: Long = 0,
        var delivered: Long = 0,
        var deleted: Long = 0,
        var otherAssertions: MutableList<StatusItem> = mutableListOf(),
        var bundleStatusReportReason: Int = StatusReportReason.NoInformation.code,
        var sourceNodeId: URI = nullDtnEid(),
        var creationTimestamp: Long = 0,
        var fragmentOffset: Long = -1,
        var appDataLength: Long = -1
) : AdministrativeData()

