package ca.fxco.betterblockstates.mixin.blocks.comparator;

import ca.fxco.betterblockstates.common.patches.blockEntitySkipable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ComparatorBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ComparatorBlockEntity.class)
public abstract class ComparatorBlockEntity_bypassMixin extends BlockEntity implements blockEntitySkipable {

    public ComparatorBlockEntity_bypassMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {super(type, pos, state);}

    private static final IntProperty OUTPUT_SIGNAL = IntProperty.of("output_signal", 0, 1023);


    @Override
    public boolean shouldSkip() {
        return true;
    }


    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.setCachedState(this.getCachedState().with(OUTPUT_SIGNAL, nbt.getInt("OutputSignal")));
    }
}
