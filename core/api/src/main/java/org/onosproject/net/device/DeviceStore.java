/*
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.net.device;

import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.provider.ProviderId;
import org.onosproject.store.Store;

import java.util.List;

/**
 * Manages inventory of infrastructure devices; not intended for direct use.
 */
public interface DeviceStore extends Store<DeviceEvent, DeviceStoreDelegate> {

    /**
     * Returns the number of devices known to the system.
     *
     * @return number of devices
     */
    int getDeviceCount();

    /**
     * Returns an iterable collection of all devices known to the system.
     *
     * @return device collection
     */
    Iterable<Device> getDevices();

    /**
     * Returns an iterable collection of all devices currently available to the system.
     *
     * @return device collection
     */
    Iterable<Device> getAvailableDevices();



    /**
     * Returns the device with the specified identifier.
     *
     * @param deviceId device identifier
     * @return device
     */
    Device getDevice(DeviceId deviceId);

    /**
     * Creates a new infrastructure device, or updates an existing one using
     * the supplied device description.
     *
     * @param providerId        provider identifier
     * @param deviceId          device identifier
     * @param deviceDescription device description
     * @return ready to send event describing what occurred; null if no change
     */
    DeviceEvent createOrUpdateDevice(ProviderId providerId, DeviceId deviceId,
                                     DeviceDescription deviceDescription);

    // TODO: We may need to enforce that ancillary cannot interfere this state
    /**
     * Removes the specified infrastructure device.
     *
     * @param deviceId device identifier
     * @return ready to send event describing what occurred; null if no change
     */
    DeviceEvent markOffline(DeviceId deviceId);

    /**
     * Updates the ports of the specified infrastructure device using the given
     * list of port descriptions. The list is assumed to be comprehensive.
     *
     * @param providerId        provider identifier
     * @param deviceId         device identifier
     * @param portDescriptions list of port descriptions
     * @return ready to send events describing what occurred; empty list if no change
     */
    List<DeviceEvent> updatePorts(ProviderId providerId, DeviceId deviceId,
                                  List<PortDescription> portDescriptions);

    /**
     * Updates the port status of the specified infrastructure device using the
     * given port description.
     *
     * @param providerId        provider identifier
     * @param deviceId        device identifier
     * @param portDescription port description
     * @return ready to send event describing what occurred; null if no change
     */
    DeviceEvent updatePortStatus(ProviderId providerId, DeviceId deviceId,
                                 PortDescription portDescription);

    /**
     * Returns the list of ports that belong to the specified device.
     *
     * @param deviceId device identifier
     * @return list of device ports
     */
    List<Port> getPorts(DeviceId deviceId);

    /**
     * Returns the specified device port.
     *
     * @param deviceId   device identifier
     * @param portNumber port number
     * @return device port
     */
    Port getPort(DeviceId deviceId, PortNumber portNumber);

    /**
     * Indicates whether the specified device is available/online.
     *
     * @param deviceId device identifier
     * @return true if device is available
     */
    boolean isAvailable(DeviceId deviceId);

    /**
     * Administratively removes the specified device from the store.
     *
     * @param deviceId device to be removed
     * @return null if no such device, or was forwarded to remove master
     */
    DeviceEvent removeDevice(DeviceId deviceId);
}
