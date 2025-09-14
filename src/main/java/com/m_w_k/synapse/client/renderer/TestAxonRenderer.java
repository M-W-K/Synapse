package com.m_w_k.synapse.client.renderer;

import com.m_w_k.synapse.SynapseMod;
import com.m_w_k.synapse.block.entity.TestBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class TestAxonRenderer implements BlockEntityRenderer<TestBlockEntity> {

    private static final ResourceLocation testTex = new ResourceLocation(SynapseMod.MODID, "block/test_texture");

    public TestAxonRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(@NotNull TestBlockEntity be, float partialTicks, @NotNull PoseStack pose, @NotNull MultiBufferSource bufferSource, int light, int overlay) {
        if (Minecraft.getInstance().getCameraEntity() == null || be.getLevel() == null) return;
        pose.pushPose();
        BlockPos pos = be.getBlockPos();
        Vec3 testSource = be.getBlockPos().getCenter();
        pose.translate(-pos.getX(), -pos.getY(), -pos.getZ());
        Vec3 testTarget = be.getBlockPos().north(5).west().above(5).getCenter();
        int points = 1 + (int) (curveLength(testSource, testTarget) * 2);
        Vec3[] ropePoints = new Vec3[points];
        for (int i = 0; i < points; i++) {
            double lerp = (double) i / points;
            double ylerp = testTarget.y > testSource.y ? lerp : (lerp - 1);
            ropePoints[i] = new Vec3(testSource.x + lerp * (testTarget.x - testSource.x),
                    testSource.y + ylerp * ylerp * (testTarget.y - testSource.y),
                    testSource.z + lerp * (testTarget.z - testSource.z));
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(testTex);
        float minU = sprite.getU(0);
        float maxU = sprite.getU(16);
        float minV = sprite.getV(0);
        float maxV = sprite.getV(16);
//        Tesselator tesselator = RenderSystem.renderThreadTesselator();
//        BufferBuilder bufferbuilder = tesselator.getBuilder();
//        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.solid());
        Vec3 camera = Minecraft.getInstance().getCameraEntity().getEyePosition(partialTicks);
        for (int i = 0; i < points - 1; i++) {
            Vec3 start = ropePoints[i];
            Vec3 end = ropePoints[i + 1];

            Vec3 midpoint = start.lerp(end, 0.5);
            int sectionLight = LevelRenderer.getLightColor(be.getLevel(), BlockPos.containing(midpoint.x, midpoint.y, midpoint.z));
            Vec3 cam = camera.subtract(midpoint).normalize();

            Vec3 rope = end.subtract(start);
            Vec3 ropePrev = i > 0 ? start.subtract(ropePoints[i - 1]).add(rope) : rope;
            Vec3 ropeNext = i < points - 2 ? ropePoints[i + 2].subtract(end).add(rope) : rope;

            Vec3 widthPrev = ropePrev.cross(cam).normalize().scale(0.125);
            Vec3 widthNext = ropeNext.cross(cam).normalize().scale(0.125);
            vertex(buffer, pose, start.subtract(widthPrev), minU, maxV, cam, sectionLight, overlay);
            vertex(buffer, pose, start.add(widthPrev), maxU, maxV, cam, sectionLight, overlay);
            vertex(buffer, pose, end.add(widthNext), maxU, minV, cam, sectionLight, overlay);
            vertex(buffer, pose, end.subtract(widthNext), minU, minV, cam, sectionLight, overlay);
        }
        pose.popPose();
    }

    private double curveLength(Vec3 source, Vec3 target) {
        double dx = Math.abs(target.x - source.x);
        double dy = Math.abs(target.y - source.y);
        double dz = Math.abs(target.z - source.z);

        // position x/z = source + t * (target - source)
        // position y = source + t^2 * (target - source) (or (t-1)^2)
        // velocity x/z = target - source
        // velocity y = 2t * (target - source) (or 2(t-1))
        // magnitude squared = (dx)^2 + 4(t^2)(dy)^2 + (dz)^2
        // magnitude = sqrt(dx^2 + 4t^2*(dy)^2 + dz^2)

        // integral from 0 to 1
        double dxz = dx + dz;
        double sqrtDy = Math.sqrt(dy);
        double sqrt4 = Math.sqrt(dx + 4 * dy + dz);
        return dxz * (Math.log((sqrt4 + 2 * sqrtDy) / Math.sqrt(dxz))) / (4 * sqrtDy) + sqrt4 / 2;
    }

    private static void vertex(VertexConsumer b, PoseStack mat, Vec3 pos, float u, float v, Vec3 normal, int light, int overlay) {
        b.vertex(mat.last().pose(), (float) pos.x, (float) pos.y, (float) pos.z)
                .color(1F, 1F, 1F, 1F)
                .uv(u, v)
                .overlayCoords(overlay)
                .uv2(light)
                .normal(mat.last().normal(), (float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }
}
