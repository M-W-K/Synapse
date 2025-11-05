package com.m_w_k.synapse.common.block;

import com.m_w_k.synapse.SynapseUtil;
import com.m_w_k.synapse.api.KnifeAction;
import com.m_w_k.synapse.api.block.AxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.OldAxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.common.block.entity.EndpointBlockEntity;
import com.m_w_k.synapse.common.item.AxonItem;
import com.m_w_k.synapse.common.item.KnifeItem;
import com.m_w_k.synapse.common.menu.EndpointMenu;
import com.m_w_k.synapse.registry.SynapseBlockEntityRegistry;
import com.m_w_k.synapse.registry.SynapseBlockRegistry;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EndpointBlock extends AxonBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final VoxelShape NORTH_SHAPE = Shapes.create(2/16d, 2/16d, 0, 14/16d, 14/16d, 2/16d);
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final VoxelShape EAST_SHAPE = Shapes.create(14/16d, 2/16d, 2/16d, 1, 14/16d, 14/16d);
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final VoxelShape SOUTH_SHAPE = Shapes.create(2/16d, 2/16d, 14/16d, 14/16d, 14/16d, 1);
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final VoxelShape WEST_SHAPE = Shapes.create(0, 2/16d, 2/16d, 2/16d, 14/16d, 14/16d);
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final VoxelShape UP_SHAPE = Shapes.create(2/16d, 14/16d, 2/16d, 14/16d, 1, 14/16d);
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final VoxelShape DOWN_SHAPE = Shapes.create(2/16d, 0, 2/16d, 14/16d, 2/16d, 14/16d);
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION =
            new EnumMap<>(PipeBlock.PROPERTY_BY_DIRECTION);
    public static final Map<BitSet, VoxelShape> SHAPE_CACHE = new Object2ObjectOpenHashMap<>();

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty ENDPOINTS = IntegerProperty.create("endpoints", 1, 6);

    public EndpointBlock(Properties p_49795_) {
        super(p_49795_);
        BlockState def = this.stateDefinition.any().setValue(ENDPOINTS, 1)
                .setValue(WATERLOGGED, Boolean.FALSE);
        for (BooleanProperty prop : PROPERTY_BY_DIRECTION.values()) {
            def = def.setValue(prop, Boolean.FALSE);
        }
        this.registerDefaultState(def);
    }

    @Override
    protected boolean noMenuItem(ItemStack stack) {
        return stack.getItem() instanceof BlockItem b && b.getBlock() == this;
    }

    @Override
    protected void openInteractMenu(@NotNull ServerPlayer player, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull IAxonBlockEntity be, @NotNull BlockHitResult hit) {
        MenuProvider prov = new SimpleMenuProvider(
                (containerId, playerInventory, p) -> EndpointMenu.of(containerId, playerInventory, be),
                Component.translatable("synapse.menu.title.endpoint"));
        Vec3 rel = hit.getLocation().subtract(hit.getBlockPos().getCenter());
        NetworkHooks.openScreen(player, prov, EndpointMenu.writer(be, Direction.getNearest(rel.x, rel.y, rel.z).name()));
    }

    @Override
    protected InteractionResult handleKnife(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit, @NotNull IAxonBlockEntity usAxon, @NotNull KnifeItem knife, @NotNull ItemStack knifeStack) {
        InteractionResult res = super.handleKnife(state, level, pos, player, hand, hit, usAxon, knife, knifeStack);
        if (res.consumesAction() && knife.getAction() == KnifeAction.REMOVE) {
            if (state.getValue(ENDPOINTS) == 1) {
                dropResources(state, level, pos);
                level.removeBlock(pos, false);
            } else {
                Vec3 rel = hit.getLocation().subtract(pos.getCenter());
                Direction dir = Direction.getNearest(rel.x, rel.y, rel.z);
                if (state.getValue(PROPERTY_BY_DIRECTION.get(dir))) {
                    level.setBlock(pos, state.setValue(PROPERTY_BY_DIRECTION.get(dir), false).setValue(ENDPOINTS, state.getValue(ENDPOINTS) - 1), Block.UPDATE_ALL);
                    Block.popResource(level, pos, new ItemStack(SynapseBlockRegistry.ENDPOINT_BASIC.get()));
                }
            }
        }
        return res;
    }

    @Override
    protected Set<ResourceLocation> knifeAffectedSlots(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit, @NotNull IAxonBlockEntity usAxon, @NotNull KnifeItem knife, @NotNull ItemStack knifeStack) {
        Set<ResourceLocation> set = new ObjectOpenHashSet<>();
        Vec3 rel = hit.getLocation().subtract(pos.getCenter());
        Direction dir = Direction.getNearest(rel.x, rel.y, rel.z);
        for (AxonType type : AxonType.values()) {
            ResourceLocation slot = AxonDeviceDefinitions.endpoint(type, dir, false);
            if (slot != null) set.add(slot);
        }
        return set;
    }

    @Override
    protected @Nullable ResourceLocation determineHitSlot(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof AxonItem iAxon) {
            Vec3 rel = hit.getLocation().subtract(hit.getBlockPos().getCenter());
            return AxonDeviceDefinitions.endpoint(iAxon.getType(), Direction.getNearest(rel.x, rel.y, rel.z), false);
        }
        return null;
    }

    @Override
    public void onNeighborChange(BlockState state, LevelReader level, BlockPos pos, BlockPos neighbor) {
        super.onNeighborChange(state, level, pos, neighbor);
        level.getBlockEntity(pos, SynapseBlockEntityRegistry.ENDPOINT_BLOCK.get()).ifPresent(be -> be.neighborChanged(SynapseUtil.facingTo(pos, neighbor)));
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState blockstate = ctx.getLevel().getBlockState(ctx.getClickedPos());
        Direction face = ctx.getClickedFace();
        if (ctx.isInside()) face = face.getOpposite();
        if (!blockstate.is(this)) {
            face = face.getOpposite();
            FluidState fluidstate = ctx.getLevel().getFluidState(ctx.getClickedPos());
            boolean flag = fluidstate.getType() == Fluids.WATER;
            blockstate = defaultBlockState().setValue(WATERLOGGED, flag);
        } else {
            blockstate = blockstate.setValue(ENDPOINTS, Math.min(6, blockstate.getValue(ENDPOINTS) + 1));
        }
        return blockstate.setValue(PROPERTY_BY_DIRECTION.get(face), true);
    }

    @Override
    public boolean canBeReplaced(@NotNull BlockState state, BlockPlaceContext ctx) {
        return !ctx.isSecondaryUseActive() && ctx.getItemInHand().is(this.asItem()) &&
                !state.getValue(PROPERTY_BY_DIRECTION.get(ctx.getClickedFace())) || super.canBeReplaced(state, ctx);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> def) {
        def.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, BlockStateProperties.WATERLOGGED, ENDPOINTS);
    }

    @Override
    public @NotNull EndpointBlockEntity newBlockEntity(@NotNull BlockPos p_153215_, @NotNull BlockState p_153216_) {
        return new EndpointBlockEntity(p_153215_, p_153216_);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_51039_, @NotNull BlockGetter p_51040_, @NotNull BlockPos p_51041_) {
        return p_51039_.getFluidState().isEmpty();
    }

    @Override
    public boolean isPathfindable(@NotNull BlockState p_60475_, @NotNull BlockGetter p_60476_, @NotNull BlockPos p_60477_, @NotNull PathComputationType p_60478_) {
        return false;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_60573_, BlockPos p_60574_, CollisionContext p_60575_) {
        BitSet set = new BitSet();
        if (state.getValue(NORTH)) {
            set.set(Direction.NORTH.ordinal(), true);
        }
        if (state.getValue(SOUTH)) {
            set.set(Direction.SOUTH.ordinal(), true);
        }
        if (state.getValue(EAST)) {
            set.set(Direction.EAST.ordinal(), true);
        }
        if (state.getValue(WEST)) {
            set.set(Direction.WEST.ordinal(), true);
        }
        if (state.getValue(DOWN)) {
            set.set(Direction.DOWN.ordinal(), true);
        }
        if (state.getValue(UP)) {
            set.set(Direction.UP.ordinal(), true);
        }
        return SHAPE_CACHE.computeIfAbsent(set, (key) -> {
            VoxelShape shape = Shapes.empty();
            if (key.get(Direction.NORTH.ordinal())) {
                shape = Shapes.or(shape, NORTH_SHAPE);
            }
            if (key.get(Direction.SOUTH.ordinal())) {
                shape = Shapes.or(shape, SOUTH_SHAPE);
            }
            if (key.get(Direction.EAST.ordinal())) {
                shape = Shapes.or(shape, EAST_SHAPE);
            }
            if (key.get(Direction.WEST.ordinal())) {
                shape = Shapes.or(shape, WEST_SHAPE);
            }
            if (key.get(Direction.UP.ordinal())) {
                shape = Shapes.or(shape, UP_SHAPE);
            }
            if (key.get(Direction.DOWN.ordinal())) {
                shape = Shapes.or(shape, DOWN_SHAPE);
            }
            return shape;
        });
    }

    @Override
    public void appendHoverText(ItemStack p_49816_, @org.jetbrains.annotations.Nullable BlockGetter p_49817_, List<Component> p_49818_, TooltipFlag p_49819_) {
        p_49818_.add(Component.translatable("block.synapse.endpoint_desc").withStyle(ChatFormatting.GRAY));
    }
}
