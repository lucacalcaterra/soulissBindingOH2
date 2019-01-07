/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.souliss.internal.protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingUDPConstants;
import org.openhab.binding.souliss.handler.SoulissGatewayHandler;
import org.openhab.binding.souliss.handler.SoulissGatewayJobHealty;
import org.openhab.binding.souliss.handler.SoulissGenericHandler;
import org.openhab.binding.souliss.handler.SoulissT11Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provide to take packet, and send it to regular interval to Souliss
 * Network
 *
 * @author Tonino Fazio
 * @since 1.7.0
 */
public class SoulissBindingSendDispatcherJob implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(SoulissGatewayJobHealty.class);
    // private String _iPAddressOnLAN;
    // private short _userIndex;
    // private short _nodeIndex;
    private int _maxDelay;
    private SoulissGatewayHandler gw;
    static boolean bPopSuspend = false;
    public static ArrayList<SoulissBindingSocketAndPacketStruct> packetsList = new ArrayList<SoulissBindingSocketAndPacketStruct>();
    private long start_time = System.currentTimeMillis();
    static String _iPAddressOnLAN;
    static int iDelay = 0; // equal to 0 if array is empty
    static int SEND_MIN_DELAY = 0;

    public SoulissBindingSendDispatcherJob(Bridge bridge) {
        gw = (SoulissGatewayHandler) bridge.getHandler();
        _iPAddressOnLAN = gw.IPAddressOnLAN;
        // _userIndex = gw.userIndex;
        // _nodeIndex = gw.nodeIndex;
        set_sendMaxDelay(gw.sendRefreshInterval);
    }

    /**
     * Put packet to send in ArrayList PacketList
     */
    public synchronized static void put(DatagramSocket socket, DatagramPacket packetToPUT) {

        bPopSuspend = true;
        boolean bPacchettoGestito = false;
        // estraggo il nodo indirizzato dal pacchetto in ingresso
        // restituisce -1 se il pacchetto non è del tipo Souliss_UDP_function_force
        int node = getNode(packetToPUT);
        if (node >= 0) {
            logger.debug("Push packet in queue - Node {}", node);
        } else {
            logger.debug("Push packet in queue");
        }

        if (packetsList.size() == 0 || node < 0) {
            bPacchettoGestito = false;
        } else {
            // OTTIMIZZATORE
            // scansione lista pacchetti da inviare
            for (int i = 0; i < packetsList.size(); i++) {
                if (node >= 0 && getNode(packetsList.get(i).packet) == node && !packetsList.get(i).isSent()) {
                    // frame per lo stesso nodo già  presente in lista
                    logger.debug("Frame UPD per nodo {} già presente in coda. Esecuzione ottimizzazione.", node);
                    bPacchettoGestito = true;
                    // se il pacchetto da inserire è più corto (o uguale) di
                    // quello in coda allora sovrascrivo i byte del pacchetto
                    // presente nella coda
                    if (packetToPUT.getData().length <= packetsList.get(i).packet.getData().length) {
                        // scorre i byte di comando e se il byte è diverso da
                        // zero sovrascrive il byte presente nel pacchetto in
                        // coda
                        logger.debug("Optimizer.             Packet to push: " + MaCacoToString(packetToPUT.getData()));
                        logger.debug("Optimizer.             Previous frame: "
                                + MaCacoToString(packetsList.get(i).packet.getData()));
                        // i valori dei tipici partono dal byte 12 in poi
                        for (int j = 12; j < packetToPUT.getData().length; j++) {
                            // se il j-esimo byte è diverso da zero allora lo
                            // sovrascrivo al byte del pacchetto già presente
                            if (packetToPUT.getData()[j] != 0) {
                                packetsList.get(i).packet.getData()[j] = packetToPUT.getData()[j];
                            }
                        }
                        logger.debug("Optimizer. Previous frame modified to: "
                                + MaCacoToString(packetsList.get(i).packet.getData()));
                    } else {
                        // se il pacchetto da inserire è più lungo di quello in
                        // lista allora sovrascrivo i byte del pacchetto da
                        // inserire, poi elimino quello in lista ed inserisco
                        // quello nuovo
                        if (packetToPUT.getData().length > packetsList.get(i).packet.getData().length) {
                            for (int j = 12; j < packetsList.get(i).packet.getData().length; j++) {
                                // se il j-esimo byte è diverso da zero allora
                                // lo sovrascrivo al byte del pacchetto già
                                // presente
                                if (packetsList.get(i).packet.getData()[j] != 0) {
                                    if (packetToPUT.getData()[j] == 0) {
                                        // sovrascrive i byte dell'ultimo frame
                                        // soltanto se il byte è uguale a zero.
                                        // Se è diverso da zero l'ultimo frame
                                        // ha la precedenza e deve sovrascrivere
                                        packetToPUT.getData()[j] = packetsList.get(i).packet.getData()[j];
                                    }
                                }
                            }
                            // rimuove il pacchetto
                            logger.debug(
                                    "Optimizer. Remove frame: " + MaCacoToString(packetsList.get(i).packet.getData()));
                            packetsList.remove(i);
                            // inserisce il nuovo
                            logger.debug("Optimizer. Add frame: " + MaCacoToString(packetToPUT.getData()));
                            packetsList.add(new SoulissBindingSocketAndPacketStruct(socket, packetToPUT));
                        }
                    }
                }
            }
        }

        if (!bPacchettoGestito) {
            logger.debug("Add frame: " + MaCacoToString(packetToPUT.getData()));
            packetsList.add(new SoulissBindingSocketAndPacketStruct(socket, packetToPUT));
        }
        bPopSuspend = false;
    }

    private ScheduledFuture<?> UDPserverJob_DefaultPort;

    @Override
    public void run() {
        try {

            if (checkTime()) {

                SoulissBindingSocketAndPacketStruct sp = pop();
                if (sp != null) {
                    logger.debug(
                            "SendDispatcherJob - Functional Code 0x{} - Packet: {} - Elementi rimanenti in lista: {}",
                            Integer.toHexString(sp.packet.getData()[7]), MaCacoToString(sp.packet.getData()),
                            packetsList.size());

                    sp.socket.send(sp.packet);
                }
                // confronta gli stati in memoria con i frame inviati. Se
                // corrispondono cancella il frame dalla lista inviati
                SoulissBindingSendDispatcherJob.safeSendCheck();
                resetTime();
            }
        } catch (IOException e) {
            logger.warn(e.getMessage());
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }

    }

    public int get_sendMaxDelay() {
        return _maxDelay;
    }

    public void set_sendMaxDelay(int _refreshInterval) {
        this._maxDelay = _refreshInterval;
    }

    /**
     * Get node number from packet
     */
    private static int getNode(DatagramPacket packet) {
        // 7 è il byte del frame VNet al quale trovo il codice comando
        // 10 è il byte del frame VNet al quale trovo l'ID del nodo
        if (packet.getData()[7] == SoulissBindingUDPConstants.Souliss_UDP_function_force) {
            return packet.getData()[10];
        }
        return -1;
    }

    private static String MaCacoToString(byte[] frame2) {
        byte[] frame = frame2.clone();
        StringBuilder sb = new StringBuilder();
        sb.append("HEX: [");
        for (byte b : frame) {
            sb.append(String.format("%02X ", b));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * check frame updates with packetList, where flag "sent" is true. If all
     * commands was executed there delete packet in list.
     * Confronta gli aggiornamenti ricevuti con i frame inviati. Se corrispondono allora cancella il
     * frame nella lista inviati .
     */
    public static void safeSendCheck() {
        // short sVal = getByteAtSlot(macacoFrame, slot);
        // scansione lista paccetti inviati
        for (int i = 0; i < packetsList.size(); i++) {
            if (packetsList.get(i).isSent()) {
                int node = getNode(packetsList.get(i).packet);
                int iSlot = 0;
                for (int j = 12; j < packetsList.get(i).packet.getData().length; j++) {
                    // controllo lo slot solo se il comando è diverso da ZERO
                    if (packetsList.get(i).packet.getData()[j] != 0) {
                        // recupero tipico dalla memoria
                        SoulissGenericHandler typ = getHandler(_iPAddressOnLAN, node, iSlot);

                        // traduce il comando inviato con lo stato previsto e
                        // poi fa il confronto con lo stato attuale
                        if (logger.isDebugEnabled() && typ != null) {
                            String s1 = Integer.toHexString(
                                    (int) ((SoulissT11Handler) typ.getThing().getHandler()).getFeedbackState());
                            String sStateMemoria = s1.length() < 2 ? "0x0" + s1.toUpperCase() : "0x" + s1.toUpperCase();

                            String sCmd = Integer.toHexString(packetsList.get(i).packet.getData()[j]);
                            sCmd = sCmd.length() < 2 ? "0x0" + sCmd.toUpperCase() : "0x" + sCmd.toUpperCase();
                            logger.debug(
                                    "Compare. Node: {} Slot: {} Typical: {} Command: {} EXPECTED: {} - IN MEMORY: {}",
                                    node, iSlot, Integer.toHexString(typ.getType()), sCmd,
                                    expectedState(typ.getType(), packetsList.get(i).packet.getData()[j]),
                                    sStateMemoria);
                        }
                        //
                        // if (typ != null && checkExpectedState((int) typ.getState(),
                        // expectedState(typ.getType(), packetsList.get(i).packet.getData()[j]))) {
                        // // se il valore del tipico coincide con il valore
                        // // trasmesso allora pongo il byte a zero.
                        // // quando tutti i byte saranno uguale a zero allora
                        // // si
                        // // cancella il frame
                        // packetsList.get(i).packet.getData()[j] = 0;
                        // logger.debug("T{} Node: {} Slot: {} - OK Expected State",
                        // Integer.toHexString(typ.getType()), node, iSlot);
                        // } else if (typ == null) {
                        // // se allo slot j non esiste un tipico allora vuol dire che si tratta di uno slot collegato
                        // // al precedente (es: RGB, T31,...)
                        // // allora se lo slot j-1=0 allora anche j puÃ² essere messo a 0
                        // if (packetsList.get(i).packet.getData()[j - 1] == 0) {
                        // packetsList.get(i).packet.getData()[j] = 0;
                        // }
                        // }
                    }
                    iSlot++;
                }
                // if (checkAllsSlotZero(packetsList.get(i).packet)) {
                // logger.debug("Command packet executed - Removed");
                // packetsList.remove(i);
                // } else {
                // // se il frame non Ã¨ uguale a zero controllo il TIMEOUT e se
                // // Ã¨ scaduto allora pongo il flag SENT a false
                // long t = System.currentTimeMillis();
                // if (SoulissNetworkParameter.SECURE_SEND_TIMEOUT_TO_REQUEUE < t - packetsList.get(i).getTime()) {
                // if (SoulissNetworkParameter.SECURE_SEND_TIMEOUT_TO_REMOVE_PACKET < t
                // - packetsList.get(i).getTime()) {
                // logger.info("Packet Execution timeout - Removed");
                // packetsList.remove(i);
                // } else {
                // logger.info("Packet Execution timeout - Requeued");
                // packetsList.get(i).setSent(false);
                // }
                // }
                // }
            }
        }
    }

    private static SoulissGenericHandler getHandler(String _iPAddressOnLAN, int node, int slot) {
        // recupero il riferimento al gateway
        SoulissGatewayHandler gateway = null;
        short _lastByteGatewayIP = Short.parseShort(_iPAddressOnLAN.split("\\.")[3]);
        try {
            gateway = (SoulissGatewayHandler) SoulissBindingNetworkParameters.getGateway(_lastByteGatewayIP)
                    .getHandler();
        } catch (Exception ex) {
        }

        Iterator thingsIterator;
        if (gateway != null && gateway.IPAddressOnLAN != null
                && Short.parseShort(gateway.IPAddressOnLAN.split("\\.")[3]) == _lastByteGatewayIP) {
            thingsIterator = gateway.getThing().getThings().iterator();
            boolean bFound = false;
            Thing typ = null;
            while (thingsIterator.hasNext() && !bFound) {
                typ = (Thing) thingsIterator.next();
                String sUID_Array[] = typ.getUID().getAsString().split(":");
                SoulissGenericHandler handler = (SoulissGenericHandler) typ.getHandler();
                if (handler != null) { // execute it only if binding is Souliss and update is for my
                                       // Gateway
                    if (sUID_Array[0].equals(SoulissBindingConstants.BINDING_ID) && Short
                            .parseShort(handler.getGatewayIP().toString().split("\\.")[3]) == _lastByteGatewayIP) {

                        if ((handler) != null && handler.getNode() == node) {

                            return handler;
                        }
                    }
                }
            }
        }
        return null;
    }

    // private static boolean checkExpectedState(int state, String expectedState) {
    // // if expected state is null than return true. The frame will not requeued
    // if (expectedState == null) {
    // return true;
    // }
    // String s1 = String.valueOf(state);
    // String sState = s1.length() < 2 ? "0x0" + s1.toUpperCase() : "0x" + s1.toUpperCase();
    // return sState.equals(expectedState);
    // }
    //
    // private static String expectedState(short soulissType, byte command) {
    // return StateTraslator.translateCommandsToExpectedStates(soulissType, command);
    // }
    //
    // private static boolean checkAllsSlotZero(DatagramPacket packet) {
    // boolean bflag = true;
    // for (int j = 12; j < packet.getData().length; j++) {
    // if (!(packet.getData()[j] == 0)) {
    // bflag = false;
    // }
    // }
    // return bflag;
    // }

    long t, t_prec = 0;

    /**
     * Pop SocketAndPacket from ArrayList PacketList
     */
    private synchronized SoulissBindingSocketAndPacketStruct pop() {
        synchronized (this) {
            // non esegue il pop se bPopSuspend=true
            // bPopSuspend è impostato dal metodo put
            if (!bPopSuspend) {
                t = System.currentTimeMillis();
                // riporta l'intervallo al minimo solo se:
                // - la lunghezza della coda minore o uguale a 1;
                // - se è trascorso il tempo SEND_DELAY.

                if (packetsList.size() <= 1) {
                    iDelay = SEND_MIN_DELAY;
                } else {
                    iDelay = this.get_sendMaxDelay();
                }

                int iPacket = 0;
                boolean bFlagWhile = true;
                // scarta i pacchetti già  inviati
                while (!(iPacket >= packetsList.size()) && bFlagWhile) {
                    if (packetsList.get(iPacket).sent) {
                        iPacket++;
                    } else {
                        bFlagWhile = false;
                    }
                }

                boolean tFlag = (t - t_prec) >= this.get_sendMaxDelay();
                ;
                // se siamo arrivati alla fine della lista e quindi tutti i
                // pacchetti sono già  stati inviati allora pongo anche il tFlag
                // a false (come se il timeout non fosse ancora trascorso)
                if (iPacket >= packetsList.size()) {
                    tFlag = false;
                }

                if (packetsList.size() > 0 && tFlag) {
                    t_prec = System.currentTimeMillis();
                    // SocketAndPacket sp = packetsList.remove(0);

                    // estratto il primo elemento della lista
                    SoulissBindingSocketAndPacketStruct sp = packetsList.get(iPacket);

                    // GESTIONE PACCHETTO: eliminato dalla lista oppure
                    // contrassegnato come inviato se è un FORCE
                    if (packetsList.get(iPacket).packet
                            .getData()[7] == SoulissBindingUDPConstants.Souliss_UDP_function_force) {
                        // flag inviato a true
                        packetsList.get(iPacket).setSent(true);
                        // imposto time
                        packetsList.get(iPacket).setTime(System.currentTimeMillis());
                    } else {
                        packetsList.remove(iPacket);
                    }

                    logger.debug("POP: " + packetsList.size() + " packets in memory");
                    if (logger.isDebugEnabled()) {
                        int iPacketSentCounter = 0;
                        int i = 0;
                        while (!(i >= packetsList.size())) {
                            if (packetsList.get(i).sent) {
                                iPacketSentCounter++;
                            }
                            i++;
                        }
                        logger.debug("POP: " + (iPacketSentCounter) + " force frame sent");
                    }

                    logger.debug("Pop frame {} - Delay for 'SendDispatcherThread' setted to {} mills.",
                            MaCacoToString(sp.packet.getData()), iDelay);
                    return sp;
                }
            }
        }
        return null;
    }

    private void resetTime() {
        start_time = System.currentTimeMillis();
    }

    private boolean checkTime() {
        return start_time < (System.currentTimeMillis() - iDelay);
    }
}
