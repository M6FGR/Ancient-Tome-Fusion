package zeta_team.atf.client.screen.subs;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import zeta_team.atf.config.AncientTomeFusionConfig;

@OnlyIn(Dist.CLIENT)
public class AncientTomeWeightsScreen extends Screen {
    private final Screen parentScreen;
    private EditBox keyInput;
    private EditBox weightInput;
    private WeightList listWidget;
    private String ghostText = "";

    public AncientTomeWeightsScreen(Screen parent) {
        super(Component.literal("Edit Enchantment Weight Overrides"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        int leftX = this.width / 2 - 130;
        int topY = 35;

        this.keyInput = new EditBox(this.font, leftX, topY, 130, 20, Component.literal("Enchantment ID..."));
        this.keyInput.setResponder(this::updateGhostText);
        this.addRenderableWidget(this.keyInput);

        this.weightInput = new EditBox(this.font, leftX + 135, topY, 45, 20, Component.literal("Weight"));
        this.weightInput.setValue("10");
        this.addRenderableWidget(this.weightInput);

        this.addRenderableWidget(Button.builder(Component.literal("Set"), button -> addWeightOverride())
                .bounds(leftX + 185, topY, 75, 20)
                .build());

        this.listWidget = new WeightList(this, leftX, topY + 25, 260, this.height - topY - 65);
        this.addRenderableWidget(this.listWidget);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                .bounds(this.width / 2 - 100, this.height - 30, 200, 20)
                .build());

        this.listWidget.refresh();
    }

    private void updateGhostText(String input) {
        if (input.isEmpty()) {
            this.ghostText = "";
            return;
        }

        String search = input.toLowerCase();
        this.ghostText = this.minecraft.level.registryAccess()
                .registry(net.minecraft.core.registries.Registries.ENCHANTMENT)
                .map(registry -> registry.keySet().stream()
                        .map(ResourceLocation::toString)
                        .filter(id -> id.startsWith(search))
                        .findFirst()
                        .orElse("")
                )
                .orElse("");
    }

    private void addWeightOverride() {
        String key = !this.ghostText.isEmpty() ? this.ghostText : this.keyInput.getValue().trim();
        String weightStr = this.weightInput.getValue().trim();

        if (!key.isEmpty() && !weightStr.isEmpty()) {
            try {
                int weight = Integer.parseInt(weightStr);
                AncientTomeFusionConfig.enchantmentWeights.put(key, weight);
                this.listWidget.refresh();
                this.keyInput.setValue("");
                this.ghostText = "";
            } catch (NumberFormatException ignored) {}
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.keyInput.isFocused() || this.weightInput.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_TAB && this.keyInput.isFocused()) {
                if (!this.ghostText.isEmpty()) {
                    this.keyInput.setValue(this.ghostText);
                    return true;
                }
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                this.addWeightOverride();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);

        if (!this.ghostText.isEmpty() && this.keyInput.isVisible() && this.keyInput.isFocused()) {
            String currentInput = this.keyInput.getValue();
            if (this.ghostText.toLowerCase().startsWith(currentInput.toLowerCase())) {
                int typedWidth = this.font.width(currentInput);
                int textX = this.keyInput.getX() + 4 + typedWidth;
                int textY = this.keyInput.getY() + (this.keyInput.getHeight() - 8) / 2;

                String suffix = this.ghostText.substring(currentInput.length());
                guiGraphics.drawString(this.font, suffix, textX, textY, 0x606060, false);
            }
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    class WeightList extends ObjectSelectionList<WeightList.WeightEntry> {
        public WeightList(AncientTomeWeightsScreen parent, int x, int y, int width, int height) {
            super(parent.minecraft, width, height, y, 18);
            this.setX(x);
        }

        public void refresh() {
            this.clearEntries();
            AncientTomeFusionConfig.enchantmentWeights.forEach((key, weight) ->
                    this.addEntry(new WeightEntry(key, weight))
            );
        }

        class WeightEntry extends ObjectSelectionList.Entry<WeightEntry> {
            private final String key;
            private final int weight;

            public WeightEntry(String key, int weight) {
                this.key = key;
                this.weight = weight;
            }

            @Override
            public void render(GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float pt) {
                g.drawString(font, key + " = " + weight, left + 5, top + 4, 0x00FF00);
                g.drawString(font, "X", left + width - 12, top + 4, isHovered ? 0xFF5555 : 0x555555);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    AncientTomeFusionConfig.enchantmentWeights.remove(this.key);
                    WeightList.this.refresh();
                    return true;
                }
                return false;
            }

            @Override
            public Component getNarration() {
                return Component.literal(key + " weight " + weight);
            }
        }
    }
}