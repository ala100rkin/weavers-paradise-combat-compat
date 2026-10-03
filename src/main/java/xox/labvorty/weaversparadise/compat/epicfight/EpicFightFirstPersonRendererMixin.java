package xox.labvorty.weaversparadise.compat.epicfight;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xox.labvorty.weaversparadise.items.armor.ModelReplacer;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.FirstPersonRenderer;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

@Mixin(value = FirstPersonRenderer.class, remap = false)
public abstract class EpicFightFirstPersonRendererMixin {
    @Redirect(
            method = "render(Lnet/minecraft/client/player/LocalPlayer;Lyesman/epicfight/client/world/capabilites/entitypatch/player/LocalPlayerPatch;Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;IF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lyesman/epicfight/api/client/model/SkinnedMesh$SkinnedMeshPart;setHidden(Z)V"
            )
    )
    private void weaversparadise$hideSkinSleevesForCosplay(
            SkinnedMesh.SkinnedMeshPart part,
            boolean hidden,
            LocalPlayer player,
            LocalPlayerPatch playerPatch,
            LivingEntityRenderer<LocalPlayer, PlayerModel<LocalPlayer>> entityRenderer,
            MultiBufferSource buffer,
            PoseStack poseStack,
            int packedLight,
            float partialTick
    ) {
        if (CosplayCurio.find(player) != null) {
            HumanoidMesh mesh = (HumanoidMesh) ((FirstPersonRenderer) (Object) this)
                    .getMeshProvider(playerPatch).get();
            if (part == mesh.leftSleeve || part == mesh.rightSleeve) {
                hidden = true;
            }
        }

        part.setHidden(hidden);
    }
}
