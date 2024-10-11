package org.chubby.github.mofoes.common.entity;


import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.chubby.github.mofoes.common.entity.goals.ProtectAnimalGoal;
import org.chubby.github.mofoes.common.entity.goals.StayWithEntityGoal;
import org.chubby.github.mofoes.common.entity.spawn.BisonSpawnData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class EntityBison extends TamableAnimal implements NeutralMob {
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME;
    public static final Predicate<LivingEntity> PREY_SELECTOR;
    private static final UniformInt PERSISTENT_ANGER_TIME;
    @Nullable
    private UUID persistentAngerTarget;
    private int attackTick;
    public EntityBison(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(1.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.2D));
        this.goalSelector.addGoal(2, new ProtectAnimalGoal(this, Cow.class, true));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.3D, Ingredient.of(Items.GOLDEN_CARROT), false));
        this.goalSelector.addGoal(4, new FollowOwnerGoal(this, 1.25d, 18f, 7f, false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1d));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new StayWithEntityGoal(this, EntityHogmen.class, 1.2F, true));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, false));
        this.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return Ravager.
                createAttributes();
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        List<Cow> nearestCows = this.level().getEntitiesOfClass(Cow.class,this.getBoundingBox().inflate(10));
        List<Cow> lastHurtCows = List.of();
        if(target.getLastHurtMob() instanceof Cow cow)
        {
            lastHurtCows = List.of(cow);
        }

        if(!nearestCows.isEmpty() && !lastHurtCows.isEmpty())
        {
            return true;
        }
        return super.canAttack(target);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    public void setRemainingPersistentAngerTime(int remainingPersistentAngerTime) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, remainingPersistentAngerTime);
    }

    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    public @Nullable UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    public void setPersistentAngerTarget(@Nullable UUID uuid) {
        this.persistentAngerTarget = uuid;
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        InteractionResult result = InteractionResult.sidedSuccess(this.level().isClientSide);
        List<ItemStack> tameItems = List.of(
                new ItemStack(Items.GOLDEN_CARROT),
                new ItemStack(Items.GOLDEN_APPLE),
                new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)
        );
        ItemStack heldItem = player.getMainHandItem();

        if (tameItems.stream().anyMatch(item -> ItemStack.isSameItem(item, heldItem)) && !isTame()) {
            if (this.level().isClientSide()) {
                return InteractionResult.CONSUME;
            } else {
                if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }
                super.tame(player);
                this.navigation.recomputePath();
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, (byte) 7);
                setOrderedToSit(true);
                this.setInSittingPose(true);
            }
            return InteractionResult.SUCCESS;
        }

        if (isTame() && hand == InteractionHand.MAIN_HAND) {
            if (!isFood(heldItem)) {
                if (!player.isCrouching()) {
                    setRiding(player);
                } else {
                    setOrderedToSit(!isOrderedToSit());
                    setInSittingPose(!isOrderedToSit());
                }
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    public void setTame(boolean tame) {
        super.setTame(tame);
    }

    private void setRiding(Player pPlayer) {
        this.setInSittingPose(false);

        pPlayer.setYRot(this.getYRot());
        pPlayer.setXRot(this.getXRot());
        pPlayer.startRiding(this);
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return ((LivingEntity) this.getFirstPassenger());
    }

    @Override
    public void travel(Vec3 pTravelVector) {
        if(this.isVehicle() && getControllingPassenger() instanceof Player) {
            LivingEntity livingentity = this.getControllingPassenger();
            this.setYRot(livingentity.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(livingentity.getXRot() * 0.5F);
            this.setRot(this.getYRot(), this.getXRot());
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.yBodyRot;
            float f = livingentity.xxa * 0.5F;
            float f1 = livingentity.zza;

            // Inside this if statement, we are on the client!
            if (this.isControlledByLocalInstance()) {
                float newSpeed = (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
                // increasing speed by 100% if the spring key is held down (number for testing purposes)
                if(Minecraft.getInstance().options.keySprint.isDown()) {
                    newSpeed *= 2f;
                }

                this.setSpeed(newSpeed);
                super.travel(new Vec3(f, pTravelVector.y, f1));
            }
        } else {
            super.travel(pTravelVector);
        }
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity pLivingEntity) {
        Direction direction = this.getMotionDirection();
        if (direction.getAxis() != Direction.Axis.Y) {
            int[][] offsets = DismountHelper.offsetsForDirection(direction);
            BlockPos blockpos = this.blockPosition();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (Pose pose : pLivingEntity.getDismountPoses()) {
                AABB aabb = pLivingEntity.getLocalBoundsForPose(pose);

                for (int[] offset : offsets) {
                    blockpos$mutableblockpos.set(blockpos.getX() + offset[0], blockpos.getY(), blockpos.getZ() + offset[1]);
                    double d0 = this.level().getBlockFloorHeight(blockpos$mutableblockpos);
                    if (DismountHelper.isBlockFloorValid(d0)) {
                        Vec3 vec3 = Vec3.upFromBottomCenterOf(blockpos$mutableblockpos, d0);
                        if (DismountHelper.canDismountTo(this.level(), pLivingEntity, aabb.move(vec3))) {
                            pLivingEntity.setPose(pose);
                            return vec3;
                        }
                    }
                }
            }
        }

        return super.getDismountLocationForPassenger(pLivingEntity);
    }

    static
    {
        DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(EntityBison.class, EntityDataSerializers.INT);
        PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
        PREY_SELECTOR = (entity) -> {
            EntityType<?> type = entity.getType();
            return (!entity.isVehicle() && !(entity.getControllingPassenger()instanceof Player)) &&
                    (!(type instanceof Npc) || type != EntityType.IRON_GOLEM || type != EntityType.CREEPER);
        };
    }

    public int getAttackTick()
    {
        return attackTick;
    }

    public boolean isAdult()
    {
        return !this.isBaby();
    }

    public boolean canBeHunted()
    {
        return true;
    }

    public static class SpawnData extends BisonSpawnData
    {

        public SpawnData(float chance) {
            super(true);
        }
    }
}
