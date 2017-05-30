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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.binding.souliss.handler.SoulissGenericTypical.typicalCommonMethods;
import org.openhab.binding.souliss.internal.HalfFloatUtils;
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

        if (command instanceof RefreshType) {

        } else if (command instanceof PrimitiveType) {
            switch ((command).toString()) {
                // FAN
                case "HIGH":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanHigh);
                    break;
                case "MEDIUM":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanMed);
                    break;
                case "LOW":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanLow);
                    break;
                case "AUTO":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanAuto);
                    break;
                case "OFF":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanOff);
                    break;
                // MODE
                case "HEAT":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_Heating);
                    break;
                case "COOL":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_Cooling);
                    break;
                case "POWEREDOFF":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_ShutDown);
                    break;
            }
        } else if (command instanceof OnOffType) {
            commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_AsMeasured);
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

        /*
         * the control state bit meaning follow as:
         * BIT 0 Not used
         * BIT 1 (0 Heating OFF, 1 Heating ON)
         * BIT 2 (0 Cooling OFF, 1 Cooling ON)
         * BIT 3 (0 Fan 1 OFF, 1 Fan 1 ON)
         * BIT 4 (0 Fan 2 OFF, 1 Fan 2 ON)
         * BIT 5 (0 Fan 3 OFF, 1 Fan 3 ON)
         * BIT 6 (0 Manual Mode, 1 Automatic Mode for Fan)
         * BIT 7 (0 Heating mode, 1 Cooling Mode
         */

        // updateState(SoulissBindingConstants.SLEEP_CHANNEL, OnOffType.OFF);

        // if (((OnOffType) _state) != this.T1nState) {
        // this.updateState(SoulissBindingConstants.ONOFF_CHANNEL, (OnOffType) _state);
        // this.updateThing(this.thing);
        // this.T1nState = (OnOffType) _state;
        // }
    }

}
