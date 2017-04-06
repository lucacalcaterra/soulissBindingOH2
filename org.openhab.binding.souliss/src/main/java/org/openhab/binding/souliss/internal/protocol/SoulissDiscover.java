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

import org.openhab.binding.souliss.internal.SoulissDatagramSocketFactory;
import org.openhab.binding.souliss.internal.network.typicals.SoulissTypicals;
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

        void gatewayDetected(InetAddress addr, String id);

        void noBridgeDetected();

        boolean isGatewayDetected();

        void setGatewayDetected();

        void setGatewayUndetected();
    }

    ///// Network
    // private byte[] discoverbuffer = "Link_Wi-Fi".getBytes();
    // final private DatagramPacket discoverPacket;
    private boolean willbeclosed = false;
    private DatagramSocket datagramSocket;

    ///// Debug
    private Logger logger = LoggerFactory.getLogger(SoulissDiscover.class);

    ///// Result and resend
    final private DiscoverResult discoverResult;
    private int resendCounter = 0;
    private ScheduledFuture<?> resendTimer;
    final private int resendTimeoutInMillis;
    final private int resendAttempts;

    SoulissBindingUDPServerThread UDP_Server = null;

    public SoulissDiscover(DiscoverResult discoverResult, int resendTimeoutInMillis, int resendAttempts)
            throws SocketException {
        this.resendAttempts = resendAttempts;
        this.resendTimeoutInMillis = resendTimeoutInMillis;

        this.discoverResult = discoverResult;
    }

    public void stopReceiving() {
        willbeclosed = true;

        UDP_Server.closeSocket();
        UDP_Server = null;

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
        if (datagramSocket != null) {
            datagramSocket.close();
            datagramSocket = null;
        }
    }

    /**
     * Used by the scheduler to resend discover messages. Stops after 3 attempts.
     */
    private class SendDiscoverRunnable implements Runnable {
        DatagramSocket datagramSocket;

        public SendDiscoverRunnable() {
            // costruire pacchetto
            datagramSocket = SoulissDatagramSocketFactory.getDatagram_for_broadcast();
        }

        @Override
        public void run() {
            try {
                SoulissCommonCommands.sendBroadcastGatewayDiscover(datagramSocket);
                logger.debug("Sent discovery packet");

                if (++resendCounter > resendAttempts) {
                    if (resendTimer != null) {
                        resendTimer.cancel(false);
                        resendTimer = null;
                    }
                    discoverResult.noBridgeDetected();
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

            if (UDP_Server == null) {
                logger.info("UDP_Server start");
                SoulissTypicals SoulissTypicalsRecipients = new SoulissTypicals();
                UDP_Server = new SoulissBindingUDPServerThread(SoulissTypicalsRecipients, discoverResult);
                UDP_Server.start();

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

            // Reset the length of the packet before reusing it.
            // packet.setLength(buffer.length);
        }
    }
}