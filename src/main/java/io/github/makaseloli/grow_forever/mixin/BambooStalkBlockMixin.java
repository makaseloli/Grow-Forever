package io.github.makaseloli.grow_forever.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BambooStalkBlock.class)
public abstract class BambooStalkBlockMixin {
    private static int growForever$countBambooBelow(LevelReader level, BlockPos pos) {
        int height = 0;
        BlockPos cursor = pos.below();

        while (level.getBlockState(cursor).is(Blocks.BAMBOO)) {
            ++height;
            if (cursor.getY() <= level.getMinY()) {
                break;
            }
            cursor = cursor.below();
        }

        return height;
    }

    private static int growForever$countBambooAbove(LevelReader level, BlockPos pos) {
        int height = 0;
        BlockPos cursor = pos.above();

        while (level.getBlockState(cursor).is(Blocks.BAMBOO)) {
            ++height;
            if (cursor.getY() >= level.getMaxY()) {
                break;
            }
            cursor = cursor.above();
        }

        return height;
    }

    @Shadow
    protected abstract void growBamboo(BlockState state, Level level, BlockPos pos, RandomSource random, int height);

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void growForever$randomTickIgnoreHeight(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        BlockPos abovePos = pos.above();
        if (level.isEmptyBlock(abovePos) && level.getRawBrightness(abovePos, 0) >= 9) {
            int height = growForever$countBambooBelow(level, pos) + 1;
            if (random.nextInt(3) == 0) {
                this.growBamboo(state, level, pos, random, height);
            }
        }
        ci.cancel();
    }

    @Inject(method = "isRandomlyTicking", at = @At("HEAD"), cancellable = true)
    private void growForever$isRandomlyTickingAlways(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "isValidBonemealTarget", at = @At("HEAD"), cancellable = true)
    private void growForever$isValidBonemealTargetIgnoreHeight(LevelReader level, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        int above = growForever$countBambooAbove(level, pos);
        BlockPos topPos = pos.above(above);
        cir.setReturnValue(level.isEmptyBlock(topPos.above()));
    }

    @Inject(method = "performBonemeal", at = @At("HEAD"), cancellable = true)
    private void growForever$performBonemealIgnoreHeight(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, CallbackInfo ci) {
        int above = growForever$countBambooAbove(level, pos);
        int total = above + growForever$countBambooBelow(level, pos) + 1;
        int steps = 1 + random.nextInt(2);

        for (int i = 0; i < steps; ++i) {
            BlockPos topPos = pos.above(above);
            BlockState topState = level.getBlockState(topPos);
            if (!topState.is(Blocks.BAMBOO) || !level.isEmptyBlock(topPos.above())) {
                ci.cancel();
                return;
            }

            this.growBamboo(topState, level, topPos, random, total);
            ++above;
            ++total;
        }

        ci.cancel();
    }
}
