package xox.labvorty.weaversparadise.compat.epicfight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xox.labvorty.weaversparadise.items.armor.ModelReplacer;
import xox.labvorty.weaversparadise.compat.shared.CosplayCurio;
import xox.labvorty.weaversparadise.compat.shared.AdditionalCosplayMeshCache;
import yesman.epicfight.api.client.event.types.render.AnimatedArmorTextureEvent;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(targets = "yesman.epicfight.client.renderer.patched.layer.WearableItemLayer", remap = false)
public abstract class EpicFightWearableItemLayerMixin {
    @Shadow(remap = false)
    private void renderArmor(PoseStack poseStack, MultiBufferSource buffer, int packedLight, SkinnedMesh armorModel,
                             Armature armature, float red, float green, float blue, ResourceLocation texture,
                             OpenMatrix4f[] poseMatrices) {
    }

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
        return cosplayModel;
    }

    @Redirect(
            method = "renderLayer(Lyesman/epicfight/world/capabilities/entitypatch/LivingEntityPatch;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I[Lyesman/epicfight/api/utils/math/OpenMatrix4f;FFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lyesman/epicfight/client/renderer/patched/layer/WearableItemLayer;renderArmor(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILyesman/epicfight/api/client/model/SkinnedMesh;Lyesman/epicfight/api/model/Armature;FFFLnet/minecraft/resources/ResourceLocation;[Lyesman/epicfight/api/utils/math/OpenMatrix4f;)V"
            ),
            remap = false
    )
    private void weaversparadise$renderCosplayAdditionalMesh(
            WearableItemLayer<?, ?, ?, ?> wearableItemLayer,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            SkinnedMesh armorMesh,
            Armature armature,
            float red,
            float green,
            float blue,
            ResourceLocation texture,
            OpenMatrix4f[] poseMatrices,
            LivingEntityPatch<?> entityPatch,
            LivingEntity livingEntity,
            HumanoidArmorLayer<?, ?, ?> armorLayer,
            PoseStack layerPoseStack,
            MultiBufferSource layerBuffer,
            int layerPackedLight,
            OpenMatrix4f[] layerPoseMatrices,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            @Local(index = 30) int materialLayerIndex,
            @Local(index = 15) EquipmentSlot slot,
            @Local(index = 17) ItemStack itemStack,
            @Local(index = 19) ArmorItem armorItem,
            @Local(index = 21) HumanoidModel<?> defaultModel
    ) {
        this.renderArmor(poseStack, buffer, packedLight, armorMesh, armature, red, green, blue, texture, poseMatrices);

        if (materialLayerIndex != 0) {
            return;
        }

        CosplayCurio.Match match = CosplayCurio.find(livingEntity);
        if (match == null) {
            return;
        }

        HumanoidModel<?> additionalModel = match.replacer().getAdditionalModelForSlot(livingEntity, slot, defaultModel);
        if (additionalModel == null) {
            return;
        }

        SkinnedMesh additionalMesh = AdditionalCosplayMeshCache.getOrBake(match.stack().getItem(), slot, additionalModel);
        ResourceLocation additionalTexture = match.replacer().getAdditionalTextureForSlot(slot);
        var renderType = match.replacer().getAdditionalRenderTypeForSlot(slot, additionalTexture);
        if (renderType == null) {
            return;
        }

        var renderingData = match.replacer().getAdditionalRenderingDataForSlot(
                slot, itemStack, packedLight, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        org.joml.Vector4f color = yesman.epicfight.api.utils.ColorUtil.unpackToARGBF(renderingData.color());
        additionalMesh.draw(poseStack, buffer, renderType, Mesh.DrawingFunction.NEW_ENTITY,
                renderingData.light(), color.x, color.y, color.z, color.w, renderingData.overlay(), armature, poseMatrices);
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
