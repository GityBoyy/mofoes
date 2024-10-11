package org.chubby.github.mofoes.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.chubby.github.mofoes.Mofoes;
import org.chubby.github.mofoes.client.ModModelLayers;
import org.chubby.github.mofoes.client.model.AbstractKomodoModel;
import org.chubby.github.mofoes.common.entity.komodo.AbstractKomodo;
import org.jetbrains.annotations.NotNull;

public class BasicKomodoRenderer extends AbstractKomodoRenderer{
    public BasicKomodoRenderer(EntityRendererProvider.Context context) {
        super(context, new AbstractKomodoModel<>(context.bakeLayer(ModModelLayers.BASIC_KOMODO)), 1.0F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(AbstractKomodo entity) {
        return new ResourceLocation(Mofoes.MOD_ID,"textures/entity/komodo.png");
    }
}
