package org.chubby.github.mofoes.common.entity.ai.memory;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import org.chubby.github.mofoes.Mofoes;
import org.chubby.github.mofoes.common.entity.EntityBison;
import org.chubby.github.mofoes.common.entity.EntityHogmen;

import java.util.List;
import java.util.Optional;

public class MemoryModules {

    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULES = DeferredRegister.create(Mofoes.MOD_ID, Registries.MEMORY_MODULE_TYPE);

    public static final RegistrySupplier<MemoryModuleType<List<EntityBison>>> NEAREST_BISON = MEMORY_MODULES.register(
            new ResourceLocation(Mofoes.MOD_ID, "nearest_bison"),
            () -> new MemoryModuleType<>(Optional.empty())
    );

    public static final RegistrySupplier<MemoryModuleType<Player>> NEAREST_PLAYER_NOT_WEARING_CHAIN = MEMORY_MODULES.register(
        new ResourceLocation(Mofoes.MOD_ID,"nearest_player_not_wearing_chain"),
            ()-> new MemoryModuleType<>(Optional.empty())
    );

    public static final RegistrySupplier<MemoryModuleType<List<EntityHogmen>>> NEARBY_HOGMEN = MEMORY_MODULES.register(
            new ResourceLocation(Mofoes.MOD_ID, "nearby_hogmen"),
            ()-> new MemoryModuleType<>(Optional.empty())
    );

    public static final RegistrySupplier<MemoryModuleType<List<EntityHogmen>>> NEAREST_ADULT_HOGMEN = MEMORY_MODULES.register(
            new ResourceLocation(Mofoes.MOD_ID,"nearest_adult_hogmen"),
            ()-> new MemoryModuleType<>(Optional.empty())
    );

    public static final RegistrySupplier<MemoryModuleType<Boolean>> TELEPORTED_RECENTLY = MEMORY_MODULES.register(
            new ResourceLocation(Mofoes.MOD_ID,"teleported_recently"),
            ()-> new MemoryModuleType<>(Optional.empty())
    );



    public static void register() {
        MEMORY_MODULES.register();
    }
}
