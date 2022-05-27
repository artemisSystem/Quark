package vazkii.quark.content.building.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.model.generators.ModelFile;
import vazkii.quark.base.block.QuarkBlock;
import vazkii.quark.base.datagen.QuarkBlockStateProvider;
import vazkii.quark.base.handler.RenderLayerHandler;
import vazkii.quark.base.handler.RenderLayerHandler.RenderTypeSkeleton;
import vazkii.quark.base.module.QuarkModule;

import javax.annotation.Nonnull;

public class WoodPostBlock extends QuarkBlock implements SimpleWaterloggedBlock {

	private static final VoxelShape SHAPE_X = Block.box(0F, 6F, 6F, 16F, 10F, 10F);
	private static final VoxelShape SHAPE_Y = Block.box(6F, 0F, 6F, 10F, 16F, 10F);
	private static final VoxelShape SHAPE_Z = Block.box(6F, 6F, 0F, 10F, 10F, 16F);

	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final EnumProperty<Axis> AXIS = BlockStateProperties.AXIS;

	public static final BooleanProperty[] CHAINED = new BooleanProperty[] {
			BooleanProperty.create("chain_down"),
			BooleanProperty.create("chain_up"),
			BooleanProperty.create("chain_north"),
			BooleanProperty.create("chain_south"),
			BooleanProperty.create("chain_west"),
			BooleanProperty.create("chain_east")
	};

	private final Block parent;
	private final boolean nether;

	public WoodPostBlock(QuarkModule module, Block parent, String prefix, boolean nether) {
		super(prefix + parent.getRegistryName().getPath().replace("_fence", "_post"), module, CreativeModeTab.TAB_BUILDING_BLOCKS,
				Properties.copy(parent).sound(nether ? SoundType.STEM : SoundType.WOOD));
		this.parent = parent;
		this.nether = nether;

		BlockState state = stateDefinition.any().setValue(WATERLOGGED, false).setValue(AXIS, Axis.Y);
		for(BooleanProperty prop : CHAINED)
			state = state.setValue(prop, false);
		registerDefaultState(state);

		RenderLayerHandler.setRenderType(this, RenderTypeSkeleton.CUTOUT);
	}

	@Nonnull
	@Override
	public VoxelShape getShape(BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
		return switch (state.getValue(AXIS)) {
			case X -> SHAPE_X;
			case Y -> SHAPE_Y;
			default -> SHAPE_Z;
		};
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, @Nonnull BlockGetter reader, @Nonnull BlockPos pos) {
		return !state.getValue(WATERLOGGED);
	}

	@Nonnull
	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return getState(context.getLevel(), context.getClickedPos(), context.getClickedFace().getAxis());
	}

	@Nonnull
	@Override
	public BlockState updateShape(BlockState state, @Nonnull Direction facing, @Nonnull BlockState facingState, @Nonnull LevelAccessor level, @Nonnull BlockPos pos, @Nonnull BlockPos facingPos) {
		if (state.getValue(WATERLOGGED)) {
			level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}

		return super.updateShape(state, facing, facingState, level, pos, facingPos);
	}

	@Override
	public void neighborChanged(@Nonnull BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos, boolean isMoving) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);

		BlockState newState = getState(worldIn, pos, state.getValue(AXIS));
		if(!newState.equals(state))
			worldIn.setBlockAndUpdate(pos, newState);
	}

	private BlockState getState(Level world, BlockPos pos, Axis axis) {
		BlockState state = defaultBlockState().setValue(WATERLOGGED, world.getFluidState(pos).getType() == Fluids.WATER).setValue(AXIS, axis);

		for(Direction d : Direction.values()) {
			if(d.getAxis() == axis)
				continue;

			BlockState sideState = world.getBlockState(pos.relative(d));
			if((sideState.getBlock() instanceof ChainBlock && sideState.getValue(BlockStateProperties.AXIS) == d.getAxis())
					|| (d == Direction.DOWN && sideState.getBlock() instanceof LanternBlock && sideState.getValue(LanternBlock.HANGING))) {
				BooleanProperty prop = CHAINED[d.ordinal()];
				state = state.setValue(prop, true);
			}
		}

		return state;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED, AXIS);
		for(BooleanProperty prop : CHAINED)
			builder.add(prop);
	}

	@Override
	public void dataGen(QuarkBlockStateProvider states) {
		ModelFile basePost = states.models().singleTexture(getRegistryName().getPath(), states.modLoc("block/post"),
				new ResourceLocation(parent.getRegistryName().getNamespace(), "block/" + getRegistryName().getPath().replace("_post", nether ? "_stem" : "_log")));

		ModelFile chain = states.models().getExistingFile(states.modLoc("block/chain_small"));
		ModelFile chainTop = states.models().getExistingFile(states.modLoc("block/chain_small_top"));
		states.getMultipartBuilder(this)
				.part().modelFile(basePost).addModel().condition(AXIS, Axis.Y).end()
				.part().modelFile(basePost).rotationX(90).rotationY(90).addModel().condition(AXIS, Axis.X).end()
				.part().modelFile(basePost).rotationX(90).addModel().condition(AXIS, Axis.Z).end()
				.part().modelFile(chain).addModel().condition(CHAINED[0], true).end()
				.part().modelFile(chainTop).addModel().condition(CHAINED[1], true).end()
				.part().modelFile(chainTop).rotationX(90).addModel().condition(CHAINED[2], true).end()
				.part().modelFile(chain).rotationX(90).addModel().condition(CHAINED[3], true).end()
				.part().modelFile(chainTop).rotationX(90).rotationY(90).addModel().condition(CHAINED[4], true).end()
				.part().modelFile(chain).rotationX(90).rotationY(90).addModel().condition(CHAINED[5], true).end();
		states.simpleBlockItem(this, basePost);
	}
}
