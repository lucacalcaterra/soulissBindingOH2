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

    public static final int DISCOVERY_resendTimeoutInMillis = 1;
    public static final int DISCOVERY_resendAttempts = 10;
    public static final int DISCOVERY_TimeoutInSeconds = 15;
    public static final int PING_resendTimeoutInSeconds = 5;

    // List of all Thing Type UIDs
    // public final static ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "sample");
    public final static ThingTypeUID GATEWAY_THING_TYPE = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID T11_THING_TYPE = new ThingTypeUID(BINDING_ID, "t11");
    public final static ThingTypeUID T12_THING_TYPE = new ThingTypeUID(BINDING_ID, "t12");
    public final static ThingTypeUID T13_THING_TYPE = new ThingTypeUID(BINDING_ID, "t13");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(T11_THING_TYPE, T12_THING_TYPE,
            T13_THING_TYPE);
    // List of all Channel ids
    public final static String CHANNEL_1 = "channel1";

    public static final String CONFIG_IP_ADDRESS = "GATEWAY_IP_ADDRESS";
    public static final String CONFIG_PORT = "GATEWAY_PORT_NUMBER";
    public static final String CONFIG_LOCAL_PORT = "PREFERRED_LOCAL_PORT_NUMBER";
    public static final String CONFIG_USER_INDEX = "USER_INDEX"; // DEFAULT 70;
    public static final String CONFIG_NODE_INDEX = "NODE_INDEX"; // DEFAULT 120; // 0..127
    public static final String CONFIG_ID = "ID";
    public static final String CONFIG_REFRESH = "REFRESH";

}
