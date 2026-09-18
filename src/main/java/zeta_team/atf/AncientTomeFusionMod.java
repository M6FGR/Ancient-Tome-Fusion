package zeta_team.tome_randomizer;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.loading.FMLLoader;
import org.slf4j.Logger;
import zeta_team.tome_randomizer.config.TomeRandomizerConfig;

@Mod(value = TomeRandomizer.MODID, dist = Dist.DEDICATED_SERVER)
public class TomeRandomizer {
    public static final String MODID = "tome_randomizer";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TomeRandomizer(IEventBus modEventBus, ModContainer modContainer) {
        // safety check, we do NOT want this server to initialize on the client
        modContainer.registerConfig(Type.SERVER, TomeRandomizerConfig.SPEC);

    }

}
