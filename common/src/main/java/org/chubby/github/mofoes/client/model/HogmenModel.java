package org.chubby.github.mofoes.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import org.chubby.github.mofoes.common.entity.EntityHogmen;

@Environment(EnvType.CLIENT)
public class HogmenModel<T extends Mob> extends PlayerModel<T> {
    public final ModelPart rightEar;
    private final ModelPart leftEar;
    private final PartPose bodyDefault;
    private final PartPose headDefault;
    private final PartPose leftArmDefault;
    private final PartPose rightArmDefault;
    public static final CubeDeformation OUTER_ARMOR_DEFORMATION = new CubeDeformation(1.0F);
    public static final CubeDeformation INNER_ARMOR_DEFORMATION = new CubeDeformation(0.5F);
    public static LayerDefinition layerdefinition1 = LayerDefinition.create(HumanoidArmorModel.createBodyLayer(OUTER_ARMOR_DEFORMATION), 64, 32);
    public static LayerDefinition layerdefinition3 = LayerDefinition.create(HumanoidArmorModel.createBodyLayer(INNER_ARMOR_DEFORMATION), 64, 32);
    public HogmenModel(ModelPart modelPart) {
        super(modelPart, false);
        this.rightEar = this.head.getChild("right_ear");
        this.leftEar = this.head.getChild("left_ear");
        this.bodyDefault = this.body.storePose();
        this.headDefault = this.head.storePose();
        this.leftArmDefault = this.leftArm.storePose();
        this.rightArmDefault = this.rightArm.storePose();
    }

    public static LayerDefinition createLayer()
    {
        return LayerDefinition.create(createMesh(new CubeDeformation(0)),64,64);
    }

    public static MeshDefinition createMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = PlayerModel.createMesh(cubeDeformation, false);
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, cubeDeformation), PartPose.ZERO);
        addHead(cubeDeformation, meshDefinition);
        partDefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        return meshDefinition;
    }

    public static void addHead(CubeDeformation cubeDeformation, MeshDefinition mesh) {
        PartDefinition partDefinition = mesh.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, cubeDeformation).texOffs(31, 1).addBox(-2.0F, -4.0F, -5.0F, 4.0F, 4.0F, 1.0F, cubeDeformation).texOffs(2, 4).addBox(2.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, cubeDeformation).texOffs(2, 0).addBox(-3.0F, -2.0F, -5.0F, 1.0F, 2.0F, 1.0F, cubeDeformation), PartPose.ZERO);
        partDefinition2.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(51, 6).addBox(0.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, cubeDeformation), PartPose.offsetAndRotation(4.5F, -6.0F, 0.0F, 0.0F, 0.0F, -0.5235988F));
        partDefinition2.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(39, 6).addBox(-1.0F, 0.0F, -2.0F, 1.0F, 5.0F, 4.0F, cubeDeformation), PartPose.offsetAndRotation(-4.5F, -6.0F, 0.0F, 0.0F, 0.0F, 0.5235988F));
    }

    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.body.loadPose(this.bodyDefault);
        this.head.loadPose(this.headDefault);
        this.leftArm.loadPose(this.leftArmDefault);
        this.rightArm.loadPose(this.rightArmDefault);
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float f = 0.5235988F;
        float g = ageInTicks * 0.1F + limbSwing * 0.5F;
        float h = 0.08F + limbSwingAmount * 0.4F;
        this.leftEar.zRot = -0.5235988F - Mth.cos(g * 1.2F) * h;
        this.rightEar.zRot = 0.5235988F + Mth.cos(g) * h;
        if (entity instanceof EntityHogmen abstractPiglin) {
            PiglinArmPose piglinArmPose = abstractPiglin.getArmPose();
            if (piglinArmPose == PiglinArmPose.DANCING) {
                float i = ageInTicks / 60.0F;
                this.rightEar.zRot = 0.5235988F + 0.017453292F * Mth.sin(i * 30.0F) * 10.0F;
                this.leftEar.zRot = -0.5235988F - 0.017453292F * Mth.cos(i * 30.0F) * 10.0F;
                this.head.x = Mth.sin(i * 10.0F);
                this.head.y = Mth.sin(i * 40.0F) + 0.4F;
                this.rightArm.zRot = 0.017453292F * (70.0F + Mth.cos(i * 40.0F) * 10.0F);
                this.leftArm.zRot = this.rightArm.zRot * -1.0F;
                this.rightArm.y = Mth.sin(i * 40.0F) * 0.5F + 1.5F;
                this.leftArm.y = Mth.sin(i * 40.0F) * 0.5F + 1.5F;
                this.body.y = Mth.sin(i * 40.0F) * 0.35F;
            } else if (piglinArmPose == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON && this.attackTime == 0.0F) {
                this.holdWeaponHigh(entity);
            } else if (piglinArmPose == PiglinArmPose.CROSSBOW_HOLD) {
                AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, !entity.isLeftHanded());
            } else if (piglinArmPose == PiglinArmPose.CROSSBOW_CHARGE) {
                AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, entity, !entity.isLeftHanded());
            } else if (piglinArmPose == PiglinArmPose.ADMIRING_ITEM) {
                this.head.xRot = 0.5F;
                this.head.yRot = 0.0F;
                if (entity.isLeftHanded()) {
                    this.rightArm.yRot = -0.5F;
                    this.rightArm.xRot = -0.9F;
                } else {
                    this.leftArm.yRot = 0.5F;
                    this.leftArm.xRot = -0.9F;
                }
            }
        }

        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.jacket.copyFrom(this.body);
        this.hat.copyFrom(this.head);
    }

    protected void setupAttackAnimation(T livingEntity, float ageInTicks) {
        if (this.attackTime > 0.0F && livingEntity instanceof EntityHogmen && ((EntityHogmen)livingEntity).getArmPose() == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON) {
            AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, livingEntity, this.attackTime, ageInTicks);
        } else {
            super.setupAttackAnimation(livingEntity, ageInTicks);
        }
    }

    private void holdWeaponHigh(T mob) {
        if (mob.isLeftHanded()) {
            this.leftArm.xRot = -1.8F;
        } else {
            this.rightArm.xRot = -1.8F;
        }

    }
}
