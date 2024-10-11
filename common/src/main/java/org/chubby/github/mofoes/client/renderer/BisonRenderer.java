package org.chubby.github.mofoes.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.chubby.github.mofoes.Mofoes;
import org.chubby.github.mofoes.client.ModModelLayers;
import org.chubby.github.mofoes.client.model.BisonModel;
import org.chubby.github.mofoes.common.entity.EntityBison;

public class BisonRenderer extends MobRenderer<EntityBison, BisonModel> {
    public BisonRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new BisonModel(ctx.bakeLayer(ModModelLayers.BISON)), 1.75F);
    }

    @Override
    public void render(EntityBison mob, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        super.render(mob, f, g, poseStack, multiBufferSource, i);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityBison entity) {
        return new ResourceLocation(Mofoes.MOD_ID,"textures/entity/bison.png");
    }
}
