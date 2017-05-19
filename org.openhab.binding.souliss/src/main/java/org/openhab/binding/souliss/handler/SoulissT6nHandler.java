package org.openhab.binding.souliss.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
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
 * The {@link SoulissT6nHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 */

public class SoulissT6nHandler extends SoulissGenericTypical implements typicalCommonMethods {

    private Logger logger = LoggerFactory.getLogger(SoulissT6nHandler.class);

    DecimalType T6nState;

    public SoulissT6nHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.T6n_VALUE_CHANNEL:
                    updateState(channelUID, T6nState);
                    break;
            }
        } else if (command instanceof DecimalType) {
            int uu = HalfFloatUtils.fromFloat(((DecimalType) command).floatValue());
            byte B2 = (byte) (uu >> 8);
            byte B1 = (byte) uu;
            // setpoint command
            CommandSEND(SoulissBindingProtocolConstants.Souliss_T61, B1, B2);
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void setState(PrimitiveType state) {
        this.setUpdateTimeNow();

        this.updateState(SoulissBindingConstants.LASTSTATUSSTORED_CHANNEL, this.getLastUpdateTime());
        this.updateState(SoulissBindingConstants.T6n_VALUE_CHANNEL, (DecimalType) state);
        T6nState = (DecimalType) state;
    }

}
