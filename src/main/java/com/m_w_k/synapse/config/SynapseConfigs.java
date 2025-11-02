package com.m_w_k.synapse.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class SynapseConfigs {

    private static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);

    private static SynapseConfigClient client;
    private static SynapseConfigCommon common;
    private static SynapseConfigServer server;

    public static SynapseConfigClient client() {
        return client;
    }

    public static SynapseConfigCommon common() {
        return common;
    }

    public static SynapseConfigServer server() {
        return server;
    }

    private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
        Pair<T, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(builder -> {
            T config = factory.get();
            config.registerAll(builder);
            return config;
        });

        T config = specPair.getLeft();
        config.specification = specPair.getRight();
        CONFIGS.put(side, config);
        return config;
    }

    public static void register(ModLoadingContext context) {
        client = register(SynapseConfigClient::new, ModConfig.Type.CLIENT);
        common = register(SynapseConfigCommon::new, ModConfig.Type.COMMON);
        server = register(SynapseConfigServer::new, ModConfig.Type.SERVER);

        for (var pair : CONFIGS.entrySet()) {
            context.registerConfig(pair.getKey(), pair.getValue().specification);
        }
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        for (ConfigBase config : CONFIGS.values()) {
            if (config.specification == event.getConfig().getSpec()) {
                config.onLoad();
            }
        }
    }

    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading event) {
        for (ConfigBase config : CONFIGS.values()) {
            if (config.specification == event.getConfig().getSpec()) {
                config.onReload();
            }
        }
    }
}
