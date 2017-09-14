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

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.handler.SoulissGatewayHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissDiscoverThread extends Thread {
    /**
     * Result callback interface.
     */
    public interface DiscoverResult {
        static boolean isGatewayDetected = false;
        Bridge bridge = null;

        void gatewayDetected(InetAddress addr, String id);

        void thingDetected_Typicals(byte lastByteGatewayIP, short typical, short node, short slot);

        void thingDetected_ActionMessages(String sTopicNumber, String sTopicVariant);

        ThingUID getGatewayUID();
    }

    // private boolean willbeclosed = false;
    private DatagramSocket datagramSocket;
    SoulissBindingUDPServerThread UDP_Server_OnDefaultPort = null;
    ///// Debug
    private Logger logger = LoggerFactory.getLogger(SoulissDiscoverThread.class);

    ///// Result and resend
    final private DiscoverResult discoverResult;

    final private int resendTimeoutInMillis;
    final private int resendAttempts;
    private long millis = 0;
    private int resendCounter = 0;
    private boolean doResend = true;
    private long millisStartThread;

    public SoulissDiscoverThread(DatagramSocket _datagramSocket, DiscoverResult discoverResult,
            int resendTimeoutInMillis, int resendAttempts) throws SocketException {
        this.resendAttempts = resendAttempts;
        this.resendTimeoutInMillis = resendTimeoutInMillis;
        datagramSocket = _datagramSocket;
        this.discoverResult = discoverResult;
    }

    @Override
    public void run() {
        while (doResend) {
            if (System.currentTimeMillis() - millis >= SoulissBindingConstants.DISCOVERY_resendTimeoutInMillis) {
                if (resendCounter++ >= resendAttempts || (System.currentTimeMillis()
                        - millisStartThread) >= SoulissBindingConstants.DISCOVERY_TimeoutInSeconds * 1000) {
                    doResend = false;
                }

                logger.debug("Sending discovery packet nr.{}", resendCounter);
                /**
                 * Used by the scheduler to resend discover messages. Stops after 3 attempts.
                 */
                try {
                    // ===============================================================================
                    // ===============================================================================
                    SoulissCommonCommands.sendBroadcastGatewayDiscover(datagramSocket);
                    // ===============================================================================
                    // ===============================================================================
                    logger.debug("Sent discovery packet");

                } catch (Exception e) {
                    logger.error("Sending a discovery packet failed. " + e.getLocalizedMessage());
                }

                // everytime user click on refresh Inbox > Souliss Binding > Search
                ConcurrentHashMap<Byte, Thing> gwMaps = SoulissBindingNetworkParameters.getHashTableGateways();
                Collection<Thing> gwMapsCollection = gwMaps.values();
                for (Thing t : gwMapsCollection) {
                    SoulissGatewayHandler gw = (SoulissGatewayHandler) t.getHandler();
                    logger.debug("Sending request to gateway for souliss network", resendCounter);
                    SoulissCommonCommands.sendDBStructFrame(SoulissBindingNetworkParameters.getDatagramSocket(),
                            gw.IPAddressOnLAN, gw.nodeIndex, gw.userIndex);
                }
                millis = System.currentTimeMillis();
            }
        }
    }

    public void scan() {
        millisStartThread = System.currentTimeMillis();
        this.run();

    }
}