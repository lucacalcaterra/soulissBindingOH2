/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.internal.discovery;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TimerTask;
import java.util.TreeMap;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.binding.souliss.handler.SoulissGatewayHandler;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingNetworkParameters;
import org.openhab.binding.souliss.internal.protocol.SoulissDiscover;
import org.openhab.binding.souliss.internal.protocol.SoulissDiscover.DiscoverResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link soulissHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author David Graeff - Initial contribution
 */
public class SoulissGatewayDiscovery extends AbstractDiscoveryService implements DiscoverResult {
    private Logger logger = LoggerFactory.getLogger(SoulissGatewayDiscovery.class);
    private SoulissDiscover soulissDiscoverThread;
    private boolean bGatewayDetected = false;
    private ThingUID gatewayUID;
    // private ScheduledFuture<?> schedulerFuture;

    class DetectTask extends TimerTask {
        @Override
        public void run() {
            soulissDiscoverThread.sendDiscover(scheduler);
        }
    }

    private void startDiscoveryService() {
        if (soulissDiscoverThread == null) {
            try {
                // receiveThread = new SoulissDiscover(broadcast, this, 50, 2000 / 50);
                soulissDiscoverThread = new SoulissDiscover(this,
                        SoulissBindingConstants.DISCOVERY_resendTimeoutInMillis,
                        SoulissBindingConstants.DISCOVERY_resendAttempts);
            } catch (SocketException e) {
                logger.error("Opening a socket for the souliss discovery service failed. " + e.getLocalizedMessage());
                return;
            }
            soulissDiscoverThread.start();
        }
    }

    public SoulissGatewayDiscovery() throws IllegalArgumentException, UnknownHostException {
        super(SoulissBindingConstants.SUPPORTED_THING_TYPES_UIDS, SoulissBindingConstants.DISCOVERY_TimeoutInSeconds,
                false);
        // startDiscoveryService();
    }

    @Override
    protected void startBackgroundDiscovery() {
        // if (backgroundFuture != null) {
        // return;
        // }
        // // per adesso non mi serve il discovery in background
        // // startDiscoveryService();
        //
        // backgroundFuture = scheduler.scheduleAtFixedRate(new DetectTask(), 50, 60000 * 30, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        // stopScan();
        // if (backgroundFuture != null) {
        // backgroundFuture.cancel(false);
        // backgroundFuture = null;
        // }

    }

    /**
     * The {@link gatewayDetected} used to create the Gateway
     *
     * @author Tonino Fazio - Initial contribution
     */
    @Override
    public void gatewayDetected(InetAddress addr, String id) {
        logger.debug("souliss gateway found " + addr.getHostName() + " " + id);
        gatewayUID = new ThingUID(SoulissBindingConstants.GATEWAY_THING_TYPE, id);

        String label = "Souliss Gateway " + id;
        Map<String, Object> properties = new TreeMap<>();
        properties.put(SoulissBindingConstants.CONFIG_ID, id);
        properties.put(SoulissBindingConstants.CONFIG_IP_ADDRESS, addr.getHostAddress());
        // SoulissBindingNetworkParameters.IPAddressOnLAN = addr.getHostAddress();
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(gatewayUID).withLabel(label)
                .withProperties(properties).build();
        thingDiscovered(discoveryResult);
        setGatewayDetected();

    }

    /**
     * The {@link gatewayDetected} not used here
     *
     * @author Tonino Fazio - Initial contribution
     */
    @Override
    public void gatewayDetected(byte lastByteGatewayIP) {
        // TODO Auto-generated method stub

    }

    @Override
    public void dbStructAnswerReceived(SoulissGatewayHandler gateway) {

    }

    @Override
    protected void startScan() {
        // schedulerFuture = scheduler.scheduleAtFixedRate(new DetectTask(), 50,
        // SoulissBindingConstants.DISCOVERY_TimeoutInMillis, TimeUnit.MILLISECONDS);
        startDiscoveryService();
        soulissDiscoverThread.sendDiscover(scheduler);
    }

