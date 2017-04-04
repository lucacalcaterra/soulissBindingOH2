/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.internal.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.openhab.binding.souliss.internal.SoulissDatagramSocketFactory;
import org.openhab.binding.souliss.internal.network.typicals.SoulissTypicals;
import org.openhab.binding.souliss.internal.protocol.SoulissDiscover.DiscoverResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provide receive packet from network
 *
 * @author Alessandro Del Pex
 * @author Tonino Fazio
 * @since 1.7.0
 */
public class SoulissBindingUDPServerThread extends Thread {

    // protected DatagramSocket socket = null;
    protected BufferedReader in = null;
    protected boolean bExit = false;
    SoulissBindingUDPDecoder decoder = null;
    DiscoverResult discoverResult;
    static DatagramSocket soulissDatagramSocket;
    private static Logger logger = LoggerFactory.getLogger(SoulissBindingUDPServerThread.class);

    public SoulissBindingUDPServerThread(SoulissTypicals typicals, DiscoverResult discoverResultLoc) {
        super();

        soulissDatagramSocket = SoulissDatagramSocketFactory.getDatagram_for_broadcast();

        // if (SoulissBindingNetworkParameters.serverPort != null) {
        // SoulissBindingNetworkParameters.datagramsocket = new DatagramSocket(
        // SoulissBindingNetworkParameters.serverPort);
        // } else {
        // SoulissBindingNetworkParameters.datagramsocket = new DatagramSocket();
        // }
        // discoverResult = discoverResultLoc;
        decoder = new SoulissBindingUDPDecoder();
        logger.info("Start UDPServerThread - Server in ascolto sulla porta "
                + SoulissDatagramSocketFactory.getTrasmissionDatagram().getLocalPort());
    }

    @Override
    public void run() {

        while (true) {
            try {
                byte[] buf = new byte[256];

                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                SoulissDatagramSocketFactory.getTrasmissionDatagram().receive(packet);
                buf = packet.getData();

                // **************** DECODER ********************
                logger.debug("Packet received");
                logger.debug(MaCacoToString(buf));
                decoder.decodeVNetDatagram(packet, discoverResult);

            } catch (IOException e) {
                e.printStackTrace();
                logger.error(e.getMessage());
            }
        }
    }

    // public DatagramSocket getSocket() {
    // return SoulissBindingNetworkParameters.datagramsocket;
    // }

    public void closeSocket() {
        SoulissDatagramSocketFactory.doClose();
        bExit = true;
    }

    private String MaCacoToString(byte[] frame) {
        StringBuilder sb = new StringBuilder();
        sb.append("HEX: [");
        for (byte b : frame) {
            sb.append(String.format("%02X ", b));
        }
        sb.append("]");
        return sb.toString();
    }
}