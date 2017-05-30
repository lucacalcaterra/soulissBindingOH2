package org.openhab.binfing.souliss.primitivetypes;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.Convertible;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.State;

public enum FanModeType implements PrimitiveType, State, Command, Convertible {
    AUTO,
    MANUAL,
    LOW,
    MEDIUM,
    HIGH;

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