package org.chubby.github.mofoes.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.phys.Vec3;
import org.chubby.github.mofoes.Mofoes;
import org.chubby.github.mofoes.client.ModModelLayers;
import org.chubby.github.mofoes.client.model.StalkerEndermanModel;
import org.chubby.github.mofoes.client.renderer.layer.ModEyesLayer;
import org.chubby.github.mofoes.common.entity.StalkerEnderman;
import org.jetbrains.annotations.NotNull;

public class StalkerEndermanRenderer extends MobRenderer<StalkerEnderman, StalkerEndermanModel<StalkerEnderman>> {
    private final RandomSource random = RandomSource.create();

    public static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/enderman/enderman.png");

    public StalkerEndermanRenderer(EntityRendererProvider.Context context) {
        super(context, new StalkerEndermanModel<>(context.bakeLayer(ModModelLayers.STALKER_ENDERMAN)), 1.0F);
        this.addLayer(new ModEyesLayer<>(this));
    }

    public @NotNull Vec3 getRenderOffset(StalkerEnderman entity, float partialTicks) {
        if (entity.isCreepy()) {
            double d = 0.02;
            return new Vec3(this.random.nextGaussian() * 0.02, 0.0, this.random.nextGaussian() * 0.02);
        } else {
            return super.getRenderOffset(entity, partialTicks);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(StalkerEnderman entity) {
        return TEXTURE;
    }
}
