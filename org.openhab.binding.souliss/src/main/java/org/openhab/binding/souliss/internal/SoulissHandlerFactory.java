/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.internal;

import static org.openhab.binding.souliss.SoulissBindingConstants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.handler.SoulissGatewayHandler;
import org.openhab.binding.souliss.handler.SoulissT11Handler;
import org.openhab.binding.souliss.handler.SoulissT12Handler;
import org.openhab.binding.souliss.handler.SoulissT13Handler;
import org.openhab.binding.souliss.handler.SoulissT14Handler;
import org.openhab.binding.souliss.handler.SoulissT22Handler;
import org.openhab.binding.souliss.handler.SoulissT31Handler;
import org.openhab.binding.souliss.handler.SoulissT41Handler;
import org.openhab.binding.souliss.handler.SoulissT52Handler;
import org.openhab.binding.souliss.handler.SoulissT53Handler;
import org.openhab.binding.souliss.handler.SoulissT55Handler;
import org.openhab.binding.souliss.handler.SoulissT56Handler;
import org.openhab.binding.souliss.handler.SoulissT57Handler;
import org.openhab.binding.souliss.handler.SoulissT61Handler;
import org.openhab.binding.souliss.handler.SoulissT62Handler;
import org.openhab.binding.souliss.handler.SoulissT63Handler;
import org.openhab.binding.souliss.handler.SoulissT64Handler;
import org.openhab.binding.souliss.handler.SoulissT65Handler;
import org.openhab.binding.souliss.handler.SoulissT66Handler;
import org.openhab.binding.souliss.handler.SoulissT67Handler;
import org.openhab.binding.souliss.handler.SoulissT68Handler;
import org.openhab.binding.souliss.handler.SoulissTopicsHandler;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingNetworkParameters;

/**
 * The {@link SoulissHandlerFactory} is responsible for creating things and thing
 * handlers. It fire when a new thing is added.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissHandlerFactory extends BaseThingHandlerFactory {

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(GATEWAY_THING_TYPE)) {
            // get last byte of IP number
            Configuration gwConfigurationMap = thing.getConfiguration();
            String IPAddressOnLAN = (String) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_IP_ADDRESS);
            SoulissBindingNetworkParameters.addGateway(Byte.parseByte(IPAddressOnLAN.split("\\.")[3]), thing);
            return new SoulissGatewayHandler((Bridge) thing);
        } else if (thingTypeUID.equals(T11_THING_TYPE)) {
            return new SoulissT11Handler(thing);
        } else if (thingTypeUID.equals(T12_THING_TYPE)) {
            return new SoulissT12Handler(thing);
        } else if (thingTypeUID.equals(T13_THING_TYPE)) {
            return new SoulissT13Handler(thing);
        } else if (thingTypeUID.equals(T14_THING_TYPE)) {
            return new SoulissT14Handler(thing);
        } else if (thingTypeUID.equals(T21_THING_TYPE) || (thingTypeUID.equals(T22_THING_TYPE))) {
            return new SoulissT22Handler(thing);
        } else if (thingTypeUID.equals(T31_THING_TYPE)) {
            return new SoulissT31Handler(thing);
        } else if (thingTypeUID.equals(T41_THING_TYPE)) {
            return new SoulissT41Handler(thing);
        } else if (thingTypeUID.equals(T52_THING_TYPE)) {
            return new SoulissT52Handler(thing);
        } else if (thingTypeUID.equals(T53_THING_TYPE)) {
            return new SoulissT53Handler(thing);
        } else if (thingTypeUID.equals(T55_THING_TYPE)) {
            return new SoulissT55Handler(thing);
        } else if (thingTypeUID.equals(T56_THING_TYPE)) {
            return new SoulissT56Handler(thing);
        } else if (thingTypeUID.equals(T57_THING_TYPE)) {
            return new SoulissT57Handler(thing);
        } else if (thingTypeUID.equals(T61_THING_TYPE)) {
            return new SoulissT61Handler(thing);
        } else if (thingTypeUID.equals(T62_THING_TYPE)) {
            return new SoulissT62Handler(thing);
        } else if (thingTypeUID.equals(T63_THING_TYPE)) {
            return new SoulissT63Handler(thing);
        } else if (thingTypeUID.equals(T64_THING_TYPE)) {
            return new SoulissT64Handler(thing);
        } else if (thingTypeUID.equals(T65_THING_TYPE)) {
            return new SoulissT65Handler(thing);
        } else if (thingTypeUID.equals(T66_THING_TYPE)) {
            return new SoulissT66Handler(thing);
        } else if (thingTypeUID.equals(T67_THING_TYPE)) {
            return new SoulissT67Handler(thing);
        } else if (thingTypeUID.equals(T68_THING_TYPE)) {
            return new SoulissT68Handler(thing);
        } else if (thingTypeUID.equals(TOPICS_THING_TYPE)) {
            return new SoulissTopicsHandler(thing);
        }

        return null;
    }
}
