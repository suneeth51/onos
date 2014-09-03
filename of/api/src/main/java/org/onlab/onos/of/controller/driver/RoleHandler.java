package org.onlab.onos.of.controller.driver;

import java.io.IOException;

import org.onlab.onos.of.controller.RoleState;
import org.projectfloodlight.openflow.protocol.OFErrorMsg;
import org.projectfloodlight.openflow.protocol.OFExperimenter;
import org.projectfloodlight.openflow.protocol.OFRoleReply;

/**
 * Role handling.
 *
 */
public interface RoleHandler {

    /**
     * Extract the role from an OFVendor message.
     *
     * Extract the role from an OFVendor message if the message is a
     * Nicira role reply. Otherwise return null.
     *
     * @param experimenterMsg The vendor message to parse.
     * @return The role in the message if the message is a Nicira role
     * reply, null otherwise.
     * @throws SwitchStateException If the message is a Nicira role reply
     * but the numeric role value is unknown.
     */
    public RoleState extractNiciraRoleReply(OFExperimenter experimenterMsg)
            throws SwitchStateException;

    /**
     * Send a role request with the given role to the switch and update
     * the pending request and timestamp.
     * Sends an OFPT_ROLE_REQUEST to an OF1.3 switch, OR
     * Sends an NX_ROLE_REQUEST to an OF1.0 switch if configured to support it
     * in the IOFSwitch driver. If not supported, this method sends nothing
     * and returns 'false'. The caller should take appropriate action.
     *
     * One other optimization we do here is that for OF1.0 switches with
     * Nicira role message support, we force the Role.EQUAL to become
     * Role.SLAVE, as there is no defined behavior for the Nicira role OTHER.
     * We cannot expect it to behave like SLAVE. We don't have this problem with
     * OF1.3 switches, because Role.EQUAL is well defined and we can simulate
     * SLAVE behavior by using ASYNC messages.
     *
     * @param role
     * @throws IOException
     * @returns false if and only if the switch does not support role-request
     * messages, according to the switch driver; true otherwise.
     */
    public boolean sendRoleRequest(RoleState role, RoleRecvStatus exp)
            throws IOException;

    /**
     * Extract the role information from an OF1.3 Role Reply Message.
     * @param h
     * @param rrmsg
     * @return RoleReplyInfo object
     * @throws SwitchStateException
     */
    public RoleReplyInfo extractOFRoleReply(OFRoleReply rrmsg)
            throws SwitchStateException;

    /**
     * Deliver a received role reply.
     *
     * Check if a request is pending and if the received reply matches the
     * the expected pending reply (we check both role and xid) we set
     * the role for the switch/channel.
     *
     * If a request is pending but doesn't match the reply we ignore it, and
     * return
     *
     * If no request is pending we disconnect with a SwitchStateException
     *
     * @param rri information about role-reply in format that
     *                      controller can understand.
     * @throws SwitchStateException if no request is pending
     */
    public RoleRecvStatus deliverRoleReply(RoleReplyInfo rri)
            throws SwitchStateException;


    /**
     * Called if we receive an  error message. If the xid matches the
     * pending request we handle it otherwise we ignore it.
     *
     * Note: since we only keep the last pending request we might get
     * error messages for earlier role requests that we won't be able
     * to handle
     */
    public RoleRecvStatus deliverError(OFErrorMsg error)
            throws SwitchStateException;

}