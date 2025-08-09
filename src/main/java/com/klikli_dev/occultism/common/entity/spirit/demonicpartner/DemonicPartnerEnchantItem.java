package com.klikli_dev.occultism.common.entity.spirit.demonicpartner;

import com.klikli_dev.occultism.Occultism;
import com.klikli_dev.occultism.registry.OccultismDataComponents;
import com.klikli_dev.occultism.registry.OccultismEntities;
import com.klikli_dev.occultism.registry.OccultismItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class DemonicPartnerEnchantItem extends Goal {
    final DemonicPartner mob;
    final UniformFloat maxDuration;
    Optional<RecipeHolder<SmokingRecipe>> lastRecipe = Optional.empty();
    float duration;
    public DemonicPartnerEnchantItem(DemonicPartner mob, UniformFloat duration){
        this.mob = mob;
        this.maxDuration = duration;
    }

    @Override
    public boolean canUse() {
        if (this.mob.getType().equals(OccultismEntities.DEMONIC_CHARMER.get())) return false;
        ItemStack itemStack = this.mob.getMainHandItem();
        if (itemStack.isEmpty() || !this.mob.isInSittingPose()) return false;
        return itemStack.is(OccultismItems.CATALYST_CRYSTAL_ITEM);
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.mob.getMainHandItem().getOrDefault(OccultismDataComponents.CATALYST_CRYSTAL_CHARGE_STATE,false);
    }

    @Override
    public void start() {
        super.start();
        this.duration = this.maxDuration.sample(this.mob.getRandom()) * 20;
        this.mob.getNavigation().stop(); // Prevent it moving
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void stop() {
        super.stop();
        try{
            this.mob.dropItem(this.mob.getMainHandItem());
        }catch (Exception ignored){}
        this.mob.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        super.tick();
        this.duration--;
        if (this.duration <= 0)
        {
            ItemStack heldItem = this.mob.getMainHandItem();
            heldItem.set(OccultismDataComponents.CATALYST_CRYSTAL_CHARGE_STATE,true);
        }
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }
}
