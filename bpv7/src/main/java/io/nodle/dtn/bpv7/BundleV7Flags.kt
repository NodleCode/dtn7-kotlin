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
    IsFragment(0),
    AdministrativeRecordPayload(1),
    MustNotFragment(2),
    Reserved3(3),
    Reserved4(4),
    RequestUserApplicationAck(5),
    RequestStatusTime(6),
    Reserved7(7),
    Reserved8(8),
    Reserved9(9),
    Reserved10(10),
    Reserved11(11),
    Reserved12(12),
    Reserved13(13),
    StatusRequestReception(14),
    Reserved15(15),
    StatusRequestForward(16),
    StatusRequestDelivery(17),
    StatusRequestDeletion(18),
    Reserved19(19),
    Reserved20(20)
}