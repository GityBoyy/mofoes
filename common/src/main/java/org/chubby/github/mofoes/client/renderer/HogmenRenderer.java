package org.chubby.github.mofoes.client.renderer;

import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import org.chubby.github.mofoes.Mofoes;
import org.chubby.github.mofoes.client.ModModelLayers;
import org.chubby.github.mofoes.client.model.HogmenModel;

public class HogmenRenderer extends HumanoidMobRenderer<Mob, HogmenModel<Mob>> {

    public HogmenRenderer(EntityRendererProvider.Context context, ModelLayerLocation layer, ModelLayerLocation p_174346_, ModelLayerLocation p_174347_) {
        super(context, new HogmenModel<>(context.bakeLayer(ModModelLayers.HOGMEN)), 1.0F);
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidArmorModel(context.bakeLayer(p_174346_)), new HumanoidArmorModel(context.bakeLayer(p_174347_)), context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(Mob entity) {
        return new ResourceLocation(Mofoes.MOD_ID,"textures/entity/hogmen.png");
    }
}
