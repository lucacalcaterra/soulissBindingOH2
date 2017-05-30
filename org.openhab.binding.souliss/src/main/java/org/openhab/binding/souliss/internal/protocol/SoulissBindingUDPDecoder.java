/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.internal.protocol;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.binding.souliss.SoulissBindingUDPConstants;
import org.openhab.binding.souliss.handler.SoulissGenericTypical;
import org.openhab.binding.souliss.handler.SoulissT11Handler;
import org.openhab.binding.souliss.handler.SoulissT12Handler;
import org.openhab.binding.souliss.handler.SoulissT13Handler;
import org.openhab.binding.souliss.handler.SoulissT14Handler;
import org.openhab.binding.souliss.handler.SoulissT22Handler;
import org.openhab.binding.souliss.handler.SoulissT31Handler;
import org.openhab.binding.souliss.handler.SoulissT5nHandler;
import org.openhab.binding.souliss.handler.SoulissT61Handler;
import org.openhab.binding.souliss.handler.SoulissT62Handler;
import org.openhab.binding.souliss.handler.SoulissT63Handler;
import org.openhab.binding.souliss.handler.SoulissT64Handler;
import org.openhab.binding.souliss.handler.SoulissT65Handler;
import org.openhab.binding.souliss.handler.SoulissT66Handler;
import org.openhab.binding.souliss.handler.SoulissT67Handler;
import org.openhab.binding.souliss.handler.SoulissT68Handler;
import org.openhab.binding.souliss.internal.protocol.SoulissDiscover.DiscoverResult;
import org.openhab.binfing.souliss.primitivetypes.FanModeType;
import org.openhab.binfing.souliss.primitivetypes.ThermostatModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class decodes incoming Souliss packets, starting from decodevNet
 *
 * @author Alessandro Del Pex
 * @author Tonino Fazio
 * @since 1.7.0
 */
public class SoulissBindingUDPDecoder {

    // private SoulissTypicals soulissTypicalsRecipients;
    private static Logger logger = LoggerFactory.getLogger(SoulissBindingUDPDecoder.class);
    private DiscoverResult discoverResult = null;

    // public SoulissBindingUDPDecoder(SoulissTypicals typicals) {
    // soulissTypicalsRecipients = typicals;
    // }

    public SoulissBindingUDPDecoder(DiscoverResult discoverResult2) {
        discoverResult = discoverResult2;
    }

    /**
     * Get packet from VNET Frame
     *
     * @param packet
     *            incoming datagram
     */
    public void decodeVNetDatagram(DatagramPacket packet) {
        int checklen = packet.getLength();
        ArrayList<Short> mac = new ArrayList<Short>();
        for (int ig = 7; ig < checklen; ig++) {
            mac.add((short) (packet.getData()[ig] & 0xFF));
        }
        decodeMacaco(mac);
    }

    /**
     * Decodes lower level MaCaCo packet
     *
     * @param macacoPck
     */
    private void decodeMacaco(ArrayList<Short> macacoPck) {
        int functionalCode = macacoPck.get(0);
        switch (functionalCode) {

            case SoulissBindingUDPConstants.Souliss_UDP_function_ping_resp:
                logger.debug("Received functional code: 0x" + Integer.toHexString(functionalCode) + " - Ping answer");
                decodePing(macacoPck);
                break;
            case SoulissBindingUDPConstants.Souliss_UDP_function_discover_GW_node_bcas_resp:
                logger.debug("Received functional code: 0x" + Integer.toHexString(functionalCode)
                        + " - Discover a gateway node answer (broadcast)");
                try {
                    decodePingBroadcast(macacoPck);
                } catch (UnknownHostException e) {
                    logger.debug("Error: {}", e.getLocalizedMessage());
                    e.printStackTrace();
                }
                break;

            case SoulissBindingUDPConstants.Souliss_UDP_function_subscribe_resp:
            case SoulissBindingUDPConstants.Souliss_UDP_function_poll_resp:
                logger.debug(
                        "Received functional code: 0x" + Integer.toHexString(functionalCode) + " - Read state answer");
                decodeStateRequest(macacoPck);
                break;

            case SoulissBindingUDPConstants.Souliss_UDP_function_typreq_resp:// Answer for assigned typical logic
                logger.debug("Received functional code: 0x" + Integer.toHexString(functionalCode)
                        + " - Read typical logic answer");
                decodeTypRequest(macacoPck);
                break;

            // case (byte) ConstantsUDP.Souliss_UDP_function_health_resp:// Answer
            // // nodes
            // // healty
            // logger.debug("function_health_resp");
            // decodeHealthRequest(macacoPck);
            // break;

            case (byte) SoulissBindingUDPConstants.Souliss_UDP_function_db_struct_resp:
                logger.debug("Received functional code: 0x" + Integer.toHexString(functionalCode)
                        + " - Database structure answer");
                decodeDBStructRequest(macacoPck);
                break;
            // case 0x83:
            // logger.debug("Functional code not supported");
            // break;
            // case 0x84:
            // logger.debug("Data out of range");
            // break;
            // case 0x85:
            // logger.debug("Subscription refused");
            // break;
            // default:
            // logger.debug("Unknown functional code");
            // break;
            default:
                logger.debug("Received functional code: 0x" + Integer.toHexString(functionalCode) + " - Unused");
                break;

        }
    }

