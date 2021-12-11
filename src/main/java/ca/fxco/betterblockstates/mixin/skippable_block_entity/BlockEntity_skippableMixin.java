package ca.fxco.betterblockstates.mixin.skippable_block_entity;

import ca.fxco.betterblockstates.common.patches.blockEntitySkipable;
import net.minecraft.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockEntity.class)
public class BlockEntity_skippableMixin implements blockEntitySkipable {}
