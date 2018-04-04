package mcjty.questutils.blocks.screen;

import mcjty.lib.container.BaseBlock;
import mcjty.questutils.blocks.ModBlocks;
import mcjty.questutils.blocks.QUBlock;
import mcjty.questutils.proxy.GuiProxy;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

public class ScreenBlock extends QUBlock<ScreenTE, ScreenContainer> {

    public static final PropertyDirection HORIZONTAL_FACING = PropertyDirection.create("horizontal_facing", EnumFacing.Plane.HORIZONTAL);

    public ScreenBlock() {
        super(ScreenTE.class, ScreenContainer.class, "screen");
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(HORIZONTAL_FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        if(meta > 5) {
            meta -= 4;
        } else if(meta > 1) {
            EnumFacing facing = EnumFacing.VALUES[meta];
            return getDefaultState().withProperty(FACING, facing).withProperty(HORIZONTAL_FACING, facing);
        }
        EnumFacing horizontalFacing = EnumFacing.VALUES[(meta >> 1) + 2];
        EnumFacing facing = (meta & 1) == 0 ? EnumFacing.DOWN : EnumFacing.UP;
        return getDefaultState().withProperty(HORIZONTAL_FACING, horizontalFacing).withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        EnumFacing horizontalFacing = state.getValue(HORIZONTAL_FACING);
        int meta = 0;
        switch(facing) {
        case UP:
            meta = 1;
            //$FALL-THROUGH$
        case DOWN:
            meta += (horizontalFacing.getIndex() << 1);
            if(meta < 6) meta -= 4;
            return meta;
        default:
            return facing.getIndex();
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, HORIZONTAL_FACING);
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        BlockPos pos = data.getPos();
        addProbeInfoScreen(mode, probeInfo, player, world, pos);
    }

    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfoScreen(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, BlockPos pos) {
//        TileEntity te = world.getTileEntity(pos);
//        if (te instanceof ScreenTileEntity) {
//            ScreenTileEntity screenTileEntity = (ScreenTileEntity) te;
//            IScreenModule module = screenTileEntity.getHoveringModule();
//            if (module instanceof ITooltipInfo) {
//                String[] info = ((ITooltipInfo) module).getInfo(world, screenTileEntity.getHoveringX(), screenTileEntity.getHoveringY(), player);
//                for (String s : info) {
//                    probeInfo.text(s);
//                }
//            }
//        }
    }

    private static long lastTime = 0;

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.getWailaBody(itemStack, currenttip, accessor, config);
        TileEntity te = accessor.getTileEntity();
        if (te instanceof ScreenTE) {
            return getWailaBodyScreen(currenttip, accessor.getPlayer(), (ScreenTE) te);
        } else {
            return Collections.emptyList();
        }
    }

