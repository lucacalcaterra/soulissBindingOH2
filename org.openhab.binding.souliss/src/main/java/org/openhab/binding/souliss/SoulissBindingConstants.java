/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.Sets;

/**
 * The {@link SoulissBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissBindingConstants {

    public static final String BINDING_ID = "souliss";

    // List of all Thing Type UIDs
    // public final static ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "sample");
    public final static ThingTypeUID GATEWAY_THING_TYPE = new ThingTypeUID(BINDING_ID, "gateway");
    public final static ThingTypeUID T11_THING_TYPE = new ThingTypeUID(BINDING_ID, "t11");
    public final static ThingTypeUID T12_THING_TYPE = new ThingTypeUID(BINDING_ID, "t12");
    public final static ThingTypeUID T13_THING_TYPE = new ThingTypeUID(BINDING_ID, "t13");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(T11_THING_TYPE, T12_THING_TYPE,
            T13_THING_TYPE);
    // List of all Channel ids
    public final static String CHANNEL_1 = "channel1";

    // public static final int PORT_SEND = 8899;

    public static final String CONFIG_HOST_NAME = "ADDR";
    public static final String CONFIG_PORT = "PORT";
    public static final String CONFIG_ID = "ID";
    public static final String CONFIG_REFRESH = "REFRESH";
}
