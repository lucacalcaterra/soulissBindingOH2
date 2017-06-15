/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.internal.protocol;

import java.net.DatagramSocket;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.souliss.internal.protocol.SoulissDiscover.DiscoverResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contain parameter of Souliss Network.
 * Those are loaded at startup from SoulissBinding.updated(), from file openhab.cfg
 * and used by SoulissBinding.execute(), SoulissCommGate.send(), UDPServerThread, decodeDBStructRequest.decodeMacaco
 *
 * @author Tonino Fazio
 * @since 1.7.0
 */
public class SoulissBindingNetworkParameters {

    public static short defaultNodeIndex = 130;
    public static short defaultUserIndex = 70;
    public static int presetTime = 1000;
    public static int SEND_DELAY = presetTime;
    public static int SEND_MIN_DELAY = presetTime;
    public static long SECURE_SEND_TIMEOUT_TO_REQUEUE = presetTime;
    public static long SECURE_SEND_TIMEOUT_TO_REMOVE_PACKET = presetTime;
    private static Logger logger = LoggerFactory.getLogger(SoulissBindingNetworkParameters.class);

    private static ConcurrentHashMap<Byte, Thing> hashTableGateway = new ConcurrentHashMap<Byte, Thing>();
    private static DatagramSocket datagramSocketPort230;
    public static DiscoverResult discoverResult;

    public static DatagramSocket getDatagramSocket() {
        return datagramSocketPort230;
    }

    public static void closeDatagramSocket() {
        datagramSocketPort230.close();
        datagramSocketPort230 = null;
    }

    public static void setDatagramSocket(DatagramSocket datagramSocket) {
        SoulissBindingNetworkParameters.datagramSocketPort230 = datagramSocket;
    }

    public static void addGateway(byte lastByteGatewayIP, Thing thing) {
        hashTableGateway.put(lastByteGatewayIP, thing);
    }

    public static ConcurrentHashMap<Byte, Thing> getHashTableGateway() {
        return hashTableGateway;
    }

    public static Bridge getGateway(byte lastByteGatewayIP) {
        return (Bridge) hashTableGateway.get(lastByteGatewayIP);
    }

    public static void removeGateway(byte lastByteGatewayIP) {
        hashTableGateway.remove(lastByteGatewayIP);
    }
}
