package io.nodle.dtn.interfaces

import io.nodle.dtn.bpv7.Bundle
import java.net.URI

sealed class DeliveryStatus {
    object DeliverySuccessful : DeliveryStatus()
    object EndpointNotFound : DeliveryStatus()
    object DeliveryTemporaryUnavailable : DeliveryStatus()
    object DeliveryRejected : DeliveryStatus()
}

typealias AARxHandler = suspend (Bundle) -> DeliveryStatus
typealias AATxHandler = suspend (Bundle) -> Boolean

/**
 * @author Lucien Loiseau on 17/02/21.
 */
interface IApplicationAgent {

    fun endpoints() : List<URI>

    /**
     * Application Agent's Administrative agent.
     */
    val administrativeAgent: IAdministrativeAgent

    /**
     * scheduleForTransmission is called by the Application Agent whenever a bundle is schedule for transmission.
     * This method expects a Bundle as a parameter and returns true if the BPA successfully scheduled the bundle.
     * It returns false otherwise.
     */
    val scheduleForTransmission: AATxHandler

    /**
     * receiveForLocalDelivery is called by the BPA whenever a bundle is scheduled for local delivery.
     * This method expects a Bundle as a parameter and return true if the bundle was successfully delivered.
     * It returns false otherwise.
     */
    val receiveForLocalDelivery: AARxHandler

}


fun IApplicationAgent.nodeId() : URI = administrativeAgent.administrativeEndpoint

fun IApplicationAgent.isLocal(eid : URI) : Boolean = endpoints().contains(eid)