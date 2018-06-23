/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    public Integer pollingPeriod;

}
