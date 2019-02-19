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
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.binding.souliss.handler.SoulissGenericHandler.typicalCommonMethods;

/**
 * The {@link SoulissT22Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissT22Handler extends SoulissGenericHandler implements typicalCommonMethods {
    Configuration gwConfigurationMap;
    // private Logger logger = LoggerFactory.getLogger(SoulissT22Handler.class);
    byte T2nRawState;

    Number bSecureSend = -1; // -1 means that Secure Send is disabled

    public SoulissT22Handler(Thing _thing) {
        super(_thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
        gwConfigurationMap = thing.getConfiguration();
        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND) != null) {
            bSecureSend = (Number) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SECURE_SEND);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ROLLERSHUTTER_CHANNEL:
                    // updateState(channelUID, T2nState);
                    break;
            }
        } else {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.ROLLERSHUTTER_CHANNEL:
                    if (command instanceof UpDownType) {
                        if (command.equals(UpDownType.UP)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T2n_OpenCmd);
                        } else if (command.equals(UpDownType.DOWN)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T2n_CloseCmd);
                        }
                    } else if (command instanceof StopMoveType) {
                        if (command.equals(StopMoveType.STOP)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T2n_StopCmd);
                        }
                    }
                    break;
                case SoulissBindingConstants.ONOFF_CHANNEL:
                    if (command instanceof OnOffType) {
                        if (command.equals(OnOffType.ON)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T2n_OpenCmd_Local);
                        } else if (command.equals(OnOffType.OFF)) {
                            commandSEND(SoulissBindingProtocolConstants.Souliss_T2n_CloseCmd_Local);
                        }
                    }
                    break;
            }
        }
    }

    public void setState(PrimitiveType _state) {
        if (_state != null) {
            if (_state instanceof UpDownType) {
                this.updateState(SoulissBindingConstants.ROLLERSHUTTER_CHANNEL, (UpDownType) _state);
                // this.updateThing(this.thing);
            } else if (_state instanceof StopMoveType) {
                this.updateState(SoulissBindingConstants.ROLLERSHUTTER_CHANNEL, (State) _state);
                // this.updateThing(this.thing);
            }
        }
        // this.updateThing(this.thing);
    }

    public void setState_Message(String rollershutterMessage) {
        this.updateState(SoulissBindingConstants.ROLLERSHUTTER_STATE_CHANNEL_CHANNEL,
                StringType.valueOf(rollershutterMessage));
    }

    @Override
    public void setRawState(byte _rawState) {
        T2nRawState = _rawState;
        // update Last Status stored time
        super.setLastStatusStored();
        // update item state only if it is different from previous
        if (T2nRawState != _rawState) {
            if (T2nRawState == SoulissBindingProtocolConstants.Souliss_T2n_Coil_Open) {
                this.setState(UpDownType.UP);
                this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_OPENING_CHANNEL);
            } else if (T2nRawState == SoulissBindingProtocolConstants.Souliss_T2n_Coil_Close) {
                this.setState(UpDownType.DOWN);
                this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_CLOSING_CHANNEL);
            }
            switch (T2nRawState) {
                case SoulissBindingProtocolConstants.Souliss_T2n_Coil_Stop:
                    this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_STOP_CHANNEL);
                    break;
                case SoulissBindingProtocolConstants.Souliss_T2n_Coil_Off:
                    this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_OPENING_CHANNEL);
                    break;
                case SoulissBindingProtocolConstants.Souliss_T2n_LimSwitch_Close:
                    this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_LIMITSWITCH_CLOSE_CHANNEL);
                    break;
                case SoulissBindingProtocolConstants.Souliss_T2n_LimSwitch_Open:
                    this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_LIMITSWITCH_OPEN_CHANNEL);
                    break;
                case SoulissBindingProtocolConstants.Souliss_T2n_NoLimSwitch:
                    this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_LIMITSWITCH_OPEN_CHANNEL);
                    break;
                case SoulissBindingProtocolConstants.Souliss_T2n_Timer_Off:
                    this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_TIMER_OFF);
                    break;
                case SoulissBindingProtocolConstants.Souliss_T2n_State_Open:
                    this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_STATE_OPEN_CHANNEL);
                    break;
                case SoulissBindingProtocolConstants.Souliss_T2n_State_Close:
                    this.setState_Message(SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_STATE_CLOSE_CHANNEL);
                    break;
            }
        }

    }

    @Override
    public byte getRawState() {
        // TODO Auto-generated method stub
        return T2nRawState;
    }

    @Override
    public byte getExpectedRawState(byte bCmd) {
        if (bCmd == SoulissBindingProtocolConstants.Souliss_T2n_OpenCmd) {
            return SoulissBindingProtocolConstants.Souliss_T2n_Coil_Open;
        } else if (bCmd == SoulissBindingProtocolConstants.Souliss_T2n_CloseCmd) {
            return SoulissBindingProtocolConstants.Souliss_T2n_Coil_Close;
        } else if (bCmd >= SoulissBindingProtocolConstants.Souliss_T2n_StopCmd) {
            return SoulissBindingProtocolConstants.Souliss_T2n_Coil_Stop;
        }

        return -1;
    }

}
