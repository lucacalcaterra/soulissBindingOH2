/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.internal.protocol;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.souliss.handler.SoulissGatewayHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissDiscover extends Thread {
    /**
     * Result callback interface.
     */
    public interface DiscoverResult {
        static boolean isGatewayDetected = false;
        Bridge bridge = null;

        void gatewayDetected(InetAddress addr, String id);

        boolean isGatewayDetected();

        void setGatewayDetected();

        void setGatewayUndetected();

        void thingDetected_Typicals(byte lastByteGatewayIP, short typical, short node, short slot);

        void thingDetected_ActionMessages(String sTopicNumber, String sTopicVariant);

        ThingUID getGatewayUID();
    }

    // private boolean willbeclosed = false;
    private DatagramSocket datagramSocket;
    SoulissBindingUDPServerThread UDP_Server_OnDefaultPort = null;
    ///// Debug
    private Logger logger = LoggerFactory.getLogger(SoulissDiscover.class);

    ///// Result and resend
    final private DiscoverResult discoverResult;
    private int resendCounter = 0;
    private ScheduledFuture<?> resendTimer;
    final private int resendTimeoutInMillis;
    final private int resendAttempts;

    public SoulissDiscover(DatagramSocket _datagramSocket, DiscoverResult discoverResult, int resendTimeoutInMillis,
            int resendAttempts) throws SocketException {
        this.resendAttempts = resendAttempts;
        this.resendTimeoutInMillis = resendTimeoutInMillis;
        datagramSocket = _datagramSocket;
        this.discoverResult = discoverResult;
    }

    public void stopReceiving() {
        // willbeclosed = true;
        // try {
        // join(500);
        // } catch (InterruptedException e) {
        // }
        // interrupt();
    }

    public void stopResend() {
        if (resendTimer != null) {
            resendTimer.cancel(false);
            resendTimer = null;
        }
    }

    /**
     * Used by the scheduler to resend discover messages. Stops after 3 attempts.
     */
    private class SendDiscoverRunnable implements Runnable {
        public SendDiscoverRunnable() {

        }

        @Override
        public void run() {
            try {
                // ===============================================================================
                // ===============================================================================
                SoulissCommonCommands.sendBroadcastGatewayDiscover(datagramSocket);
                // ===============================================================================
                // ===============================================================================
                logger.debug("Sent discovery packet");

                if (++resendCounter > resendAttempts) {
                    if (resendTimer != null) {
                        resendTimer.cancel(false);
                        resendTimer = null;
                    }
                    discoverResult.setGatewayUndetected();
                    return;
                }
            } catch (Exception e) {
                logger.error("Sending a discovery packet failed. " + e.getLocalizedMessage());
            }
        }
    }

    /**
     * Send a discover message and resends the message until either a valid response
     * is received or the resend counter reaches the maximum attempts.
     *
     * @param scheduler The scheduler is used for resending.
     */
    public void sendDiscover(ScheduledExecutorService scheduler) {

        // Do nothing if there is already a discovery running
        if (resendTimer != null) {
            return;
        }
        resendCounter = 0;
        resendTimer = scheduler.scheduleWithFixedDelay(new SendDiscoverRunnable(), 0, resendTimeoutInMillis,
                TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        // everytime user click on refresh Inbox > Souliss Binding > Search
        ConcurrentHashMap<Byte, Thing> gwMaps = SoulissBindingNetworkParameters.getHashTableGateways();
        Collection<Thing> gwMapsCollection = gwMaps.values();
        for (Thing t : gwMapsCollection) {
            SoulissGatewayHandler gw = (SoulissGatewayHandler) t.getHandler();
            SoulissCommonCommands.sendDBStructFrame(SoulissBindingNetworkParameters.getDatagramSocket(),
                    gw.IPAddressOnLAN, gw.nodeIndex, gw.userIndex);
        }
    }
}