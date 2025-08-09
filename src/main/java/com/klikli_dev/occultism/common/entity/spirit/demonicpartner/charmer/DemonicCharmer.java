package com.klikli_dev.occultism.common.entity.spirit.demonicpartner.charmer;

import com.klikli_dev.occultism.Occultism;
import com.klikli_dev.occultism.common.entity.spirit.demonicpartner.DemonicPartner;
import com.klikli_dev.occultism.registry.OccultismDataComponents;
import com.klikli_dev.occultism.registry.OccultismItems;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.Lazy;
import net.tslat.smartbrainlib.util.RandomUtil;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Objects;

public class DemonicCharmer extends DemonicPartner implements GeoEntity {
    public static final EntityDataAccessor<Integer> HAIR_VARIANT = SynchedEntityData.defineId(DemonicCharmer.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> HORN_VARIANT = SynchedEntityData.defineId(DemonicCharmer.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> POWER_LEVEL = SynchedEntityData.defineId(DemonicCharmer.class, EntityDataSerializers.FLOAT);

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Occultism.MODID, "demonic_charmer");
    public static final Lazy<EntityType<DemonicCharmer>> ENTITY_TYPE =
            Lazy.of(() -> EntityType.Builder.of(DemonicCharmer::new, MobCategory.CREATURE)
                    .sized(0.6F, 2)
                    .fireImmune()
                    .clientTrackingRange(8)
                    .build(ID.toString()));
    boolean isInCombatEnergized = false;
    AnimatableInstanceCache animatableInstanceCache = GeckoLibUtil.createInstanceCache(this);

    protected DemonicCharmer(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class,true,(x)-> this.isInCombatEnergized && x.equals(this.getOwner())));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HAIR_VARIANT, this.getRandom().nextIntBetweenInclusive(0,1));
        builder.define(HORN_VARIANT, this.getRandom().nextIntBetweenInclusive(0,2));
        builder.define(POWER_LEVEL,-1f);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("hair_variant",this.entityData.get(HAIR_VARIANT));
        compound.putInt("horn_variant",this.entityData.get(HORN_VARIANT));
        compound.putFloat("full_power",this.entityData.get(POWER_LEVEL));
        compound.putBoolean("energized",this.isInCombatEnergized);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.entityData.set(HAIR_VARIANT,compound.getInt("hair_variant"));
        this.entityData.set(HORN_VARIANT,compound.getInt("horn_variant"));
        this.entityData.set(POWER_LEVEL,compound.getFloat("full_power"));
        this.isInCombatEnergized = compound.getBoolean("energized");
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
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pHand);
        if (!this.isInCombatEnergized && this.entityData.get(POWER_LEVEL) == -1 && !itemStack.isEmpty() && itemStack.is(OccultismItems.CATALYST_CRYSTAL_ITEM) && itemStack.getOrDefault(OccultismDataComponents.CATALYST_CRYSTAL_CHARGE_STATE,false)){
            if (RandomUtil.oneInNChance(3))
            { // One in 3 chance that it will enter its "Combat Energized" and will require beating to fully control
                this.isInCombatEnergized = true;
                this.setOrderedToSit(false);
            }
            this.entityData.set(POWER_LEVEL,this.entityData.get(POWER_LEVEL)+1);
            if (!pPlayer.isCreative())
                itemStack.shrink(1);
        } else if (this.isInCombatEnergized)
            return InteractionResult.FAIL;
        return super.mobInteract(pPlayer,pHand);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.entityData.get(POWER_LEVEL) >= 0 && !this.isInCombatEnergized)
        {
            if (this.entityData.get(POWER_LEVEL) < this.getMaxHealth())
                this.entityData.set(POWER_LEVEL,this.entityData.get(POWER_LEVEL)+0.05f);
            else
                this.entityData.set(POWER_LEVEL,-2f);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean wasHurt = super.hurt(source, amount);
        if (this.isInCombatEnergized && this.getHealth() <= 0)
        {
            this.isInCombatEnergized = false;
            this.setHealth(1);
            this.entityData.set(POWER_LEVEL,-2f);
        } else if (this.isInCombatEnergized){
            this.entityData.set(POWER_LEVEL,this.entityData.get(POWER_LEVEL)+amount);
        }
        return wasHurt;
    }

    @Override
    public void die(DamageSource cause) {
        if (!this.isInCombatEnergized)
            super.die(cause);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animatableInstanceCache;
    }
    @Override
    public double getAttributeValue(Holder<Attribute> attribute) {
        if (this.entityData.get(POWER_LEVEL) == -1)
            return super.getAttributeValue(attribute) / 2;
        return super.getAttributeValue(attribute);
    }

    @Nullable
    @Override
    public AttributeInstance getAttribute(Holder<Attribute> attribute) {
        AttributeInstance baseAttribute = super.getAttribute(attribute);
        if (this.entityData.get(POWER_LEVEL) == -1 && baseAttribute != null)
            baseAttribute.setBaseValue(baseAttribute.getBaseValue() / 2);
        return baseAttribute;
    }

    @Override
    public double getAttributeBaseValue(Holder<Attribute> attribute) {
        if (this.entityData.get(POWER_LEVEL) == -1)
            return super.getAttributeValue(attribute) / 2;
        return super.getAttributeValue(attribute);
    }
}
