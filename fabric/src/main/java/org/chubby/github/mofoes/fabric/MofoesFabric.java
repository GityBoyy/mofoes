package org.chubby.github.mofoes.fabric;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.chubby.github.mofoes.Mofoes;
import net.fabricmc.api.ModInitializer;
import org.chubby.github.mofoes.common.entity.EntityBison;
import org.chubby.github.mofoes.common.entity.EntityHogmen;
import org.chubby.github.mofoes.common.entity.HogmanBrute;
import org.chubby.github.mofoes.common.entity.StalkerEnderman;
import org.chubby.github.mofoes.common.entity.komodo.AbstractKomodo;
import org.chubby.github.mofoes.common.entity.komodo.KomodoSorcerer;
import org.chubby.github.mofoes.common.init.ModEntities;

import static org.chubby.github.mofoes.common.init.ModEntities.*;

public final class MofoesFabric implements ModInitializer {
    @Override
    public void onInitialize() {

        Mofoes.init();
        onAttributeRegister();
    }

    public void onAttributeRegister()
    {

        ModEntities.registerAttributes((type, builder) -> FabricDefaultAttributeRegistry.register(type.get(), builder.get()));
    }
}
