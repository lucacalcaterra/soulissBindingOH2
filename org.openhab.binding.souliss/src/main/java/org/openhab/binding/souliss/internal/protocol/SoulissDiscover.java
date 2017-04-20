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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.openhab.binding.souliss.internal.SoulissDatagramSocketFactory;
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

        void gatewayDetected();

        boolean isGatewayDetected();

        void setGatewayDetected();

        void setGatewayUndetected();

        void dbStructAnswerReceived();

        void thingDetected(short typical, short node, short slot);

        Bridge getGateway();
    }

    private boolean willbeclosed = false;
    private DatagramSocket datagramSocket_port230;
    SoulissBindingUDPServerThread UDP_Server_port230 = null;
    ///// Debug
    private Logger logger = LoggerFactory.getLogger(SoulissDiscover.class);

    ///// Result and resend
    final private DiscoverResult discoverResult;
    private int resendCounter = 0;
    private ScheduledFuture<?> resendTimer;
    final private int resendTimeoutInMillis;
    final private int resendAttempts;

    public SoulissDiscover(DiscoverResult discoverResult, int resendTimeoutInMillis, int resendAttempts)
            throws SocketException {
        this.resendAttempts = resendAttempts;
        this.resendTimeoutInMillis = resendTimeoutInMillis;

        this.discoverResult = discoverResult;
    }

    public void stopReceiving() {
        willbeclosed = true;

        // UDP_Server.closeSocket();
        // UDP_Server = null;

        try {
            join(500);
        } catch (InterruptedException e) {
        }
        interrupt();
    }

    public void stopResend() {
        if (resendTimer != null) {
            resendTimer.cancel(false);
            resendTimer = null;
        }
        // if (datagramSocket_port230 != null) {
        // datagramSocket_port230.close();
        // datagramSocket_port230 = null;
        // }
    }

    /**
     * Used by the scheduler to resend discover messages. Stops after 3 attempts.
     */
    private class SendDiscoverRunnable implements Runnable {
        DatagramSocket datagramSocket_port230;

        public SendDiscoverRunnable() {
            // costruire pacchetto
            datagramSocket_port230 = SoulissDatagramSocketFactory.getDatagram_for_broadcast();
        }

        @Override
        public void run() {
            try {
                SoulissCommonCommands.sendBroadcastGatewayDiscover(datagramSocket_port230);
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
        logger.debug("Discovery receive thread ready");

        // Now loop forever, waiting to receive packets and printing them.
        while (!willbeclosed) {
            discoverResult.setGatewayUndetected();

            if (UDP_Server_port230 == null) {
                // logger.info("UDP_Server start");
                // SoulissTypicals SoulissTypicalsRecipients = new SoulissTypicals();
                datagramSocket_port230 = SoulissDatagramSocketFactory.getDatagram_for_broadcast();
                UDP_Server_port230 = new SoulissBindingUDPServerThread(datagramSocket_port230, discoverResult);
                UDP_Server_port230.start();

            }

            // datagramSocket.receive(packet);
            // return bGateway_Detected TRUE if gateway detected
            // decoder.decodeVNetDatagram(packet,discoverResult);

            if (discoverResult.isGatewayDetected()) {
                // Stop resend timer if we got a packet.
                if (resendTimer != null) {
                    resendTimer.cancel(true);
                    resendTimer = null;
                }
            }
        }
    }
}