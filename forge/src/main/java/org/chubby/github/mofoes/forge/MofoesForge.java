package org.chubby.github.mofoes.forge;

import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.level.entity.SpawnPlacementsRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.server.command.ConfigCommand;
import org.chubby.github.mofoes.Mofoes;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.chubby.github.mofoes.client.renderer.BisonRenderer;
import org.chubby.github.mofoes.common.commands.CheckSpawnCommand;
import org.chubby.github.mofoes.common.init.ModEntities;
import org.chubby.github.mofoes.forge.datagen.ModBlockTagGenerator;
import org.chubby.github.mofoes.forge.datagen.ModTagProvider;

import java.util.concurrent.CompletableFuture;

@Mod(Mofoes.MOD_ID)
public final class MofoesForge {
    public MofoesForge() {
        final IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(Mofoes.MOD_ID, eventBus);
        MinecraftForge.EVENT_BUS.register(this);
        // Run our common setup.
        Mofoes.init();
        eventBus.addListener(this::onAttributeRegister);
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::gathetData);
    }

    public void onAttributeRegister(EntityAttributeCreationEvent event)
    {
        ModEntities.registerAttributes((supplier, builderSupplier) -> event.put(supplier.get(),builderSupplier.get().build()));
    }

    public void onSpawnPlacementRegister(SpawnPlacementRegisterEvent event)
    {

    }
    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {

    }


    public void clientSetup(FMLClientSetupEvent event)
    {
    }

    public void gathetData(GatherDataEvent event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        BlockTagsProvider blockTagsProvider = new ModBlockTagGenerator(packOutput, lookupProvider, existingFileHelper);
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new ModTagProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
    }
}
