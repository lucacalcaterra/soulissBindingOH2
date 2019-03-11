/**
 * Copyright (c) 2014-2018 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.openhab.binding.souliss.SoulissBindingConstants;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * The {@link SoulissT5nHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissT5nHandler extends SoulissGenericHandler {

    // private Logger logger = LoggerFactory.getLogger(SoulissT5nHandler.class);
    float fVal;

    public SoulissT5nHandler(Thing _thing) {
        super(_thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    public void setState(PrimitiveType state) {
        if (state != null) {
            this.updateState(SoulissBindingConstants.T5n_VALUE_CHANNEL, (DecimalType) state);
        }
    }

    @Override
    public void setRawState(byte _rawState) {
        throw new NotImplementedException();
    }

    public void setFloatValue(float valueOf) {
        super.setLastStatusStored();
        if (fVal != valueOf) {
            this.setState(DecimalType.valueOf(Float.toString(valueOf)));
            fVal = valueOf;
        }
    }

    @Override
    public byte getRawState() {
        throw new NotImplementedException();
    }

    public float getFloatState() {
        return fVal;
    }

    @Override
    public byte getExpectedRawState(byte bCommand) {
        return -1;
    }
}
