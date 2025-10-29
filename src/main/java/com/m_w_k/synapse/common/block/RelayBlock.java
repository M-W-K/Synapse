package com.m_w_k.synapse.common.block;

import com.m_w_k.synapse.SynapseUtil;
import com.m_w_k.synapse.api.block.AxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.common.block.entity.RelayBlockEntity;
import com.m_w_k.synapse.common.item.AxonItem;
import com.m_w_k.synapse.common.menu.RelayMenu;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
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

import javax.annotation.Nullable;
import java.util.Map;

public class RelayBlock extends AxonBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty MOUNT_DIRECTION = BlockStateProperties.FACING;
    public static final IntegerProperty RELAYS = IntegerProperty.create("relays", 1, 4);
    public static final VoxelShape[] SOUTH_SHAPE = new VoxelShape[] {
            Shapes.create(1/16d, 9/16d, 0, 7/16d, 15/16d, 3/16d),
            Shapes.create(9/16d, 9/16d, 0, 15/16d, 15/16d, 3/16d),
            Shapes.create(9/16d, 1/16d, 0, 15/16d, 7/16d, 3/16d),
            Shapes.create(1/16d, 1/16d, 0, 7/16d, 7/16d, 3/16d)
    };
    public static final VoxelShape[] WEST_SHAPE = new VoxelShape[] {
            Shapes.create(13/16d, 9/16d, 1/16d, 1, 15/16d, 7/16d),
            Shapes.create(13/16d, 9/16d, 9/16d, 1, 15/16d, 15/16d),
            Shapes.create(13/16d, 1/16d, 9/16d, 1, 7/16d, 15/16d),
            Shapes.create(13/16d, 1/16d, 1/16d, 1, 7/16d, 7/16d)
    };
    public static final VoxelShape[] NORTH_SHAPE = new VoxelShape[] {
            Shapes.create(9/16d, 9/16d, 13/16d, 15/16d, 15/16d, 1),
            Shapes.create(1/16d, 9/16d, 13/16d, 7/16d, 15/16d, 1),
            Shapes.create(1/16d, 1/16d, 13/16d, 7/16d, 7/16d, 1),
            Shapes.create(9/16d, 1/16d, 13/16d, 15/16d, 7/16d, 1)
    };
    public static final VoxelShape[] EAST_SHAPE = new VoxelShape[] {
            Shapes.create(0, 9/16d, 9/16d, 3/16d, 15/16d, 15/16d),
            Shapes.create(0, 9/16d, 1/16d, 3/16d, 15/16d, 7/16d),
            Shapes.create(0, 1/16d, 1/16d, 3/16d, 7/16d, 7/16d),
            Shapes.create(0, 1/16d, 9/16d, 3/16d, 7/16d, 15/16d)
    };
    public static final VoxelShape[] DOWN_SHAPE = new VoxelShape[] {
            Shapes.create(9/16d, 13/16d, 9/16d, 15/16d, 1, 15/16d),
            Shapes.create(9/16d, 13/16d, 1/16d, 15/16d, 1, 7/16d),
            Shapes.create(1/16d, 13/16d, 1/16d, 7/16d, 1, 7/16d),
            Shapes.create(1/16d, 13/16d, 9/16d, 7/16d, 1, 15/16d)
    };
    public static final VoxelShape[] UP_SHAPE = new VoxelShape[] {
            Shapes.create(1/16d, 0, 9/16d, 7/16d, 3/16d, 15/16d),
            Shapes.create(1/16d, 0, 1/16d, 7/16d, 3/16d, 7/16d),
            Shapes.create(9/16d, 0, 1/16d, 15/16d, 3/16d, 7/16d),
            Shapes.create(9/16d, 0, 9/16d, 15/16d, 3/16d, 15/16d)
    };
    public static final Map<IntObjectPair<Direction>, VoxelShape> SHAPE_CACHE = new Object2ObjectOpenHashMap<>();



    public RelayBlock(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.stateDefinition.any().setValue(RELAYS, 1)
                .setValue(WATERLOGGED, Boolean.FALSE).setValue(MOUNT_DIRECTION, Direction.DOWN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> def) {
        def.add(BlockStateProperties.WATERLOGGED, RELAYS, MOUNT_DIRECTION);
    }

    @Override
    protected void openInteractMenu(@NotNull ServerPlayer player, @NotNull Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull IAxonBlockEntity be, @NotNull BlockHitResult hit) {
        MenuProvider prov = new SimpleMenuProvider(
                (containerId, playerInventory, p) -> RelayMenu.of(containerId, playerInventory, be),
                Component.translatable("synapse.menu.title.relay"));
        Vec3 relative = hit.getLocation().subtract(pos.getCenter());
        NetworkHooks.openScreen(player, prov, RelayMenu.writer(be, String.valueOf(
                SynapseUtil.getNearest(relative, RelayBlockEntity.centers(state.getValue(MOUNT_DIRECTION))))));
    }

    @Override
    protected int determineHitSlot(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof AxonItem iAxon) {
            Vec3 relative = hit.getLocation().subtract(pos.getCenter());
            return AxonDeviceDefinitions.relay(iAxon.getType(),
                    SynapseUtil.getNearest(relative, RelayBlockEntity.centers(state.getValue(MOUNT_DIRECTION))));
        }
        return 0;
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState blockstate = ctx.getLevel().getBlockState(ctx.getClickedPos());

        if (!blockstate.is(this)) {
            FluidState fluidstate = ctx.getLevel().getFluidState(ctx.getClickedPos());
            boolean flag = fluidstate.getType() == Fluids.WATER;
            return defaultBlockState().setValue(MOUNT_DIRECTION, ctx.getClickedFace()).setValue(WATERLOGGED, flag);
        } else {
            return blockstate.setValue(RELAYS, Math.min(4, blockstate.getValue(RELAYS) + 1));
        }
    }

    @Override
    public boolean canBeReplaced(@NotNull BlockState state, BlockPlaceContext ctx) {
        return !ctx.isSecondaryUseActive() && ctx.getItemInHand().is(this.asItem()) &&
                state.getValue(RELAYS) < 4 || super.canBeReplaced(state, ctx);
    }

    @Override
    public @NotNull RelayBlockEntity newBlockEntity(@NotNull BlockPos p_153215_, @NotNull BlockState p_153216_) {
        return new RelayBlockEntity(p_153215_, p_153216_);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos pos, CollisionContext p_60558_) {
        IntObjectPair<Direction> k = IntObjectPair.of(state.getValue(RELAYS), state.getValue(MOUNT_DIRECTION));
        return SHAPE_CACHE.computeIfAbsent(k, (key) -> {
            VoxelShape shape = Shapes.empty();
            VoxelShape[] shapes = switch (key.second()) {
                case DOWN -> DOWN_SHAPE;
                case UP -> UP_SHAPE;
                case NORTH -> NORTH_SHAPE;
                case SOUTH -> SOUTH_SHAPE;
                case WEST -> WEST_SHAPE;
                case EAST -> EAST_SHAPE;
            };
            for (int i = 0; i < key.firstInt(); i++) {
                shape = Shapes.or(shape, shapes[i]);
            }
            return shape;
        });
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
    protected boolean noMenuItem(ItemStack stack) {
        return stack.getItem() instanceof BlockItem b && b.getBlock() == this;
    }
}
