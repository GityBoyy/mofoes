package org.chubby.github.mofoes.common.init;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import org.chubby.github.mofoes.Mofoes;
import org.chubby.github.mofoes.common.entity.EntityBison;
import org.chubby.github.mofoes.common.entity.EntityHogmen;
import org.chubby.github.mofoes.common.entity.HogmanBrute;
import org.chubby.github.mofoes.common.entity.StalkerEnderman;
import org.chubby.github.mofoes.common.entity.komodo.*;

import java.util.function.BiConsumer;

public class ModEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Mofoes.MOD_ID,Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<EntityBison>> BISON = ENTITIES.register(
            new ResourceLocation(Mofoes.MOD_ID,"bison"),Suppliers.memoize(()->EntityType.Builder.of(
                    EntityBison::new, MobCategory.CREATURE
            ).build("bison")) );

    public static final RegistrySupplier<EntityType<EntityHogmen>> HOGMEN = ENTITIES.register(
            new ResourceLocation(Mofoes.MOD_ID,"hogmen"), Suppliers.memoize(()-> EntityType.Builder.of(
                    EntityHogmen::new, MobCategory.MONSTER
            ).build("hogmen"))
    );
    public static final RegistrySupplier<EntityType<HogmanBrute>> HOGMEN_BRUTE = ENTITIES.register(
            new ResourceLocation(Mofoes.MOD_ID,"hogmen_brute"), Suppliers.memoize(()-> EntityType.Builder.of(
                    HogmanBrute::new, MobCategory.MONSTER
            ).build("hogmen_brute"))
    );

    public static final RegistrySupplier<EntityType<BasicKomodo>> KOMODO = ENTITIES.register(
            new ResourceLocation(Mofoes.MOD_ID,"komodo"),Suppliers.memoize(()-> EntityType.Builder.of(
                    BasicKomodo::new, MobCategory.MONSTER
            ).build("komodo")));
    public static final RegistrySupplier<EntityType<KomodoBrute>> KOMODO_BRUTE = ENTITIES.register(
            new ResourceLocation(Mofoes.MOD_ID,"komodo_brute"),Suppliers.memoize(()-> EntityType.Builder.of(
                    KomodoBrute::new, MobCategory.MONSTER
            ).build("komodo_brute")));
    public static final RegistrySupplier<EntityType<KomodoSorcerer>> KOMODO_SORCERER = ENTITIES.register(
            new ResourceLocation(Mofoes.MOD_ID,"komodo_sorcerer"),Suppliers.memoize(()-> EntityType.Builder.of(
                    KomodoSorcerer::new, MobCategory.MONSTER
            ).build("komodo_sorcerer")));

    public static final RegistrySupplier<EntityType<KomodoZealot>> KOMODO_ZEALOT = ENTITIES.register(
            new ResourceLocation(Mofoes.MOD_ID,"komodo_zealot"),Suppliers.memoize(()-> EntityType.Builder.of(
                    KomodoZealot::new,MobCategory.MONSTER
            ).build("komodo_zealot"))
    );

    public static final RegistrySupplier<EntityType<StalkerEnderman>> STALKER_ENDERMAN = ENTITIES.register(
            new ResourceLocation(Mofoes.MOD_ID,"stalker_enderman"), Suppliers.memoize(()-> EntityType.Builder.of(
                    StalkerEnderman::new,MobCategory.MONSTER
            ).build("stalker_enderman")));


    public static void registerAttributes(BiConsumer<Supplier<? extends EntityType<? extends LivingEntity>>, Supplier<AttributeSupplier.Builder>> attributes)
    {
        attributes.accept(BISON::get,EntityBison::createAttributes);
        attributes.accept(HOGMEN::get,EntityHogmen::createAttributes);
        attributes.accept(HOGMEN_BRUTE::get,HogmanBrute::createAttributes);
        attributes.accept(KOMODO::get, AbstractKomodo::createAttributes);
        attributes.accept(KOMODO_BRUTE::get,AbstractKomodo::createAttributes);
        attributes.accept(KOMODO_SORCERER::get,AbstractKomodo::createAttributes);
        attributes.accept(KOMODO_ZEALOT::get, KomodoZealot::createAttributes);
        attributes.accept(STALKER_ENDERMAN::get,StalkerEnderman::createAttributes);

    }

    public static void registerSpawnPlacements()
    {
        SpawnPlacementsRegistry.register(BISON, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Animal::checkAnimalSpawnRules);
        SpawnPlacementsRegistry.register(HOGMEN, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EntityHogmen::checkHogmanSpawnRule);
        SpawnPlacementsRegistry.register(HOGMEN_BRUTE, SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, HogmanBrute::checkHogmanSpawnRule);

    }

}
