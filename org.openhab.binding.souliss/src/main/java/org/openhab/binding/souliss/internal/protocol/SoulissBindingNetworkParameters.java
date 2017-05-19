/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.internal.protocol;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
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
    public static int nodes;
    public static int maxnodes;
    public static int maxTypicalXnode;
    public static int maxrequests;
    public static int MaCacoIN_s;
    public static int MaCacoTYP_s;
    public static int MaCacoOUT_s;
    static Properties prop = new Properties();
    public static int presetTime = 1000;
    public static int REFRESH_DBSTRUCT_TIME = presetTime;
    public static int REFRESH_SUBSCRIPTION_TIME = presetTime;
    public static int REFRESH_HEALTY_TIME = presetTime;
    public static int REFRESH_MONITOR_TIME = presetTime;
    public static int SEND_DELAY = presetTime;
    public static int SEND_MIN_DELAY = presetTime;
    public static long SECURE_SEND_TIMEOUT_TO_REQUEUE = presetTime;
    public static long SECURE_SEND_TIMEOUT_TO_REMOVE_PACKET = presetTime;
    private static Logger logger = LoggerFactory.getLogger(SoulissBindingNetworkParameters.class);

    private static ConcurrentHashMap<Byte, Thing> hashTableGateway = new ConcurrentHashMap<Byte, Thing>();

    public static void addGateway(byte lastByteGatewayIP, Thing thing) {
        hashTableGateway.put(lastByteGatewayIP, thing);
    }

    public static Bridge getGateway(byte lastByteGatewayIP) {
        return (Bridge) hashTableGateway.get(lastByteGatewayIP);
    }

    public static void removeGateway(byte lastByteGatewayIP) {
        hashTableGateway.remove(lastByteGatewayIP);
    }
}
