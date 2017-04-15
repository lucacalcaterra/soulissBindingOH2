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
import org.openhab.binding.souliss.internal.SoulissDatagramSocketFactory;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingNetworkParameters;
import org.openhab.binding.souliss.internal.protocol.SoulissCommonCommands;
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
    // private ScheduledFuture<?> backgroundFuture;
    private Logger logger = LoggerFactory.getLogger(SoulissGatewayDiscovery.class);
    private SoulissDiscover soulissDiscoverThread;
    private boolean bGatewayDetected = false;
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

    @Override
    public void gatewayDetected(InetAddress addr, String id) {
        logger.debug("souliss gateway found " + addr.getHostName() + " " + id);
        ThingUID thingUID = new ThingUID(SoulissBindingConstants.GATEWAY_THING_TYPE, id);
        String label = "Souliss Gateway " + id;
        Map<String, Object> properties = new TreeMap<>();
        properties.put(SoulissBindingConstants.CONFIG_ID, id);
        properties.put(SoulissBindingConstants.CONFIG_IP_ADDRESS, addr.getHostAddress());
        SoulissBindingNetworkParameters.IPAddressOnLAN = addr.getHostAddress();

        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(label)
                .withProperties(properties).build();
        thingDiscovered(discoveryResult);
        setGatewayDetected();
        SoulissCommonCommands.sendDBStructFrame(SoulissDatagramSocketFactory.getDatagram_for_broadcast(),
                addr.getHostAddress());

    }

    @Override
    public void thingDetected(short typical, short node, short slot) {
        ThingUID thingUID;
        String label;
        DiscoveryResult discoveryResult;

        switch (typical) {
            case SoulissBindingProtocolConstants.Souliss_T11:
                thingUID = new ThingUID(SoulissBindingConstants.BINDING_ID, "t11");
                label = "T11: node " + node + ", slot " + slot;
                discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(label).build();
                thingDiscovered(discoveryResult);
                break;
            case SoulissBindingProtocolConstants.Souliss_T14:
                thingUID = new ThingUID(SoulissBindingConstants.BINDING_ID, "t14");
                label = "T14: node " + node + ", slot " + slot;
                discoveryResult = DiscoveryResultBuilder.create(thingUID).withLabel(label).build();
                thingDiscovered(discoveryResult);
                break;
        }

    }

    @Override
    public void gatewayDetected() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dbStructAnswerReceived() {
        SoulissCommonCommands.sendTYPICAL_REQUESTframe(SoulissDatagramSocketFactory.getDatagram_for_broadcast(),
                SoulissBindingNetworkParameters.IPAddressOnLAN);

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
}
