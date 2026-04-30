package io.github.makaseloli.grow_forever.mixin;

import net.minecraft.block.AbstractPlantStemBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractPlantStemBlock.class)
public abstract class GrowingPlantHeadBlockMixin {
    @ModifyConstant(method = "hasRandomTicks", constant = @Constant(intValue = 25))
    private int growForever$ignoreMaxAgeForTicking(int vanillaMaxAge) {
        return vanillaMaxAge + 1;
    }

    @ModifyConstant(method = "randomTick", constant = @Constant(intValue = 25))
    private int growForever$ignoreMaxAgeForGrowth(int vanillaMaxAge) {
        return vanillaMaxAge + 1;
    }
}
