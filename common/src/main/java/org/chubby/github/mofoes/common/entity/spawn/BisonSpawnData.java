package org.chubby.github.mofoes.common.entity.spawn;

import net.minecraft.world.entity.SpawnGroupData;

public class BisonSpawnData implements SpawnGroupData
{
    private int groupSize;
    private final boolean shouldSpawnBaby;
    private final float babySpawnChance;

    private BisonSpawnData(boolean bl, float f) {
        this.shouldSpawnBaby = bl;
        this.babySpawnChance = f;
    }

    public BisonSpawnData(boolean bl) {
        this(bl, 0.05F);
    }

    public BisonSpawnData(float f) {
        this(true, f);
    }

    public int getGroupSize() {
        return this.groupSize;
    }

    public void increaseGroupSizeByOne() {
        ++this.groupSize;
    }

    public boolean isShouldSpawnBaby() {
        return this.shouldSpawnBaby;
    }

    public float getBabySpawnChance() {
        return this.babySpawnChance;
    }
}