    @Override
    protected synchronized void stopScan() {
        if (soulissDiscoverThread != null) {
            soulissDiscoverThread.stopResend();
            soulissDiscoverThread.stopReceiving();
            soulissDiscoverThread = null;
        }
        super.stopScan();
    }

    @Override
    public boolean isGatewayDetected() {
        return bGatewayDetected;
    }

    @Override
    public void setGatewayDetected() {
        bGatewayDetected = true;
    }

    @Override
    public void setGatewayUndetected() {
        bGatewayDetected = false;

    }

    @Override
    public ThingUID getGateway() {
        return gatewayUID;
    }

    @Override
    public void thingDetected(byte lastByteGatewayIP, short typical, short node, short slot) {
        ThingUID thingUID = null;
        String label = "";
        DiscoveryResult discoveryResult;

        if (lastByteGatewayIP == Byte.parseByte(((SoulissGatewayHandler) SoulissBindingNetworkParameters
                .getGateway(lastByteGatewayIP).getHandler()).IPAddressOnLAN)) {

            String sNodeId = node + SoulissBindingConstants.UUID_NODE_SLOT_SEPARATOR + slot;
            switch (typical) {
                case SoulissBindingProtocolConstants.Souliss_T11:
                    thingUID = new ThingUID(SoulissBindingConstants.T11_THING_TYPE, sNodeId);
                    label = "T11: node " + node + ", slot " + slot;
                    break;
                case SoulissBindingProtocolConstants.Souliss_T12:
                    thingUID = new ThingUID(SoulissBindingConstants.T12_THING_TYPE, sNodeId);
                    label = "T12: node " + node + ", slot " + slot;
                    break;
                case SoulissBindingProtocolConstants.Souliss_T13:
                    thingUID = new ThingUID(SoulissBindingConstants.T13_THING_TYPE, sNodeId);
                    label = "T13: node " + node + ", slot " + slot;
                    break;
                case SoulissBindingProtocolConstants.Souliss_T14:
                    thingUID = new ThingUID(SoulissBindingConstants.T14_THING_TYPE, sNodeId);
                    label = "T14: node " + node + ", slot " + slot;
                    break;
                case SoulissBindingProtocolConstants.Souliss_T16:
                    thingUID = new ThingUID(SoulissBindingConstants.T16_THING_TYPE, sNodeId);
                    label = "T16: node " + node + ", slot " + slot;
                    break;
                case SoulissBindingProtocolConstants.Souliss_T21:
                    thingUID = new ThingUID(SoulissBindingConstants.T21_THING_TYPE, sNodeId);
                    label = "T21: node " + node + ", slot " + slot;
                    break;
                case SoulissBindingProtocolConstants.Souliss_T22:
                    thingUID = new ThingUID(SoulissBindingConstants.T22_THING_TYPE, sNodeId);
                    label = "T22: node " + node + ", slot " + slot;
                    break;
                case SoulissBindingProtocolConstants.Souliss_T52_TemperatureSensor:
                    thingUID = new ThingUID(SoulissBindingConstants.T52_THING_TYPE, sNodeId);
                    label = "T52: node " + node + ", slot " + slot;
                    break;
                case SoulissBindingProtocolConstants.Souliss_T55_VoltageSensor:
                    thingUID = new ThingUID(SoulissBindingConstants.T55_THING_TYPE, sNodeId);
                    label = "T55: node " + node + ", slot " + slot;
                    break;
                case SoulissBindingProtocolConstants.Souliss_T56_CurrentSensor:
                    thingUID = new ThingUID(SoulissBindingConstants.T56_THING_TYPE, sNodeId);
                    label = "T56: node " + node + ", slot " + slot;
                    break;
                case SoulissBindingProtocolConstants.Souliss_T57_PowerSensor:
                    thingUID = new ThingUID(SoulissBindingConstants.T57_THING_TYPE, sNodeId);
                    label = "T57: node " + node + ", slot " + slot;
                    break;
            }
            if (thingUID != null) {
                label = "[" + gatewayUID + "] " + label;
                discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(label).withBridge(gatewayUID)
                        .build();
                thingDiscovered(discoveryResult);
            }
        }
    }

}
