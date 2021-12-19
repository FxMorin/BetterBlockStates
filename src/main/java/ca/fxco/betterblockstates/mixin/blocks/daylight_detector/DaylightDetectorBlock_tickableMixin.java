package ca.fxco.betterblockstates.mixin.blocks.daylight_detector;

import ca.fxco.betterblockstates.common.classes.StateSystem;
import ca.fxco.betterblockstates.common.patches.BlockTicker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DaylightDetectorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Map;

@Mixin(DaylightDetectorBlock.class)
public abstract class DaylightDetectorBlock_tickableMixin extends Block implements BlockTicker {

    protected DaylightDetectorBlock_tickableMixin(Settings settings) {super(settings);}

    @Shadow private static void updateState(BlockState state, World world, BlockPos pos) {}

    static {
        StateSystem.blockTickersOnLoad.add(() -> Map.ofEntries(Map.entry((world) -> !world.isClient && world.getDimension().hasSkyLight() && world.getTime() % 20L == 0L,new HashSet<>(Blocks.DAYLIGHT_DETECTOR.getStateManager().getStates()))));
    }

    @Override
    public void tick(World world, BlockState state, BlockPos pos) {
        updateState(state,world,pos);
    }


    @Inject(
            method = "createBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/entity/BlockEntity;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void createBlockEntity(BlockPos pos, BlockState state, CallbackInfoReturnable<BlockEntity> cir) {
        cir.setReturnValue(null);
    }
}
