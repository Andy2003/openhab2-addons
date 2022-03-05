/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.smainverter.internal;

/**
 * The {@link SMAInverterConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Andreas Berger - Initial contribution
 */
public class SMAInverterConfiguration {

    public static final String IP = "ip";
    public static final String PASSWORD = "password";

    /**
     * IP address
     */
    public String ip;
    public String password;
    public Integer pollingPeriod = 60;
}
