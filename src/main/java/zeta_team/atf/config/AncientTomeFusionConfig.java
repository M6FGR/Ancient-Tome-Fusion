package zeta_team.atf.config;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AncientTomeFusionConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue USE_WHITELIST;
    private static final ModConfigSpec.IntValue COMBINATION_COST;
    private static final ModConfigSpec.IntValue DEFAULT_ENCHANTMENT_WEIGHT;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> WHITELISTED_ENCHANTMENTS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLISTED_ENCHANTMENTS;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ENCHANTMENT_WEIGHTS_RAW;

    // Local mutable state
    public static boolean useWhitelist;
    public static int combinationCost;
    public static int defaultEnchantmentWeight;
    public static List<String> whitelistedEnchantments = new ArrayList<>();
    public static List<String> blacklistedEnchantments = new ArrayList<>();
    public static Map<String, Integer> enchantmentWeights = new HashMap<>();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("General Settings");
        USE_WHITELIST = builder.define("useWhitelist", false);
        COMBINATION_COST = builder.defineInRange("combinationCost", 10, 0, 100);
        DEFAULT_ENCHANTMENT_WEIGHT = builder.defineInRange("defaultEnchantmentWeight", 10, 1, 100);

        WHITELISTED_ENCHANTMENTS = builder.defineListAllowEmpty("whitelistedEnchantments", List.of("minecraft:sharpness", "minecraft:fortune"), obj -> obj instanceof String);

        List<String> defaultBlacklist = List.of(
                "minecraft:binding_curse",
                "minecraft:vanishing_curse"
        );
        BLACKLISTED_ENCHANTMENTS = builder.defineListAllowEmpty("blacklistedEnchantments", () -> new ArrayList<>(defaultBlacklist), obj -> obj instanceof String);

        // Keep weights raw list empty by default
        ENCHANTMENT_WEIGHTS_RAW = builder.defineListAllowEmpty("enchantmentWeights", ArrayList::new, obj -> obj instanceof String);

        builder.pop();
        SPEC = builder.build();
    }

    public static void save() {
        USE_WHITELIST.set(useWhitelist);
        COMBINATION_COST.set(combinationCost);
        DEFAULT_ENCHANTMENT_WEIGHT.set(defaultEnchantmentWeight);

        WHITELISTED_ENCHANTMENTS.set(new ArrayList<>(whitelistedEnchantments));
        BLACKLISTED_ENCHANTMENTS.set(new ArrayList<>(blacklistedEnchantments));

        List<String> rawWeights = new ArrayList<>();
        enchantmentWeights.forEach((key, weight) -> rawWeights.add(key + "=" + weight));
        ENCHANTMENT_WEIGHTS_RAW.set(rawWeights);

        SPEC.save();
    }

    public static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            useWhitelist = USE_WHITELIST.get();
            combinationCost = COMBINATION_COST.get();
            defaultEnchantmentWeight = DEFAULT_ENCHANTMENT_WEIGHT.get();

            whitelistedEnchantments = new ArrayList<>(WHITELISTED_ENCHANTMENTS.get());
            blacklistedEnchantments = new ArrayList<>(BLACKLISTED_ENCHANTMENTS.get());

            enchantmentWeights = new HashMap<>();
            for (String entry : ENCHANTMENT_WEIGHTS_RAW.get()) {
                String[] parts = entry.split("=");
                if (parts.length == 2) {
                    try {
                        enchantmentWeights.put(parts[0].trim(), Integer.parseInt(parts[1].trim()));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }
}