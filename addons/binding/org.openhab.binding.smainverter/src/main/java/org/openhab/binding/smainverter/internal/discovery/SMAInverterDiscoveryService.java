/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.smainverter.internal.discovery;

import com.github.andy2003.smareader.SmaReader;
import com.github.andy2003.smareader.inverter.InvDeviceClass;
import com.github.andy2003.smareader.inverter.Inverter;
import com.github.andy2003.smareader.inverter.InverterDataType;
import com.github.andy2003.smareader.inverter.LoginException;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.smainverter.internal.SMAInverterConfiguration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.smainverter.internal.SMAInverterBindingConstants.*;

/**
 * The {@link SMAInverterDiscoveryService} class implements a service
 * for discovering the SMA Energy Meter.
 *
 * @author Andreas Berger - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.smaenergymeter")
public class SMAInverterDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(SMAInverterDiscoveryService.class);

    public SMAInverterDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, 15, false);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    protected void startBackgroundDiscovery() {
        logger.debug("Start SMAInverter background discovery");
        scheduler.schedule(this::discover, 0, TimeUnit.SECONDS);
    }

    @Override
    public void startScan() {
        logger.debug("Start SMAInverter scan");
        discover();
    }

    private synchronized void discover() {
        logger.debug("Try to discover all SMA Inverter devices");
        try (SmaReader smaApi = new SmaReader()) {
            Collection<Inverter> inverter = smaApi.detectDevices();
            if (inverter.isEmpty()) {
                logger.debug("No SMA Inverter found.");
                return;
            }
            for (Inverter inv : inverter) {
                discoverInverter(inv);
            }
        } catch (java.io.IOException e) {
            logger.warn("", e);
        }
    }

    private void discoverInverter(Inverter inv) throws IOException {
        try {
            inv.logon(DEFAULT_PASSWORD);

            String id = String.valueOf(inv.getSerial());

            Map<String, Object> properties = new HashMap<>();

            properties.put(Thing.PROPERTY_VENDOR, "SMA");
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, id);
            properties.put(SMAInverterConfiguration.IP, inv.getIp());
            properties.put(SMAInverterConfiguration.PASSWORD, DEFAULT_PASSWORD);

            ThingUID uid = new ThingUID(THING_TYPE_SOLAR_INVERTER, id);
            if (inv.getInverterData(InverterDataType.TypeLabel)) {
                properties.put(Thing.PROPERTY_MODEL_ID, inv.getDeviceType());
                if (inv.getDeviceClass() == InvDeviceClass.BatteryInverter) {
                    uid = new ThingUID(THING_TYPE_BATTERY_INVERTER, id);
                }
            }
            DiscoveryResult result = DiscoveryResultBuilder.create(uid).withProperties(properties)
                    .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).withLabel(inv.getDeviceType() + " " + id)
                    .build();
            thingDiscovered(result);
            logger.debug("Thing discovered '{}'", result);
        } catch (LoginException e) {
            logger.debug("", e);
        } finally {
            inv.close();
        }
    }

}
