package io.github.and3r817.ar.sceneform.example

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Light
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.util.concurrent.CompletableFuture


class MainFragment : ArFragment() {

    private var botHeadRenderable: ModelRenderable? = null

    private var botBodyRenderable: ModelRenderable? = null

    private var bubbleRenderable: ViewRenderable? = null

    private var botHeadNode: TransformableNode? = null

    private var bubbleNode: TransformableNode? = null

    private var ledAnimator: ValueAnimator? = null

    private val bubbleIntervalSec = 3L

    private var bubbleTime = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val botHeadFuture = ModelRenderable.builder()
                .setSource(requireContext(), Uri.parse("model-head.sfb"))
                .build()

        val botBodyFuture = ModelRenderable.builder()
                .setSource(requireContext(), Uri.parse("model-body.sfb"))
                .build()

        val bubbleFuture = ViewRenderable.builder()
                .setView(requireContext(), R.layout.layout_bubble)
                .build()

        CompletableFuture.allOf(botHeadFuture, botBodyFuture, bubbleFuture)
                .handle { _, _ ->
                    botHeadRenderable = botHeadFuture.get()
                    botBodyRenderable = botBodyFuture.get()
                    bubbleRenderable = bubbleFuture.get()
                }
                .exceptionally {
                    val toast = Toast.makeText(requireContext(), "Unable to load renderable", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                    null
                }

        setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, _: MotionEvent ->
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                return@setOnTapArPlaneListener
            }

            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arSceneView?.scene)

            val botNode = TransformableNode(transformationSystem)
            botNode.setParent(anchorNode)
            botNode.select()

            val cameraPosition = arSceneView?.scene?.camera?.worldPosition!!
            val headPosition = botNode.worldPosition
            val headDirection = Vector3.subtract(Vector3(cameraPosition.x, headPosition.y, cameraPosition.z), headPosition)
            botNode.worldRotation = Quaternion.lookRotation(headDirection, Vector3.up())

            botHeadNode = TransformableNode(transformationSystem)
            botHeadNode?.setParent(botNode)
            botHeadNode?.renderable = botHeadRenderable

            val localPosition = botHeadNode?.localPosition
            botHeadNode?.localPosition = Vector3(localPosition!!.x, 0.2316f, 0.0202f)

            val botBodyNode = TransformableNode(transformationSystem)
            botBodyNode.setParent(botNode)
            botBodyNode.renderable = botBodyRenderable

            val lensLight = Light.builder(Light.Type.POINT)
                    .setColor(com.google.ar.sceneform.rendering.Color(0.212f, 0.984f, 1f, 1f))
                    .setFalloffRadius(0.03f)
                    .build()

            val lensLightNode = TransformableNode(transformationSystem)
            lensLightNode.setParent(botHeadNode)
            lensLightNode.localPosition = Vector3(0.0f, 0.1016f, -0.1018f)
            lensLightNode.light = lensLight

            val ledLight = Light.builder(Light.Type.POINT)
                    .setColor(com.google.ar.sceneform.rendering.Color(Color.RED))
                    .setFalloffRadius(0.15f)
                    .build()

            val ledLightNode = TransformableNode(transformationSystem)
            ledLightNode.setParent(botHeadNode)
            ledLightNode.localPosition = Vector3(-0.0539f, 0.1916f, -0.0746f)
            ledLightNode.light = ledLight
            ledLightNode.select()

            bubbleNode = TransformableNode(transformationSystem)
            bubbleNode!!.setParent(botHeadNode)
            bubbleNode!!.localPosition = Vector3(0.2054f, 0.2221f, 0.0f)
            bubbleNode!!.renderable = bubbleRenderable
            bubbleNode!!.isEnabled = false

            ledAnimator = ObjectAnimator.ofFloat(ledLight, "intensity",
                    1.0E-4f, 100f)
            ledAnimator?.duration = 1000L
            ledAnimator?.repeatCount = ValueAnimator.INFINITE
            ledAnimator?.repeatMode = ValueAnimator.REVERSE
            ledAnimator?.start()
        }
    }

    override fun onUpdate(frameTime: FrameTime?) {
        super.onUpdate(frameTime)

        val cameraPosition = arSceneView?.scene?.camera?.worldPosition ?: return
        val headPosition = botHeadNode?.worldPosition ?: return
        val headDirection = Vector3.subtract(cameraPosition, headPosition)
        val headLookRotation = Quaternion.lookRotation(headDirection, Vector3.up())
        botHeadNode?.worldRotation = headLookRotation

        val frameTimeSec = frameTime!!.startSeconds.toLong()
        if (bubbleTime < frameTimeSec - bubbleIntervalSec) {
            bubbleNode!!.isEnabled = !bubbleNode!!.isEnabled
            bubbleTime = frameTimeSec
        }
    }
}
