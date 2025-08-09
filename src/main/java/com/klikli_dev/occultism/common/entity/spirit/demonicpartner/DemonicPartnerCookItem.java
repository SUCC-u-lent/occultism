package com.klikli_dev.occultism.common.entity.spirit.demonicpartner;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentTable;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.Optional;

public class DemonicPartnerCookItem extends Goal {
    final DemonicPartner mob;
    final UniformFloat maxDuration;
    Optional<RecipeHolder<SmokingRecipe>> lastRecipe = Optional.empty();
    float duration;
    public DemonicPartnerCookItem(DemonicPartner mob, UniformFloat duration){
        this.mob = mob;
        this.maxDuration = duration;
    }

    @Override
    public boolean canUse() {
        ItemStack itemStack = this.mob.getMainHandItem();
        if (itemStack.isEmpty() || !this.mob.isInSittingPose()) return false;
        Optional<RecipeHolder<SmokingRecipe>> recipe = this.lastRecipe.isPresent() ? this.lastRecipe.get().value().ingredient.test(itemStack) ? this.lastRecipe : this.mob.getRecipe(itemStack) : this.mob.getRecipe(itemStack);
        recipe.ifPresent((holder)->this.lastRecipe = recipe);
        return recipe.isPresent();
    }

    @Override
    public void start() {
        super.start();
        this.duration = this.maxDuration.sample(this.mob.getRandom()) * 20;
        this.mob.getNavigation().stop(); // Prevent it moving
    }

    private void cookItem()
    {
        Level level = this.mob.level();
        if (this.lastRecipe.isEmpty()) return;
        ItemStack result = this.lastRecipe.get().value().getResultItem(level.registryAccess());

        this.mob.dropItem(result); // Just drop the item, much more realistic instead of making it appear in their inventory.

        for (int i = 0; i < 2; i++) {
            Vec3 pos = this.mob.position().add((this.mob.getRandom().nextFloat() - 0.5f) * 0.7,
                    1.5 + (this.mob.getRandom().nextFloat() - 0.5f) * 0.7, (this.mob.getRandom().nextFloat() - 0.5f) * 0.7);
            ((ServerLevel) level).sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
        }
        this.lastRecipe = Optional.empty();
        this.mob.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        this.duration--;
        if (this.duration <= 0)
            this.cookItem();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }
}
