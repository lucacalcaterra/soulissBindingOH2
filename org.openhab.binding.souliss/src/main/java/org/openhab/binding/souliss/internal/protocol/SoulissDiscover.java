/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.internal.protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Milight bridges can be discovered by sending specially formated UDP packets.
 * This class sends UDP packets on port PORT_SEND_DISCOVER up to three times in a row
 * and listens for the response and will call discoverResult.bridgeDetected() eventually.
 *
 * @author David Graeff - Initial contribution
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
    private byte[] buffer = new byte[1024];
    private DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
    private SoulissBindingUDPDecoder decoder = new SoulissBindingUDPDecoder();

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

        // costruire pacchetto
        datagramSocket = SoulissBindingNetworkParameters.initDatagramSocket_for_broadcast();
        datagramSocket.setBroadcast(true);

        this.discoverResult = discoverResult;
    }

    public void stopReceiving() {
        willbeclosed = true;
        try {
            join(500);
        } catch (InterruptedException e) {
        }
        interrupt();
        if (datagramSocket != null) {
            datagramSocket.close();
            datagramSocket = null;
        }
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

        public SendDiscoverRunnable(DatagramSocket datagramSocket) {
            this.datagramSocket = datagramSocket;
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
        resendTimer = scheduler.scheduleWithFixedDelay(new SendDiscoverRunnable(datagramSocket), 0,
                resendTimeoutInMillis, TimeUnit.SECONDS);
    }

    boolean bGateway_Detected = false;

    @Override
    public void run() {
        try {
            logger.debug("Discovery receive thread ready");

            // Now loop forever, waiting to receive packets and printing them.
            while (!willbeclosed) {
                discoverResult.setGatewayUndetected();
                datagramSocket.receive(packet);
                // return bGateway_Detected TRUE if gateway detected
                decoder.decodeVNetDatagram(packet, discoverResult);

                if (discoverResult.isGatewayDetected()) {
                    // Stop resend timer if we got a packet.
                    if (resendTimer != null) {
                        resendTimer.cancel(true);
                        resendTimer = null;
                    }
                }

                // Reset the length of the packet before reusing it.
                packet.setLength(buffer.length);
            }
        } catch (IOException e) {
            if (willbeclosed) {
                return;
            }
            logger.error(e.getLocalizedMessage());
        }
    }
}