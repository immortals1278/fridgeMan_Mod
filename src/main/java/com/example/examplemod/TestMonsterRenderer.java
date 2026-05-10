package com.example.examplemod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TestMonsterRenderer extends GeoEntityRenderer<TestMonsterEntity> {
    private static final float MODEL_SCALE = 3.0f;

    public TestMonsterRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TestMonsterModel());
        this.withScale(MODEL_SCALE);
    }

    @Override
    protected void applyRotations(TestMonsterEntity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick);
        // The source model faces the opposite direction, so rotate 180 degrees around Y.
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
    }
}
