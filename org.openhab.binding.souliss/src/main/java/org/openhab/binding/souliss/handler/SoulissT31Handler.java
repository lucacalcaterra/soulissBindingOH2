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
 * The {@link SoulissT31Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 */
public class SoulissT31Handler extends SoulissGenericTypical implements typicalCommonMethods {

    Configuration gwConfigurationMap;
    DecimalType _setPointValue = DecimalType.ZERO;
    StringType _fanStateValue = StringType.EMPTY;
    StringType _powerState = StringType.EMPTY;
    StringType _lastModeState = StringType.EMPTY;
    StringType _modeStateValue = StringType.EMPTY;
    private Logger logger = LoggerFactory.getLogger(SoulissT11Handler.class);

    public SoulissT31Handler(Thing _thing) {
        super(_thing);
        thing = _thing;
    }

    // called on every status change or change request
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (command instanceof RefreshType) {

        } else if (command instanceof StringType) {
            switch ((command).toString()) {
                // FAN
                case "HIGH":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanManual);
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanHigh);
                    _fanStateValue = StringType.valueOf("HIGH");
                    break;
                case "MEDIUM":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanManual);
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanMed);
                    _fanStateValue = StringType.valueOf("MEDIUM");
                    break;
                case "LOW":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanManual);
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanLow);
                    _fanStateValue = StringType.valueOf("LOW");
                    break;
                case "AUTO":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanAuto);
                    _fanStateValue = StringType.valueOf("AUTO");
                    break;
                case "OFF":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanOff);
                    _fanStateValue = StringType.valueOf("FANOFF");
                    break;
                // MODE
                case "HEATING_MODE":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_Heating);
                    _lastModeState = StringType.valueOf("HEAT");
                    break;
                case "COOLING_MODE":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_Cooling);
                    _lastModeState = StringType.valueOf("COOL");
                    break;
                case "POWEREDOFF":
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_ShutDown);
                    _lastModeState = StringType.valueOf("POWEREDOFF");
                    break;
            }
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
            if (!_setPointValue.equals(_state)) {
                this.updateState(SoulissBindingConstants.T31_SETPOINT_CHANNEL, (DecimalType) _state);
                _setPointValue = (DecimalType) _state;
            }
        } else if (_state instanceof StringType) {
            switch (_state.toString()) {
                case "LOW":
                    if (!_fanStateValue.equals(_state)) {
                        this.updateState(SoulissBindingConstants.T31_FAN_CHANNEL, (StringType) _state);
                        _fanStateValue = (StringType) _state;
                    }
                    break;
                case "MEDIUM":
                    if (!_fanStateValue.equals(_state)) {
                        this.updateState(SoulissBindingConstants.T31_FAN_CHANNEL, (StringType) _state);
                        _fanStateValue = (StringType) _state;
                    }
                    break;
                case "HIGH":
                    if (!_fanStateValue.equals(_state)) {
                        this.updateState(SoulissBindingConstants.T31_FAN_CHANNEL, (StringType) _state);
                        _fanStateValue = (StringType) _state;
                    }
                    break;
                case "AUTO":
                    if (!_fanStateValue.equals(_state)) {
                        this.updateState(SoulissBindingConstants.T31_FAN_CHANNEL, (StringType) _state);
                        _fanStateValue = (StringType) _state;
                    }
                    break;
                case "FANOFF":
                    if (!_fanStateValue.equals(_state)) {
                        this.updateState(SoulissBindingConstants.T31_FAN_CHANNEL, (StringType) _state);
                        _fanStateValue = (StringType) _state;
                    }
                case "HEATING_MODE":
                    if (!_modeStateValue.equals(_state)) {
                        this.updateState(SoulissBindingConstants.T31_MODE_CHANNEL, (StringType) _state);
                        _modeStateValue = (StringType) _state;
                    }
                    break;
                case "COOLING_MODE":
                    if (!_modeStateValue.equals(_state)) {
                        this.updateState(SoulissBindingConstants.T31_MODE_CHANNEL, (StringType) _state);
                        _modeStateValue = (StringType) _state;
                    }
                    break;
                case "POWEROFF":
                    if (!_powerState.equals(StringType.valueOf("POWEROFF"))) {
                        _lastModeState = _modeStateValue;
                        this.updateState(SoulissBindingConstants.T31_CONDITIONING_CHANNEL, OnOffType.OFF);
                        _modeStateValue = StringType.valueOf("POWEROFF");
                    }
                    break;
                case "POWERON":
                    if (!_powerState.equals(StringType.valueOf("POWERON"))) {
                        this.updateState(SoulissBindingConstants.T31_CONDITIONING_CHANNEL, OnOffType.ON);
                        _modeStateValue = StringType.valueOf("POWERON");
                    }
                    break;
            }
        }
        this.updateThing(this.thing);
    }

}
