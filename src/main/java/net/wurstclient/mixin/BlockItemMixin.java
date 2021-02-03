package net.wurstclient.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.wurstclient.mixinterface.IBlockItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockItem.class)
public class BlockItemMixin implements IBlockItem {

    @Shadow
    public boolean canPlace(ItemPlacementContext context, BlockState state) {
        return false;
    }
}
