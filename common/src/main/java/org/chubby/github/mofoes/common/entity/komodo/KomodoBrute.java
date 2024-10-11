package org.chubby.github.mofoes.common.entity.komodo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Predicate;

public class KomodoBrute extends AbstractKomodo{

    static final Predicate<Difficulty> DOOR_BREAKING_PREDICATE = (difficulty) -> {
        return difficulty == Difficulty.NORMAL || difficulty == Difficulty.HARD;
    };

    public KomodoBrute(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new KomodoBrute.BreakGoalDoor(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this,1.2D,false));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, new Class[]{Raider.class})).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, IronGolem.class, true));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    protected void customServerAiStep() {
        if (!this.isNoAi() && GoalUtils.hasGroundPathNavigation(this)) {
            boolean bl = ((ServerLevel)this.level()).isRaided(this.blockPosition());
            ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(bl);
        }

        super.customServerAiStep();
    }

    public AbstractKomodo.KomodoArmPoses getArmPose() {
        if (this.isAggressive()) {
            return AbstractKomodo.KomodoArmPoses.ATTACKING;
        } else {
            return AbstractKomodo.KomodoArmPoses.CROSSED;
        }
    }


    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData spawnGroupData = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
        RandomSource randomSource = level.getRandom();
        this.populateDefaultEquipmentSlots(randomSource, difficulty);
        this.populateDefaultEquipmentEnchantments(randomSource, difficulty);
        return spawnGroupData;
    }

    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, createSpawnWeapon());
    }

    private ItemStack createSpawnWeapon() {
        ItemStack[] possibleWeapons = new ItemStack[]{
                new ItemStack(Items.STONE_SWORD),
                new ItemStack(Items.WOODEN_SWORD),
                new ItemStack(Items.STONE_AXE),
                new ItemStack(Items.WOODEN_AXE)
        };
        return possibleWeapons[this.random.nextInt(possibleWeapons.length)];
    }

    static class BreakGoalDoor extends BreakDoorGoal {
        public BreakGoalDoor(Mob mob) {
            super(mob, 6, KomodoBrute.DOOR_BREAKING_PREDICATE);
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public boolean canContinueToUse() {
            KomodoBrute vindicator = (KomodoBrute)this.mob;
            return super.canContinueToUse();
        }

        public boolean canUse() {
            KomodoBrute vindicator = (KomodoBrute)this.mob;
            return vindicator.random.nextInt(reducedTickDelay(10)) == 0 && super.canUse();
        }

        public void start() {
            super.start();
            this.mob.setNoActionTime(0);
        }
    }
}
