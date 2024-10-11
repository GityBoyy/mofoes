package org.chubby.github.mofoes.common.world;

import net.minecraft.world.level.biome.Biome;

public interface IComplexSpawn
{
    Biome[] getSpawnBiomes();

    int getSpawnRadius();

    boolean isComplex();


}
