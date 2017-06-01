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
    // public static final int PING_resendTimeoutInSeconds = 5;

    // List of all Thing Type UIDs
    // public final static ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "sample");
    public final static ThingTypeUID GATEWAY_THING_TYPE = new ThingTypeUID(BINDING_ID, "gateway");

    public final static String T11 = "t11";
    public final static String T12 = "t12";
    public final static String T13 = "t13";
    public final static String T14 = "t14";
    public final static String T16 = "t16";
    public final static String LYT = "LYT";
    public final static String T21 = "t21";
    public final static String T22 = "t22";
    public final static String T31 = "t31";
    public final static String T51 = "t51";
    public final static String T52 = "t52";
    public final static String T53 = "t53";
    public final static String T54 = "t54";
    public final static String T55 = "t55";
    public final static String T56 = "t56";
    public final static String T57 = "t57";
    public final static String T58 = "t58";
    public final static String T61 = "t61";
    public final static String T62 = "t62";
    public final static String T63 = "t63";
    public final static String T64 = "t64";
    public final static String T65 = "t65";
    public final static String T66 = "t66";
    public final static String T67 = "t67";
    public final static String T68 = "t68";

    public final static ThingTypeUID T11_THING_TYPE = new ThingTypeUID(BINDING_ID, T11);
    public final static ThingTypeUID T12_THING_TYPE = new ThingTypeUID(BINDING_ID, T12);
    public final static ThingTypeUID T13_THING_TYPE = new ThingTypeUID(BINDING_ID, T13);
    public final static ThingTypeUID T14_THING_TYPE = new ThingTypeUID(BINDING_ID, T14);
    public final static ThingTypeUID T16_THING_TYPE = new ThingTypeUID(BINDING_ID, T16);
    public final static ThingTypeUID LYT_THING_TYPE = new ThingTypeUID(BINDING_ID, LYT);
    public final static ThingTypeUID T21_THING_TYPE = new ThingTypeUID(BINDING_ID, T21);
    public final static ThingTypeUID T22_THING_TYPE = new ThingTypeUID(BINDING_ID, T22);
    public final static ThingTypeUID T31_THING_TYPE = new ThingTypeUID(BINDING_ID, T31);
    public final static ThingTypeUID T51_THING_TYPE = new ThingTypeUID(BINDING_ID, T51);
    public final static ThingTypeUID T52_THING_TYPE = new ThingTypeUID(BINDING_ID, T52);
    public final static ThingTypeUID T53_THING_TYPE = new ThingTypeUID(BINDING_ID, T53);
    public final static ThingTypeUID T54_THING_TYPE = new ThingTypeUID(BINDING_ID, T54);
    public final static ThingTypeUID T55_THING_TYPE = new ThingTypeUID(BINDING_ID, T55);
    public final static ThingTypeUID T56_THING_TYPE = new ThingTypeUID(BINDING_ID, T56);
    public final static ThingTypeUID T57_THING_TYPE = new ThingTypeUID(BINDING_ID, T57);
    public final static ThingTypeUID T58_THING_TYPE = new ThingTypeUID(BINDING_ID, T58);
    public final static ThingTypeUID T61_THING_TYPE = new ThingTypeUID(BINDING_ID, T61);
    public final static ThingTypeUID T62_THING_TYPE = new ThingTypeUID(BINDING_ID, T62);
    public final static ThingTypeUID T63_THING_TYPE = new ThingTypeUID(BINDING_ID, T63);
    public final static ThingTypeUID T64_THING_TYPE = new ThingTypeUID(BINDING_ID, T64);
    public final static ThingTypeUID T65_THING_TYPE = new ThingTypeUID(BINDING_ID, T65);
    public final static ThingTypeUID T66_THING_TYPE = new ThingTypeUID(BINDING_ID, T66);
    public final static ThingTypeUID T67_THING_TYPE = new ThingTypeUID(BINDING_ID, T67);
    public final static ThingTypeUID T68_THING_TYPE = new ThingTypeUID(BINDING_ID, T68);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(GATEWAY_THING_TYPE,
            T11_THING_TYPE, T12_THING_TYPE, T13_THING_TYPE, T14_THING_TYPE, T16_THING_TYPE, LYT_THING_TYPE,
            T21_THING_TYPE, T22_THING_TYPE, T31_THING_TYPE, T51_THING_TYPE, T52_THING_TYPE, T53_THING_TYPE,
            T54_THING_TYPE, T55_THING_TYPE, T56_THING_TYPE, T57_THING_TYPE, T58_THING_TYPE, T61_THING_TYPE,
            T62_THING_TYPE, T63_THING_TYPE, T64_THING_TYPE, T65_THING_TYPE, T66_THING_TYPE, T67_THING_TYPE,
            T68_THING_TYPE);

    // List of all Channel ids
    public final static String ONOFF_CHANNEL = "onoff";
    public final static String PULSE_CHANNEL = "pulse";
    public final static String SLEEP_CHANNEL = "sleep";
    public final static String AUTOMODE_CHANNEL = "automode";
    public final static String STATEONOFF_CHANNEL = "stateOnOff";
    public final static String ROLLERSHUTTER_CHANNEL = "rollershutter";
    public final static String ROLLERSHUTTER_STATE_CHANNEL_CHANNEL = "rollershutter_state";
    public final static String ROLLERSHUTTER_MESSAGE_OPENING_CHANNEL = "opening";
    public final static String ROLLERSHUTTER_MESSAGE_CLOSING_CHANNEL = "closing";
    public final static String ROLLERSHUTTER_MESSAGE_LIMITSWITCH_OPEN_CHANNEL = "limSwitch_open";
    public final static String ROLLERSHUTTER_MESSAGE_LIMITSWITCH_CLOSE_CHANNEL = "limSwitch_close";
    public final static String ROLLERSHUTTER_MESSAGE_STATE_OPEN_CHANNEL = "state_open";
    public final static String ROLLERSHUTTER_MESSAGE_STATE_CLOSE_CHANNEL = "state_close";
    public final static String ROLLERSHUTTER_MESSAGE_NO_LIMITSWITCH_CHANNEL = "NoLimSwitch";
    public static final String ROLLERSHUTTER_MESSAGE_STOP_CHANNEL = "stop";

    public final static String T31_FAN_CHANNEL = "fan";
    public final static String T31_MODE_CHANNEL = "mode";
    public final static String T31_CONDITIONING_CHANNEL = "conditioning";
    public final static String T31_BUTTON_CHANNEL = "asMeasured";
    public final static String T31_SETPOINT_CHANNEL = "setPoint";
    public final static String T31_COOLINGMODE_MESSAGE_CHANNEL = "COOLING_MODE";
    public final static String T31_HEATINGMODE_MESSAGE_CHANNEL = "HEATING_MODE";
    public final static String T31_POWER_MESSAGE_CHANNEL = "POWER";
    public final static String T31_FANAUTO_MESSAGE_CHANNEL = "AUTO";
    public final static String T31_FANOFF_MESSAGE_CHANNEL = "FANOFF";
    public final static String T31_FANLOW_MESSAGE_CHANNEL = "LOW";
    public final static String T31_FANMEDIUM_MESSAGE_CHANNEL = "MEDIUM";
    public final static String T31_FANHIGH_MESSAGE_CHANNEL = "HIGH";
    public final static String T31_POWEROFF_MESSAGE_CHANNEL = "POWEROFF";
    public final static String T31_POWERON_MESSAGE_CHANNEL = "POWERON";

    public static final String WHITE_MODE_CHANNEL = "whitemode";
    public static final String ROLLER_BRIGHTNESS_CHANNEL = "roller_brightness";
    public static final String DIMMER_BRIGHTNESS_CHANNEL = "dimmer_brightness";
    public static final String LED_COLOR_CHANNEL = "ledcolor";

    public final static String LASTMESSAGE_CHANNEL = "lastMessage";
    public final static String LASTSTATUSSTORED_CHANNEL = "lastStatusStored";

    public final static String T5n_VALUE_CHANNEL = "value";
    public final static String T6n_VALUE_CHANNEL = "value";
    public final static String FLOATING_POINT_CHANNEL = "float";
    public final static String HUMIDITY_CHANNEL = "humidity";
    public final static String TEMPERATURE_CHANNEL = "temperature";
    public final static String AMPERE_CHANNEL = "ampere";
    public final static String VOLTAGE_CHANNEL = "voltage";
    public final static String POWER_CHANNEL = "power";

    public static final String CONFIG_IP_ADDRESS = "GATEWAY_IP_ADDRESS";
    public static final String CONFIG_PORT = "GATEWAY_PORT_NUMBER";
    public static final String CONFIG_LOCAL_PORT = "PREFERRED_LOCAL_PORT_NUMBER";
    public static final String CONFIG_USER_INDEX = "USER_INDEX"; // DEFAULT 70;
    public static final String CONFIG_NODE_INDEX = "NODE_INDEX"; // DEFAULT 120; // 0..127
    public static final String CONFIG_ID = "ID";
    public static final String CONFIG_PING_REFRESH = "PING_INTERVAL";
    public static final String CONFIG_SUBSCRIPTION_REFRESH = "SUBSCRIBTION_INTERVAL";

    public static final String UUID_NODE_SLOT_SEPARATOR = "-";

}
