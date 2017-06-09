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
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.binding.souliss.handler.SoulissGenericTypical.typicalCommonMethods;
import org.openhab.binding.souliss.internal.HalfFloatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissT41Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 */
public class SoulissT41Handler extends SoulissGenericTypical implements typicalCommonMethods {

    Configuration gwConfigurationMap;

    private Logger logger = LoggerFactory.getLogger(SoulissT11Handler.class);

    public SoulissT41Handler(Thing _thing) {
        super(_thing);
        thing = _thing;
    }

    // called on every status change or change request
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {

        } else if (command instanceof StringType) {

            commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_ShutDown);

        } else if (command instanceof OnOffType) {
            if (command.equals(OnOffType.ON)) {
                commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_AsMeasured);
            }

        } else if (command instanceof DecimalType) {

            int uu = HalfFloatUtils.fromFloat(((DecimalType) command).floatValue());
            byte B2 = (byte) (uu >> 8);
            byte B1 = (byte) uu;
            // setpoint command
            commandSEND(SoulissBindingProtocolConstants.Souliss_T31_Use_Of_Slot_SETPOINT_COMMAND, B1, B2);
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

        this.updateState(SoulissBindingConstants.T31_BUTTON_CHANNEL, OnOffType.OFF);

        this.setUpdateTimeNow();
        this.updateState(SoulissBindingConstants.LASTSTATUSSTORED_CHANNEL, this.getLastUpdateTime());
        if (_state instanceof DecimalType) {
            this.updateState(SoulissBindingConstants.T31_SETPOINT_CHANNEL, (DecimalType) _state);
        } else if (_state instanceof StringType) {

            this.updateState(SoulissBindingConstants.T31_CONDITIONING_CHANNEL, OnOffType.ON);

        }
        this.updateThing(this.thing);
    }

}
