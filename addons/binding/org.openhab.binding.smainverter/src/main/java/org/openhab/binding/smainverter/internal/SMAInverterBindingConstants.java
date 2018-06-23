/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.smainverter.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The {@link SMAInverterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class SMAInverterBindingConstants {

    public static final String DEFAULT_PASSWORD = "0000";

    private static final String BINDING_ID = "smainverter";

    public static final ThingTypeUID THING_TYPE_SOLAR_INVERTER = new ThingTypeUID(BINDING_ID, "solar-inverter");
    public static final ThingTypeUID THING_TYPE_BATTERY_INVERTER = new ThingTypeUID(BINDING_ID, "battery-inverter");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(THING_TYPE_SOLAR_INVERTER, THING_TYPE_BATTERY_INVERTER));

    // List of all Channel ids
    public static final String CHANNEL_STATE_OF_CHARGE = "soc";
    public static final String CHANNEL_POWER_OUT_AC_L1 = "powerOutAC_L1";
    public static final String CHANNEL_POWER_OUT_AC_L2 = "powerOutAC_L2";
    public static final String CHANNEL_POWER_OUT_AC_L3 = "powerOutAC_L3";
    public static final String CHANNEL_POWER_OUT_AC_TOTAL = "powerOutAC_Total";
    public static final String CHANNEL_ENERGY_OUT_COUNTER = "energyOutCounter";
}
