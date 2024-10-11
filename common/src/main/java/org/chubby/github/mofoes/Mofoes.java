package org.chubby.github.mofoes;

import com.mojang.brigadier.CommandDispatcher;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.chubby.github.mofoes.common.commands.CheckSpawnCommand;
import org.chubby.github.mofoes.common.entity.ai.memory.MemoryModules;
import org.chubby.github.mofoes.common.entity.ai.sensor.ModSensors;
import org.chubby.github.mofoes.common.init.ModCreativeTab;
import org.chubby.github.mofoes.common.init.ModEntities;
import org.chubby.github.mofoes.common.init.ModItems;

public final class Mofoes {
    public static final String MOD_ID = "mofoes";

    public static void init() {
        ModEntities.ENTITIES.register();
        MemoryModules.register();
        ModSensors.SENSOR.register();
        ModItems.ITEMS.register();
        ModCreativeTab.TABS.register();
        ModEntities.registerSpawnPlacements();
       // CheckSpawnCommand.register(new CommandDispatcher<>());
        registerCommands();
    }

    public static void registerCommands()
    {
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> CheckSpawnCommand.register(dispatcher));
    }
}
