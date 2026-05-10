package com.example.examplemod;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TestMonsterRenderer extends GeoEntityRenderer<TestMonsterEntity> {
    public TestMonsterRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TestMonsterModel());
    }
}
