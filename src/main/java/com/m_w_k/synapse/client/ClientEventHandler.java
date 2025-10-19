package com.m_w_k.synapse.client;


import com.m_w_k.synapse.SynapseMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = SynapseMod.MODID)
public final class ClientEventHandler {

}