    /**
     * @param mac
     */
    private void decodePing(ArrayList<Short> mac) {
        int putIn_1 = mac.get(1); // not used
        int putIn_2 = mac.get(2); // not used
        logger.debug("decodePing: putIn code: {}, {}", putIn_1, putIn_2);
        discoverResult.gatewayDetected();
    }

    private void decodePingBroadcast(ArrayList<Short> macaco) throws UnknownHostException {
        String IP = macaco.get(5) + "." + macaco.get(6) + "." + macaco.get(7) + "." + macaco.get(8);
        byte[] addr = { new Short(macaco.get(5)).byteValue(), new Short(macaco.get(6)).byteValue(),
                new Short(macaco.get(7)).byteValue(), new Short(macaco.get(8)).byteValue() };
        logger.debug("decodePingBroadcast. Gateway Discovery. IP: {}", IP);

        discoverResult.gatewayDetected(InetAddress.getByAddress(addr), macaco.get(8).toString());
    }

    private void decodeTypRequest(ArrayList<Short> mac) {
        try {
            short tgtnode = mac.get(3);
            int numberOf = mac.get(4);

            int typXnodo = SoulissBindingNetworkParameters.maxnodes;
            // logger.debug("Node: {} Nodes: {} Typicals x node: {}", tgtnode, numberOf, typXnodo);
            // creates Souliss nodes
            for (int j = 0; j < numberOf; j++) {
                if (mac.get(5 + j) != 0) {// create only not-empty typicals
                    if (!(mac.get(5 + j) == SoulissBindingProtocolConstants.Souliss_T_related)) {
                        // String hTyp = Integer.toHexString(mac.get(5 + j));
                        short typical = mac.get(5 + j);
                        short slot = (short) (j % typXnodo);
                        short node = (short) (j / typXnodo + tgtnode);

                        discoverResult.thingDetected(typical, node, slot);
                    }
                }
            }
        } catch (Exception uy) {
            logger.error("decodeTypRequest ERROR");
        }
    }

    /**
     * decode DB Struct Request Packet
     * It return Souliss Network:
     * node number
     * max supported number of nodes
     * max typical per node
     * max requests
     * See Souliss wiki for details
     *
     * @param mac
     */
    private void decodeDBStructRequest(ArrayList<Short> mac) {
        try {
            int nodes = mac.get(5);
            int maxnodes = mac.get(6);
            int maxTypicalXnode = mac.get(7);
            int maxrequests = mac.get(8);
            // int MaCaco_IN_S = mac.get(9);
            // int MaCaco_TYP_S = mac.get(10);
            // int MaCaco_OUT_S = mac.get(11);

            SoulissBindingNetworkParameters.nodes = nodes;
            SoulissBindingNetworkParameters.maxnodes = maxnodes;
            SoulissBindingNetworkParameters.maxTypicalXnode = maxTypicalXnode;
            SoulissBindingNetworkParameters.maxrequests = maxrequests;
            // SoulissBindingNetworkParameters.MaCacoIN_s = MaCaco_IN_S;
            // SoulissBindingNetworkParameters.MaCacoTYP_s = MaCaco_TYP_S;
            // SoulissBindingNetworkParameters.MaCacoOUT_s = MaCaco_OUT_S;

            // logger.debug("decodeDBStructRequest");
            // logger.debug("Nodes: " + nodes);
            // logger.debug("maxnodes: " + maxnodes);
            // logger.debug("maxTypicalXnode: " + maxTypicalXnode);
            // logger.debug("maxrequests: " + maxrequests);
            // // logger.debug("MaCaco_IN_S: " + MaCaco_IN_S);
            // logger.debug("MaCaco_TYP_S: " + MaCaco_TYP_S);
            // logger.debug("MaCaco_OUT_S: " + MaCaco_OUT_S);

            discoverResult.dbStructAnswerReceived();

        } catch (Exception e) {
            logger.error("decodeDBStructRequest: SoulissNetworkParameter update ERROR");
        }
    }

