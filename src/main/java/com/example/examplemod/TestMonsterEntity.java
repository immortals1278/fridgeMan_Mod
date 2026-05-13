package com.example.examplemod;

import net.minecraft.server.level.ServerPlayer;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class TestMonsterEntity extends PathfinderMob implements GeoEntity {
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final RawAnimation STAND_ANIM = RawAnimation.begin().thenLoop("stand");
    private static final RawAnimation EXECUTE_ANIM = RawAnimation.begin().thenLoop("execute");
    private static final int GRAB_DURATION = 40;
    private static final EntityDataAccessor<Boolean> EXECUTING =
        SynchedEntityData.defineId(TestMonsterEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> GRABBED_PLAYER_UUID =
        SynchedEntityData.defineId(TestMonsterEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private Player grabbedPlayer;
    private int grabTimer;
    private Vec3 grabFacing = new Vec3(0.0D, 0.0D, 1.0D);

    protected TestMonsterEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement_controller", 5, this::movementPredicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(EXECUTING, false);
        this.entityData.define(GRABBED_PLAYER_UUID, Optional.empty());
    }

    private PlayState movementPredicate(AnimationState<TestMonsterEntity> animationState) {
        if (this.isExecuting()) {
            return animationState.setAndContinue(EXECUTE_ANIM);
        }
        
        if (animationState.isMoving()) {
            return animationState.setAndContinue(WALK_ANIM);
        }

        return animationState.setAndContinue(STAND_ANIM);
    }

    private boolean isExecuting() {
        return this.entityData.get(EXECUTING);
    }

    public boolean isGrabbing(Player player) {
        return this.isExecuting() && this.entityData.get(GRABBED_PLAYER_UUID)
            .map(player.getUUID()::equals)
            .orElse(false);
    }

    private void setExecuting(boolean executing) {
        this.entityData.set(EXECUTING, executing);
        this.setNoAi(executing);
    }

    private Vec3 captureGrabFacing(Player player) {
        Vec3 towardGhost = new Vec3(this.getX() - player.getX(), 0.0D, this.getZ() - player.getZ());

        if (towardGhost.lengthSqr() < 1.0E-6D) {
            towardGhost = Vec3.directionFromRotation(0.0F, this.getYRot()).scale(-1.0D);
            towardGhost = new Vec3(towardGhost.x, 0.0D, towardGhost.z);
        }

        return towardGhost.normalize();
    }

    private Vec3 getGrabbedPlayerPosition() {
        Vec3 right = new Vec3(-this.grabFacing.z, 0.0D, this.grabFacing.x);
        return new Vec3(this.getX(), this.getY(), this.getZ())
            .subtract(this.grabFacing.scale(3.0D))
            .add(right.scale(1.5D))
            .add(0.0D, -1.0D, 0.0D);
    }

    private static float getYawFromDirection(Vec3 direction) {
        return (float)(Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0D);
    }

    private void faceGrabbedPlayer() {
        Vec3 toPlayer = this.getGrabbedPlayerPosition().subtract(this.position());
        float yaw = getYawFromDirection(toPlayer);

        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.setYBodyRot(yaw);
        this.yRotO = yaw;
        this.yHeadRotO = yaw;
        this.yBodyRotO = yaw;
        this.setXRot(0.0F);
        this.xRotO = 0.0F;
    }

    private void positionGrabbedPlayer() {
        Vec3 targetPos = this.getGrabbedPlayerPosition();
        float yaw = getYawFromDirection(this.grabFacing);
        float pitch = 0.0F;

        if (this.grabbedPlayer instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.teleport(targetPos.x, targetPos.y, targetPos.z, yaw, pitch);
        } else {
            this.grabbedPlayer.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            this.grabbedPlayer.setXRot(pitch);
        }

        this.grabbedPlayer.setYRot(yaw);
        this.grabbedPlayer.setYHeadRot(yaw);
        this.grabbedPlayer.setYBodyRot(yaw);
        this.grabbedPlayer.yRotO = yaw;
        this.grabbedPlayer.yHeadRotO = yaw;
        this.grabbedPlayer.yBodyRotO = yaw;
        this.grabbedPlayer.setXRot(pitch);
        this.grabbedPlayer.xRotO = pitch;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof Player player && this.grabbedPlayer == null) {
            this.grabbedPlayer = player;
            this.grabTimer = GRAB_DURATION;
            this.grabFacing = this.captureGrabFacing(player);
            this.entityData.set(GRABBED_PLAYER_UUID, Optional.of(player.getUUID()));
            this.setExecuting(true);
            return true;
        }
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            return;
        }

        if (this.grabbedPlayer == null) {
            return;
        }

        if (!this.grabbedPlayer.isAlive() || this.isDeadOrDying()) {
            this.grabbedPlayer = null;
            this.grabFacing = new Vec3(0.0D, 0.0D, 1.0D);
            this.entityData.set(GRABBED_PLAYER_UUID, Optional.empty());
            this.setExecuting(false);
            return;
        }

        if (this.grabTimer > 0) {
            this.grabbedPlayer.setDeltaMovement(Vec3.ZERO);
            this.grabbedPlayer.xxa = 0;
            this.grabbedPlayer.zza = 0;
            this.faceGrabbedPlayer();
            this.positionGrabbedPlayer();
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            this.grabTimer--;
        } else {
            this.grabbedPlayer.kill();
            this.grabbedPlayer = null;
            this.grabFacing = new Vec3(0.0D, 0.0D, 1.0D);
            this.entityData.set(GRABBED_PLAYER_UUID, Optional.empty());
            this.setExecuting(false);
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new TestMonsterMeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 1000.0D)
            .add(Attributes.ATTACK_DAMAGE, 8.0D)
            .add(Attributes.FOLLOW_RANGE, 32.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D);
    }

    private static class TestMonsterMeleeAttackGoal extends MeleeAttackGoal {
        public TestMonsterMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(mob, speedModifier, followingTargetEvenIfNotSeen);
        }

        @Override
        protected double getAttackReachSqr(LivingEntity target) {
            // Large model needs a longer melee reach to avoid orbiting around the target.
            return Math.pow(this.mob.getBbWidth() * 2.2F, 2) + target.getBbWidth();
        }
    }
}

