package vectorientation.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.FallingBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import vectorientation.main.Vectorientation;

@Mixin(FallingBlockEntityRenderer.class)
public class FallingBlockRendererMixin {
	@Inject(
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/block/BlockModelRenderer;render("
							+ "Lnet/minecraft/world/BlockRenderView;"
							+ "Lnet/minecraft/client/render/model/BakedModel;"
							+ "Lnet/minecraft/block/BlockState;"
							+ "Lnet/minecraft/util/math/BlockPos;"
							+ "Lnet/minecraft/client/util/math/MatrixStack;"
							+ "Lnet/minecraft/client/render/VertexConsumer;"
							+ "Z"
							+ "Ljava/util/Random;"
							+ "J"
							+ "I"
							+ ")Z"
					),
			method = "render(Lnet/minecraft/entity/FallingBlockEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
			)
	public void addRotation(FallingBlockEntity fallingBlockEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo info) {
		Vec3f vel = new Vec3f(fallingBlockEntity.getVelocity());
		float y = vel.getY();
		//if(Math.abs(y) > .04D * g) {
			y -= .04D * g;
			y *= .98D;
			//}
		float speed = Vectorientation.squetch ? (float) Math.sqrt(vel.getX() * vel.getX() + y * y + vel.getZ() * vel.getZ()) : 0;
		vel.normalize();
		float angle = (float) Math.acos((y));
		vel = new Vec3f(-1 * vel.getZ(), 0, vel.getX());
		vel.normalize();
		Quaternion rot = new Quaternion(vel, -angle, false);
		matrixStack.translate(0.5D, 0.5D, 0.5D);
		matrixStack.multiply(rot);
		if(Vectorientation.squetch) {
			speed += 0.75f;
			matrixStack.scale(1/speed, speed, 1/speed);
		}
		matrixStack.translate(-0.5D, -0.5D, -0.5D);
	}
}
