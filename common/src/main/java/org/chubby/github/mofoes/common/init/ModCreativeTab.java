package org.chubby.github.mofoes.common.init;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.chubby.github.mofoes.Mofoes;

public class ModCreativeTab
{
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Mofoes.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> MY_TAB = TABS.register(
            "mofoes_tab", // Tab ID
            () -> CreativeTabRegistry.create(
                    Component.translatable("category.mofoes.tab"), // Tab Name
                    () -> new ItemStack(ModItems.BISON_SPAWN_EGG.get()) // Icon
            )
    );
}
