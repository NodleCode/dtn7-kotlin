package io.nodle.dtn.interfaces

/**
 * @author Lucien Loiseau on 17/02/21.
 */
interface IBundleNode {

    /**
     * get the application agent associated with this bundle node.
     */
    val applicationAgent: IApplicationAgent

    /**
     * get the bundle protocol agent associated with this bundle node
     */
    val bpa: IBundleProtocolAgent

    /**
     * get the routing agent associated with this bundle node mapping to all CLAs
     */
    val router : IRouter

    /**
     * get the storage layer associated with this bundle node
     */
    val store : Bpv7Storage

}
