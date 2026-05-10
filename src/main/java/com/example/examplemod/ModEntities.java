package com.example.examplemod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ExampleMod.MODID);

    public static final RegistryObject<EntityType<TestMonsterEntity>> TEST_MONSTER =
            ENTITIES.register("test_monster",
                    () -> EntityType.Builder.of(TestMonsterEntity::new, MobCategory.MONSTER)
                            .sized(1.0f, 2.0f)
                            .build(new ResourceLocation(ExampleMod.MODID, "test_monster").toString()));
}

