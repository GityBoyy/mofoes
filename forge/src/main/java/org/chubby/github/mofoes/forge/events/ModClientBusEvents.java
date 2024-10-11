package org.chubby.github.mofoes.forge.events;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.chubby.github.mofoes.Mofoes;
import org.chubby.github.mofoes.client.ModModelLayers;
import org.chubby.github.mofoes.client.model.AbstractKomodoModel;
import org.chubby.github.mofoes.client.model.BisonModel;
import org.chubby.github.mofoes.client.model.HogmenModel;
import org.chubby.github.mofoes.client.model.StalkerEndermanModel;
import org.chubby.github.mofoes.client.renderer.*;
import org.chubby.github.mofoes.common.entity.EntityHogmen;
import org.chubby.github.mofoes.common.init.ModEntities;

@Mod.EventBusSubscriber(modid = Mofoes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientBusEvents
{
    @SubscribeEvent
    public static void registerMobLayers(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(ModModelLayers.BISON, BisonModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.HOGMEN, HogmenModel::createLayer);
        event.registerLayerDefinition(ModModelLayers.HOGMEN_INNER, ()-> HogmenModel.layerdefinition3);
        event.registerLayerDefinition(ModModelLayers.HOGMEN_OUTER, ()-> HogmenModel.layerdefinition1);

        event.registerLayerDefinition(ModModelLayers.HOGMEN_BRUTE, HogmenModel::createLayer);
        event.registerLayerDefinition(ModModelLayers.HOGMEN_BRUTE_INNER, ()-> HogmenModel.layerdefinition3);
        event.registerLayerDefinition(ModModelLayers.HOGMEN_BRUTE_OUTER, ()-> HogmenModel.layerdefinition1);

        event.registerLayerDefinition(ModModelLayers.BASIC_KOMODO, AbstractKomodoModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.KOMODO_BRUTE, AbstractKomodoModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.KOMODO_SORCERER, AbstractKomodoModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.KOMODO_ZEALOT_LAYER, AbstractKomodoModel::createBodyLayer);

        event.registerLayerDefinition(ModModelLayers.STALKER_ENDERMAN, StalkerEndermanModel::createBodyLayer);
    }
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(ModEntities.BISON.get(), BisonRenderer::new);
        event.registerEntityRenderer(ModEntities.HOGMEN.get(), ctx ->
                new HogmenRenderer(ctx,ModModelLayers.HOGMEN,ModModelLayers.HOGMEN_INNER,ModModelLayers.HOGMEN_OUTER));
        event.registerEntityRenderer(ModEntities.HOGMEN_BRUTE.get(), ctx ->
                new HogmenBruteRenderer(ctx,ModModelLayers.HOGMEN,ModModelLayers.HOGMEN_INNER,ModModelLayers.HOGMEN_OUTER));
        event.registerEntityRenderer(ModEntities.KOMODO.get(), BasicKomodoRenderer::new);
        event.registerEntityRenderer(ModEntities.KOMODO_BRUTE.get(), KomodoBruteRenderer::new);
        event.registerEntityRenderer(ModEntities.KOMODO_SORCERER.get(), KomodoSorcererRenderer::new);
        event.registerEntityRenderer(ModEntities.STALKER_ENDERMAN.get(), StalkerEndermanRenderer::new);
        event.registerEntityRenderer(ModEntities.KOMODO_ZEALOT.get(), KomodoZealotRenderer::new);
    }


}
