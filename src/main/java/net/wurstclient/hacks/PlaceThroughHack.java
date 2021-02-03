package net.wurstclient.hacks;

import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.events.RightClickListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixin.BlockItemAccessor;
import net.wurstclient.util.BlockUtils;

public class PlaceThroughHack extends Hack implements RightClickListener {

    public PlaceThroughHack() {
        super("PlaceThrough", "Allows placing blocks at the opposite side");
        setCategory(Category.BLOCKS);
    }

    @Override
    public void onRightClick(RightClickEvent event) {
        ItemStack itemStack = MC.player.inventory.getMainHandStack();
        if (!(itemStack.getItem() instanceof BlockItem)) {
            return;
        }

        BlockHitResult targetBlock = getTargetBlock();
        if (targetBlock == null) {
            return;
        }

        if (canPlaceAgainst(itemStack, targetBlock)) {
            return;
        }

        Direction oppositeSide = targetBlock.getSide().getOpposite();
        BlockPos oppositeBlock = targetBlock.getBlockPos().offset(oppositeSide);

        if (!BlockUtils.getState(oppositeBlock).getMaterial().isReplaceable()) {
            return;
        }

        Vec3d posVec = Vec3d.ofCenter(oppositeBlock);
        Vec3d dirVec = Vec3d.of(oppositeSide.getVector());
        Vec3d hitVec = posVec.add(dirVec.multiply(0.5));
        IMC.getInteractionManager().rightClickBlock(oppositeBlock, oppositeSide, hitVec);
        MC.player.swingHand(Hand.MAIN_HAND);
        IMC.setItemUseCooldown(4);

        event.cancel();
    }

    private boolean canPlaceAgainst(ItemStack itemStack, BlockHitResult targetBlock) {
        ItemUsageContext usageContext = new ItemUsageContext(MC.player, Hand.MAIN_HAND, targetBlock);
        CachedBlockPosition cachedPosition = new CachedBlockPosition(usageContext.getWorld(), targetBlock.getBlockPos(), false);
        ItemPlacementContext placementContext = new ItemPlacementContext(usageContext);
        BlockState targetBlockState = cachedPosition.getBlockState();
        return ((BlockItemAccessor) itemStack.getItem()).canPlace(placementContext, targetBlockState);
    }

    private BlockHitResult getTargetBlock() {
        HitResult hitResult = MC.crosshairTarget;
        if (hitResult == null || hitResult.getPos() == null || hitResult.getType() != HitResult.Type.BLOCK || !(hitResult instanceof BlockHitResult)) {
            return null;
        }

        return (BlockHitResult) hitResult;
    }

    @Override
    public void onEnable() {
        EVENTS.add(RightClickListener.class, this);
    }

    @Override
    public void onDisable() {
        EVENTS.remove(RightClickListener.class, this);
    }
}
