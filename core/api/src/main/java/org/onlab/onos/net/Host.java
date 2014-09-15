package org.onlab.onos.net;

import org.onlab.packet.IPAddress;
import org.onlab.packet.MACAddress;
import org.onlab.packet.VLANID;

import java.util.Set;

/**
 * Abstraction of an end-station host on the network, essentially a NIC.
 */
public interface Host extends Element {

    /**
     * Host identification.
     *
     * @return host id
     */
    @Override
    HostId id();

    /**
     * Returns the host MAC address.
     *
     * @return mac address
     */
    MACAddress mac();

    /**
     * Returns the VLAN ID tied to this host.
     *
     * @return VLAN ID value
     */
    VLANID vlan();

    /**
     * Returns set of IP addresses currently bound to the host MAC address.
     *
     * @return set of IP addresses; empty if no IP address is bound
     */
    Set<IPAddress> ipAddresses();

    /**
     * Returns the most recent host location where the host attaches to the
     * network edge.
     *
     * @return host location
     */
    HostLocation location();

    // TODO: explore capturing list of recent locations to aid in mobility

}