package zeta_team.atf.mixins;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ItemCombinerMenu.class, priority = 1005)
public abstract class ItemCombinerMenuMixin {

    @Inject(method = "quickMoveStack", at = @At("HEAD"))
    private void revealOnQuickMove(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        ItemCombinerMenu menu = (ItemCombinerMenu) (Object) this;

        if (menu instanceof AnvilMenu && index == menu.getResultSlot()) {
            ItemStack resultStack = menu.getSlot(menu.getResultSlot()).getItem();
            this.revealStack(resultStack);
        }
    }

    @Unique
    private void revealStack(ItemStack stack) {
        if (stack.isEmpty()) return;

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.copyTag().getBoolean("IsHiddenTome")) {
            stack.remove(DataComponents.CUSTOM_NAME);
            stack.remove(DataComponents.LORE);
            stack.remove(DataComponents.HIDE_ADDITIONAL_TOOLTIP);

            CustomData.update(DataComponents.CUSTOM_DATA, stack, compound -> {
                compound.remove("IsHiddenTome");
                compound.remove("TomeSeed");
            });
        }
    }
}