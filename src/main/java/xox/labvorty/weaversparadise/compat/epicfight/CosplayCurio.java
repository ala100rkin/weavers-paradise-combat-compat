package xox.labvorty.weaversparadise.compat.epicfight;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import xox.labvorty.weaversparadise.items.armor.ModelReplacer;

final class CosplayCurio {
    record Match(ItemStack stack, ModelReplacer replacer) {
    }

    private CosplayCurio() {
    }

    static Match find(LivingEntity entity) {
        var handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return null;
        }

        for (var slotHandler : handler.getCurios().values()) {
            var stacks = slotHandler.getStacks();
            for (int slot = 0; slot < stacks.getSlots(); slot++) {
                ItemStack stack = stacks.getStackInSlot(slot);
                if (!stack.isEmpty() && stack.getItem() instanceof ModelReplacer replacer) {
                    return new Match(stack, replacer);
                }
            }
        }

        return null;
    }
}
