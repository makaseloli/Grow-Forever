package io.github.makaseloli.grow_forever.mixin;

import net.minecraft.block.BambooBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BambooBlock.class)
public abstract class BambooStalkBlockMixin {
    private static int growForever$countBambooBelow(WorldView world, BlockPos pos) {
        int height = 0;
        BlockPos cursor = pos.down();

        while (world.getBlockState(cursor).isOf(Blocks.BAMBOO)) {
            ++height;
            if (cursor.getY() <= world.getBottomY()) {
                break;
            }
            cursor = cursor.down();
        }

        return height;
    }

    private static int growForever$countBambooAbove(WorldView world, BlockPos pos) {
        int height = 0;
        BlockPos cursor = pos.up();

        while (world.getBlockState(cursor).isOf(Blocks.BAMBOO)) {
            ++height;
            if (cursor.getY() >= world.getTopY()) {
                break;
            }
            cursor = cursor.up();
        }

        return height;
    }

    @Shadow
    protected abstract void updateLeaves(BlockState state, World world, BlockPos pos, Random random, int height);

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void growForever$randomTickIgnoreHeight(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        BlockPos abovePos = pos.up();
        if (world.isAir(abovePos) && world.getBaseLightLevel(abovePos, 0) >= 9) {
            int height = growForever$countBambooBelow(world, pos) + 1;
            if (random.nextInt(3) == 0) {
                this.updateLeaves(state, world, pos, random, height);
            }
        }
        ci.cancel();
    }

    @Inject(method = "hasRandomTicks", at = @At("HEAD"), cancellable = true)
    private void growForever$isRandomlyTickingAlways(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(method = "isFertilizable", at = @At("HEAD"), cancellable = true)
    private void growForever$isValidBonemealTargetIgnoreHeight(WorldView world, BlockPos pos, BlockState state, boolean isClient, CallbackInfoReturnable<Boolean> cir) {
        int above = growForever$countBambooAbove(world, pos);
        BlockPos topPos = pos.up(above);
        cir.setReturnValue(world.isAir(topPos.up()));
    }

    @Inject(method = "grow", at = @At("HEAD"), cancellable = true)
    private void growForever$performBonemealIgnoreHeight(ServerWorld world, Random random, BlockPos pos, BlockState state, CallbackInfo ci) {
        int above = growForever$countBambooAbove(world, pos);
        int total = above + growForever$countBambooBelow(world, pos) + 1;
        int steps = 1 + random.nextInt(2);

        for (int i = 0; i < steps; ++i) {
            BlockPos topPos = pos.up(above);
            BlockState topState = world.getBlockState(topPos);
            if (!topState.isOf(Blocks.BAMBOO) || !world.isAir(topPos.up())) {
                ci.cancel();
                return;
            }

            this.updateLeaves(topState, world, topPos, random, total);
            ++above;
            ++total;
        }

        ci.cancel();
    }
}
