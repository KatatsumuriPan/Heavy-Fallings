package kpan.heavy_fallings.asm.hook;

import javax.annotation.Nullable;
import kpan.heavy_fallings.config.ConfigHolder;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@SuppressWarnings("unused")
public class HK_EntityFallingBlock {

    public static boolean mayPlace(World world, Block blockIn, BlockPos pos, boolean skipCollisionCheck, EnumFacing sidePlacedOn, @Nullable Entity fallingBlock) {
        IBlockState iblockstate = world.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (ConfigHolder.common.breaks.stream().anyMatch(p -> p.test(iblockstate))) {
            drop(world, fallingBlock, block, iblockstate, false);
            return true;
        }
        if (ConfigHolder.common.nonBreaks.stream().anyMatch(p -> p.test(iblockstate)))
            return false;

        if (!((fallingBlock instanceof EntityPlayer) || !net.minecraftforge.event.ForgeEventFactory.onBlockPlace(fallingBlock, new net.minecraftforge.common.util.BlockSnapshot(world, pos, blockIn.getDefaultState()), sidePlacedOn).isCanceled()))
            return false;

        if (iblockstate.getMaterial() == Material.CIRCUITS && blockIn == Blocks.ANVIL) {
            drop(world, fallingBlock, block, iblockstate, false);
            return true;
        }
        if (block.isReplaceable(world, pos) && blockIn.canPlaceBlockOnSide(world, pos, sidePlacedOn)) {
            drop(world, fallingBlock, block, iblockstate, false);
            return true;
        }

        if (iblockstate.getCollisionBoundingBox(world, pos) == Block.NULL_AABB || iblockstate.getBlockHardness(world, pos) <= 0.5f) {
            drop(world, fallingBlock, block, iblockstate, true);
            return true;
        }


        return false;
    }

    private static void drop(World world, @Nullable Entity fallingBlock, Block block, IBlockState iblockstate1, boolean defaultValue) {
        if (willDrop(iblockstate1, defaultValue))
            fallingBlock.entityDropItem(new ItemStack(block.getItemDropped(iblockstate1, world.rand, 0), 1, block.damageDropped(iblockstate1)), 0.0F);
    }

    private static boolean willDrop(IBlockState iblockstate1, boolean defaultValue) {
        if (ConfigHolder.common.dropOnBroken.stream().anyMatch(p -> p.test(iblockstate1)))
            return true;
        if (ConfigHolder.common.notDropOnBroken.stream().anyMatch(p -> p.test(iblockstate1)))
            return false;
        return defaultValue;
    }

}
