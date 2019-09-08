/**
 * Copyright (c) 2014-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.handler;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.souliss.SoulissBindingConstants;

/**
 * The {@link SoulissT1AHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 */
public class SoulissT1AHandler extends SoulissGenericHandler {
    Configuration gwConfigurationMap;
    // private Logger logger = LoggerFactory.getLogger(SoulissT1AHandler.class);
    byte T1nRawState;

    public SoulissT1AHandler(Thing _thing) {
        super(_thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    private OnOffType getTypeFromBool(boolean value) {

        if (value == false) {
            return OnOffType.OFF;
        } else {
            return OnOffType.ON;
        }
    }

    private boolean getBitState(int value, int bit) {

        if ((value & (1L << bit)) == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void setRawState(byte _rawState) {
        T1nRawState = _rawState;
        // update Last Status stored time
        super.setLastStatusStored();
        // update item state only if it is different from previous
        if (T1nRawState != _rawState) {
            this.updateState(SoulissBindingConstants.T1A_1_CHANNEL, getTypeFromBool(getBitState(T1nRawState, 0)));
            this.updateState(SoulissBindingConstants.T1A_2_CHANNEL, getTypeFromBool(getBitState(T1nRawState, 1)));
            this.updateState(SoulissBindingConstants.T1A_3_CHANNEL, getTypeFromBool(getBitState(T1nRawState, 2)));
            this.updateState(SoulissBindingConstants.T1A_4_CHANNEL, getTypeFromBool(getBitState(T1nRawState, 3)));
            this.updateState(SoulissBindingConstants.T1A_5_CHANNEL, getTypeFromBool(getBitState(T1nRawState, 4)));
            this.updateState(SoulissBindingConstants.T1A_6_CHANNEL, getTypeFromBool(getBitState(T1nRawState, 5)));
            this.updateState(SoulissBindingConstants.T1A_7_CHANNEL, getTypeFromBool(getBitState(T1nRawState, 6)));
            this.updateState(SoulissBindingConstants.T1A_8_CHANNEL, getTypeFromBool(getBitState(T1nRawState, 7)));
        }
    }

    @Override
    public byte getRawState() {
        return T1nRawState;
    }

    @Override
    public byte getExpectedRawState(byte bCommand) {
        // Secure Send is disabled
        return -1;
    }
}
