/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.handler;

import java.math.BigDecimal;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.binding.souliss.handler.SoulissGenericTypical.typicalCommonMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissT16Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissT16Handler extends SoulissGenericTypical implements typicalCommonMethods {
    Configuration gwConfigurationMap;
    private Logger logger = LoggerFactory.getLogger(SoulissT16Handler.class);
    OnOffType T1nState = OnOffType.OFF;
    short xSleepTime = 0;
    // Thing thing;
    short stateRED;
    short stateGREEN;
    short stateBLU;
    private int dimmerValue;

    public SoulissT16Handler(Thing _thing) {
        super(_thing);
        thing = _thing;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ONOFF_CHANNEL:
                    updateState(channelUID, T1nState);
                    break;
                case SoulissBindingConstants.LASTSTATUSSTORED_CHANNEL:
                    if (this.getLastUpdateTime() != null) {
                        updateState(channelUID, this.getLastUpdateTime());
                    }
                    break;
            }
        } else {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ONOFF_CHANNEL:
                    if (command instanceof OnOffType) {
                        if (command.equals(OnOffType.ON)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T1n_OnCmd);

                        } else if (command.equals(OnOffType.OFF)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T1n_OffCmd);
                        }
                    }
                    break;
                case SoulissBindingConstants.WHITE_MODE_CHANNEL:
                    if (command instanceof OnOffType) {
                        stateBLU = 255;
                        stateGREEN = 255;
                        stateRED = 255;
                        commandSEND_RGB(SoulissBindingProtocolConstants.Souliss_T1n_Set, stateRED, stateGREEN,
                                stateBLU);
                    }
                    break;
                case SoulissBindingConstants.SLEEP_CHANNEL:
                    if (command instanceof OnOffType) {
                        if (command.equals(OnOffType.ON)) {
                            commandSEND((short) (SoulissBindingProtocolConstants.Souliss_T1n_Timed + xSleepTime));
                            // set Off
                            updateState(channelUID, OnOffType.OFF);
                        }
                    }
                    break;

                case SoulissBindingConstants.DIMMER_BRIGHTNESS_CHANNEL:
                    if (command instanceof PercentType) {
                        dimmerValue = ((PercentType) command).intValue();
                        commandSEND_RGB(SoulissBindingProtocolConstants.Souliss_T1n_Set, percent(dimmerValue, stateRED),
                                percent(dimmerValue, stateGREEN), percent(dimmerValue, stateBLU));

                    } else if (command instanceof OnOffType) {
                        if (command.equals(OnOffType.ON)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T1n_OnCmd);

                        } else if (command.equals(OnOffType.OFF)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T1n_OffCmd);
                        }
                    }
                    break;

                case SoulissBindingConstants.ROLLER_BRIGHTNESS_CHANNEL:
                    if (command instanceof UpDownType) {
                        if (command.equals(UpDownType.UP)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T1n_BrightUp);
                        } else if (command.equals(UpDownType.DOWN)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T1n_BrightDown);
                        }
                    }
                    break;

                case SoulissBindingConstants.LED_COLOR_CHANNEL:
                    if (command instanceof HSBType) {
                        HSBType hsb = (HSBType) command;
                        stateBLU = hsb.getBlue().shortValue();
                        stateGREEN = hsb.getGreen().shortValue();
                        stateRED = hsb.getRed().shortValue();
                        commandSEND_RGB(SoulissBindingProtocolConstants.Souliss_T1n_Set, stateRED, stateGREEN,
                                stateBLU);
                    }
                    break;

            }
        }

    }

    short percent(int percentValue, int value) {
        return (short) ((float) percentValue / 100 * value);
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.

        updateStatus(ThingStatus.ONLINE);

        gwConfigurationMap = thing.getConfiguration();

        if (gwConfigurationMap.get(SoulissBindingConstants.SLEEP_CHANNEL) != null) {
            xSleepTime = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.SLEEP_CHANNEL)).shortValue();
        }
        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @Override
    public void setState(PrimitiveType _state) {

        this.setUpdateTimeNow();
        this.updateState(SoulissBindingConstants.LASTSTATUSSTORED_CHANNEL, this.getLastUpdateTime());

        // if (thing.getThingTypeUID().equals(SoulissBindingConstants.T11_THING_TYPE)) {
        updateState(SoulissBindingConstants.SLEEP_CHANNEL, OnOffType.OFF);
        // }

        if (((OnOffType) _state) != this.T1nState) {
            this.updateState(SoulissBindingConstants.ONOFF_CHANNEL, (OnOffType) _state);
            this.updateThing(this.thing);
            this.T1nState = (OnOffType) _state;
        }

    }

}
