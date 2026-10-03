package xox.labvorty.weaversparadise.compat.epicfight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xox.labvorty.weaversparadise.items.armor.ModelReplacer;
import xox.labvorty.weaversparadise.items.armor.RenderingData;
import yesman.epicfight.api.client.event.types.render.AnimatedArmorTextureEvent;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(targets = "yesman.epicfight.client.renderer.patched.layer.WearableItemLayer", remap = false)
public abstract class EpicFightWearableItemLayerMixin {
    @ModifyExpressionValue(
            method = "getArmorModel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/DefaultedRegistry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;",
                    ordinal = 0
            )
    )
    private ResourceLocation weaversparadise$useSeparateCosplayMeshCache(
            ResourceLocation armorItemKey,
            HumanoidArmorLayer<?, ?, ?> armorLayer,
            HumanoidModel<?> defaultArmorModel,
            Model armorModel,
            LivingEntity livingEntity
    ) {
        CosplayCurio.Match match = CosplayCurio.find(livingEntity);
        if (match == null) {
            return armorItemKey;
        }

        return BuiltInRegistries.ITEM.getKey(match.stack().getItem());
    }

    @Redirect(
            method = "renderLayer(Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I[Lyesman/epicfight/api/utils/math/OpenMatrix4f;FFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lyesman/epicfight/api/client/event/types/render/AnimatedArmorTextureEvent;getResultLocation()Lnet/minecraft/resources/ResourceLocation;"
            ),
            remap = false
    )
    private ResourceLocation weaversparadise$useCosplayTexture(AnimatedArmorTextureEvent event) {
        CosplayCurio.Match match = CosplayCurio.find(event.getLivingEntity());
        if (match != null) {
            return match.replacer().getTextureForSlot(event.getEquipmentSlot());
        }

        return event.getResultLocation();
    }

    @Redirect(
            method = "renderLayer(Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I[Lyesman/epicfight/api/utils/math/OpenMatrix4f;FFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/client/ClientHooks;getArmorModel(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/client/model/HumanoidModel;)Lnet/minecraft/client/model/Model;"
            ),
            remap = false
    )
    private Model weaversparadise$useCosplayModel(
            LivingEntity livingEntity,
            ItemStack itemStack,
            EquipmentSlot slot,
            HumanoidModel<?> defaultModel,
            LivingEntityPatch<?> entityPatch,
            LivingEntity target,
            HumanoidArmorLayer<?, ?, ?> armorLayer,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            OpenMatrix4f[] poseMatrices,
            float f1,
            float f2,
            float f3,
            float f4
    ) {
        CosplayCurio.Match match = CosplayCurio.find(livingEntity);
        if (match == null) {
            return ClientHooks.getArmorModel(livingEntity, itemStack, slot, defaultModel);
        }

        HumanoidModel<?> cosplayModel = match.replacer().getModelForSlot(livingEntity, slot, defaultModel);
        ClientHooks.copyModelProperties(defaultModel, cosplayModel);

        // Weavers Paradise draws wings and other emissive details as a separate
        // model layer. Epic Fight replaces the vanilla armor layer, so those
        // extra parts must be rendered here alongside the cosplay model.
        HumanoidModel<?> additionalModel = match.replacer().getAdditionalModelForSlot(livingEntity, slot, defaultModel);
        ResourceLocation additionalTexture = match.replacer().getAdditionalTextureForSlot(slot);
        RenderType additionalRenderType = match.replacer().getAdditionalRenderTypeForSlot(slot, additionalTexture);
        if (additionalModel != null && additionalRenderType != null) {
            ClientHooks.copyModelProperties(defaultModel, additionalModel);
            VertexConsumer vertexConsumer = buffer.getBuffer(additionalRenderType);
            RenderingData renderingData = match.replacer().getAdditionalRenderingDataForSlot(
                    slot, livingEntity.getItemBySlot(slot), packedLight, OverlayTexture.NO_OVERLAY);
            match.replacer().renderAdditionalLayerBySlot(slot, additionalModel, renderingData, poseStack, vertexConsumer);
        }

        return cosplayModel;
    }

    @ModifyExpressionValue(
            method = "getArmorModel",
            at = @At(
                    value = "INVOKE",
                    target = "Lyesman/epicfight/client/events/engine/RenderEngine;shouldRenderVanillaModel()Z",
                    remap = false
            ),
            remap = false
    )
    private boolean weaversparadise$bypassCachedArmorMesh(
            boolean shouldRenderVanillaModel,
            HumanoidArmorLayer<?, ?, ?> armorLayer,
            HumanoidModel<?> defaultArmorModel,
            Model armorModel,
            LivingEntity livingEntity
    ) {
        if (CosplayCurio.find(livingEntity) != null) {
            return true;
        }

        return shouldRenderVanillaModel;
    }
}
