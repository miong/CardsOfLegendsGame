package com.bubul.col.game.ui.utils

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.bubul.col.game.ui.getGameResource

class TextureBuilder {
    companion object {

        private val cache = mutableMapOf<String, TextureRegionDrawable>()

        fun getTextureRegionDrawable(resourcePath: String): TextureRegionDrawable {
            if (cache.containsKey(resourcePath))
                return cache[resourcePath]!!
            val res = TextureRegionDrawable(TextureRegion(Texture(getGameResource(resourcePath))))
            cache[resourcePath] = res
            return res
        }

        fun getColorFilledTextureRegionDrawable(width: Int, height: Int, color: Color): TextureRegionDrawable {
            val backPixmap = Pixmap(width, height, Pixmap.Format.RGBA8888)
            backPixmap.setColor(color)
            backPixmap.fill()
            return TextureRegionDrawable(TextureRegion(Texture(backPixmap)))
        }
    }
}