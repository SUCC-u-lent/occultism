package com.klikli_dev.occultism.common.entity.spirit.demonicpartner.charmer;

import com.klikli_dev.occultism.Occultism;
import com.klikli_dev.occultism.common.entity.spirit.demonicpartner.DemonicPartner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DemonicCharmer extends DemonicPartner implements GeoEntity {
    public static final EntityDataAccessor<Integer> HAIR_VARIANT = SynchedEntityData.defineId(DemonicCharmer.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> HORN_VARIANT = SynchedEntityData.defineId(DemonicCharmer.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> IS_FULL_POWER = SynchedEntityData.defineId(DemonicCharmer.class, EntityDataSerializers.BOOLEAN);

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Occultism.MODID, "demonic_charmer");
    public static final Lazy<EntityType<DemonicCharmer>> ENTITY_TYPE =
            Lazy.of(() -> EntityType.Builder.of(DemonicCharmer::new, MobCategory.CREATURE)
                    .sized(0.6F, 2)
                    .fireImmune()
                    .clientTrackingRange(8)
                    .build(ID.toString()));
    AnimatableInstanceCache animatableInstanceCache = GeckoLibUtil.createInstanceCache(this);

    protected DemonicCharmer(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HAIR_VARIANT, this.getRandom().nextIntBetweenInclusive(0,1));
        builder.define(HORN_VARIANT, this.getRandom().nextIntBetweenInclusive(0,2));
        builder.define(IS_FULL_POWER,false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("hair_variant",this.entityData.get(HAIR_VARIANT));
        compound.putInt("horn_variant",this.entityData.get(HORN_VARIANT));
        compound.putBoolean("full_power",this.entityData.get(IS_FULL_POWER));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(HAIR_VARIANT,compound.getInt("hair_variant"));
        this.entityData.set(HORN_VARIANT,compound.getInt("horn_variant"));
        this.entityData.set(IS_FULL_POWER,compound.getBoolean("full_power"));
    }

    @Override
    public int getCurrentSwingDuration() {
        return 11; //to match our attack animation speed + 1 tick
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        var mainController = new AnimationController<>(this, "mainController", 0, this::animPredicate);
        controllers.add(mainController);
    }

    private <T extends GeoAnimatable> PlayState animPredicate(AnimationState<T> tAnimationState) {

        if (this.swinging) {
            return tAnimationState.setAndContinue(RawAnimation.begin().thenPlay("attack"));
        }

        if (this.isInSittingPose())
            return tAnimationState.setAndContinue(RawAnimation.begin().thenPlay("sit"));

        if (this.isLying())
            return tAnimationState.setAndContinue(RawAnimation.begin().thenPlay("lies"));

        return tAnimationState.setAndContinue(tAnimationState.isMoving() ? RawAnimation.begin().thenPlay("walk") : RawAnimation.begin().thenPlay("idle"));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animatableInstanceCache;
    }
}
