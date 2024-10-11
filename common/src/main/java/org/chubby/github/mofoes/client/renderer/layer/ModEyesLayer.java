package org.chubby.github.mofoes.client.renderer.layer;

import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.chubby.github.mofoes.Mofoes;
import org.chubby.github.mofoes.client.model.StalkerEndermanModel;
import org.chubby.github.mofoes.common.entity.StalkerEnderman;

public class ModEyesLayer <T extends StalkerEnderman> extends EyesLayer<T, StalkerEndermanModel<T>> {

    private static final RenderType ENDERMAN_EYES = RenderType.eyes(new ResourceLocation(Mofoes.MOD_ID,
            "textures/entity/layer/enderman_eyes.png"));

    public ModEyesLayer(RenderLayerParent<T, StalkerEndermanModel<T>> renderer) {
        super(renderer);
    }

    @Override
    public RenderType renderType() {
        return ENDERMAN_EYES;
    }
}
