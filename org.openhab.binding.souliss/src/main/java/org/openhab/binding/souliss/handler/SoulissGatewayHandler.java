/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.handler;

import java.net.DatagramSocket;
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
import org.openhab.binding.souliss.internal.discovery.ThingDiscoveryService;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingNetworkParameters;
import org.openhab.binding.souliss.internal.protocol.SoulissCommonCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissGatewayHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(SoulissGatewayHandler.class);
    private DatagramSocket datagramSocket;
    Configuration gwConfigurationMap;
    // private SoulissCommunication com;
    // private int refrehInterval;
    // private String bridgeid;
    // private ScheduledFuture<?> discoverTimer;
    // private SoulissDiscover discover;
    private ThingDiscoveryService thingDiscoveryService;

    public SoulissGatewayHandler(Bridge bridge) {
        super(bridge);
        gwConfigurationMap = bridge.getConfiguration();

        SoulissBindingNetworkParameters.IPAddressOnLAN = (String) gwConfigurationMap
                .get(SoulissBindingConstants.CONFIG_IP_ADDRESS);

        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_LOCAL_PORT) != null) {
            SoulissBindingNetworkParameters.preferred_local_port = (Integer) gwConfigurationMap
                    .get(SoulissBindingConstants.CONFIG_LOCAL_PORT);
        }

        if (SoulissBindingNetworkParameters.preferred_local_port < 0
                && SoulissBindingNetworkParameters.preferred_local_port > 65000) {
            bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_LOCAL_PORT, 0);
        }

        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_PORT) != null) {
            if (SoulissBindingNetworkParameters.souliss_gateway_port < 0
                    && SoulissBindingNetworkParameters.souliss_gateway_port > 65000)

            {
                bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_PORT,
                        SoulissBindingUDPConstants.SOULISS_DEFAULT_GATEWAY_PORT);
            }
        }

        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_USER_INDEX) != null) {
            SoulissBindingNetworkParameters.UserIndex = (Integer) gwConfigurationMap
                    .get(SoulissBindingConstants.CONFIG_USER_INDEX);
            if (SoulissBindingNetworkParameters.UserIndex < 0 && SoulissBindingNetworkParameters.UserIndex > 255) {
                bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_USER_INDEX,
                        SoulissBindingUDPConstants.SOULISS_DEFAULT_USER_INDEX);
            }
        }

        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX) != null) {
            SoulissBindingNetworkParameters.NodeIndex = (Integer) gwConfigurationMap
                    .get(SoulissBindingConstants.CONFIG_NODE_INDEX);
        }
        if (SoulissBindingNetworkParameters.NodeIndex < 0 && SoulissBindingNetworkParameters.NodeIndex > 255) {
            bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_NODE_INDEX,
                    SoulissBindingUDPConstants.SOULISS_DEFAULT_NODE_INDEX);

        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initialize() {
        logger.debug("initializing server handler for thing {}", getThing());
        thingDiscoveryService = new ThingDiscoveryService(thing.getUID(), this);
        thingDiscoveryService.start(bundleContext);

        scheduler.schedule(new Runnable() {

            @Override
            public void run() {
                sendPing();
            }

        }, SoulissBindingConstants.PING_resendTimeoutInSeconds, TimeUnit.SECONDS);
    }

    private void sendPing() {
        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_IP_ADDRESS).toString().length() > 0) {
            datagramSocket = SoulissDatagramSocketFactory.getDatagram_for_broadcast();
            SoulissCommonCommands.sendPing(datagramSocket,
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
        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "id");

        riprendere da qui
        lo stato del thing (GW) aggiunto deve essere aggiornato.
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

    // @Override
    // public void gatewayDetected(InetAddress addr, String id) {
    //
    // }

    // @Override
    // public void noBridgeDetected() {
    // updateStatus(ThingStatus.OFFLINE);
    // }

    // /**
    // * Sets up the periodically refresh via the scheduler. If the user set CONFIG_REFRESH to 0, no refresh will be
    // * done.
    // */
    // private void setupRefreshTimer() {
    // // Version 1/2 do not support response messages / detection.
    // if (bridgeid == null) {
    // return;
    // }
    //
    // if (discoverTimer != null) {
    // discoverTimer.cancel(true);
    // }
    //
    // BigDecimal interval_config = (BigDecimal) thing.getConfiguration().get(SoulissBindingConstants.CONFIG_REFRESH);
    // if (interval_config == null || interval_config.intValue() == 0) {
    // refrehInterval = 0;
    // return;
    // }
    //
    // refrehInterval = interval_config.intValue();
    //
    // // This timer will do the state update periodically.
    // discoverTimer = scheduler.scheduleAtFixedRate(new Runnable() {
    // @Override
    // public void run() {
    // discover.sendDiscover(scheduler);
    // }
    // }, refrehInterval, refrehInterval, TimeUnit.MINUTES);
    // }

}
