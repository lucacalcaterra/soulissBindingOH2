/**
 * Copyright (c) 2014-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.souliss.handler;

import java.util.ArrayList;

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
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.binding.souliss.handler.SoulissGenericHandler.typicalCommonMethods;
import org.openhab.binding.souliss.internal.HalfFloatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * The {@link SoulissT31Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 */
public class SoulissT31Handler extends SoulissGenericHandler implements typicalCommonMethods {

    Configuration gwConfigurationMap;
    byte T3nRawState;

    DecimalType _setPointValue = DecimalType.ZERO;
    StringType _fanStateValue = StringType.EMPTY;
    StringType _powerState = StringType.EMPTY;
    StringType _fireState = StringType.EMPTY;

    StringType _lastModeState = StringType.EMPTY;
    StringType _modeStateValue = StringType.EMPTY;
    private Logger logger = LoggerFactory.getLogger(SoulissT11Handler.class);
    DecimalType _setMeasuredValue = DecimalType.ZERO;

    public SoulissT31Handler(Thing _thing) {
        super(_thing);
        thing = _thing;
    }

    // called on every status change or change request
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        switch (channelUID.getId()) {
            // FAN
            case SoulissBindingConstants.T31_SYSTEM_CHANNEL:
                if (command.equals(OnOffType.OFF)) {
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_ShutDown);
                } else {
                    if (_modeStateValue.equals(SoulissBindingConstants.T31_HEATINGMODE_MESSAGE_MODE_CHANNEL)) {
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_Heating);
                    } else {
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_Cooling);
                    }
                }
                break;
            case SoulissBindingConstants.T31_MODE_CHANNEL:
                if (command.equals(SoulissBindingConstants.T31_HEATINGMODE_MESSAGE_MODE_CHANNEL)) {
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_Heating);
                } else {
                    commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_Cooling);
                }
                break;
            case SoulissBindingConstants.T31_BUTTON_CHANNEL:
                commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_AsMeasured);
                break;
            case SoulissBindingConstants.T31_FAN_CHANNEL:
                switch (command.toString()) {
                    case SoulissBindingConstants.T31_FANHIGH_MESSAGE_FAN_CHANNEL:
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanManual);
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanHigh);
                        _fanStateValue = StringType.valueOf(SoulissBindingConstants.T31_FANHIGH_MESSAGE_FAN_CHANNEL);
                        break;
                    case SoulissBindingConstants.T31_FANMEDIUM_MESSAGE_FAN_CHANNEL:
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanManual);
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanMed);
                        _fanStateValue = StringType.valueOf(SoulissBindingConstants.T31_FANMEDIUM_MESSAGE_FAN_CHANNEL);
                        break;
                    case SoulissBindingConstants.T31_FANLOW_MESSAGE_FAN_CHANNEL:
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanManual);
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanLow);
                        _fanStateValue = StringType.valueOf(SoulissBindingConstants.T31_FANLOW_MESSAGE_FAN_CHANNEL);
                        break;
                    case SoulissBindingConstants.T31_FANAUTO_MESSAGE_FAN_CHANNEL:
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanAuto);
                        _fanStateValue = StringType.valueOf(SoulissBindingConstants.T31_FANAUTO_MESSAGE_FAN_CHANNEL);
                        break;
                    case SoulissBindingConstants.T31_FANOFF_MESSAGE_FAN_CHANNEL:
                        commandSEND(SoulissBindingProtocolConstants.Souliss_T3n_FanOff);
                        _fanStateValue = StringType.valueOf(SoulissBindingConstants.T31_FANOFF_MESSAGE_FAN_CHANNEL);
                        break;
                }
                break;
            case SoulissBindingConstants.T31_SETPOINT_CHANNEL:
                int uu = HalfFloatUtils.fromFloat(((DecimalType) command).floatValue());
                byte B2 = (byte) (uu >> 8);
                byte B1 = (byte) uu;
                // setpoint command
                commandSEND(SoulissBindingProtocolConstants.Souliss_T31_Use_Of_Slot_SETPOINT_COMMAND, B1, B2);
                break;
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void setState(PrimitiveType _state) {

        this.updateState(SoulissBindingConstants.T31_BUTTON_CHANNEL, OnOffType.OFF);

        super.setLastStatusStored();
        if (_state != null) {
            if (_state instanceof StringType) {
                switch (_state.toString()) {
                    case SoulissBindingConstants.T31_FANLOW_MESSAGE_FAN_CHANNEL:
                    case SoulissBindingConstants.T31_FANMEDIUM_MESSAGE_FAN_CHANNEL:
                    case SoulissBindingConstants.T31_FANHIGH_MESSAGE_FAN_CHANNEL:
                    case SoulissBindingConstants.T31_FANAUTO_MESSAGE_FAN_CHANNEL:
                    case SoulissBindingConstants.T31_FANOFF_MESSAGE_FAN_CHANNEL:
                        if (!_fanStateValue.equals(_state)) {
                            this.updateState(SoulissBindingConstants.T31_FAN_CHANNEL, (StringType) _state);
                            _fanStateValue = (StringType) _state;
                        }
                        break;

                    case SoulissBindingConstants.T31_HEATINGMODE_MESSAGE_MODE_CHANNEL:
                    case SoulissBindingConstants.T31_COOLINGMODE_MESSAGE_MODE_CHANNEL:
                        if (!_modeStateValue.equals(_state)) {
                            this.updateState(SoulissBindingConstants.T31_MODE_CHANNEL, (StringType) _state);
                            _modeStateValue = (StringType) _state;
                        }
                        break;

                    case SoulissBindingConstants.T31_OFF_MESSAGE_SYSTEM_CHANNEL:
                        if (!_powerState.equals(_state)) {
                            this.updateState(SoulissBindingConstants.T31_SYSTEM_CHANNEL, OnOffType.OFF);
                            _powerState = (StringType) _state;
                        }
                        break;
                    case SoulissBindingConstants.T31_ON_MESSAGE_SYSTEM_CHANNEL:
                        if (!_powerState.equals(_state)) {
                            this.updateState(SoulissBindingConstants.T31_SYSTEM_CHANNEL, OnOffType.ON);
                            _powerState = (StringType) _state;
                        }
                        break;

                    case SoulissBindingConstants.T31_ON_MESSAGE_FIRE_CHANNEL:
                        if (!_fireState.equals(_state)) {
                            this.updateState(SoulissBindingConstants.T31_FIRE_CHANNEL, OnOffType.ON);
                            _powerState = (StringType) _state;
                        }
                        break;
                    case SoulissBindingConstants.T31_OFF_MESSAGE_FIRE_CHANNEL:
                        if (!_fireState.equals(_state)) {
                            this.updateState(SoulissBindingConstants.T31_FIRE_CHANNEL, OnOffType.OFF);
                            _powerState = (StringType) _state;
                        }
                        break;
                }

            }

        }
    }

    public void setMeasuredValue(DecimalType valueOf) {
        if (valueOf instanceof DecimalType) {
            if (!_setMeasuredValue.equals(valueOf)) {
                this.updateState(SoulissBindingConstants.T31_VALUE_CHANNEL, valueOf);
                _setMeasuredValue = valueOf;
            }
        }

    }

    public void setSetpointValue(DecimalType valueOf) {
        if (valueOf instanceof DecimalType) {
            if (!_setPointValue.equals(valueOf)) {
                this.updateState(SoulissBindingConstants.T31_SETPOINT_CHANNEL, valueOf);
                _setPointValue = valueOf;
            }
        }

    }

    String sMessage = "";

    public void setRawStateValues(byte _rawState_byte0, float _valTemp, byte _valSetPoint) {

        sMessage = "";
        switch (getBitState(_rawState_byte0, 0)) {
            case 0:
                sMessage = SoulissBindingConstants.T31_OFF_MESSAGE_SYSTEM_CHANNEL;
                break;
            case 1:
                sMessage = SoulissBindingConstants.T31_ON_MESSAGE_SYSTEM_CHANNEL;
                break;
        }
        this.setState(StringType.valueOf(sMessage));

        switch (getBitState(_rawState_byte0, 7)) {
            case 0:
                sMessage = SoulissBindingConstants.T31_HEATINGMODE_MESSAGE_MODE_CHANNEL;
                break;
            case 1:
                sMessage = SoulissBindingConstants.T31_COOLINGMODE_MESSAGE_MODE_CHANNEL;
                break;
        }
        this.setState(StringType.valueOf(sMessage));

        // button indicante se il sistema sta andando o meno
        switch (getBitState(_rawState_byte0, 1) + getBitState(_rawState_byte0, 2)) {
            case 0:
                sMessage = SoulissBindingConstants.T31_OFF_MESSAGE_FIRE_CHANNEL;
                break;
            case 1:
                sMessage = SoulissBindingConstants.T31_ON_MESSAGE_FIRE_CHANNEL;
                break;
        }
        this.setState(StringType.valueOf(sMessage));

        // FAN SPEED
        switch (getBitState(_rawState_byte0, 3) + getBitState(_rawState_byte0, 4) + getBitState(_rawState_byte0, 5)) {
            case 0:
                sMessage = SoulissBindingConstants.T31_FANOFF_MESSAGE_FAN_CHANNEL;
                break;
            case 1:
                sMessage = SoulissBindingConstants.T31_FANLOW_MESSAGE_FAN_CHANNEL;
                break;
            case 2:
                sMessage = SoulissBindingConstants.T31_FANMEDIUM_MESSAGE_FAN_CHANNEL;
                break;
            case 3:
                sMessage = SoulissBindingConstants.T31_FANHIGH_MESSAGE_FAN_CHANNEL;
                break;
        }

        this.setState(StringType.valueOf(sMessage));

        // SLOT 1-2: Temperature Value
        if (!Float.isNaN(_valTemp)) {
            this.setMeasuredValue(DecimalType.valueOf(String.valueOf(_valTemp)));
        }

        // SLOT 3-4: Setpoint Value
        if (!Float.isNaN(_valSetPoint)) {
            this.setSetpointValue(DecimalType.valueOf(String.valueOf(_valSetPoint)));
        }

    }

    @Override
    public byte getRawState() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte getExpectedRawState(byte bCommand) {
        // TODO Auto-generated method stub
        return 0;
    }

    private float getFloatAtSlot(ArrayList<Byte> mac, int slot) {
        int iOutput = mac.get(5 + slot);
        int iOutput2 = mac.get(5 + slot + 1);
        // ora ho i due bytes, li converto
        int shifted = iOutput2 << 8;
        float ret = HalfFloatUtils.toFloat(shifted + iOutput);
        return ret;
    }

    public short getBitState(short vRaw, int iBit) {
        final int MASK_BIT_1 = 0x1;

        if (((vRaw >>> iBit) & MASK_BIT_1) == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public void setRawState(byte _rawState) {
        throw new NotImplementedException();
    }
}
