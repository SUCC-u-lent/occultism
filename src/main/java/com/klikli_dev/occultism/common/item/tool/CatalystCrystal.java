package com.klikli_dev.occultism.common.item.tool;

import com.klikli_dev.occultism.registry.OccultismDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CatalystCrystal extends Item {
    public CatalystCrystal(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.getComponents().has(OccultismDataComponents.CATALYST_CRYSTAL_CHARGE_STATE.get()) && Boolean.TRUE.equals(stack.getComponents().get(OccultismDataComponents.CATALYST_CRYSTAL_CHARGE_STATE.get()));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false; // Never enchantable.
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        boolean isCharged = false;
        if (stack.getComponents().has(OccultismDataComponents.CATALYST_CRYSTAL_CHARGE_STATE.get()))
            isCharged = Boolean.TRUE.equals(stack.getComponents().get(OccultismDataComponents.CATALYST_CRYSTAL_CHARGE_STATE.get()));
        tooltipComponents.add(
                isCharged ?
                Component.translatable("enum.occultism.catalyst_crystal.charged") :
                Component.translatable("enum.occultism.catalyst_crystal.uncharged")
        );
        if (isCharged)
            tooltipComponents.add(Component.translatable("enum.occultism.catalyst_crystal.charged.usage"));
    }
}
