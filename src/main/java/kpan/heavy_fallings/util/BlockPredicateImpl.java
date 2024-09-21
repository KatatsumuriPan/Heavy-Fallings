package kpan.heavy_fallings.util;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public final class BlockPredicateImpl implements IBlockPredicate {

    public static BlockPredicateImpl parse(String value) {
        return new BlockPredicateImpl(new ResourceLocation(value));
    }

    public static boolean canParse(String str) {
        Pattern p = Pattern.compile("^([a-zA-Z_0-9]+:)?[a-zA-Z_0-9]+$");
        Matcher m = p.matcher(str);
        return m.find();
    }

    private final ResourceLocation blockId;

    public BlockPredicateImpl(ResourceLocation blockId) {
        this.blockId = blockId;
    }

    @Override
    public boolean test(IBlockState blockState, IBlockAccess world, BlockPos pos) {
        if (!blockId.equals(blockState.getBlock().getRegistryName()))
            return false;


        return true;
    }


    @Override
    public String toString() {
        return blockId.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        BlockPredicateImpl that = (BlockPredicateImpl) o;
        return Objects.equals(blockId, that.blockId);
    }
    @Override
    public int hashCode() {
        return Objects.hashCode(blockId);
    }

}
