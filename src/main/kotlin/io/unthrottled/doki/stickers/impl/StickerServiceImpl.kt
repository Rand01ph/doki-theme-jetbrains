package io.unthrottled.doki.stickers.impl

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ApplicationManager.getApplication
import com.intellij.openapi.wm.impl.IdeBackgroundUtil
import io.unthrottled.doki.assets.AssetCategory
import io.unthrottled.doki.assets.AssetManager
import io.unthrottled.doki.stickers.StickerService
import io.unthrottled.doki.themes.Background
import io.unthrottled.doki.themes.DokiTheme
import io.unthrottled.doki.util.runSafely
import io.unthrottled.doki.util.toOptional
import java.util.Optional

const val DOKI_BACKGROUND_PROP: String = "io.unthrottled.doki.background"

const val DOKI_STICKER_PROP: String = "io.unthrottled.doki.stickers"
private const val PREVIOUS_STICKER = "io.unthrottled.doki.sticker.previous"

class StickerServiceImpl : StickerService {

  override fun activateForTheme(dokiTheme: DokiTheme) {
    listOf(
      {
        installSticker(dokiTheme)
      },
      {
        installBackgroundImage(dokiTheme)
      }
    ).forEach {
      getApplication().executeOnPooledThread(it)
    }
  }

  override fun checkForUpdates(dokiTheme: DokiTheme) {
    getApplication().executeOnPooledThread {
      getLocallyInstalledBackgroundImagePath(dokiTheme)
      getLocallyInstalledStickerPath(dokiTheme)
    }
  }

  private fun installSticker(dokiTheme: DokiTheme) =
    getLocallyInstalledStickerPath(dokiTheme)
      .ifPresent {
        setBackgroundImageProperty(
          it,
          "98",
          IdeBackgroundUtil.Fill.PLAIN.name,
          IdeBackgroundUtil.Anchor.BOTTOM_RIGHT.name,
          DOKI_STICKER_PROP
        )
      }

  private fun installBackgroundImage(dokiTheme: DokiTheme) =
    getLocallyInstalledBackgroundImagePath(dokiTheme)
      .ifPresent {
        setBackgroundImageProperty(
          it.first,
          "100",
          IdeBackgroundUtil.Fill.SCALE.name,
          it.second.position.name,
          DOKI_BACKGROUND_PROP
        )
      }

  private fun getLocallyInstalledBackgroundImagePath(
    dokiTheme: DokiTheme
  ): Optional<Pair<String, Background>> =
    dokiTheme.getBackground()
      .flatMap { background ->
        AssetManager.resolveAssetUrl(
          AssetCategory.BACKGROUNDS,
          background.name
        ).map { it to background }
      }

  private fun getLocallyInstalledStickerPath(
    dokiTheme: DokiTheme
  ): Optional<String> =
    dokiTheme.getStickerPath()
      .flatMap {
        AssetManager.resolveAssetUrl(
          AssetCategory.STICKERS,
          it
        )
      }

  override fun remove() {
    val propertiesComponent = PropertiesComponent.getInstance()
    val previousSticker = propertiesComponent.getValue(DOKI_STICKER_PROP, "")
    if (previousSticker.isNotEmpty()) {
      propertiesComponent.setValue(
        PREVIOUS_STICKER,
        previousSticker
      )
    }

    propertiesComponent.unsetValue(DOKI_STICKER_PROP)
    propertiesComponent.unsetValue(DOKI_BACKGROUND_PROP)
    repaintWindows()
  }

  private fun repaintWindows() = runSafely({
    IdeBackgroundUtil.repaintAllWindows()
  })

  override fun getPreviousSticker(): Optional<String> =
    PropertiesComponent.getInstance().getValue(PREVIOUS_STICKER).toOptional()

  override fun clearPreviousSticker() {
    PropertiesComponent.getInstance().unsetValue(PREVIOUS_STICKER)
  }
}

private fun setBackgroundImageProperty(
  imagePath: String,
  opacity: String,
  fill: String,
  anchor: String,
  propertyKey: String
) {
  // org.intellij.images.editor.actions.SetBackgroundImageDialog has all of the answers
  // as to why this looks this way
  val propertyValue = listOf(imagePath, opacity, fill, anchor)
    .reduceRight { a, b -> "$a,$b" }
  setPropertyValue(propertyKey, propertyValue)
}

private fun setPropertyValue(propertyKey: String, propertyValue: String) {
  PropertiesComponent.getInstance().unsetValue(propertyKey)
  PropertiesComponent.getInstance().setValue(propertyKey, propertyValue)
}
