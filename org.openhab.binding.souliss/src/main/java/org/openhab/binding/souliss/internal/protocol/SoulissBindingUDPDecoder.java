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

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingProtocolConstants;
import org.openhab.binding.souliss.SoulissBindingUDPConstants;
import org.openhab.binding.souliss.handler.SoulissGenericTypical;
import org.openhab.binding.souliss.handler.SoulissT11Handler;
import org.openhab.binding.souliss.internal.protocol.SoulissDiscover.DiscoverResult;
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
        logger.debug("decodeMacaco: Received functional code: 0x" + Integer.toHexString(functionalCode));
        switch (functionalCode) {

            case SoulissBindingUDPConstants.Souliss_UDP_function_ping_resp:
                logger.debug("function_ping_resp");
                decodePing(macacoPck);
                break;
            case SoulissBindingUDPConstants.Souliss_UDP_function_discover_GW_node_bcas_resp:
                logger.debug("function_ping_broadcast_resp");
                try {
                    decodePingBroadcast(macacoPck);
                } catch (UnknownHostException e) {
                    logger.debug("Error: {}", e.getLocalizedMessage());
                    e.printStackTrace();
                }
                break;

            case SoulissBindingUDPConstants.Souliss_UDP_function_subscribe_resp:
            case SoulissBindingUDPConstants.Souliss_UDP_function_poll_resp:
                logger.debug("Souliss_UDP_function_subscribe_resp / Souliss_UDP_function_poll_resp");
                decodeStateRequest(macacoPck);
                break;

            case SoulissBindingUDPConstants.Souliss_UDP_function_typreq_resp:// Answer for assigned typical logic
                logger.debug("** TypReq answer");
                decodeTypRequest(macacoPck);
                break;

            // case (byte) ConstantsUDP.Souliss_UDP_function_health_resp:// Answer
            // // nodes
            // // healty
            // logger.debug("function_health_resp");
            // decodeHealthRequest(macacoPck);
            // break;

            case (byte) SoulissBindingUDPConstants.Souliss_UDP_function_db_struct_resp:// Answer
                // nodes
                logger.debug("function_db_struct_resp");
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
            logger.debug("--DECODE MACACO OFFSET: {} NUMOF: {} TYPICALSXNODE: {}", tgtnode, numberOf, typXnodo);
            // // creates Souliss nodes
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

            logger.debug("decodeDBStructRequest");
            logger.debug("Nodes: " + nodes);
            logger.debug("maxnodes: " + maxnodes);
            logger.debug("maxTypicalXnode: " + maxTypicalXnode);
            logger.debug("maxrequests: " + maxrequests);
            // logger.debug("MaCaco_IN_S: " + MaCaco_IN_S);
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
                        short sVal = getByteAtSlot(mac, slot);

                        // update Txx
                        switch (sUID_Array[1]) {
                            case SoulissBindingConstants.T11:
                                OnOffType typicalState = null;
                                if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OnCoil) {
                                    typicalState = OnOffType.ON;
                                } else if (sVal == SoulissBindingProtocolConstants.Souliss_T1n_OffCoil) {
                                    typicalState = OnOffType.OFF;
                                }
                                ((SoulissT11Handler) typ.getHandler()).setState(typicalState);
                                 cercare di capire come forzare un update
                                break;
                            case SoulissBindingConstants.T12:
                                break;
                            case SoulissBindingConstants.T13:
                                break;
                            case SoulissBindingConstants.T14:
                                break;
                            case SoulissBindingConstants.T52:
                                break;
                            case SoulissBindingConstants.T53:
                                break;
                            case SoulissBindingConstants.T55:
                                break;
                            case SoulissBindingConstants.T57:
                                break;

                        }

                    }
                }

            }

        }
        // SoulissThingState SoulisTState = new SoulissThingState();

        // // QUI. AGGIORNAMENTO DEL TIMESTAMP PER OGNI NODO. DA FARE USANDO NODI
        // // FITTIZI
        // SoulissHandlerFactory sHFactory = new org.openhab.binding.souliss.internal.SoulissHandlerFactory();
        // if (sHFactory.supportsThingType(SoulissBindingConstants.GATEWAY_THING_TYPE)) {
        //
        // }

        // SoulissTServiceUpdater.updateTIMESTAMP(soulissTypicalsRecipients,
        // tgtnode);
        // // sfoglio hashtable e scelgo tipici del nodo indicato nel frame
        // // leggo valore tipico in base allo slot
        // synchronized (this) {
        // Iterator<Entry<String, SoulissGenericTypical>> iteratorTypicals = soulissTypicalsRecipients
        // .getIterator();
        // while (iteratorTypicals.hasNext()) {
        // SoulissGenericTypical typ = iteratorTypicals.next().getValue();
        // // se il tipico estratto appartiene al nodo che il frame deve
        // // aggiornare...
        // bDecoded_forLOG = false;
        // if (typ.getSoulissNodeID() == tgtnode) {
        //
        // // ...allora controllo lo slot
        // int slot = typ.getSlot();
        // // ...ed aggiorno lo stato in base al tipo
        // int iNumBytes = 0;
        //
        // try {
        // String sHex = Integer.toHexString(typ.getType());
        // String sRes = SoulissNetworkParameter
        // .getPropTypicalBytes(sHex.toUpperCase());
        // if (sRes != null)
        // iNumBytes = Integer.parseInt(sRes);
        // } catch (NumberFormatException e) {
        // e.printStackTrace();
        // iNumBytes = 0;
        // }
        // float val = 0;
        // // ***** T1A *****
        // if (typ.getType() == 0x1A) {
        // short sVal = getByteAtSlot(mac, slot);
        // ((SoulissT1A) typ).setState(sVal);
        // bDecoded_forLOG = true;
        // // ***** T19 *****
        // } else if (typ.getType() == 0x19) {
        // // set value of T19 at number of second slot
        // short sVal = getByteAtSlot(mac, slot + 1);
        // typ.setState(sVal);
        // bDecoded_forLOG = true;
        // } else if (iNumBytes == 1) {
        // // caso valori digitali
        // val = getByteAtSlot(mac, slot);
        // typ.setState(val);
        // bDecoded_forLOG = true;
        // } else if (iNumBytes == 2) {
        // // caso valori float
        // val = getFloatAtSlot(mac, slot);
        // typ.setState(val);
        // bDecoded_forLOG = true;
        // } else if (iNumBytes == 4) {
        // // ***** T16 RGB *****
        // val = getByteAtSlot(mac, slot);
        // typ.setState(val);
        // ((SoulissT16) typ).setStateRED(getByteAtSlot(mac,
        // slot + 1));
        // ((SoulissT16) typ).setStateGREEN(getByteAtSlot(mac,
        // slot + 2));
        // ((SoulissT16) typ).setStateBLU(getByteAtSlot(mac,
        // slot + 3));
        // bDecoded_forLOG = true;
        // } else if (iNumBytes == 5) {
        // // ***** T31 *****
        // // *******************
        // // SLOT 0: Control State
        // short sVal = getByteAtSlot(mac, slot);
        // ((SoulissT31) typ).setRawCommandState(sVal);
        // /*
        // * The control state bit meaning follow as:
        // * BIT 0 Not used
        // * BIT 1 (0 Heating OFF , 1 Heating ON)
        // * BIT 2 (0 Cooling OFF , 1 Cooling ON)
        // * BIT 3 (0 Fan 1 OFF , 1 Fan 1 ON)
        // * BIT 4 (0 Fan 2 OFF , 1 Fan 2 ON)
        // * BIT 5 (0 Fan 3 OFF , 1 Fan 3 ON)
        // * BIT 6 (0 Manual Mode , 1 Automatic Mode for Fan)
        // * BIT 7 (0 Heating Mode, 1 Cooling Mode)
        // */
        //
        // ((SoulissT31) typ).power.setState(getBitState(sVal, 0));
        // ((SoulissT31) typ).heating.setState(getBitState(sVal, 1));
        // ((SoulissT31) typ).cooling.setState(getBitState(sVal, 2));
        // ((SoulissT31) typ).fanLow.setState(getBitState(sVal, 3));
        // ((SoulissT31) typ).fanMed.setState(getBitState(sVal, 4));
        // ((SoulissT31) typ).fanHigh.setState(getBitState(sVal, 5));
        // ((SoulissT31) typ).fanAutoMode.setState(getBitState(sVal, 6));
        // ((SoulissT31) typ).heatingCoolingModeValue.setState(getBitState(sVal, 7));
        //
        //
        // // SLOT 1-2: Temperature Measured Value
        // val = getFloatAtSlot(mac, slot + 1);
        // ((SoulissT31) typ).setMeasuredValue(val);
        // // SLOT 3-4: Temperature Setpoint Value
        // val = getFloatAtSlot(mac, slot + 3);
        // ((SoulissT31) typ).setSetpointValue(val);
        // bDecoded_forLOG = true;
        // }
        // // non esegue per healt e timestamp, perchÃ¨ il LOG viene
        // // inserito in un altro punto del codice
        // if (typ.getType() != 152 && typ.getType() != 153)
        // if (iNumBytes == 4)
        // // RGB Log
        // logger.debug(
        // "decodeStateRequest: {} ({}) = {}. RGB= {}, {}, {}",
        // typ.getName(),
        // Short.valueOf(typ.getType()),
        // ((SoulissT16) typ).getState(),
        // ((SoulissT16) typ).getStateRED(),
        // ((SoulissT16) typ).getStateGREEN(),
        // ((SoulissT16) typ).getStateBLU());
        // else if (iNumBytes == 5) {
        // // T31 Thermostat
        // logger.debug(
        // "decodeStateRequest: {} ({}). Thermostat= {}, Temp.Measured= {}, Temp.SetPoint= {}",
        // typ.getName(),
        // Short.valueOf(typ.getType()),
        // ((SoulissT31) typ).getRawCommandState(),
        // ((SoulissT31) typ)
        // .getTemperatureMeasuredValue(),
        // ((SoulissT31) typ).getSetpointValue());
        //
        // } else if (bDecoded_forLOG) {
        // if (typ.getType() == 0x1A) {
        // logger.debug(
        // "decodeStateRequest: {} (0x{}) = {}",
        // typ.getName(),
        // Integer.toHexString(typ.getType()),
        // Integer.toBinaryString(((SoulissT1A) typ)
        // .getRawState()));
        // } else
        // logger.debug(
        // "decodeStateRequest: {} (0x{}) = {}",
        // typ.getName(),
        // Integer.toHexString(typ.getType()),
        // Float.valueOf(val));
        // }
        // }
        // }
        // }
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
}
