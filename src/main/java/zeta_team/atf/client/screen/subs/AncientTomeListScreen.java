package zeta_team.atf.client.screen.subs;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import zeta_team.atf.config.AncientTomeFusionConfig;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class AncientTomeListScreen extends Screen {
    private final Screen parentScreen;
    private EditBox addInput;
    private EntryList listWidget;
    private String ghostText = "";

    public AncientTomeListScreen(Screen parent) {
        super(Component.literal("Edit Filter List (" + (AncientTomeFusionConfig.useWhitelist ? "Whitelist" : "Blacklist") + ")"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        int leftX = this.width / 2 - 120;
        int topY = 35;

        this.addInput = new EditBox(this.font, leftX, topY, 180, 20, Component.literal("Enchantment ID..."));
        this.addInput.setResponder(this::updateGhostText);
        this.addRenderableWidget(this.addInput);

        this.addRenderableWidget(Button.builder(Component.literal("Add"), button -> addEntry())
                .bounds(leftX + 185, topY, 55, 20)
                .build());

        this.listWidget = new EntryList(this, leftX, topY + 25, 240, this.height - topY - 65);
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

    private void addEntry() {
        String val = !this.ghostText.isEmpty() ? this.ghostText : this.addInput.getValue().trim();
        if (!val.isEmpty()) {
            List<String> targetList = AncientTomeFusionConfig.useWhitelist ?
                    AncientTomeFusionConfig.whitelistedEnchantments :
                    AncientTomeFusionConfig.blacklistedEnchantments;

            if (!targetList.contains(val)) {
                targetList.add(val);
                this.listWidget.refresh();
            }
            this.addInput.setValue("");
            this.ghostText = "";
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.addInput.isFocused()) {
            // TAB auto-completes ghost text into the box
            if (keyCode == GLFW.GLFW_KEY_TAB) {
                if (!this.ghostText.isEmpty()) {
                    this.addInput.setValue(this.ghostText);
                    return true;
                }
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                this.addEntry();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);

        // Render Ghost Text
        if (!this.ghostText.isEmpty() && this.addInput.isVisible() && this.addInput.isFocused()) {
            String currentInput = this.addInput.getValue();
            if (this.ghostText.toLowerCase().startsWith(currentInput.toLowerCase())) {
                int typedWidth = this.font.width(currentInput);
                int textX = this.addInput.getX() + 4 + typedWidth;
                int textY = this.addInput.getY() + (this.addInput.getHeight() - 8) / 2;

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

    class EntryList extends ObjectSelectionList<EntryList.ListEntry> {
        public EntryList(AncientTomeListScreen parent, int x, int y, int width, int height) {
            super(parent.minecraft, width, height, y, 18);
            this.setX(x);
        }

        public void refresh() {
            this.clearEntries();
            List<String> entries = AncientTomeFusionConfig.useWhitelist ?
                    AncientTomeFusionConfig.whitelistedEnchantments :
                    AncientTomeFusionConfig.blacklistedEnchantments;

            entries.forEach(item -> this.addEntry(new ListEntry(item)));
        }

        class ListEntry extends ObjectSelectionList.Entry<ListEntry> {
            private final String value;

            public ListEntry(String value) {
                this.value = value;
            }

            @Override
            public void render(GuiGraphics g, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float pt) {
                g.drawString(font, value, left + 5, top + 4, 0xAAAAAA);
                g.drawString(font, "X", left + width - 12, top + 4, isHovered ? 0xFF5555 : 0x555555);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    List<String> targetList = AncientTomeFusionConfig.useWhitelist ?
                            AncientTomeFusionConfig.whitelistedEnchantments :
                            AncientTomeFusionConfig.blacklistedEnchantments;
                    targetList.remove(this.value);
                    EntryList.this.refresh();
                    return true;
                }
                return false;
            }

            @Override
            public Component getNarration() {
                return Component.literal(value);
            }
        }
    }
}