package org.chubby.github.mofoes.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.chubby.github.mofoes.client.ModModelLayers;
import org.chubby.github.mofoes.client.model.AbstractKomodoModel;
import org.chubby.github.mofoes.common.entity.komodo.KomodoZealot;

public class KomodoZealotRenderer extends MobRenderer<KomodoZealot, AbstractKomodoModel<KomodoZealot>>
{

    public KomodoZealotRenderer(EntityRendererProvider.Context context) {
        super(context, new AbstractKomodoModel<>(context.bakeLayer(ModModelLayers.KOMODO_ZEALOT_LAYER)), 1.0F);
    }

    @Override
    public void render(KomodoZealot entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.isInvisible()) {
            Vec3[] avec3 = entity.getIllusionOffsets(partialTicks);
            float f = this.getBob(entity, partialTicks);

            for (int i = 0; i < avec3.length; i++) {
                poseStack.pushPose();
                poseStack.translate(
                        avec3[i].x + (double) Mth.cos((float)i + f * 0.5F) * 0.025,
                        avec3[i].y + (double)Mth.cos((float)i + f * 0.75F) * 0.0125,
                        avec3[i].z + (double)Mth.cos((float)i + f * 0.7F) * 0.025
                );
                super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
                poseStack.popPose();
            }
        } else {
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(KomodoZealot entity) {
        return new ResourceLocation("mofoes","textures/entity/komodo_zealot.png");
    }
}
