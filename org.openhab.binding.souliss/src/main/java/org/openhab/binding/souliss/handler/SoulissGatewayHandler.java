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
    public DatagramSocket datagramSocket_forBroadcasting;
    public DatagramSocket datagramSocket;
    SoulissBindingUDPServerThread UDP_Server = null;
    SoulissBindingUDPServerThread UDP_Server_DefaultPort = null;

    private int countPING_KO = 0;
    boolean bGatewayDetected = false;

    Configuration gwConfigurationMap;

    private int pingRefreshInterval;
    private ScheduledFuture<?> pingTimer;
    private ScheduledFuture<?> subscriptionTimer;
    private int subscriptionRefreshInterval;
    private Bridge bridge;
    public int preferred_local_port;
    public int souliss_gateway_port;
    public short userIndex;
    public short nodeIndex;
    public String IPAddressOnLAN;
    private int nodes;
    private int maxnodes;
    private int maxTypicalXnode;
    private int maxrequests;
    private boolean bFirtTimeDiscover = true;

    public SoulissGatewayHandler(Bridge _bridge) {
        super(_bridge);
        bridge = _bridge;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    @Override
    public void handleRemoval() {

        SoulissBindingNetworkParameters.removeGateway(Byte.parseByte(IPAddressOnLAN.split("\\.")[3]));

        if (datagramSocket_forBroadcasting != null
                && souliss_gateway_port != SoulissBindingUDPConstants.SOULISS_GATEWAY_DEFAULT_PORT) {
            UDP_Server_DefaultPort.stopServer();
            UDP_Server_DefaultPort = null;
            datagramSocket_forBroadcasting.close();
        }
        if (datagramSocket != null) {
            // stop server and close socket
            UDP_Server.stopServer();
            UDP_Server = null;
            // datagramSocket.close();
        }
    }

    @Override
    public void initialize() {
        logger.debug("initializing server handler for thing {}", getThing());

        gwConfigurationMap = bridge.getConfiguration();
        IPAddressOnLAN = (String) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_IP_ADDRESS);

        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_LOCAL_PORT) != null) {
            preferred_local_port = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_LOCAL_PORT))
                    .intValue();
        }

        if (preferred_local_port < 0 && preferred_local_port > 65000) {
            bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_LOCAL_PORT, 0);
        }

        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_PORT) != null) {
            souliss_gateway_port = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_PORT))
                    .intValue();
        }
        if (souliss_gateway_port < 0 && souliss_gateway_port > 65000)

        {
            bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_PORT,
                    SoulissBindingUDPConstants.SOULISS_GATEWAY_DEFAULT_PORT);
        }

        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_USER_INDEX) != null)

        {
            userIndex = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_USER_INDEX)).shortValue();
            if (userIndex < 0 && userIndex > 255) {
                bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_USER_INDEX,
                        SoulissBindingUDPConstants.SOULISS_DEFAULT_USER_INDEX);
            }
        }

        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX) != null) {
            nodeIndex = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX)).shortValue();
        }
        if (nodeIndex < 0 && nodeIndex > 255) {
            bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_NODE_INDEX,
                    SoulissBindingUDPConstants.SOULISS_DEFAULT_NODE_INDEX);

        }

        datagramSocket = SoulissDatagramSocketFactory.getSocketDatagram(preferred_local_port);
        UDP_Server = new SoulissBindingUDPServerThread(datagramSocket, SoulissBindingNetworkParameters.discoverResult);
        UDP_Server.start();

        // if souliss_gateway_port == SOULISS_GATEWAY_DEFAULT_PORT then get datagramSocket if it already created
        if (souliss_gateway_port == SoulissBindingUDPConstants.SOULISS_GATEWAY_DEFAULT_PORT) {
            datagramSocket_forBroadcasting = SoulissBindingNetworkParameters.getDatagramSocket();
        }
        // START SERVER ON DEFAULT PORT - Used for ping and discovery
        if (datagramSocket_forBroadcasting == null) {
            datagramSocket_forBroadcasting = SoulissDatagramSocketFactory.getSocketDatagram(souliss_gateway_port);
            if (datagramSocket_forBroadcasting != null) {
                UDP_Server_DefaultPort = new SoulissBindingUDPServerThread(datagramSocket_forBroadcasting,
                        SoulissBindingNetworkParameters.discoverResult);
                UDP_Server_DefaultPort.start();
            }
        }
        setupRefreshTimer();

    }

    private void sendPing() {
        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_IP_ADDRESS).toString().length() > 0) {
            SoulissCommonCommands.sendPing(datagramSocket_forBroadcasting,
                    gwConfigurationMap.get(SoulissBindingConstants.CONFIG_IP_ADDRESS).toString(),
                    Short.parseShort(gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX).toString()),
                    Short.parseShort(gwConfigurationMap.get(SoulissBindingConstants.CONFIG_USER_INDEX).toString()),
                    (byte) 0, (byte) 0);
            logger.debug("Sent ping packet");
        }
    }

    private void sendSubscription() {
        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_IP_ADDRESS).toString().length() > 0) {
            SoulissCommonCommands.sendSUBSCRIPTIONframe(datagramSocket,
                    gwConfigurationMap.get(SoulissBindingConstants.CONFIG_IP_ADDRESS).toString(),
                    Short.parseShort(gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX).toString()),
                    Short.parseShort(gwConfigurationMap.get(SoulissBindingConstants.CONFIG_USER_INDEX).toString()),
                    nodes);
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        this.thing = thing;
    }

    /**
     * Sets up the periodically refresh via the scheduler. If the user set CONFIG_REFRESH to 0, no refresh will be
     * done.
     */
    private void setupRefreshTimer() {

        BigDecimal ping_interval_config = (BigDecimal) thing.getConfiguration()
                .get(SoulissBindingConstants.CONFIG_PING_REFRESH);
        BigDecimal subscription_interval_config = (BigDecimal) thing.getConfiguration()
                .get(SoulissBindingConstants.CONFIG_SUBSCRIPTION_REFRESH);
        if (ping_interval_config == null || ping_interval_config.intValue() == 0) {
            pingRefreshInterval = 0;
            return;
        }

        if (subscription_interval_config == null || subscription_interval_config.intValue() == 0) {
            subscriptionRefreshInterval = 0;
            return;
        }

        pingRefreshInterval = ping_interval_config.intValue();
        subscriptionRefreshInterval = subscription_interval_config.intValue();
        if (pingTimer != null) {
            pingTimer.cancel(true);
        }
        if (subscriptionTimer != null) {
            subscriptionTimer.cancel(true);
        }
        // This timer will do the state update periodically.
        pingTimer = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                sendPing();

                if (++countPING_KO > 3) {
                    // if GW do not respond to ping it is setted to OFFLINE
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                            "Gateway " + gwConfigurationMap.get(SoulissBindingConstants.CONFIG_ID).toString()
                                    + " do not respond to " + countPING_KO + " ping");
                }

            }
        }, 0, pingRefreshInterval, TimeUnit.SECONDS);

        subscriptionTimer = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                sendSubscription();
            }
        }, 0, subscriptionRefreshInterval, TimeUnit.MINUTES);
    }

    public void dbStructAnswerReceived() {
        SoulissCommonCommands.sendTYPICAL_REQUESTframe(datagramSocket, IPAddressOnLAN, nodeIndex, userIndex, nodes);
    }

    public String getGatewayIP() {
        return ((SoulissGatewayHandler) thingRegistry.get(thing.getBridgeUID()).getHandler()).IPAddressOnLAN;
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    public void setMaxnodes(int maxnodes) {
        this.maxnodes = maxnodes;
    }

    public void setMaxTypicalXnode(int maxTypicalXnode) {
        this.maxTypicalXnode = maxTypicalXnode;
    }

    public void setMaxrequests(int maxrequests) {
        this.maxrequests = maxrequests;
    }

    public int getMaxTypicalXnode() {
        return maxTypicalXnode;
    }

    /**
     * The {@link gatewayDetected} is used to notify that UDPServer decoded a Ping Response from gateway
     *
     * @author Tonino Fazio - Initial contribution
     */

    public void gatewayDetected() {
        updateStatus(ThingStatus.ONLINE);
        countPING_KO = 0; // reset counter
    }

}
