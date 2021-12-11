package ca.fxco.betterblockstates.mixin.blocks.comparator;

import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ComparatorMode;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.block.ComparatorBlock.MODE;

@Mixin(ComparatorBlock.class)
public abstract class ComparatorBlock_BElessMixin extends AbstractRedstoneGateBlock {

    @Shadow protected abstract int calculateOutputSignal(World world, BlockPos pos, BlockState state);

    protected ComparatorBlock_BElessMixin(Settings settings) {super(settings);}

    private static final IntProperty OUTPUT_SIGNAL = IntProperty.of("output_signal", 0, 1023);


    @Redirect(
            method = "<init>(Lnet/minecraft/block/AbstractBlock$Settings;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/ComparatorBlock;setDefaultState(Lnet/minecraft/block/BlockState;)V"
            )
    )
    protected final void setDefaultStateWithSignal(ComparatorBlock instance, BlockState blockState) {
        this.setDefaultState(blockState.with(OUTPUT_SIGNAL, 0));
    }


    @Inject(
            method = "createBlockEntity(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/block/entity/BlockEntity;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void createBlockEntity(BlockPos pos, BlockState state, CallbackInfoReturnable<BlockEntity> cir) {
        cir.setReturnValue(null);
    }


    @Inject(
            method = "appendProperties(Lnet/minecraft/state/StateManager$Builder;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    protected void appendPropertiesPlusSignal(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(new Property[]{FACING, MODE, POWERED, OUTPUT_SIGNAL});
        ci.cancel();
    }


    @Inject(
            method = "getOutputLevel(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    protected void getOutputLevel(BlockView world, BlockPos pos, BlockState state, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(state.get(OUTPUT_SIGNAL));
    }


    @Inject(
            method = "updatePowered(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    protected void updatePowered(World world, BlockPos pos, BlockState state, CallbackInfo ci) {
        if (!world.getBlockTickScheduler().isTicking(pos, this)) {
            int i = this.calculateOutputSignal(world, pos, state);
            if (i != state.get(OUTPUT_SIGNAL) || state.get(POWERED) != this.hasPower(world, pos, state)) {
                TickPriority tickPriority = this.isTargetNotAligned(world, pos, state) ? TickPriority.HIGH : TickPriority.NORMAL;
                world.createAndScheduleBlockTick(pos, this, 2, tickPriority);
            }
        }
        ci.cancel();
    }


    @Inject(
            method = "update(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void update(World world, BlockPos pos, BlockState state, CallbackInfo ci) {
        int i = this.calculateOutputSignal(world, pos, state);
        int j = state.get(OUTPUT_SIGNAL);
        if (j != i || state.get(MODE) == ComparatorMode.COMPARE) {
            boolean hasPower = this.hasPower(world, pos, state);
            boolean powered = state.get(POWERED);
            if (powered && !hasPower) {
                world.setBlockState(pos, state.with(POWERED, false).with(OUTPUT_SIGNAL,i), 2);
            } else if (!powered && hasPower) {
                world.setBlockState(pos, state.with(POWERED, true).with(OUTPUT_SIGNAL,i), 2);
            } else {
                world.setBlockState(pos, state.with(OUTPUT_SIGNAL,i), 2);
            }
            this.updateTarget(world, pos, state);
        }
        ci.cancel();
    }
}
