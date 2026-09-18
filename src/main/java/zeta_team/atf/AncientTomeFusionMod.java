package zeta_team.atf;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import org.violetmoon.zeta.config.Config.Min;
import zeta_team.atf.client.screen.AncientTomeFusionConfigScreen;
import zeta_team.atf.config.AncientTomeFusionConfig;
import zeta_team.atf.network.UpdateATFConfigPayload;

@Mod(AncientTomeFusionMod.MODID)
public class AncientTomeFusionMod {
    public static final String MODID = "atf";
    private static final Logger LOGGER = LogUtils.getLogger();

    public AncientTomeFusionMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerPayloads);
        modEventBus.addListener(AncientTomeFusionConfig::onLoad);
        if (FMLLoader.getDist().isClient()) {
            ClientOnly.registerConfigScreen(modContainer);
        } else {
            modContainer.registerConfig(Type.SERVER, AncientTomeFusionConfig.SPEC);
        }
    }

    // Inner static helper class to isolate Client-only references
    @OnlyIn(Dist.CLIENT)
    private static class ClientOnly {
        private static void registerConfigScreen(ModContainer modContainer) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> {
               Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && !mc.player.hasPermissions(2)) {
                    return parent;
                }
                return new AncientTomeFusionConfigScreen(container, parent);
            });
        }
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                UpdateATFConfigPayload.TYPE,
                UpdateATFConfigPayload.STREAM_CODEC,
                UpdateATFConfigPayload::handle
        );
    }
}