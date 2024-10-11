package org.chubby.github.mofoes.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import org.chubby.github.mofoes.common.entity.EntityHogmen;
import org.chubby.github.mofoes.common.entity.komodo.AbstractKomodo;
import org.chubby.github.mofoes.common.entity.komodo.KomodoZealot;
import org.jetbrains.annotations.NotNull;

public class AbstractKomodoModel <T extends AbstractKomodo> extends PlayerModel<T> {
//    private final ModelPart root;
//    private final ModelPart head;
//    private final ModelPart hat;
//    private final ModelPart arms;
//    private final ModelPart leftLeg;
//    private final ModelPart rightLeg;
//    private final ModelPart rightArm;
//    private final ModelPart leftArm;

    public AbstractKomodoModel(ModelPart root) {
        super(root,false);
//        this.root = root;
//        this.head = root.getChild("head");
//        this.hat = this.head.getChild("hat");
//        this.hat.visible = false;
//        this.arms = root.getChild("arms");
//        this.leftLeg = root.getChild("left_leg");
//        this.rightLeg = root.getChild("right_leg");
//        this.leftArm = root.getChild("left_arm");
//        this.rightArm = root.getChild("right_arm");
    }

    public static LayerDefinition createBodyLayer()
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

//    public ModelPart root() {
//        return this.root;
//    }

    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * 0.017453292F;
        this.head.xRot = headPitch * 0.017453292F;
        if (this.riding) {
            this.rightArm.xRot = -0.62831855F;
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;
            this.leftArm.xRot = -0.62831855F;
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.rightLeg.xRot = -1.4137167F;
            this.rightLeg.yRot = 0.31415927F;
            this.rightLeg.zRot = 0.07853982F;
            this.leftLeg.xRot = -1.4137167F;
            this.leftLeg.yRot = -0.31415927F;
            this.leftLeg.zRot = -0.07853982F;
        } else {
            this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + 3.1415927F) * 2.0F * limbSwingAmount * 0.5F;
            this.rightArm.yRot = 0.0F;
            this.rightArm.zRot = 0.0F;
            this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F;
            this.leftArm.yRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount * 0.5F;
            this.rightLeg.yRot = 0.0F;
            this.rightLeg.zRot = 0.0F;
            this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + 3.1415927F) * 1.4F * limbSwingAmount * 0.5F;
            this.leftLeg.yRot = 0.0F;
            this.leftLeg.zRot = 0.0F;
        }

        AbstractKomodo.KomodoArmPoses illagerArmPose = entity.getArmPose();
        if (illagerArmPose == AbstractKomodo.KomodoArmPoses.ATTACKING) {
            if (entity.getMainHandItem().isEmpty()) {
                AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, true, this.attackTime, ageInTicks);
            } else {
                AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, entity, this.attackTime, ageInTicks);
            }
        } else if (illagerArmPose == AbstractKomodo.KomodoArmPoses.SPELLCASTING) {
            this.rightArm.z = 0.0F;
            this.rightArm.x = -5.0F;
            this.leftArm.z = 0.0F;
            this.leftArm.x = 5.0F;
            this.rightArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
            this.leftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
            this.rightArm.zRot = 2.3561945F;
            this.leftArm.zRot = -2.3561945F;
            this.rightArm.yRot = 0.0F;
            this.leftArm.yRot = 0.0F;
        } else if (illagerArmPose == AbstractKomodo.KomodoArmPoses.BOW_AND_ARROW) {
            this.rightArm.yRot = -0.1F + this.head.yRot;
            this.rightArm.xRot = -1.5707964F + this.head.xRot;
            this.leftArm.xRot = -0.9424779F + this.head.xRot;
            this.leftArm.yRot = this.head.yRot - 0.4F;
            this.leftArm.zRot = 1.5707964F;
        } else if (illagerArmPose == AbstractKomodo.KomodoArmPoses.CROSSBOW_HOLD) {
            AnimationUtils.animateCrossbowHold(this.rightArm, this.leftArm, this.head, true);
        } else if (illagerArmPose == AbstractKomodo.KomodoArmPoses.CROSSBOW_CHARGE) {
            AnimationUtils.animateCrossbowCharge(this.rightArm, this.leftArm, entity, true);
        } else if (illagerArmPose == AbstractKomodo.KomodoArmPoses.CELEBRATING) {
            this.rightArm.z = 0.0F;
            this.rightArm.x = -5.0F;
            this.rightArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.05F;
            this.rightArm.zRot = 2.670354F;
            this.rightArm.yRot = 0.0F;
            this.leftArm.z = 0.0F;
            this.leftArm.x = 5.0F;
            this.leftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.05F;
            this.leftArm.zRot = -2.3561945F;
            this.leftArm.yRot = 0.0F;
        }
        else if(illagerArmPose == AbstractKomodo.KomodoArmPoses.MELEE_POSE)
        {
            if (this.attackTime > 0.0F && entity instanceof AbstractKomodo && ((AbstractKomodo)entity).getArmPose() == AbstractKomodo.KomodoArmPoses.MELEE_POSE) {
                AnimationUtils.swingWeaponDown(this.rightArm, this.leftArm, entity, this.attackTime, ageInTicks);
                holdWeaponHigh(entity);
            }
        }

        boolean bl = illagerArmPose == AbstractKomodo.KomodoArmPoses.CROSSED;
        if(entity instanceof KomodoZealot zealot){
            this.leftArm.visible = zealot.isMelee();
            this.rightArm.visible = zealot.isMelee();
        }

    }

    public @NotNull ModelPart getArm(HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? this.leftArm : this.rightArm;
    }

    private void holdWeaponHigh(T mob) {
        if (mob.isLeftHanded()) {
            this.leftArm.xRot = -1.8F;
        } else {
            this.rightArm.xRot = -1.8F;
        }

    }

    public void translateToHand(HumanoidArm side, PoseStack poseStack) {
        this.getArm(side).translateAndRotate(poseStack);
    }
}

