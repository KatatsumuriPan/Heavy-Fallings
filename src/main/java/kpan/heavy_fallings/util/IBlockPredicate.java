package kpan.heavy_fallings.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

@FunctionalInterface
public interface IBlockPredicate {

    static boolean canParse(String str) {
        return BlockPredicateImpl.canParse(str);
    }
    static IBlockPredicate parse(String value) {
        return BlockPredicateImpl.parse(value);
    }

    boolean test(IBlockState blockState, IBlockAccess world, BlockPos pos);
}
