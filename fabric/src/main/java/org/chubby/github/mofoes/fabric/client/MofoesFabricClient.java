package org.chubby.github.mofoes.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import org.chubby.github.mofoes.client.ModModelLayers;
import org.chubby.github.mofoes.client.model.AbstractKomodoModel;
import org.chubby.github.mofoes.client.model.BisonModel;
import org.chubby.github.mofoes.client.model.HogmenModel;
import org.chubby.github.mofoes.client.model.StalkerEndermanModel;
import org.chubby.github.mofoes.client.renderer.*;
import org.chubby.github.mofoes.common.init.ModEntities;

public final class MofoesFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.BISON, BisonModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.HOGMEN, HogmenModel::createLayer);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.BASIC_KOMODO, AbstractKomodoModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.KOMODO_BRUTE, AbstractKomodoModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.KOMODO_SORCERER, AbstractKomodoModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.KOMODO_ZEALOT_LAYER, AbstractKomodoModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.HOGMEN_BRUTE, HogmenModel::createLayer);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.STALKER_ENDERMAN, StalkerEndermanModel::createBodyLayer);

        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.HOGMEN_INNER, ()-> HogmenModel.layerdefinition3);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.HOGMEN_OUTER,()-> HogmenModel.layerdefinition1);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.HOGMEN_BRUTE_INNER, ()-> HogmenModel.layerdefinition3);
        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.HOGMEN_BRUTE_OUTER,()-> HogmenModel.layerdefinition1);

        EntityRendererRegistry.register(ModEntities.BISON.get(), BisonRenderer::new);
        EntityRendererRegistry.register(ModEntities.HOGMEN.get(), (ctx)->
                new HogmenRenderer(ctx,ModModelLayers.HOGMEN ,ModModelLayers.HOGMEN_INNER,ModModelLayers.HOGMEN_OUTER));
        EntityRendererRegistry.register(ModEntities.HOGMEN_BRUTE.get(), (ctx)->
                new HogmenBruteRenderer(ctx,ModModelLayers.HOGMEN_BRUTE,ModModelLayers.HOGMEN_BRUTE_INNER,ModModelLayers.HOGMEN_BRUTE_OUTER));
        EntityRendererRegistry.register(ModEntities.KOMODO.get(), BasicKomodoRenderer::new);
        EntityRendererRegistry.register(ModEntities.KOMODO_BRUTE.get(), KomodoBruteRenderer::new);
        EntityRendererRegistry.register(ModEntities.KOMODO_SORCERER.get(), KomodoSorcererRenderer::new);
        EntityRendererRegistry.register(ModEntities.KOMODO_ZEALOT.get(), KomodoZealotRenderer::new);
        EntityRendererRegistry.register(ModEntities.STALKER_ENDERMAN.get(), StalkerEndermanRenderer::new);
    }


}
