package org.chubby.github.mofoes.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.resources.ResourceLocation;
import org.chubby.github.mofoes.client.model.AbstractKomodoModel;
import org.chubby.github.mofoes.common.entity.komodo.AbstractKomodo;

public abstract class AbstractKomodoRenderer extends HumanoidMobRenderer<AbstractKomodo, AbstractKomodoModel<AbstractKomodo>> {

    public AbstractKomodoRenderer(EntityRendererProvider.Context context, AbstractKomodoModel<AbstractKomodo> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    protected void scale(AbstractKomodo livingEntity, PoseStack poseStack, float partialTickTime) {
        float f = 0.9375F;
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
