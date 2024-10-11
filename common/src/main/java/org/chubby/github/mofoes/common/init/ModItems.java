package org.chubby.github.mofoes.common.init;

import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import org.chubby.github.mofoes.Mofoes;

public class ModItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Mofoes.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<SpawnEggItem> BISON_SPAWN_EGG = ITEMS.register("bison_spawn_egg",
            ()-> new ArchitecturySpawnEggItem(ModEntities.BISON,0x0000,0x0000,new Item.Properties().arch$tab(ModCreativeTab.MY_TAB)));

    public static final RegistrySupplier<SpawnEggItem> HOGMEN_SPAWN_EGG = ITEMS.register("hogmen_spawn_egg",
            ()-> new ArchitecturySpawnEggItem(ModEntities.HOGMEN,0x0000,0x0000,new Item.Properties().arch$tab(ModCreativeTab.MY_TAB)));
    public static final RegistrySupplier<SpawnEggItem> HOGMEN_BRUTE_SPAWN_EGG = ITEMS.register("hogmen_brute_spawn_egg",
            ()-> new ArchitecturySpawnEggItem(ModEntities.HOGMEN_BRUTE,0x0000,0x0000,new Item.Properties().arch$tab(ModCreativeTab.MY_TAB)));

    public static final RegistrySupplier<SpawnEggItem> KOMODO_SPAWN_EGG = ITEMS.register("komodo_spawn_egg",
            ()-> new ArchitecturySpawnEggItem(ModEntities.KOMODO,0x0000,0x0000,new Item.Properties().arch$tab(ModCreativeTab.MY_TAB)));
    public static final RegistrySupplier<SpawnEggItem> KOMODO_BRUTE_SPAWN_EGG = ITEMS.register("komodo_brute_spawn_egg",
            ()-> new ArchitecturySpawnEggItem(ModEntities.KOMODO_BRUTE,0x0000,0x0000,new Item.Properties().arch$tab(ModCreativeTab.MY_TAB)));
    public static final RegistrySupplier<SpawnEggItem> KOMODO_SORCERER_SPAWN_EGG = ITEMS.register("komodo_sorcerer_spawn_egg",
            ()-> new ArchitecturySpawnEggItem(ModEntities.KOMODO_SORCERER,0x0000,0x0000,new Item.Properties().arch$tab(ModCreativeTab.MY_TAB)));
    public static final RegistrySupplier<SpawnEggItem> KOMODO_ZEALOT_SPAWN_EGG = ITEMS.register("komodo_zealot_spawn_egg",
            ()-> new ArchitecturySpawnEggItem(ModEntities.KOMODO_ZEALOT,0x0000,0x0000,new Item.Properties().arch$tab(ModCreativeTab.MY_TAB)));

    public static final RegistrySupplier<SpawnEggItem> STALKER_ENDERMAN_SPAWN_EGG = ITEMS.register("stalker_enderman_spawn_egg",
            ()-> new ArchitecturySpawnEggItem(ModEntities.STALKER_ENDERMAN,0x0000,0x0000,new Item.Properties().arch$tab(ModCreativeTab.MY_TAB)));
}
