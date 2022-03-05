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

import static org.openhab.binding.smainverter.internal.SMAInverterBindingConstants.*;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.andy2003.smareader.SmaReader;
import com.github.andy2003.smareader.inverter.*;

/**
 * The {@link SMAInverterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class SMAInverterHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SMAInverterHandler.class);

    private @Nullable SMAInverterConfiguration config;

    private @Nullable ScheduledFuture<?> pollingJob;

    public SMAInverterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateData();
        } else {
            logger.warn("This binding is a read-only binding and cannot handle commands");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing SMAInverter handler '{}'", getThing().getUID());
        config = getConfigAs(SMAInverterConfiguration.class);
        try (SmaReader smaApi = new SmaReader(); Inverter inverter = smaApi.createInverter(config.ip)) {
            inverter.logon(config.password);
            updateProperty(Thing.PROPERTY_VENDOR, "SMA");
            updateProperty(Thing.PROPERTY_SERIAL_NUMBER, String.valueOf(inverter.getSerial()));
            if (inverter.getInverterData(InverterDataType.TypeLabel)) {
                updateProperty(Thing.PROPERTY_MODEL_ID, inverter.getDeviceType());
            }
            logger.debug("Found a SMA Inverter with S/N '{}'", inverter.getSerial());

            updateData(inverter);
        } catch (IOException | LoginException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, e.getMessage());
            return;
        }

        int pollingPeriod = (config.pollingPeriod == null) ? 60 : config.pollingPeriod;
        pollingJob = scheduler.scheduleWithFixedDelay(this::updateData, 0, pollingPeriod, TimeUnit.SECONDS);
        logger.debug("Polling job scheduled to run every {} sec. for '{}'", pollingPeriod, getThing().getUID());

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing SMAEnergyMeter handler '{}'", getThing().getUID());

        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }
    }

    private synchronized void updateData() {
        logger.debug("Update SMAInverter data '{}'", getThing().getUID());

        try (SmaReader smaApi = new SmaReader(); Inverter inverter = smaApi.createInverter(config.ip)) {
            inverter.logon(config.password);
            updateData(inverter);
        } catch (IOException | LoginException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void updateData(Inverter inverter) throws IOException {
        if (getThing().getThingTypeUID().equals(SMAInverterBindingConstants.THING_TYPE_BATTERY_INVERTER)) {
            if (inverter.getInverterData(InverterDataType.BatteryChargeStatus)) {
                updateState(CHANNEL_STATE_OF_CHARGE, inverter.getValue(LriDef.BatChaStt));
            }
        } else {
            if (inverter.getInverterData(InverterDataType.SpotACPower)) {
                TagValue<Number> l1 = inverter.getValue(LriDef.SPOT_PAC1);
                TagValue<Number> l2 = inverter.getValue(LriDef.SPOT_PAC2);
                TagValue<Number> l3 = inverter.getValue(LriDef.SPOT_PAC3);
                updateState(CHANNEL_POWER_OUT_AC_L1, l1);
                updateState(CHANNEL_POWER_OUT_AC_L2, l2);
                updateState(CHANNEL_POWER_OUT_AC_L3, l3);
                updateState(CHANNEL_POWER_OUT_AC_TOTAL, sum(l1, l2, l3));
            }
            if (inverter.getInverterData(InverterDataType.EnergyProduction)) {
                updateState(CHANNEL_ENERGY_OUT_COUNTER, inverter.getValue(LriDef.SPOT_ETOTAL));
            }
        }
        if (getThing().getStatus().equals(ThingStatus.OFFLINE)) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @SafeVarargs
    private State sum(TagValue<Number>... tagValues) {
        double sum = 0d;
        for (TagValue<Number> tagValue : tagValues) {
            if (tagValue != null) {
                sum += tagValue.getValue().doubleValue();
            }
        }
        return new DecimalType(sum);
    }

    private void updateState(String channel, @Nullable TagValue<Number> value) {
        if (value == null) {
            updateState(channel, UnDefType.UNDEF);
        } else {
            updateState(channel, new DecimalType(value.getValue().doubleValue()));
        }
    }
}
