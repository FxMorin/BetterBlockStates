package ca.fxco.betterblockstates.mixin.blocks.sculksensor.movable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBlock.class)
public class PistonBlock_movableSculkMixin {

    @Inject(
            method = "isMovable(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;ZLnet/minecraft/util/math/Direction;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;hasBlockEntity()Z",
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private static void makeSculkMovable(BlockState state, World world, BlockPos pos, Direction direction, boolean canBreak, Direction pistonDir, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() == Blocks.SCULK_SENSOR) cir.setReturnValue(true);
    }
}
