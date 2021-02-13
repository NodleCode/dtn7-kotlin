package io.nodle.dtn.bpv7

/**
 * @author Lucien Loiseau on 12/02/21.
 */
enum class BundleV7Flags(val offset: Int) {
    /* BundleV7 Processing Control Flag {@link https://tools.ietf.org/html/draft-ietf-dtn-bpbis-11#section-4.1.3}
        . Bit 0 (the high-order bit, 0x8000): reserved.
        . Bit 1 (0x4000): reserved.
        . Bit 2 (0x2000): reserved.
        . Bit 3(0x1000): bundle deletion status reports are requested.
        . Bit 4(0x0800): bundle delivery status reports are requested.
        . Bit 5(0x0400): bundle forwarding status reports are requested.
        . Bit 6(0x0200): reserved.
        . Bit 7(0x0100): bundle reception status reports are requested.
        . Bit 8(0x0080): bundle contains a Manifest block.
        . Bit 9(0x0040): status time is requested in all status reports.
        . Bit 10(0x0020): user application acknowledgement is requested.
        . Bit 11(0x0010): reserved.
        . Bit 12(0x0008): reserved.
        . Bit 13(0x0004): bundle must not be fragmented.
        . Bit 14(0x0002): payload is an administrative record.
        . Bit 15 (the low-order bit, 0x0001: bundle is a fragment.
     */
    FRAGMENT(0),
    ADM_RECORD(1),
    NO_FRAGMENT(2),
    RESERVED_3(3),
    RESERVED_4(4),
    APP_ACK_REQUEST(5),
    STATUS_TIME_REPORT(6),
    RESERVED_7(7),
    RESERVED_8(8),
    RESERVED_9(9),
    RESERVED_10(10),
    RESERVED_11(11),
    RESERVED_12(12),
    RESERVED_13(13),
    RECEPTION_REPORT(14),
    RESERVED_15(15),
    FORWARD_REPORT(16),
    DELIVERY_REPORT(17),
    DELETION_REPORT(18),
    RESERVED_19(19),
    RESERVED_20(20)
}