package org.chubby.github.mofoes.common.entity.ai.sensor;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import org.chubby.github.mofoes.Mofoes;
import org.chubby.github.mofoes.common.entity.EntityHogmen;
import org.chubby.github.mofoes.common.entity.komodo.KomodoZealot;

public class ModSensors
{
    public static final DeferredRegister<SensorType<?>> SENSOR = DeferredRegister.create(
            Mofoes.MOD_ID, Registries.SENSOR_TYPE
    );

    public static final RegistrySupplier<SensorType<HogmenSensor>> HOGMEN_SENSOR = SENSOR.register(
            new ResourceLocation(Mofoes.MOD_ID,"hogmen_sensor"),
            () -> new SensorType<>(HogmenSensor::new)
    );
    public static final RegistrySupplier<SensorType<HogmenBruteSensor>> HOGMEN_BRUTE_SENSOR = SENSOR.register(
            new ResourceLocation(Mofoes.MOD_ID,"hogmen_brute_sensor"),
            () -> new SensorType<>(HogmenBruteSensor::new)
    );
 }
