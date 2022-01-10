package com.bubul.col.game.ui.utils

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable
import com.bubul.col.game.ui.getGameResource

class DecomposedGifSprite(val frames: Array<Texture>, val fps: Float, val delay: Float, val repeat: Boolean) :
    Sprite(frames[0]) {
    private var currentFrame = 0
    private var timer = 0f
    private var millisLastRender: Long = 0
    private var renderedOnce: Boolean = false
    private var waited: Boolean = false

    override fun draw(batch: Batch?) {
        if (renderedOnce) {
            if (!waited) {
                if (timer < delay) {
                    timer += (System.currentTimeMillis() - millisLastRender) / 1000.0f
                } else {
                    timer = 0f
                    waited = true
                }
            } else {
                if (timer < 1.0 / fps)
                    timer += (System.currentTimeMillis() - millisLastRender) / 1000.0f
                else {
                    timer = 0f
                    nextFrame();
                }
            }
        } else {
            texture = frames[currentFrame]
        }
        millisLastRender = System.currentTimeMillis()
        renderedOnce = true
        super.draw(batch)
    }

    private fun nextFrame() {
        // Change frame
        if (repeat)
            if (currentFrame < frames.size - 1) currentFrame++ else currentFrame = 0
        else
            if (currentFrame < frames.size - 1) currentFrame++ else currentFrame = frames.size - 1
        texture = frames[currentFrame]
    }

    fun resetTimer() {
        renderedOnce = false
        waited = false
        timer = 0f
        currentFrame = 0
        texture = frames[currentFrame]
    }
}

fun loadDecomposedGif(resDecomposedDirPath: String, baseName: String, numberOfFrames: Int, fps: Float): SpriteDrawable {
    val textures = Array<Texture>(numberOfFrames) {
        Texture(getGameResource("$resDecomposedDirPath/$baseName-$it.png"))
    }
    return SpriteDrawable(DecomposedGifSprite(textures, fps, 0f, true))
}

fun loadDecomposedGif(
    resDecomposedDirPath: String,
    baseName: String,
    numberOfFrames: Int,
    fps: Float,
    delay: Float,
    repeat: Boolean
): SpriteDrawable {
    val textures = Array<Texture>(numberOfFrames) {
        Texture(getGameResource("$resDecomposedDirPath/$baseName-$it.png"))
    }
    return SpriteDrawable(DecomposedGifSprite(textures, fps, delay, repeat))
}