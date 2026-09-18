package zeta_team.atf.mixins;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.base.components.QuarkDataComponents;
import zeta_team.atf.config.AncientTomeFusionConfig;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Mixin(value = AnvilMenu.class, priority = 1005)
public abstract class AnvilMenuMixin extends ItemCombinerMenu {

    @Shadow
    @Final
    private DataSlot cost;
    @Shadow
    public int repairItemCountCost;

    public AnvilMenuMixin(@Nullable MenuType<?> type, int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(type, containerId, playerInventory, access);
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    private void combineQuarkAncientTomes(CallbackInfo ci) {
        ItemStack slot0 = this.inputSlots.getItem(0);
        ItemStack slot1 = this.inputSlots.getItem(1);

        Item ancientTomeItem = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse("quark:ancient_tome"));

        if (ancientTomeItem != null && slot0.is(ancientTomeItem) && slot1.is(ancientTomeItem)) {
            ItemStack result = new ItemStack(ancientTomeItem);

            long worldSeed = this.player.level().getGameTime();
            long seed = this.getStackSeed(slot0) ^ (this.getStackSeed(slot1) * 31L) ^ worldSeed;

            Optional<Registry<Enchantment>> enchantmentRegistry = this.player.level().registryAccess().registry(Registries.ENCHANTMENT);

            if (enchantmentRegistry.isPresent()) {
                Registry<Enchantment> reg = enchantmentRegistry.get();

                boolean useWhitelist = AncientTomeFusionConfig.useWhitelist;
                List<? extends String> activeFilterList = useWhitelist ?
                        AncientTomeFusionConfig.whitelistedEnchantments :
                        AncientTomeFusionConfig.blacklistedEnchantments;

                Map<String, Integer> weightOverrides = AncientTomeFusionConfig.enchantmentWeights;
                int defaultWeight = Math.max(1, AncientTomeFusionConfig.defaultEnchantmentWeight);

                List<Holder.Reference<Enchantment>> validEnchantments = reg.holders()
                        .filter(ref -> {
                            String enchId = ref.key().location().toString();
                            if (activeFilterList == null || activeFilterList.isEmpty()) {
                                return !useWhitelist;
                            }
                            return useWhitelist == activeFilterList.contains(enchId);
                        })
                        .toList();

                if (!validEnchantments.isEmpty()) {
                    Random seededRandom = new Random(seed);

                    int totalWeight = 0;
                    for (Holder.Reference<Enchantment> ref : validEnchantments) {
                        String enchId = ref.key().location().toString();
                        int w = weightOverrides != null ? weightOverrides.getOrDefault(enchId, defaultWeight) : defaultWeight;
                        totalWeight += Math.max(0, w);
                    }

                    if (totalWeight <= 0) {
                        totalWeight = validEnchantments.size() * defaultWeight;
                    }

                    Holder<Enchantment> chosenEnch = validEnchantments.getFirst();
                    int randomWeight = seededRandom.nextInt(totalWeight);

                    for (Holder.Reference<Enchantment> ref : validEnchantments) {
                        String enchId = ref.key().location().toString();
                        int weight = weightOverrides != null ? weightOverrides.getOrDefault(enchId, defaultWeight) : defaultWeight;
                        weight = Math.max(1, weight);

                        if (randomWeight < weight) {
                            chosenEnch = ref;
                            break;
                        }
                        randomWeight -= weight;
                    }

                    ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                    builder.set(chosenEnch, 1);

                    result.set(QuarkDataComponents.TOME_ENCHANTMENTS, builder.toImmutable());
                }
            }

            Component title = Component.literal("Random Ancient Tome")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE).withItalic(false));
            result.set(DataComponents.CUSTOM_NAME, title);
            result.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);

            Component customDescriptionLine1 = Component.literal("????????????????")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withObfuscated(true).withItalic(false));
            result.set(DataComponents.LORE, new ItemLore(List.of(customDescriptionLine1)));

            CustomData.update(DataComponents.CUSTOM_DATA, result, compound -> {
                compound.putBoolean("IsHiddenTome", true);
                compound.putLong("TomeSeed", seed);
            });

            this.resultSlots.setItem(0, result);
            this.cost.set(AncientTomeFusionConfig.combinationCost);
            this.repairItemCountCost = 1;

            this.broadcastChanges();
            ci.cancel();
        }
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    private void revealEnchantmentOnTake(Player player, ItemStack stack, CallbackInfo ci) {
        this.revealStack(stack);
        this.revealStack(this.resultSlots.getItem(0));
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

    @Unique
    private long getStackSeed(ItemStack stack) {
        var customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.copyTag().contains("TomeSeed")) {
            return customData.copyTag().getLong("TomeSeed");
        }
        return stack.getComponents().hashCode();
    }
}