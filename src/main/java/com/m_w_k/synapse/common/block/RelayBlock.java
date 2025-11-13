package com.m_w_k.synapse.common.block;

import com.m_w_k.synapse.SynapseUtil;
import com.m_w_k.synapse.api.KnifeAction;
import com.m_w_k.synapse.api.block.AxonDeviceDefinitions;
import com.m_w_k.synapse.api.block.IAxonBlockEntity;
import com.m_w_k.synapse.api.connect.AxonType;
import com.m_w_k.synapse.api.item.AxonTypeItem;
import com.m_w_k.synapse.common.block.entity.RelayBlockEntity;
import com.m_w_k.synapse.common.item.AxonBlockItem;
import com.m_w_k.synapse.common.item.AxonItem;
import com.m_w_k.synapse.common.item.KnifeItem;
import com.m_w_k.synapse.common.menu.RelayMenu;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
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
import org.jetbrains.annotations.Range;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RelayBlock extends AxonBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty MOUNT_DIRECTION = BlockStateProperties.FACING;
    public static final IntegerProperty RELAYS = IntegerProperty.create("relays", 1, 4);
    public static final BooleanProperty ONE = BooleanProperty.create("one");
    public static final BooleanProperty TWO = BooleanProperty.create("two");
    public static final BooleanProperty THREE = BooleanProperty.create("three");
    public static final BooleanProperty FOUR = BooleanProperty.create("four");
    public static final BooleanProperty[] PROPERTY_BY_INT = new BooleanProperty[] { ONE, TWO, THREE, FOUR };

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
    public static final Map<Pair<BitSet, Direction>, VoxelShape> SHAPE_CACHE = new Object2ObjectOpenHashMap<>();

    public RelayBlock(Properties p_49795_) {
        super(p_49795_);
        BlockState def = this.stateDefinition.any().setValue(RELAYS, 1)
                .setValue(WATERLOGGED, Boolean.FALSE).setValue(MOUNT_DIRECTION, Direction.DOWN);
        for (BooleanProperty prop : PROPERTY_BY_INT) {
            def = def.setValue(prop, Boolean.FALSE);
        }
        this.registerDefaultState(def);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> def) {
        def.add(BlockStateProperties.WATERLOGGED, RELAYS, MOUNT_DIRECTION, ONE, TWO, THREE, FOUR);
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
    protected Set<ResourceLocation> knifeAffectedSlots(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit, @NotNull IAxonBlockEntity usAxon, @NotNull KnifeItem knife, @NotNull ItemStack knifeStack) {
        if (knife.getAction() == KnifeAction.REMOVE && state.getValue(RELAYS) == 1) {
            return null;
        }
        Set<ResourceLocation> set = new ObjectOpenHashSet<>();
        int nearest = getKeyComponent(hit, pos, state);
        for (AxonType type : AxonType.values()) {
            ResourceLocation slot = AxonDeviceDefinitions.relay(type, nearest, false);
            if (slot != null) set.add(slot);
        }
        return set;
    }

    @Override
    protected void beforeKnifeActions(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit, @NotNull IAxonBlockEntity usAxon, @NotNull KnifeItem knife, @NotNull ItemStack knifeStack, @org.jetbrains.annotations.Nullable Set<ResourceLocation> affectedSlots) {
        if (affectedSlots == null || knife.getAction() != KnifeAction.REMOVE) return;
        int nearest = getKeyComponent(hit, pos, state);
        if (state.getValue(PROPERTY_BY_INT[nearest])) {
            level.setBlock(pos, state.setValue(PROPERTY_BY_INT[nearest], false).setValue(RELAYS, state.getValue(RELAYS) - 1), Block.UPDATE_ALL);
            BlockState dropState = state.setValue(RELAYS, 1);
            for (int i = 0; i < 4; i++) {
                dropState = dropState.setValue(PROPERTY_BY_INT[i], i == nearest);
            }
            dropResources(dropState, level, pos, (BlockEntity) usAxon);
        }
    }

    @Override
    protected ResourceLocation determineHitSlot(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof AxonTypeItem iAxon) {
            int i = getKeyComponent(hit, pos, state);
            if (state.getValue(PROPERTY_BY_INT[i])) {
                return AxonDeviceDefinitions.relay(iAxon.getType(), i, false);
            }
        }
        return null;
    }

    @Override
    public boolean allowModuleInstallItem(Set<AxonType> modules, AxonBlockItem item, ItemStack stack) {
        return item.installedModules(stack).isEmpty() && modules.size() == 1;
    }

    @Override
    public boolean allowModuleInstallBlock(AxonType module, IAxonBlockEntity usAxon, ResourceLocation slot) {
        if (!super.allowModuleInstallBlock(module, usAxon, slot)) return false;
        IntObjectPair<AxonType> def = AxonDeviceDefinitions.relay(slot, false);
        if (def == null) return false;
        for (AxonType type : AxonType.values()) {
            if (usAxon.hasSlot(AxonDeviceDefinitions.relay(type, def.keyInt(), false))) {
                return false;
            }
        }
        return true;
    }

    public static @Range(from = 0, to = 3) int getKeyComponent(@NotNull BlockHitResult hit, @NotNull BlockPos pos, @NotNull BlockState state) {
        Vec3 rel = hit.getLocation().subtract(pos.getCenter());
        return SynapseUtil.getNearest(rel, RelayBlockEntity.centers(state.getValue(MOUNT_DIRECTION)));
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState blockstate = ctx.getLevel().getBlockState(ctx.getClickedPos());

        if (!blockstate.is(this)) {
            FluidState fluidstate = ctx.getLevel().getFluidState(ctx.getClickedPos());
            boolean flag = fluidstate.getType() == Fluids.WATER;
            blockstate = defaultBlockState().setValue(MOUNT_DIRECTION, ctx.getClickedFace()).setValue(WATERLOGGED, flag);
        } else {
            blockstate = blockstate.setValue(RELAYS, Math.min(4, blockstate.getValue(RELAYS) + 1));
        }
        return blockstate.setValue(PROPERTY_BY_INT[getKeyComponent(ctx.getHitResult(), ctx.getClickedPos(), blockstate)], true);
    }

    @Override
    public boolean canBeReplaced(@NotNull BlockState state, BlockPlaceContext ctx) {
        return !ctx.isSecondaryUseActive() && ctx.getItemInHand().is(this.asItem()) &&
                !state.getValue(PROPERTY_BY_INT[getKeyComponent(ctx.getHitResult(), ctx.getClickedPos(), state)])
                || super.canBeReplaced(state, ctx);
    }

    @Override
    public @NotNull RelayBlockEntity newBlockEntity(@NotNull BlockPos p_153215_, @NotNull BlockState p_153216_) {
        return new RelayBlockEntity(p_153215_, p_153216_);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos pos, CollisionContext p_60558_) {
        BitSet set = new BitSet();
        if (state.getValue(ONE)) {
            set.set(0, true);
        }
        if (state.getValue(TWO)) {
            set.set(1, true);
        }
        if (state.getValue(THREE)) {
            set.set(2, true);
        }
        if (state.getValue(FOUR)) {
            set.set(3, true);
        }
        Pair<BitSet, Direction> k = Pair.of(set, state.getValue(MOUNT_DIRECTION));
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
            for (int i = 0; i < 4; i++) {
                if (key.key().get(i)) shape = Shapes.or(shape, shapes[i]);
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

    @Override
    public void appendHoverText(ItemStack p_49816_, @org.jetbrains.annotations.Nullable BlockGetter p_49817_, List<Component> p_49818_, TooltipFlag p_49819_) {
        p_49818_.add(Component.translatable("block.synapse.relay_desc_1").withStyle(ChatFormatting.GRAY));
        p_49818_.add(Component.translatable("block.synapse.relay_desc_2").withStyle(ChatFormatting.GRAY));
        p_49818_.add(Component.translatable("block.synapse.relay_desc_3").withStyle(ChatFormatting.GRAY));
    }
}
