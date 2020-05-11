package com.mgen256.al.blocks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.mgen256.al.AdditionalLights;
import com.mgen256.al.FireBlockList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.block.material.PushReaction;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

//fire_for_standing_torch_s
public class Fire extends ModBlock implements IWaterLoggable{

    private static Map<FireBlockList, VoxelShape> SHAPES;
    private static Map<FireBlockList, BasicParticleType> PARTICLE_TYPES;
    private static Map<FireBlockList, Double> SMOKE_POS;

    static {
        SHAPES = new LinkedHashMap<FireBlockList, VoxelShape>();
        SHAPES.put( FireBlockList.standing_torch_s, Block.makeCuboidShape(4.0D, -6.0D, 4.0D, 12.0D, 2.0D, 12.0D) );
        SHAPES.put( FireBlockList.standing_torch_l, Block.makeCuboidShape(4.0D, -2.0D, 4.0D, 12.0D, 6.0D, 12.0D) );
        SHAPES.put( FireBlockList.fire_pit_s, Block.makeCuboidShape(0.0D, -10.0D, 0.0D, 16.0D, -1.0D, 16.0D) );
        SHAPES.put( FireBlockList.fire_pit_l, Block.makeCuboidShape(0.0D, -2.0D, 0.0D, 16.0D, 7.0D, 16.0D) );

        PARTICLE_TYPES = new LinkedHashMap<FireBlockList, BasicParticleType>();
        PARTICLE_TYPES.put( FireBlockList.standing_torch_s, ParticleTypes.SMOKE );
        PARTICLE_TYPES.put( FireBlockList.standing_torch_l, ParticleTypes.SMOKE );
        PARTICLE_TYPES.put( FireBlockList.fire_pit_s, ParticleTypes.LARGE_SMOKE );
        PARTICLE_TYPES.put( FireBlockList.fire_pit_l, ParticleTypes.LARGE_SMOKE );

        SMOKE_POS = new LinkedHashMap<FireBlockList, Double>();
        SMOKE_POS.put( FireBlockList.standing_torch_s, 0.2 );
        SMOKE_POS.put( FireBlockList.standing_torch_l, 0.7 );
        SMOKE_POS.put( FireBlockList.fire_pit_s, 0.0 );
        SMOKE_POS.put( FireBlockList.fire_pit_l, 0.8 );
    }

    private static Properties createProps(){
       Material material = new Material(
        MaterialColor.AIR,
        false, //isLiquid
        true,  //isSolid
        true, //Blocks Movement
        true, //isOpaque
        true, //requires no tool
        false, //isFlammable
        false, //isReplaceable
        PushReaction.NORMAL
        );
       
        Properties p = Block.Properties.create(material);
        p.lightValue(15);
        p.hardnessAndResistance(0.0f);
        p.doesNotBlockMovement();
        p.sound(SoundType.STONE);
        return p;
    }

    public Fire( FireBlockList _baseFireBlock ) {
        super( "fire_for_" + _baseFireBlock, null, createProps(), SHAPES.get(_baseFireBlock));

        baseFireBlock = _baseFireBlock;
      }

      
    private FireBlockList baseFireBlock;
    

    @Override
    public void init() {
        setRegistryName(name);
        blockItem = new BlockItem(this, AdditionalLights.ItemProps);
        blockItem.setRegistryName(getRegistryName());
    }

    protected float getFireDamageAmount() {
        return 0.0F;
    }

    protected double getSmokePos_Y() {
        return 0.9D;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES.get(baseFireBlock);
    }

    
    @Override
    public void animateTick(final BlockState stateIn, final World worldIn, final BlockPos pos, final Random rand) {
        final double d0 = (double) pos.getX() + 0.5D;
        final double d1 = (double) pos.getY() + SMOKE_POS.get(baseFireBlock);
        final double d2 = (double) pos.getZ() + 0.5D;
        worldIn.addParticle(PARTICLE_TYPES.get(baseFireBlock), d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }

    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
        if (!entityIn.isImmuneToFire() && entityIn instanceof LivingEntity
                && !EnchantmentHelper.hasFrostWalker((LivingEntity) entityIn)) {
            entityIn.attackEntityFrom(DamageSource.IN_FIRE, getFireDamageAmount());
        }

        super.onEntityWalk(worldIn, pos, entityIn);
    }


    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext p_196258_1_) {
        return getDefaultState().with(BlockStateProperties.AXIS, p_196258_1_.getFace().getAxis())
                .with(BlockStateProperties.WATERLOGGED, false);
    }

    @Override
    public boolean canContainFluid(final IBlockReader p_204510_1_, final BlockPos p_204510_2_, final BlockState p_204510_3_,
            final Fluid p_204510_4_) {
        return true;
    }

    @Override
    public IFluidState getFluidState(final BlockState p_204507_1_) {
        return p_204507_1_.get(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getStillFluidState(false)
                : super.getFluidState(p_204507_1_);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> p_206840_1_) {
        p_206840_1_.add(BlockStateProperties.AXIS, BlockStateProperties.WATERLOGGED);
    }


    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return facing == Direction.DOWN && !this.isValidPosition(stateIn, worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }
  
     public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return hasSolidSide(worldIn, pos.down(), Direction.UP);
    }


     public static boolean hasSolidSide(IWorldReader worldIn, BlockPos pos, Direction directionIn) {
        BlockState blockstate = worldIn.getBlockState(pos);
        return !blockstate.isIn(BlockTags.LEAVES) && blockstate.getBlock().isAir(null) == false;
    }

}