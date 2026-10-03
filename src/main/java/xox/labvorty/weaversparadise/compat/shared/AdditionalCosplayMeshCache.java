package xox.labvorty.weaversparadise.compat.shared;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AdditionalCosplayMeshCache {
    private static final Map<Key, SkinnedMesh> MESHES = new ConcurrentHashMap<>();

    private AdditionalCosplayMeshCache() {
    }

    public static SkinnedMesh getOrBake(Item item, EquipmentSlot slot, HumanoidModel<?> model) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
        return MESHES.computeIfAbsent(new Key(itemId, slot), ignored ->
                HumanoidModelBaker.VANILLA_TRANSFORMER.transformArmorModel(model));
    }

    private record Key(ResourceLocation item, EquipmentSlot slot) {
    }
}
