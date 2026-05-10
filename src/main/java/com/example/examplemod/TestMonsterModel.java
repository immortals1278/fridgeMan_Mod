package com.example.examplemod;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TestMonsterModel extends GeoModel<TestMonsterEntity> {
    @Override
    public ResourceLocation getModelResource(TestMonsterEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "geo/test_monster.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TestMonsterEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "textures/entity/test_monster.png.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TestMonsterEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID, "animations/test_monster.animation.json");
    }
}
