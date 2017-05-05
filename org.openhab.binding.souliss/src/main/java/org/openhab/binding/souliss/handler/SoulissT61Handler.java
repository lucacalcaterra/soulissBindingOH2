package org.openhab.binding.souliss.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;

/**
 * The {@link SoulissT61Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 */
public class SoulissT61Handler extends SoulissT6nHandler {

    private float analogSetpointValue;

    // constructor
    public SoulissT61Handler(Thing _thing) {
        super(_thing);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

}
