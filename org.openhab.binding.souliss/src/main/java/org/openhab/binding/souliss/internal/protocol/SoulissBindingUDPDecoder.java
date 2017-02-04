/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.internal.protocol;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.openhab.binding.souliss.SoulissBindingUDPConstants;
import org.openhab.binding.souliss.internal.protocol.SoulissDiscover.DiscoverResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class decodes incoming Souliss packets, starting from decodevNet
 *
 * @author Alessandro Del Pex
 * @author Tonino Fazio
 * @since 1.7.0
 */
public class SoulissBindingUDPDecoder {

    // private SoulissTypicals soulissTypicalsRecipients;
    private static Logger logger = LoggerFactory.getLogger(SoulissBindingUDPDecoder.class);
    private DiscoverResult discoverResult = null;

    // public SoulissBindingUDPDecoder(SoulissTypicals typicals) {
    // soulissTypicalsRecipients = typicals;
    // }

    /**
     * Get packet from VNET Frame
     *
     * @param packet
     *            incoming datagram
     */
    public void decodeVNetDatagram(DatagramPacket packet) {
        int checklen = packet.getLength();
        ArrayList<Short> mac = new ArrayList<Short>();
        for (int ig = 7; ig < checklen; ig++) {
            mac.add((short) (packet.getData()[ig] & 0xFF));
        }
        decodeMacaco(mac);
    }

    /**
     * Get packet from VNET Frame
     *
     * @param packet
     *            incoming datagram
     * @param discoverResult
     *            only for discovery packet
     */
    public void decodeVNetDatagram(DatagramPacket packet, DiscoverResult discoverResult) {
        this.discoverResult = discoverResult;
        decodeVNetDatagram(packet);
    }

    /**
     * Decodes lower level MaCaCo packet
     *
     * @param macacoPck
     */
    private void decodeMacaco(ArrayList<Short> macacoPck) {
        int functionalCode = macacoPck.get(0);
        logger.debug("decodeMacaco: Received functional code: 0x" + Integer.toHexString(functionalCode));
        switch (functionalCode) {

            case SoulissBindingUDPConstants.Souliss_UDP_function_ping_resp:
                logger.debug("function_ping_resp");
                decodePing(macacoPck);
                break;
            case SoulissBindingUDPConstants.Souliss_UDP_function_discover_GW_node_bcas_resp:
                logger.debug("function_ping_broadcast_resp");
                try {
                    discoverResult = decodePingBroadcast(macacoPck);
                } catch (UnknownHostException e) {
                    logger.debug("Error: {}", e.getLocalizedMessage());
                    e.printStackTrace();
                }
                break;

            // case (byte) SoulissBindingUDPConstants.Souliss_UDP_function_subscribe_resp:
            // case (byte) SoulissBindingUDPConstants.Souliss_UDP_function_poll_resp:
            // logger.debug("Souliss_UDP_function_subscribe_resp / Souliss_UDP_function_poll_resp");
            // decodeStateRequest(macacoPck);
            // break;

            // case SoulissBindingUDPConstants.Souliss_UDP_function_typreq_resp:// Answer for
            // assigned
            // // typical logic
            // logger.debug("** TypReq answer");
            // decodeTypRequest(macacoPck);
            // break;
            // case (byte) ConstantsUDP.Souliss_UDP_function_health_resp:// Answer
            // // nodes
            // // healty
            // logger.debug("function_health_resp");
            // decodeHealthRequest(macacoPck);
            // break;
            // case (byte) ConstantsUDP.Souliss_UDP_function_db_struct_resp:// Answer
            // // nodes
            // logger.debug("function_db_struct_resp");
            // decodeDBStructRequest(macacoPck);
            // break;
            // case 0x83:
            // logger.debug("Functional code not supported");
            // break;
            // case 0x84:
            // logger.debug("Data out of range");
            // break;
            // case 0x85:
            // logger.debug("Subscription refused");
            // break;
            // default:
            // logger.debug("Unknown functional code");
            // break;
        }
    }

    // /**
    // * @param mac
    // */
    private void decodePing(ArrayList<Short> mac) {
        int putIn_1 = mac.get(1);
        int putIn_2 = mac.get(2);
        logger.debug("decodePing: putIn code: {}, {}", putIn_1, putIn_2);
    }

    private DiscoverResult decodePingBroadcast(ArrayList<Short> mac) throws UnknownHostException {
        String IP = mac.get(5) + "." + mac.get(6) + "." + mac.get(7) + "." + mac.get(8);
        byte[] addr = { new Short(mac.get(5)).byteValue(), new Short(mac.get(6)).byteValue(),
                new Short(mac.get(7)).byteValue(), new Short(mac.get(8)).byteValue() };
        logger.debug("decodePingBroadcast. Gateway Discovery. IP: {}", IP);
        discoverResult.gatewayDetected(InetAddress.getByAddress(addr), "0");
        return discoverResult;
    }

    /**
     * Sovrascrive la struttura I nodi e la struttura dei tipici e richiama
     * UDPHelper.typicalRequest(opzioni, nodes, 0);
     *
     * @param mac
     */
    private void decodeDBStructRequest(ArrayList<Short> mac) {
        try {
            int nodes = mac.get(5);
            int maxnodes = mac.get(6);
            int maxTypicalXnode = mac.get(7);
            int maxrequests = mac.get(8);
            int MaCaco_IN_S = mac.get(9);
            int MaCaco_TYP_S = mac.get(10);
            int MaCaco_OUT_S = mac.get(11);

            SoulissBindingNetworkParameters.nodes = nodes;
            SoulissBindingNetworkParameters.maxnodes = maxnodes;
            SoulissBindingNetworkParameters.maxTypicalXnode = maxTypicalXnode;
            SoulissBindingNetworkParameters.maxrequests = maxrequests;
            SoulissBindingNetworkParameters.MaCacoIN_s = MaCaco_IN_S;
            SoulissBindingNetworkParameters.MaCacoTYP_s = MaCaco_TYP_S;
            SoulissBindingNetworkParameters.MaCacoOUT_s = MaCaco_OUT_S;

            logger.debug("decodeDBStructRequest");
            logger.debug("Nodes: " + nodes);
            logger.debug("maxnodes: " + maxnodes);
            logger.debug("maxTypicalXnode: " + maxTypicalXnode);
            logger.debug("maxrequests: " + maxrequests);
            logger.debug("MaCaco_IN_S: " + MaCaco_IN_S);
            logger.debug("MaCaco_TYP_S: " + MaCaco_TYP_S);
            logger.debug("MaCaco_OUT_S: " + MaCaco_OUT_S);

        } catch (Exception e) {
            logger.error("decodeDBStructRequest: SoulissNetworkParameter update ERROR");
        }
    }

}
