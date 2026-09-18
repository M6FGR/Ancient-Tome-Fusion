package zeta_team.atf.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import org.violetmoon.quark.content.tools.item.AncientTomeItem;
import org.violetmoon.quark.content.tools.module.AncientTomesModule;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class AncientTomeFusionJeiPlugin implements IModPlugin {
    public static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath("atf", "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (!AncientTomesModule.validEnchants.isEmpty()) {
            List<ItemStack> validTomes = new ArrayList<>();

            for (Holder<Enchantment> enchant : AncientTomesModule.validEnchants) {
                validTomes.add(AncientTomeItem.getEnchantedItemStack(enchant));
            }

            if (validTomes.isEmpty()) {
                validTomes.add(new ItemStack(AncientTomesModule.ancient_tome));
            }

            ItemStack tomeResult = new ItemStack(AncientTomesModule.ancient_tome);
            tomeResult.set(DataComponents.CUSTOM_NAME, Component.literal("Random Ancient Tome"));
            tomeResult.set(DataComponents.LORE, new ItemLore(List.of(
                    Component.literal("Combine two Ancient Tomes to fuse or reroll").withStyle(Style.EMPTY.withItalic(false))
            )));

            var anvilRecipe = registration.getVanillaRecipeFactory().createAnvilRecipe(
                    validTomes,
                    validTomes,
                    List.of(tomeResult),
                    ResourceLocation.fromNamespaceAndPath("atf", "ancient_tome_fusion")
            );

            registration.addRecipes(RecipeTypes.ANVIL, List.of(anvilRecipe));
        }
    }
}