package io.nodle.dtn.utils

import java.io.OutputStream

/**
 * @author Lucien Loiseau on 12/02/21.
 */
class DualOutputStream(
        private val out1 : OutputStream,
        private val out2 : OutputStream) : OutputStream() {

    override fun write(p0: Int) {
        out1.write(p0)
        out2.write(p0)
    }

}