    private void decodeStateRequest(ArrayList<Short> mac) {
        boolean bDecoded_forLOG = false;
        int tgtnode = mac.get(3);

        Iterator thingsIterator;
        if (SoulissBindingNetworkParameters.getGateway() != null
                && SoulissBindingNetworkParameters.getGateway().getThings() != null) {
            thingsIterator = SoulissBindingNetworkParameters.getGateway().getThings().iterator();
            boolean bFound = false;
            Thing typ = null;
            while (thingsIterator.hasNext() && !bFound) {
                typ = (Thing) thingsIterator.next();
                String sUID_Array[] = typ.getUID().getAsString().split(":");
                // execute only if binding is Souliss
                if (sUID_Array[0].equals(SoulissBindingConstants.BINDING_ID)) {
                    // execute only if it is node to update
                    if (((SoulissGenericTypical) typ.getHandler()) != null
                            && ((SoulissGenericTypical) typ.getHandler()).getNode() == tgtnode) {
                        // ...now check slot
                        int slot = ((SoulissGenericTypical) typ.getHandler()).getSlot();
                        // get typical value
                        short sVal = getByteAtSlot(mac, slot);

                        OnOffType typicalState = null;
                        // update Txx
                        try {
                            switch (sUID_Array[1]) {
                                case SoulissBindingConstants.T11:
                                    logger.debug("Decoding " + SoulissBindingConstants.T11 + " packet");
                                    typicalState = getOHStateFromSoulissVal(sVal);
                                    ((SoulissT11Handler) typ.getHandler()).setState(typicalState);
                                    // cercare di capire come forzare un update
                                    break;
                                case SoulissBindingConstants.T12:
                                    logger.debug("Decoding " + SoulissBindingConstants.T12 + " packet");
                                    if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OnCoil_Auto) {
                                        ((SoulissT12Handler) typ.getHandler()).setState(OnOffType.ON);
                                        ((SoulissT12Handler) typ.getHandler()).setState_Automode(OnOffType.ON);
                                    } else if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OffCoil_Auto) {
                                        ((SoulissT12Handler) typ.getHandler()).setState(OnOffType.OFF);
                                        ((SoulissT12Handler) typ.getHandler()).setState_Automode(OnOffType.ON);

                                    } else if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OnCoil) {
                                        ((SoulissT12Handler) typ.getHandler()).setState(OnOffType.ON);
                                        ((SoulissT12Handler) typ.getHandler()).setState_Automode(OnOffType.OFF);
                                    } else if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OffCoil) {
                                        ((SoulissT12Handler) typ.getHandler()).setState(OnOffType.OFF);
                                        ((SoulissT12Handler) typ.getHandler()).setState_Automode(OnOffType.OFF);
                                    }
                                    break;
                                case SoulissBindingConstants.T13:
                                    logger.debug("Decoding " + SoulissBindingConstants.T13 + " packet");
                                    typicalState = getOHStateFromSoulissVal(sVal);
                                    ((SoulissT13Handler) typ.getHandler()).setState(typicalState);
                                    break;
                                case SoulissBindingConstants.T14:
                                    logger.debug("Decoding " + SoulissBindingConstants.T14 + " packet");
                                    typicalState = getOHStateFromSoulissVal(sVal);
                                    ((SoulissT14Handler) typ.getHandler()).setState(typicalState);
                                    break;
                                case SoulissBindingConstants.T21:
                                case SoulissBindingConstants.T22:
                                    logger.debug("Decoding " + SoulissBindingConstants.T21 + "/"
                                            + SoulissBindingConstants.T21 + " packet");
                                    if (sVal == SoulissBindingProtocolConstants.Souliss_T2n_Coil_Open) {
                                        ((SoulissT22Handler) typ.getHandler()).setState(UpDownType.UP);
                                        ((SoulissT22Handler) typ.getHandler()).setState_Message(
                                                SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_OPENING_CHANNEL);
                                    } else if (sVal == SoulissBindingProtocolConstants.Souliss_T2n_Coil_Close) {
                                        ((SoulissT22Handler) typ.getHandler()).setState(UpDownType.DOWN);
                                        ((SoulissT22Handler) typ.getHandler()).setState_Message(
                                                SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_CLOSING_CHANNEL);
                                    }
                                    if (sVal == SoulissBindingProtocolConstants.Souliss_T2n_Coil_Stop
                                            || sVal == SoulissBindingProtocolConstants.Souliss_T2n_Coil_Off
                                            || sVal == SoulissBindingProtocolConstants.Souliss_T2n_LimSwitch_Close
                                            || sVal == SoulissBindingProtocolConstants.Souliss_T2n_LimSwitch_Open
                                            || sVal == SoulissBindingProtocolConstants.Souliss_T2n_NoLimSwitch
                                            || sVal == SoulissBindingProtocolConstants.Souliss_T2n_Timer_Off
                                            || sVal == SoulissBindingProtocolConstants.Souliss_T2n_State_Open
                                            || sVal == SoulissBindingProtocolConstants.Souliss_T2n_State_Close) {
                                        // ((SoulissT22Handler) typ.getHandler()).setState(PercentType.valueOf("50"));

                                        switch (sVal) {
                                            case SoulissBindingProtocolConstants.Souliss_T2n_Coil_Stop:
                                                ((SoulissT22Handler) typ.getHandler()).setState_Message(
                                                        SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_STOP_CHANNEL);
                                                break;
                                            case SoulissBindingProtocolConstants.Souliss_T2n_Coil_Off:
                                                ((SoulissT22Handler) typ.getHandler()).setState_Message(
                                                        SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_OPENING_CHANNEL);
                                                break;
                                            case SoulissBindingProtocolConstants.Souliss_T2n_LimSwitch_Close:
                                                ((SoulissT22Handler) typ.getHandler()).setState_Message(
                                                        SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_LIMITSWITCH_CLOSE_CHANNEL);
                                                break;
                                            case SoulissBindingProtocolConstants.Souliss_T2n_LimSwitch_Open:
                                                ((SoulissT22Handler) typ.getHandler()).setState_Message(
                                                        SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_LIMITSWITCH_OPEN_CHANNEL);
                                                break;
                                            case SoulissBindingProtocolConstants.Souliss_T2n_NoLimSwitch:
                                                ((SoulissT22Handler) typ.getHandler()).setState_Message(
                                                        SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_LIMITSWITCH_OPEN_CHANNEL);
                                                break;
                                            case SoulissBindingProtocolConstants.Souliss_T2n_State_Open:
                                                ((SoulissT22Handler) typ.getHandler()).setState_Message(
                                                        SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_STATE_OPEN_CHANNEL);
                                                break;
                                            case SoulissBindingProtocolConstants.Souliss_T2n_State_Close:
                                                ((SoulissT22Handler) typ.getHandler()).setState_Message(
                                                        SoulissBindingConstants.ROLLERSHUTTER_MESSAGE_STATE_CLOSE_CHANNEL);
                                                break;
                                        }
                                    }
                                    break;
                                case SoulissBindingConstants.T31:
                                    logger.debug("Decoding " + SoulissBindingConstants.T31 + "/"
                                            + SoulissBindingConstants.T31 + " packet");

                                    switch (getBitState(sVal, 1)) {
                                        case 0:
                                            ((SoulissT31Handler) typ.getHandler())
                                                    .setState(ThermostatModeType.HEATING_OFF);
                                            break;
                                        case 1:
                                            ((SoulissT31Handler) typ.getHandler())
                                                    .setState(ThermostatModeType.HEATING_ON);
                                            break;
                                    }

                                    switch (getBitState(sVal, 2)) {
                                        case 0:
                                            ((SoulissT31Handler) typ.getHandler())
                                                    .setState(ThermostatModeType.COOLING_OFF);
                                            break;
                                        case 1:
                                            ((SoulissT31Handler) typ.getHandler())
                                                    .setState(ThermostatModeType.COOLING_ON);
                                            break;
                                    }

                                    switch (getBitState(sVal, 3) + getBitState(sVal, 4) + getBitState(sVal, 5)) {
                                        case 1:
                                            ((SoulissT31Handler) typ.getHandler()).setState(FanModeType.LOW);
                                            break;
                                        case 2:
                                            ((SoulissT31Handler) typ.getHandler()).setState(FanModeType.MEDIUM);
                                            break;
                                        case 3:
                                            ((SoulissT31Handler) typ.getHandler()).setState(FanModeType.HIGH);
                                            break;
                                    }
                                    switch (getBitState(sVal, 6)) {
                                        case 0:
                                            ((SoulissT31Handler) typ.getHandler()).setState(FanModeType.MANUAL);
                                            break;
                                        case 1:
                                            ((SoulissT31Handler) typ.getHandler()).setState(FanModeType.AUTO);
                                            break;
                                    }

                                    switch (getBitState(sVal, 7)) {
                                        case 0:
                                            ((SoulissT31Handler) typ.getHandler())
                                                    .setState(ThermostatModeType.HEATING_MODE);
                                            break;
                                        case 1:
                                            ((SoulissT31Handler) typ.getHandler())
                                                    .setState(ThermostatModeType.COOLING_MODE);
                                            break;
                                    }

                                    // SLOT 1-2: Temperature Measured value
                                    float val = getFloatAtSlot(mac, slot + 1);
                                    // ((SoulissT31Handler) typ).setMeasuredValue(val);
                                    // ((SoulissT31Handler) typ.getHandler()).setState((DecimalType)val);

                                    // SLOT 3-4: Temperature Setpoint Value
                                    val = getFloatAtSlot(mac, slot + 3);
                                    ((SoulissT31Handler) typ.getHandler())
                                            .setState(DecimalType.valueOf(String.valueOf(val)));

                                    break;
                                case SoulissBindingConstants.T51:
                                case SoulissBindingConstants.T52:
                                case SoulissBindingConstants.T53:
                                case SoulissBindingConstants.T54:
                                case SoulissBindingConstants.T55:
                                case SoulissBindingConstants.T56:
                                case SoulissBindingConstants.T57:
                                case SoulissBindingConstants.T58:
                                    logger.debug("Decoding T5n packet");
                                    ((SoulissT5nHandler) typ.getHandler())
                                            .setState(DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    break;
                                case SoulissBindingConstants.T61:
                                    logger.debug("Decoding " + SoulissBindingConstants.T61 + " packet");
                                    if (getFloatAtSlot(mac, slot) != Float.NaN) {
                                        ((SoulissT61Handler) typ.getHandler()).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    }
                                    break;
                                case SoulissBindingConstants.T62:
                                    logger.debug("Decoding " + SoulissBindingConstants.T62 + " packet");
                                    if (getFloatAtSlot(mac, slot) != Float.NaN) {
                                        ((SoulissT62Handler) typ.getHandler()).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    }
                                    break;
                                case SoulissBindingConstants.T63:
                                    logger.debug("Decoding " + SoulissBindingConstants.T63 + " packet");
                                    if (getFloatAtSlot(mac, slot) != Float.NaN) {
                                        ((SoulissT63Handler) typ.getHandler()).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    }
                                    break;
                                case SoulissBindingConstants.T64:
                                    logger.debug("Decoding " + SoulissBindingConstants.T64 + " packet");
                                    if (getFloatAtSlot(mac, slot) != Float.NaN) {
                                        ((SoulissT64Handler) typ.getHandler()).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    }
                                    break;
                                case SoulissBindingConstants.T65:
                                    logger.debug("Decoding " + SoulissBindingConstants.T65 + " packet");
                                    if (getFloatAtSlot(mac, slot) != Float.NaN) {
                                        ((SoulissT65Handler) typ.getHandler()).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    }
                                    break;
                                case SoulissBindingConstants.T66:
                                    logger.debug("Decoding " + SoulissBindingConstants.T66 + " packet");
                                    if (getFloatAtSlot(mac, slot) != Float.NaN) {
                                        ((SoulissT66Handler) typ.getHandler()).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    }
                                    break;
                                case SoulissBindingConstants.T67:
                                    logger.debug("Decoding " + SoulissBindingConstants.T67 + " packet");
                                    if (getFloatAtSlot(mac, slot) != Float.NaN) {
                                        ((SoulissT67Handler) typ.getHandler()).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    }
                                    break;
                                case SoulissBindingConstants.T68:
                                    logger.debug("Decoding " + SoulissBindingConstants.T68 + " packet");
                                    if (getFloatAtSlot(mac, slot) != Float.NaN) {
                                        ((SoulissT68Handler) typ.getHandler()).setState(
                                                DecimalType.valueOf(Float.toString(getFloatAtSlot(mac, slot))));
                                    }
                                    break;

                                default:
                                    logger.debug("Unsupported typical");
                            }
                        } catch (ClassCastException ex) {
                            logger.debug(ex.getMessage());
                        }

                    }
                }
            }
        }
    }

    private OnOffType getOHStateFromSoulissVal(short sVal) {
        if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OnCoil) {
            return OnOffType.ON;
        } else if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OffCoil) {
            return OnOffType.OFF;
        }
        return null;
    }

    private Short getByteAtSlot(ArrayList<Short> mac, int slot) {
        return mac.get(5 + slot);

    }

    private float getFloatAtSlot(ArrayList<Short> mac, int slot) {
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

}
