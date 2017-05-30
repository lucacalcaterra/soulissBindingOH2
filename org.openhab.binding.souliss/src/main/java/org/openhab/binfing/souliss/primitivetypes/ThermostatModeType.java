package org.openhab.binfing.souliss.primitivetypes;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.Convertible;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.State;

public enum ThermostatModeType implements PrimitiveType, State, Command, Convertible {
    HEATING_MODE,
    COOLING_MODE,
    HEATING_ON,
    HEATING_OFF,
    COOLING_ON,
    COOLING_OFF,
    POWER;

    @Override
    public String format(String pattern) {
        return String.format(pattern, this.toString());
    }

    @Override
    public String toFullString() {
        return toFullString();
    }

    @Override
    public State as(Class<? extends State> target) {
        // TODO Auto-generated method stub
        return null;
    }

}