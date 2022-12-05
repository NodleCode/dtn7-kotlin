package io.nodle.dtn.bpv7

typealias DtnTime = Long

const val milliseconds1970To2k = 946684800000
const val milliToSec = 1000
const val nanoToMilli = 1000

fun dtnTimeFromUnixMsec(unixTimeInMillisec: Long) : DtnTime = unixTimeInMillisec - milliseconds1970To2k
fun Long.unixMsecToDtnTime() : DtnTime = dtnTimeFromUnixMsec(this)
fun dtnTimeNow() : DtnTime = dtnTimeFromUnixMsec(System.currentTimeMillis())
fun DtnTime.unixMsec() : Long = this + milliseconds1970To2k
fun DtnTime.unixSec() : Long = unixMsec() / milliToSec
fun DtnTime.unixNSec() : Long = unixMsec() * nanoToMilli
