package io.nodle.dtn.bpv7

/**
 * @author Lucien Loiseau on 12/02/21.
 */
enum class BlockV7Flags(val offset : Int) {
    /* CanonicalBlock Processing Control Flags
        . Bit 0 (the high-order bit, 0x80): reserved.
        . Bit 1 (0x40): reserved.
        . Bit 2 (0x20): reserved.
        . Bit 3 (0x10): reserved.
        . Bit 4 (0x08): bundle must be deleted if block can't be processed.
        . Bit 5 (0x04): transmission of a status report is requested if block can't be processed.
        . Bit 6 (0x02): block must be removed from bundle if it can't be processed.
        . Bit 7 (the low-order bit, 0x01): block must be replicated in every fragment.
    */
    REPLICATE_IN_EVERY_FRAGMENT(0),
    DISCARD_IF_NOT_PROCESSED(1),
    TRANSMIT_STATUSREPORT_IF_NOT_PROCESSED(2),
    DELETE_BUNDLE_IF_NOT_PROCESSED(3),
    RESERVED_1(4),
    RESERVED_2(5),
    BLOCK_IS_ENCRYPTED(6),  // not in RFC
    RESERVED_4(7)
}