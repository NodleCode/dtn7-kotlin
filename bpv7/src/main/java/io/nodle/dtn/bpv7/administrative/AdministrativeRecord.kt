package io.nodle.dtn.bpv7.administrative

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


