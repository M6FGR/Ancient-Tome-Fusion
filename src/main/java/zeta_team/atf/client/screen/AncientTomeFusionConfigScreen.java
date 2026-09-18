package zeta_team.atf.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zeta_team.atf.client.screen.button.ButtonUtil;
import zeta_team.atf.client.screen.subs.AncientTomeListScreen;
import zeta_team.atf.client.screen.subs.AncientTomeWeightsScreen;
import zeta_team.atf.config.AncientTomeFusionConfig;
import zeta_team.atf.network.UpdateATFConfigPayload;

public class AncientTomeFusionConfigScreen extends Screen {
    private ButtonUtil optionList;
    private final Screen previousScreen;

    public AncientTomeFusionConfigScreen(@Nullable ModContainer mod, Screen screen) {
        super(Component.literal("Ancient Tome Fusion Config"));
        this.previousScreen = screen;
    }

    @Override
    protected void init() {
        int buttonHeight = -32;
        this.optionList = new ButtonUtil(this.minecraft, this.width, this.height - 64, 32, 25);
        int rowY = this.height / 4 + buttonHeight;

        Button filterModeBtn = Button.builder(
                        Component.literal("Filter Mode: " + (AncientTomeFusionConfig.useWhitelist ? "Whitelist" : "Blacklist")),
                        button -> {
                            AncientTomeFusionConfig.useWhitelist = !AncientTomeFusionConfig.useWhitelist;
                            button.setMessage(Component.literal("Filter Mode: " + (AncientTomeFusionConfig.useWhitelist ? "Whitelist" : "Blacklist")));
                            this.rebuildWidgets();
                        }
                )
                .pos(this.width / 2 + 5, rowY)
                .size(160, 20)
                .tooltip(Tooltip.create(Component.literal("Toggle whether the filter acts as a Whitelist or a Blacklist")))
                .build();

        AbstractSliderButton combinationCostSlider = new AbstractSliderButton(
                this.width / 2 - 165, rowY, 160, 20,
                Component.literal("Fusion Cost: " + AncientTomeFusionConfig.combinationCost + " Levels"),
                (double) AncientTomeFusionConfig.combinationCost / 100.0D
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Fusion Cost: " + AncientTomeFusionConfig.combinationCost + " Levels"));
            }

            @Override
            protected void applyValue() {
                AncientTomeFusionConfig.combinationCost = (int) (this.value * 100.0D);
            }
        };
        combinationCostSlider.setTooltip(Tooltip.create(Component.literal("The XP level cost required to fuse Ancient Tomes in an Anvil")));

        optionList.addSmallButton(filterModeBtn, combinationCostSlider);
        buttonHeight += 24;
        rowY = this.height / 4 + buttonHeight;

        AbstractSliderButton defaultWeightSlider = new AbstractSliderButton(
                this.width / 2 - 165, rowY, 160, 20,
                Component.literal("Default Weight: " + AncientTomeFusionConfig.defaultEnchantmentWeight),
                (double) (AncientTomeFusionConfig.defaultEnchantmentWeight - 1) / 99.0D
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.literal("Default Weight: " + AncientTomeFusionConfig.defaultEnchantmentWeight));
            }

            @Override
            protected void applyValue() {
                AncientTomeFusionConfig.defaultEnchantmentWeight = 1 + (int) (this.value * 99.0D);
            }
        };
        defaultWeightSlider.setTooltip(Tooltip.create(Component.literal("Default RNG probability weight for enchantments without specific overrides")));

        int listSize = AncientTomeFusionConfig.useWhitelist ?
                AncientTomeFusionConfig.whitelistedEnchantments.size() :
                AncientTomeFusionConfig.blacklistedEnchantments.size();

        Button filterListBtn = Button.builder(
                        Component.literal("Edit Filter (" + listSize + ")"),
                        button -> {
                            if (this.minecraft != null) {
                                this.minecraft.setScreen(new AncientTomeListScreen(this));
                            }
                        }
                )
                .pos(this.width / 2 + 5, rowY)
                .size(160, 20)
                .tooltip(Tooltip.create(Component.literal("Manage entries in the active " +
                        (AncientTomeFusionConfig.useWhitelist ? "whitelist" : "blacklist"))))
                .build();

        optionList.addSmallButton(defaultWeightSlider, filterListBtn);
        buttonHeight += 24;
        rowY = this.height / 4 + buttonHeight;

        Button weightOverridesBtn = Button.builder(
                        Component.literal("Edit Weights (" + AncientTomeFusionConfig.enchantmentWeights.size() + ")"),
                        button -> {
                            if (this.minecraft != null) {
                                this.minecraft.setScreen(new AncientTomeWeightsScreen(this));
                            }
                        }
                )
                .pos(this.width / 2 - 165, rowY)
                .size(160, 20)
                .tooltip(Tooltip.create(Component.literal("Configure custom per-enchantment weight overrides")))
                .build();

        optionList.addSmallButton(weightOverridesBtn, null);

        int doneBtnWidth = 200;
        int doneBtnHeight = 20;

        Button exitButton = Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .pos(this.width / 2 - (doneBtnWidth / 2), this.height - doneBtnHeight - 10)
                .size(doneBtnWidth, doneBtnHeight)
                .build();

        this.addWidget(optionList);
        this.addRenderableWidget(exitButton);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        if (optionList != null) {
            this.renderOptionList(guiGraphics, optionList, mouseX, mouseY, partialTicks);
        }
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.previousScreen);
        }
        this.saveConfig();
    }

    @Override
    public void removed() {
        this.saveConfig();
    }

    private void saveConfig() {
        PacketDistributor.sendToServer(new UpdateATFConfigPayload(
                AncientTomeFusionConfig.useWhitelist,
                AncientTomeFusionConfig.combinationCost,
                AncientTomeFusionConfig.defaultEnchantmentWeight,
                AncientTomeFusionConfig.whitelistedEnchantments,
                AncientTomeFusionConfig.blacklistedEnchantments,
                AncientTomeFusionConfig.enchantmentWeights
        ));
    }

    private void renderOptionList(GuiGraphics guiGraphics, ContainerObjectSelectionList<?> optionList, int mouseX, int mouseY, float partialTicks) {
        optionList.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
}