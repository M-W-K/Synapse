package com.m_w_k.synapse;

import com.m_w_k.synapse.client.gui.BasicConnectorScreen;
import com.m_w_k.synapse.client.gui.EndpointScreen;
import com.m_w_k.synapse.client.gui.RelayScreen;
import com.m_w_k.synapse.client.renderer.AxonRenderer;
import com.m_w_k.synapse.config.SynapseConfigs;
import com.m_w_k.synapse.data.*;
import com.m_w_k.synapse.network.SynapsePacketHandler;
import com.m_w_k.synapse.registry.*;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SynapseMod.MODID)
public final class SynapseMod {
    public static final String MODID = "synapse";
    private static final Logger LOGGER = LogUtils.getLogger();

    public SynapseMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        registries(modEventBus);
        SynapseConfigs.register(ModLoadingContext.get());
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::clientSetup);
        SynapsePacketHandler.init();
    }

    private void registries(IEventBus bus) {
        SynapseBlockRegistry.init(bus);
        SynapseItemRegistry.init(bus);
        SynapseBlockEntityRegistry.init(bus);
        SynapseCreativeTabsRegistry.init(bus);
        SynapseMenuRegistry.init(bus);
        SynapseRecipeSerializerRegistry.init(bus);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(SynapseBlockEntityRegistry.DISTRIBUTOR_BLOCK.get(), AxonRenderer::new);
        event.registerBlockEntityRenderer(SynapseBlockEntityRegistry.DAS_BLOCK.get(), AxonRenderer::new);
        event.registerBlockEntityRenderer(SynapseBlockEntityRegistry.ENDPOINT_BLOCK.get(), AxonRenderer::new);
        event.registerBlockEntityRenderer(SynapseBlockEntityRegistry.RELAY_BLOCK.get(), AxonRenderer::new);
    }

    private void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();
        ExistingFileHelper helper = event.getExistingFileHelper();
        PackOutput out = gen.getPackOutput();
        gen.addProvider(event.includeServer(), new SynapseLootTableGen());
        gen.addProvider(event.includeServer(), new SynapseBlockTagsProvider(out, event.getLookupProvider(), helper));
        gen.addProvider(event.includeServer(), clarify(SynapseRecipeProvider::new));
        gen.addProvider(event.includeServer(), new SynapseAdvancementGen(event.getLookupProvider(), helper));
        gen.addProvider(event.includeClient(), new SynapseBlockStateProvider(out, helper));
        gen.addProvider(event.includeClient(), new SynapseItemModelProvider(out, helper));
    }

    private <T extends DataProvider> DataProvider.Factory<T> clarify(DataProvider.Factory<T> f) {
        return f;
    }

    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(
                () -> {
                    MenuScreens.register(SynapseMenuRegistry.BASIC_CONNECTOR.get(), BasicConnectorScreen::new);
                    MenuScreens.register(SynapseMenuRegistry.ENDPOINT.get(), EndpointScreen::new);
                    MenuScreens.register(SynapseMenuRegistry.RELAY.get(), RelayScreen::new);
                }
        );
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
