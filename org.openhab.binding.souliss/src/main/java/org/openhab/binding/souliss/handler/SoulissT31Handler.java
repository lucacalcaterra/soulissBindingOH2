/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.souliss.handler;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.handler.SoulissGenericTypical.typicalCommonMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissT31Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 */
public class SoulissT31Handler extends SoulissGenericTypical implements typicalCommonMethods {

    Configuration gwConfigurationMap;
    private Logger logger = LoggerFactory.getLogger(SoulissT11Handler.class);

    public SoulissT31Handler(Thing _thing) {
        super(_thing);
        thing = _thing;
    }

    // called on every status change or change request
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof StringType) {
            switch (((StringType) command).toString()) {
                // FAN
                case "HIGH":
                    break;
                case "MEDIUM":
                    break;
                case "LOW":
                    break;
                case "AUTO":
                    break;
                // MODE
                case "HEAT":
                    break;
                case "COOL":
                    break;
                case "POWEREDOFF":
                    break;
            }
        } else if (command instanceof OnOffType) {
            // As Measured clicked
        } else if (command instanceof DecimalType) {
            // Setpoint setted

        }
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.

        updateStatus(ThingStatus.ONLINE);

        // gwConfigurationMap = thing.getConfiguration();

    }

    @Override
    public void setState(PrimitiveType _state) {

        this.setUpdateTimeNow();
        this.updateState(SoulissBindingConstants.LASTSTATUSSTORED_CHANNEL, this.getLastUpdateTime());

        // updateState(SoulissBindingConstants.SLEEP_CHANNEL, OnOffType.OFF);

        // if (((OnOffType) _state) != this.T1nState) {
        // this.updateState(SoulissBindingConstants.ONOFF_CHANNEL, (OnOffType) _state);
        // this.updateThing(this.thing);
        // this.T1nState = (OnOffType) _state;
        // }
    }
}
