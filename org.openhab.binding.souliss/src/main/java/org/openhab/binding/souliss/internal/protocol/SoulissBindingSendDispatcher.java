/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.internal.protocol;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import org.eclipse.smarthome.core.thing.Bridge;
import org.openhab.binding.souliss.handler.SoulissGatewayHandler;
import org.openhab.binding.souliss.handler.SoulissGatewayJobHealty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provide to take packet, and send it to regular interval to Souliss
 * Network
 *
 * @author Tonino Fazio
 * @since 1.7.0
 */
public class SoulissBindingSendDispatcher implements Runnable {

    private Logger logger = LoggerFactory.getLogger(SoulissGatewayJobHealty.class);
    // private String _iPAddressOnLAN;
    // private short _userIndex;
    // private short _nodeIndex;
    private int _refreshInterval;
    private SoulissGatewayHandler gw;

    public SoulissBindingSendDispatcher(Bridge bridge) {
        gw = (SoulissGatewayHandler) bridge.getHandler();
        // _iPAddressOnLAN = gw.IPAddressOnLAN;
        // _userIndex = gw.userIndex;
        // _nodeIndex = gw.nodeIndex;
        set_refreshInterval(gw.healthRefreshInterval);
    }

    public static void put(DatagramSocket socket, DatagramPacket packet) {
        // TODO Auto-generated method stub

    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

    }

    public int get_refreshInterval() {
        return _refreshInterval;
    }

    public void set_refreshInterval(int _refreshInterval) {
        this._refreshInterval = _refreshInterval;
    }

}
