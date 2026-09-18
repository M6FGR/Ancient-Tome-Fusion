package zeta_team.atf.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import zeta_team.atf.config.AncientTomeFusionConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record UpdateATFConfigPayload(
        boolean useWhitelist,
        int combinationCost,
        int defaultEnchantmentWeight,
        List<String> whitelist,
        List<String> blacklist,
        Map<String, Integer> weights
) implements CustomPacketPayload {

    public static final Type<UpdateATFConfigPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("atf", "update_config"));

    private static final StreamCodec<FriendlyByteBuf, Map<String, Integer>> WEIGHTS_CODEC =
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT);

    public static final StreamCodec<FriendlyByteBuf, UpdateATFConfigPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, UpdateATFConfigPayload::useWhitelist,
            ByteBufCodecs.VAR_INT, UpdateATFConfigPayload::combinationCost,
            ByteBufCodecs.VAR_INT, UpdateATFConfigPayload::defaultEnchantmentWeight,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), UpdateATFConfigPayload::whitelist,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), UpdateATFConfigPayload::blacklist,
            WEIGHTS_CODEC, UpdateATFConfigPayload::weights,
            UpdateATFConfigPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final UpdateATFConfigPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();

            if (player.hasPermissions(2)) {
                AncientTomeFusionConfig.useWhitelist = payload.useWhitelist();
                AncientTomeFusionConfig.combinationCost = payload.combinationCost();
                AncientTomeFusionConfig.defaultEnchantmentWeight = payload.defaultEnchantmentWeight();

                AncientTomeFusionConfig.whitelistedEnchantments = new ArrayList<>(payload.whitelist());
                AncientTomeFusionConfig.blacklistedEnchantments = new ArrayList<>(payload.blacklist());
                AncientTomeFusionConfig.enchantmentWeights = new HashMap<>(payload.weights());

                AncientTomeFusionConfig.save();
            }
        });
    }
}