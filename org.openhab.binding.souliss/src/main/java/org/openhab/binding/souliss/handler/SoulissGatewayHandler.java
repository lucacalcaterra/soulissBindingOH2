/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.handler;

import java.math.BigDecimal;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingUDPConstants;
import org.openhab.binding.souliss.internal.SoulissDatagramSocketFactory;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingNetworkParameters;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingUDPServerThread;
import org.openhab.binding.souliss.internal.protocol.SoulissCommonCommands;
import org.openhab.binding.souliss.internal.protocol.SoulissDiscover.DiscoverResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissGatewayHandler extends BaseBridgeHandler implements DiscoverResult {

    private Logger logger = LoggerFactory.getLogger(SoulissGatewayHandler.class);
    private DatagramSocket datagramSocket;
    private DatagramSocket datagramSocket_port230;
    SoulissBindingUDPServerThread UDP_Server = null;
    SoulissBindingUDPServerThread UDP_Server_port230 = null;

    private int countPING_KO = 0;
    boolean bGatewayDetected = false;

    Configuration gwConfigurationMap;
    // private SoulissCommunication com;
    // private int refrehInterval;
    // private String bridgeid;
    // private ScheduledFuture<?> discoverTimer;
    // private SoulissDiscover discover;
    // private ThingDiscoveryService thingDiscoveryService;
    private int refreshInterval;
    private ScheduledFuture<?> pingTimer;

    public SoulissGatewayHandler(Bridge bridge) {
        super(bridge);
        gwConfigurationMap = bridge.getConfiguration();

        SoulissBindingNetworkParameters.IPAddressOnLAN = (String) gwConfigurationMap
                .get(SoulissBindingConstants.CONFIG_IP_ADDRESS);

        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_LOCAL_PORT) != null) {
            SoulissBindingNetworkParameters.preferred_local_port = ((BigDecimal) gwConfigurationMap
                    .get(SoulissBindingConstants.CONFIG_LOCAL_PORT)).intValue();
        }

        if (SoulissBindingNetworkParameters.preferred_local_port < 0
                && SoulissBindingNetworkParameters.preferred_local_port > 65000) {
            bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_LOCAL_PORT, 0);
        }

        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_PORT) != null) {
            SoulissBindingNetworkParameters.souliss_gateway_port = ((BigDecimal) gwConfigurationMap
                    .get(SoulissBindingConstants.CONFIG_PORT)).intValue();
        }
        if (SoulissBindingNetworkParameters.souliss_gateway_port < 0
                && SoulissBindingNetworkParameters.souliss_gateway_port > 65000)

        {
            bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_PORT,
                    SoulissBindingUDPConstants.SOULISS_DEFAULT_GATEWAY_PORT);
        }

        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_USER_INDEX) != null)

        {
            SoulissBindingNetworkParameters.UserIndex = ((BigDecimal) gwConfigurationMap
                    .get(SoulissBindingConstants.CONFIG_USER_INDEX)).intValue();
            if (SoulissBindingNetworkParameters.UserIndex < 0 && SoulissBindingNetworkParameters.UserIndex > 255) {
                bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_USER_INDEX,
                        SoulissBindingUDPConstants.SOULISS_DEFAULT_USER_INDEX);
            }
        }

        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX) != null) {
            SoulissBindingNetworkParameters.NodeIndex = ((BigDecimal) gwConfigurationMap
                    .get(SoulissBindingConstants.CONFIG_NODE_INDEX)).intValue();
        }
        if (SoulissBindingNetworkParameters.NodeIndex < 0 && SoulissBindingNetworkParameters.NodeIndex > 255) {
            bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_NODE_INDEX,
                    SoulissBindingUDPConstants.SOULISS_DEFAULT_NODE_INDEX);

        }

        // initialize();

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialize() {
        logger.debug("initializing server handler for thing {}", getThing());
        // thingDiscoveryService = new ThingDiscoveryService(thing.getUID(), this);
        // thingDiscoveryService.start(bundleContext);

        datagramSocket = SoulissDatagramSocketFactory.getSocketDatagram();
        UDP_Server = new SoulissBindingUDPServerThread(datagramSocket, this);
        UDP_Server.start();

        datagramSocket_port230 = SoulissDatagramSocketFactory.getDatagram_for_broadcast();
        UDP_Server_port230 = new SoulissBindingUDPServerThread(datagramSocket_port230, this);
        UDP_Server_port230.start();

        setupRefreshTimer();
    }

    private void sendPing() {
        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_IP_ADDRESS).toString().length() > 0) {
            SoulissCommonCommands.sendPing(datagramSocket_port230,
                    gwConfigurationMap.get(SoulissBindingConstants.CONFIG_IP_ADDRESS).toString(), (byte) 0, (byte) 0);
            logger.debug("Sent ping packet");
        }
    }

    // @Override
    // public void handleCommand(ChannelUID channelUID, Command command) {
    // if (channelUID.getId().equals(CHANNEL_1)) {
    // // TODO: handle command
    //
    // // Note: if communication with thing fails for some reason,
    // // indicate that by setting the status with detail information
    // // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
    // // "Could not control device at IP address x.x.x.x");
    // }
    // }

    @Override
    public void thingUpdated(Thing thing) {
        this.thing = thing;

        // riprendere da qui
        // lo stato del thing (GW) aggiunto deve essere aggiornato.
        // if (com == null) {
        // return;
        // }
        //
        // boolean reconnect = false;
        //
        // // Create a new communication object if the user changed the IP configuration.
        // Object host_config_obj = thing.getConfiguration().get(SoulissBindingConstants.CONFIG_HOST_NAME);
        // String host_config = ((host_config_obj instanceof String) ? (String) host_config_obj
        // : (host_config_obj instanceof InetAddress) ? ((InetAddress) host_config_obj).getHostAddress() : null);
        // if (host_config != null && !host_config.equals(com.getAddr().getHostAddress())) {
        // reconnect = true;
        // }
        //
        // // Create a new communication object if the user changed the bridge ID configuration.
        // String id_config = (String) thing.getConfiguration().get(SoulissBindingConstants.CONFIG_ID);
        // if (id_config != null && !id_config.equals(com.getBridgeId())) {
        // reconnect = true;
        // }
        //
        // Create a new communication object if the user changed the port configuration.
        // Integer port_config = (Integer) thing.getConfiguration().get(SoulissBindingConstants.CONFIG_LOCAL_PORT);
        // if (port_config != null && port_config.intValue() > 0 && port_config.intValue() <= 65000) {
        // thing.getConfiguration().put(SoulissBindingConstants.CONFIG_LOCAL_PORT, 0);
        // }
        // SoulissBindingNetworkParameters.souliss_gateway_port = port_config;
        //
        // if (reconnect) {
        // // createCommunicationObject();
        // }
        //
        // BigDecimal interval_config = (BigDecimal)
        // thing.getConfiguration().get(SoulissBindingConstants.CONFIG_REFRESH);
        // if (interval_config != null && interval_config.intValue() != refrehInterval) {
        // // setupRefreshTimer();
        // }
    }

    @Override
    public void gatewayDetected(InetAddress addr, String id) {

    }

    @Override
    public void gatewayDetected() {
        updateStatus(ThingStatus.ONLINE);
        setGatewayDetected();
    }

    @Override
    public boolean isGatewayDetected() {
        return bGatewayDetected;
    }

    @Override
    public void setGatewayDetected() {
        countPING_KO = 0; // reset counter
        bGatewayDetected = true;
    }

    @Override
    public void setGatewayUndetected() {
        bGatewayDetected = false;
    }

    /**
     * Sets up the periodically refresh via the scheduler. If the user set CONFIG_REFRESH to 0, no refresh will be
     * done.
     */
    private void setupRefreshTimer() {

        // if (discoverTimer != null) {
        // discoverTimer.cancel(true);
        // }
        //
        BigDecimal interval_config = (BigDecimal) thing.getConfiguration().get(SoulissBindingConstants.CONFIG_REFRESH);
        if (interval_config == null || interval_config.intValue() == 0) {
            refreshInterval = 0;
            return;
        }

        refreshInterval = interval_config.intValue();
        if (pingTimer != null) {
            pingTimer.cancel(true);
        }
        // This timer will do the state update periodically.
        pingTimer = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // pingTimer.sendDiscover(scheduler);
                sendPing();
                // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Gateway wait for ping reply");

                if (++countPING_KO > 3) {
                    setGatewayUndetected();
                    // if GW do not respond to ping it is setted to OFFLINE
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                            "Gateway " + gwConfigurationMap.get(SoulissBindingConstants.CONFIG_ID).toString()
                                    + " do not respond to " + countPING_KO + " ping");
                }

            }
        }, refreshInterval, refreshInterval, TimeUnit.SECONDS);
    }

}
