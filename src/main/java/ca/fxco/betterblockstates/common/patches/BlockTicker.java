package ca.fxco.betterblockstates.common.patches;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockTicker {
    void tick(World world, BlockState state, BlockPos pos);
}
