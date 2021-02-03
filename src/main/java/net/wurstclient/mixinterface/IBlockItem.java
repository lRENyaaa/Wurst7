package net.wurstclient.mixinterface;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;

public interface IBlockItem {
    boolean canPlace(ItemPlacementContext context, BlockState state);
}
