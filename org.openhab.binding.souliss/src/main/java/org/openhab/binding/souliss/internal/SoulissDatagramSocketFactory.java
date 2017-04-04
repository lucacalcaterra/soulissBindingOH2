/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.internal;

import java.net.DatagramSocket;
import java.net.SocketException;

import org.openhab.binding.souliss.SoulissBindingUDPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissDatagramSocketFactory} is responsible for creating datagramSocket object for trasmission e
 * receiving.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissDatagramSocketFactory {
    static DatagramSocket soulissDatagramSocket;

    public static Integer serverPort;
    private static Logger logger = LoggerFactory.getLogger(SoulissDatagramSocketFactory.class);

    public static DatagramSocket getTrasmissionDatagram() {
        // return DatagramSocket for packet trasmission

        if (soulissDatagramSocket == null) {
            try {
                if (serverPort != null) {
                    soulissDatagramSocket = new DatagramSocket(serverPort);
                } else {
                    soulissDatagramSocket = new DatagramSocket();
                }
                logger.debug("Trasmission Socket Created on port " + soulissDatagramSocket.getLocalPort());
            } catch (SocketException e) {
                logger.error("Error on creation of Trasmission Socket");
                logger.error(e.getMessage());
            }
        }
        return soulissDatagramSocket;
    }

    public static DatagramSocket getDatagram_for_broadcast() {
        if (soulissDatagramSocket == null) {
            try {
                soulissDatagramSocket = new DatagramSocket(SoulissBindingUDPConstants.SOULISS_GATEWAY_DEFAULT_PORT);
                soulissDatagramSocket.setBroadcast(true);
                logger.debug("Trasmission Socket Created on port (Souliss Default Port) "
                        + soulissDatagramSocket.getLocalPort());
            } catch (SocketException e) {
                logger.error("Error on creation of Trasmission Socket");
                logger.error(e.getMessage());
            }
        }
        return soulissDatagramSocket;
    }

    public static void doClose() {
        soulissDatagramSocket.close();
        soulissDatagramSocket = null;
    }
}