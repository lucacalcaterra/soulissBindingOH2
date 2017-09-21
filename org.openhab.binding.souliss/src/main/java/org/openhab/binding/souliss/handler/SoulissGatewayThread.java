/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingNetworkParameters;
import org.openhab.binding.souliss.internal.protocol.SoulissCommonCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissGatewayThread extends Thread {

    private Logger logger = LoggerFactory.getLogger(SoulissGatewayThread.class);
    private String _iPAddressOnLAN;
    private short _userIndex;
    private short _nodeIndex;
    private int _nodes;
    private long millis;
    private int _pingRefreshInterval;
    private int _subscriptionRefreshInterval;
    private String _gwID;
    private SoulissGatewayHandler gw;

    public SoulissGatewayThread(Bridge bridge) {
        gw = (SoulissGatewayHandler) bridge.getHandler();
        _iPAddressOnLAN = gw.IPAddressOnLAN;
        _userIndex = gw.userIndex;
        _nodeIndex = gw.nodeIndex;
        _nodes = gw.nodes;
        _pingRefreshInterval = gw.pingRefreshInterval;
        _subscriptionRefreshInterval = gw.subscriptionRefreshInterval;
        _gwID = gw.getThing().getUID().getAsString();

    }

    @Override
    public void run() {
        long actualmillis;
        while (true) {
            actualmillis = System.currentTimeMillis();
            // PING - refresh Interval in seconds
            if (actualmillis - millis >= _pingRefreshInterval * 1000) {
                sendPing();

                gw.pingSent();

            }

            // SUBSCRIPTION - Value in minutes
            if (actualmillis - millis >= _subscriptionRefreshInterval * 1000 * 60) {
                sendSubscription();
            }

            millis = System.currentTimeMillis();
        }

    }

    private void sendPing() {
        logger.debug("Sending ping packet");
        if (_iPAddressOnLAN.length() > 0) {
            SoulissCommonCommands.sendPing(SoulissBindingNetworkParameters.getDatagramSocket(), _iPAddressOnLAN,
                    _nodeIndex, _userIndex, (byte) 0, (byte) 0);
            logger.debug("Sent ping packet");
        }
    }

    private void sendSubscription() {
        logger.debug("Sending subscription packet");
        if (_iPAddressOnLAN.length() > 0) {
            SoulissCommonCommands.sendSUBSCRIPTIONframe(SoulissBindingNetworkParameters.getDatagramSocket(),
                    _iPAddressOnLAN, _nodeIndex, _userIndex, _nodes);
        }
        logger.debug("Sent subscription packet");
    }
}