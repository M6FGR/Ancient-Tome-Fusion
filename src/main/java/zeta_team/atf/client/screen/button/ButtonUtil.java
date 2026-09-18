package zeta_team.atf.client.screen.button;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class ButtonUtil extends ContainerObjectSelectionList<ButtonUtil.OptionEntry> {
    public ButtonUtil(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
        this.centerListVertically = false;
    }

    public int addBigButton(AbstractWidget button1) {
        return this.addEntry(OptionEntry.big(button1));
    }

    public void addSmallButton(AbstractWidget button1, @Nullable AbstractWidget button2) {
        this.addEntry(OptionEntry.small(button1, button2));
    }
    public void addSmallButton(AbstractWidget button1) {
        this.addEntry(OptionEntry.small(button1, null));
    }

    public <T> AbstractSliderButton addSliderButton(int x, int y, int width, int height, Component message, double value, Consumer<Double> updateValue) {
        return new AbstractSliderButton(x, y, width, height, message, value) {
            @Override
            protected void updateMessage() {
                this.setMessage(message);
            }

            @Override
            protected void applyValue() {
                updateValue.accept(this.value);
            }
        };
    }

    public int getRowWidth() {
        return 400;
    }

    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 46;
    }

    public static class OptionEntry extends ContainerObjectSelectionList.Entry<OptionEntry> {
        final List<AbstractWidget> children;

        private OptionEntry(List<AbstractWidget> widgets) {
            this.children = ImmutableList.copyOf(widgets);
        }

        public static OptionEntry big(AbstractWidget widget) {
            return new OptionEntry(List.of(widget));
        }

        public static OptionEntry small(AbstractWidget button1, @Nullable AbstractWidget button2) {
            return button2 == null ? new OptionEntry(List.of(button1)) : new OptionEntry(List.of(button1, button2));
        }

        public void render(@NotNull GuiGraphics guiGraphics, int x, int y, int p_94499_, int p_94500_, int p_94501_, int mouseX, int mouseY, boolean p_94504_, float partialTicks) {
            this.children.forEach((widget) -> {
                widget.setY(y);
                widget.render(guiGraphics, mouseX, mouseY, partialTicks);
            });
        }

        public @NotNull List<? extends GuiEventListener> children() {
            return this.children;
        }

        public @NotNull List<? extends NarratableEntry> narratables() {
            return this.children;
        }
    }
}