    @SideOnly(Side.CLIENT)
    @Optional.Method(modid = "waila")
    public List<String> getWailaBodyScreen(List<String> currenttip, EntityPlayer player, ScreenTE te) {
        if (System.currentTimeMillis() - lastTime > 500) {
            lastTime = System.currentTimeMillis();
//            RFToolsMessages.INSTANCE.sendToServer(new PacketGetInfoFromServer(RFTools.MODID, new ScreenInfoPacketServer(te.getWorld().provider.getDimension(),
//                    te.getPos())));
        }
//        Collections.addAll(currenttip, ScreenInfoPacketClient.infoReceived);
        return currenttip;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initModel() {
        ClientRegistry.bindTileEntitySpecialRenderer(ScreenTE.class, new ScreenRenderer());
        ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(this), 0, ScreenTE.class);
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    public boolean activate(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer playerIn) {
        if (world.isRemote) {
            RayTraceResult mouseOver = Minecraft.getMinecraft().objectMouseOver;
            ScreenTE screenTileEntity = (ScreenTE) world.getTileEntity(pos);
//            screenTileEntity.hitScreenClient(mouseOver.hitVec.x - pos.getX(), mouseOver.hitVec.y - pos.getY(), mouseOver.hitVec.z - pos.getZ(), mouseOver.sideHit, world.getBlockState(pos).getValue(mcjty.rftools.blocks.screens.ScreenBlock.HORIZONTAL_FACING));
        }
    }

    private void setInvisibleBlockSafe(World world, BlockPos pos, int dx, int dy, int dz, EnumFacing facing) {
        int yy = pos.getY() + dy;
        if (yy < 0 || yy >= world.getHeight()) {
            return;
        }
        int xx = pos.getX() + dx;
        int zz = pos.getZ() + dz;
        BlockPos posO = new BlockPos(xx, yy, zz);
        if (world.isAirBlock(posO)) {
            world.setBlockState(posO, ModBlocks.screenHitBlock.getDefaultState().withProperty(BaseBlock.FACING, facing), 3);
            ScreenHitTE screenHitTileEntity = (ScreenHitTE) world.getTileEntity(posO);
            screenHitTileEntity.setRelativeLocation(-dx, -dy, -dz);
        }
    }

    private void setInvisibleBlocks(World world, BlockPos pos, int size) {
        IBlockState state = world.getBlockState(pos);
        EnumFacing facing = state.getValue(BaseBlock.FACING);
        EnumFacing horizontalFacing = state.getValue(HORIZONTAL_FACING);

        for (int i = 0 ; i <= size ; i++) {
            for (int j = 0 ; j <= size ; j++) {
                if (i != 0 || j != 0) {
                    if (facing == EnumFacing.NORTH) {
                        setInvisibleBlockSafe(world, pos, -i, -j, 0, facing);
                    } else if (facing == EnumFacing.SOUTH) {
                        setInvisibleBlockSafe(world, pos, i, -j, 0, facing);
                    } else if (facing == EnumFacing.WEST) {
                        setInvisibleBlockSafe(world, pos, 0, -i, j, facing);
                    } else if (facing == EnumFacing.EAST) {
                        setInvisibleBlockSafe(world, pos, 0, -i, -j, facing);
                    } else if (facing == EnumFacing.UP) {
                        if (horizontalFacing == EnumFacing.NORTH) {
                            setInvisibleBlockSafe(world, pos, -i, 0, -j, facing);
                        } else if (horizontalFacing == EnumFacing.SOUTH) {
                            setInvisibleBlockSafe(world, pos, i, 0, j, facing);
                        } else if (horizontalFacing == EnumFacing.WEST) {
                            setInvisibleBlockSafe(world, pos, -i, 0, j, facing);
                        } else if (horizontalFacing == EnumFacing.EAST) {
                            setInvisibleBlockSafe(world, pos, i, 0, -j, facing);
                        }
                    } else if (facing == EnumFacing.DOWN) {
                        if (horizontalFacing == EnumFacing.NORTH) {
                            setInvisibleBlockSafe(world, pos, -i, 0, j, facing);
                        } else if (horizontalFacing == EnumFacing.SOUTH) {
                            setInvisibleBlockSafe(world, pos, i, 0, -j, facing);
                        } else if (horizontalFacing == EnumFacing.WEST) {
                            setInvisibleBlockSafe(world, pos, i, 0, j, facing);
                        } else if (horizontalFacing == EnumFacing.EAST) {
                            setInvisibleBlockSafe(world, pos, -i, 0, -j, facing);
                        }
                    }
                }
            }
        }
    }

    private void clearInvisibleBlockSafe(World world, BlockPos pos) {
        if (pos.getY() < 0 || pos.getY() >= world.getHeight()) {
            return;
        }
        if (world.getBlockState(pos).getBlock() == ModBlocks.screenHitBlock) {
            world.setBlockToAir(pos);
        }
    }

    private void clearInvisibleBlocks(World world, BlockPos pos, IBlockState state, int size) {
        EnumFacing facing = state.getValue(BaseBlock.FACING);
        EnumFacing horizontalFacing = state.getValue(HORIZONTAL_FACING);
        for (int i = 0 ; i <= size ; i++) {
            for (int j = 0 ; j <= size ; j++) {
                if (i != 0 || j != 0) {
                    if (facing == EnumFacing.NORTH) {
                        clearInvisibleBlockSafe(world, pos.add(-i, -j, 0));
                    } else if (facing == EnumFacing.SOUTH) {
                        clearInvisibleBlockSafe(world, pos.add(i, -j, 0));
                    } else if (facing == EnumFacing.WEST) {
                        clearInvisibleBlockSafe(world, pos.add(0, -i, j));
                    } else if (facing == EnumFacing.EAST) {
                        clearInvisibleBlockSafe(world, pos.add(0, -i, -j));
                    } else if (facing == EnumFacing.UP) {
                        if (horizontalFacing == EnumFacing.NORTH) {
                            clearInvisibleBlockSafe(world, pos.add(-i, 0, -j));
                        } else if (horizontalFacing == EnumFacing.SOUTH) {
                            clearInvisibleBlockSafe(world, pos.add(i, 0, j));
                        } else if (horizontalFacing == EnumFacing.WEST) {
                            clearInvisibleBlockSafe(world, pos.add(-i, 0, j));
                        } else if (horizontalFacing == EnumFacing.EAST) {
                            clearInvisibleBlockSafe(world, pos.add(i, 0, -j));
                        }
                    } else if (facing == EnumFacing.DOWN) {
                        if (horizontalFacing == EnumFacing.NORTH) {
                            clearInvisibleBlockSafe(world, pos.add(-i, 0, j));
                        } else if (horizontalFacing == EnumFacing.SOUTH) {
                            clearInvisibleBlockSafe(world, pos.add(i, 0, -j));
                        } else if (horizontalFacing == EnumFacing.WEST) {
                            clearInvisibleBlockSafe(world, pos.add(i, 0, j));
                        } else if (horizontalFacing == EnumFacing.EAST) {
                            clearInvisibleBlockSafe(world, pos.add(-i, 0, -j));
                        }
                    }
                }
            }
        }
    }

    private static class Setup {
        private final boolean transparent;
        private final int size;

        public Setup(int size, boolean transparent) {
            this.size = size;
            this.transparent = transparent;
        }

        public int getSize() {
            return size;
        }

        public boolean isTransparent() {
            return transparent;
        }
    }

    private static Setup transitions[] = new Setup[] {
            new Setup(ScreenTE.SIZE_NORMAL, false),
            new Setup(ScreenTE.SIZE_NORMAL, true),
            new Setup(ScreenTE.SIZE_LARGE, false),
            new Setup(ScreenTE.SIZE_LARGE, true),
            new Setup(ScreenTE.SIZE_HUGE, false),
            new Setup(ScreenTE.SIZE_HUGE, true),
            new Setup(ScreenTE.SIZE_ENOURMOUS, false),
            new Setup(ScreenTE.SIZE_ENOURMOUS, true),
            new Setup(ScreenTE.SIZE_GIGANTIC, false),
            new Setup(ScreenTE.SIZE_GIGANTIC, true),
    };

    @Override
    protected boolean wrenchUse(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        cycleSizeTranspMode(world, pos);
        return true;
    }

    public void cycleSizeTranspMode(World world, BlockPos pos) {
        ScreenTE screenTileEntity = (ScreenTE) world.getTileEntity(pos);
        IBlockState state = world.getBlockState(pos);
        clearInvisibleBlocks(world, pos, state, screenTileEntity.getSize());
        for (int i = 0 ; i < transitions.length ; i++) {
            Setup setup = transitions[i];
            if (setup.isTransparent() == screenTileEntity.isTransparent() && setup.getSize() == screenTileEntity.getSize()) {
                Setup next = transitions[(i+1) % transitions.length];
                screenTileEntity.setTransparent(next.isTransparent());
                screenTileEntity.setSize(next.getSize());
                setInvisibleBlocks(world, pos, screenTileEntity.getSize());
                break;
            }
        }
    }

    public void cycleSizeMode(World world, BlockPos pos) {
        ScreenTE screenTileEntity = (ScreenTE) world.getTileEntity(pos);
        IBlockState state = world.getBlockState(pos);
        clearInvisibleBlocks(world, pos, state, screenTileEntity.getSize());
        for (int i = 0 ; i < transitions.length ; i++) {
            Setup setup = transitions[i];
            if (setup.isTransparent() == screenTileEntity.isTransparent() && setup.getSize() == screenTileEntity.getSize()) {
                Setup next = transitions[(i+2) % transitions.length];
                screenTileEntity.setTransparent(next.isTransparent());
                screenTileEntity.setSize(next.getSize());
                setInvisibleBlocks(world, pos, screenTileEntity.getSize());
                break;
            }
        }
    }

    public void cycleTranspMode(World world, BlockPos pos) {
        ScreenTE screenTileEntity = (ScreenTE) world.getTileEntity(pos);
        IBlockState state = world.getBlockState(pos);
        clearInvisibleBlocks(world, pos, state, screenTileEntity.getSize());
        for (int i = 0 ; i < transitions.length ; i++) {
            Setup setup = transitions[i];
            if (setup.isTransparent() == screenTileEntity.isTransparent() && setup.getSize() == screenTileEntity.getSize()) {
                Setup next = transitions[(i % 2) == 0 ? (i+1) : (i-1)];
                screenTileEntity.setTransparent(next.isTransparent());
                screenTileEntity.setSize(next.getSize());
                setInvisibleBlocks(world, pos, screenTileEntity.getSize());
                break;
            }
        }
    }

    @Override
    protected boolean openGui(World world, int x, int y, int z, EntityPlayer player) {
        ItemStack itemStack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (!itemStack.isEmpty() && itemStack.getItem() == Items.DYE) {
            int damage = itemStack.getItemDamage();
            if (damage < 0) {
                damage = 0;
            } else if (damage > 15) {
                damage = 15;
            }
            int color = ItemDye.DYE_COLORS[damage];
            ScreenTE screenTileEntity = (ScreenTE) world.getTileEntity(new BlockPos(x, y, z));
            screenTileEntity.setColor(color);
            return true;
        }
        if (player.isSneaking()) {
            return super.openGui(world, x, y, z, player);
        } else {
            if (world.isRemote) {
//                activateOnClient(world, new BlockPos(x, y, z));
            }
            return true;
        }
    }

    public static final AxisAlignedBB BLOCK_AABB = new AxisAlignedBB(0.5F - 0.5F, 0.0F, 0.5F - 0.5F, 0.5F + 0.5F, 1.0F, 0.5F + 0.5F);
    public static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0F, 0.0F, 1.0F - 0.125F, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.125F);
    public static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(1.0F - 0.125F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 0.125F, 1.0F, 1.0F);
    public static final AxisAlignedBB UP_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
    public static final AxisAlignedBB DOWN_AABB = new AxisAlignedBB(0.0F, 1.0F - 0.125F, 0.0F, 1.0F, 1.0F, 1.0F);

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing facing = state.getValue(BaseBlock.FACING);
        if (facing == EnumFacing.NORTH) {
            return NORTH_AABB;
        } else if (facing == EnumFacing.SOUTH) {
            return SOUTH_AABB;
        } else if (facing == EnumFacing.WEST) {
            return WEST_AABB;
        } else if (facing == EnumFacing.EAST) {
            return EAST_AABB;
        } else if (facing == EnumFacing.UP) {
            return UP_AABB;
        } else if (facing == EnumFacing.DOWN) {
            return DOWN_AABB;
        } else {
            return BLOCK_AABB;
        }
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    /**
     * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
     * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
     */
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public int getGuiID() {
        return GuiProxy.GUI_SCREEN;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<ScreenGui> getGuiClass() {
        return ScreenGui.class;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack itemStack, World player, List<String> list, ITooltipFlag whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);

        list.add(TextFormatting.WHITE + "Objective and interaction screen");
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        super.onBlockPlacedBy(world, pos, state, entityLivingBase, itemStack);

        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof ScreenTE) {
            ScreenTE screenTileEntity = (ScreenTE) tileEntity;
            if (screenTileEntity.getSize() > ScreenTE.SIZE_NORMAL) {
                setInvisibleBlocks(world, pos, screenTileEntity.getSize());
            }
        }
    }


    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ScreenTE) {
            ScreenTE screenTileEntity = (ScreenTE) te;
            if (screenTileEntity.getSize() > ScreenTE.SIZE_NORMAL) {
                clearInvisibleBlocks(world, pos, state, screenTileEntity.getSize());
            }
        }
        super.breakBlock(world, pos, state);
    }
}