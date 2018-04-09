package mcjty.questutils.blocks.pedestal;

import mcjty.lib.container.GenericGuiContainer;
import mcjty.questutils.blocks.QUBlock;
import mcjty.questutils.blocks.itemcomparator.ItemComparatorBlock;
import mcjty.questutils.blocks.itemcomparator.ItemComparatorTE;
import mcjty.questutils.items.ModItems;
import mcjty.questutils.proxy.GuiProxy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PedestalBlock extends QUBlock<PedestalTE, PedestalContainer> {

    public PedestalBlock() {
        super(PedestalTE.class, PedestalContainer.class, "pedestal");
    }

    @Override
    public RotationType getRotationType() {
        return RotationType.NONE;
    }

    @Override
    public boolean hasRedstoneOutput() {
        return true;
    }


    @Override
    public int getGuiID() {
        return GuiProxy.GUI_PEDESTAL;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return PedestalGui.class;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void initModel() {
        super.initModel();
        ClientRegistry.bindTileEntitySpecialRenderer(PedestalTE.class, new PedestalTESR());
    }

    @Override
    public boolean handleModule(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (heldItem.getItem() == ModItems.controlKey) {
            return false;
        }
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof PedestalTE) {
            PedestalTE pedestal = (PedestalTE) te;
            PedestalMode mode = pedestal.getMode();
            switch (mode) {
                case MODE_DISPLAY:
                    break;
                case MODE_INTERACT:
                    pedestal.interactItem(player, hand);
                    break;
                case MODE_PLACE:
                    pedestal.placeItem(player, hand);
                    break;
                case MODE_TAKE:
                    pedestal.takeItem(player, hand);
                    break;
            }
        }
        return true;
    }

    @Override
    protected int getRedstoneOutput(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
//        EnumFacing direction = state.getValue(FACING);
        switch (side) {
            case DOWN:
            case UP:
                break;
            case NORTH:
            case SOUTH:
            case WEST:
            case EAST:
                TileEntity te = world.getTileEntity(pos);
                if (state.getBlock() instanceof PedestalBlock && te instanceof PedestalTE) {
                    PedestalTE pedestal = (PedestalTE) te;
                    return pedestal.getStackInSlot(PedestalContainer.SLOT_ITEM).isEmpty() ? 0 : 15;
                }
                break;
        }
        return 0;
    }


    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState blockState) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState blockState) {
        return false;
    }

    private PedestalTE getTE(World world, BlockPos pos) {
        return (PedestalTE) world.getTileEntity(pos);
    }
}
