package mekanism.common.block.basic;

import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.common.Mekanism;
import mekanism.common.block.BlockTileDrops;
import mekanism.common.block.interfaces.IBlockDescriptive;
import mekanism.common.block.states.BlockStateHelper;
import mekanism.common.block.states.IStateActive;
import mekanism.common.content.boiler.SynchronizedBoilerData;
import mekanism.common.multiblock.IMultiblock;
import mekanism.common.multiblock.IStructuralMultiblock;
import mekanism.common.tile.TileEntitySuperheatingElement;
import mekanism.common.tile.prefab.TileEntityBasicBlock;
import mekanism.common.util.LangUtils;
import mekanism.common.util.MekanismUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSuperheatingElement extends BlockTileDrops implements IBlockDescriptive, IStateActive {

    private final String name;

    public BlockSuperheatingElement() {
        super(Material.IRON);
        setHardness(5F);
        setResistance(10F);
        setCreativeTab(Mekanism.tabMekanism);
        this.name = "superheating_element";
        setTranslationKey(this.name);
        setRegistryName(new ResourceLocation(Mekanism.MODID, this.name));
    }

    @Override
    public String getDescription() {
        //TODO: Should name just be gotten from registry name
        return LangUtils.localize("tooltip.mekanism." + this.name);
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return BlockStateHelper.getBlockState(this);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        //TODO
        return 0;
    }

    @Nonnull
    @Override
    @Deprecated
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        return BlockStateHelper.getActualState(this, state, MekanismUtils.getTileEntitySafe(world, pos));
    }

    @Override
    @Deprecated
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos) {
        if (!world.isRemote) {
            TileEntity tileEntity = new Coord4D(pos, world).getTileEntity(world);
            if (tileEntity instanceof IMultiblock) {
                ((IMultiblock<?>) tileEntity).doUpdate();
            }
            if (tileEntity instanceof TileEntityBasicBlock) {
                ((TileEntityBasicBlock) tileEntity).onNeighborChange(neighborBlock);
            }
            if (tileEntity instanceof IStructuralMultiblock) {
                ((IStructuralMultiblock) tileEntity).doUpdate();
            }
        }
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityBasicBlock) {
            ((TileEntityBasicBlock) te).redstone = world.getRedstonePowerFromNeighbors(pos) > 0;
        }

        world.markBlockRangeForRenderUpdate(pos, pos.add(1, 1, 1));
        world.checkLightFor(EnumSkyBlock.BLOCK, pos);
        world.checkLightFor(EnumSkyBlock.SKY, pos);

        if (!world.isRemote && te != null) {
            if (te instanceof IMultiblock) {
                ((IMultiblock<?>) te).doUpdate();
            }
            if (te instanceof IStructuralMultiblock) {
                ((IStructuralMultiblock) te).doUpdate();
            }
        }
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntitySuperheatingElement();
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tileEntity = MekanismUtils.getTileEntitySafe(world, pos);
        if (tileEntity instanceof TileEntitySuperheatingElement) {
            TileEntitySuperheatingElement element = (TileEntitySuperheatingElement) tileEntity;
            if (element.multiblockUUID != null && SynchronizedBoilerData.clientHotMap.get(element.multiblockUUID) != null) {
                return SynchronizedBoilerData.clientHotMap.get(element.multiblockUUID) ? 15 : 0;
            }
        }
        return 0;
    }
}