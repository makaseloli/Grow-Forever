package io.github.makaseloli.grow_forever.mixin;

import net.minecraft.world.level.block.CactusBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(CactusBlock.class)
public abstract class CactusBlockMixin {
    @ModifyConstant(method = "randomTick", constant = @Constant(intValue = 3))
    private int growForever$ignoreMaxHeight(int vanillaMaxHeight) {
        return Integer.MAX_VALUE;
    }
}
