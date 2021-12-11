package ca.fxco.betterblockstates.mixin.blocks.sculksensor;

import ca.fxco.betterblockstates.common.patches.blockEntitySkipable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SculkSensorBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SculkSensorBlockEntity.class)
public abstract class SculkSensorBlockEntity_bypassMixin extends BlockEntity implements blockEntitySkipable {

    public SculkSensorBlockEntity_bypassMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {super(type, pos, state);}

    private static final IntProperty LAST_FREQUENCY = IntProperty.of("last_frequency", 0, 15);


    @Override
    public boolean shouldSkip() {
        return true;
    }


    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.setCachedState(this.getCachedState().with(LAST_FREQUENCY, nbt.getInt("last_vibration_frequency")));
    }
}
