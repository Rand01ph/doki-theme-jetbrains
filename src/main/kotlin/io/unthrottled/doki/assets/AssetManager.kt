package io.unthrottled.doki.assets

import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.diagnostic.Logger
import io.unthrottled.doki.assets.LocalAssetService.hasAssetChanged
import io.unthrottled.doki.assets.LocalStorageService.createDirectories
import io.unthrottled.doki.assets.LocalStorageService.getGlobalAssetDirectory
import io.unthrottled.doki.assets.LocalStorageService.getLocalAssetDirectory
import io.unthrottled.doki.util.toOptional
import org.apache.commons.io.IOUtils
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.Optional
import java.util.concurrent.TimeUnit

enum class AssetCategory(val category: String) {
  STICKERS("stickers"), BACKGROUNDS("backgrounds"), PROMOTION("promotion")
}

object HttpClientFactory {
  fun createHttpClient(): CloseableHttpClient =
    HttpClients.custom()
      .setUserAgent(ApplicationInfo.getInstance()?.fullApplicationName)
      .build()
}

object AssetManager {
  const val ASSET_SOURCE = "https://doki.assets.unthrottled.io"
  const val FALLBACK_ASSET_SOURCE = "https://raw.githubusercontent.com/doki-theme/doki-theme-assets/master"

  private val httpClient = HttpClientFactory.createHttpClient()
  private val log = Logger.getInstance(this::class.java)

  /**
   * Will return a resolvable URL that can be used to reference an asset.
   * If the asset was able to be downloaded on the local machine it will return a
   * file:// url to the local asset. If it was not able to get the asset then it
   * will return empty if the asset is not available locally.
   */
  fun resolveAssetUrl(assetCategory: AssetCategory, assetPath: String): Optional<String> =
    cachedResolve(assetCategory, assetPath, ASSET_SOURCE)
      .map { it.toOptional() }
      .orElseGet {
        cachedResolve(assetCategory, assetPath, FALLBACK_ASSET_SOURCE)
      }

  /**
   * Works just like <code>resolveAssetUrl</code> except that it will always
   * download the remote asset.
   */
  fun forceResolveAssetUrl(assetCategory: AssetCategory, assetPath: String): Optional<String> =
    forceResolve(assetCategory, assetPath, ASSET_SOURCE)
      .map { it.toOptional() }
      .orElseGet {
        forceResolve(assetCategory, assetPath, FALLBACK_ASSET_SOURCE)
      }

  private fun cachedResolve(
    assetCategory: AssetCategory,
    assetPath: String,
    assetSource: String
  ): Optional<String> =
    resolveAsset(assetCategory, assetPath, assetSource) { localAssetPath, remoteAssetUrl ->
      resolveTheAssetUrl(localAssetPath, remoteAssetUrl)
    }

  private fun forceResolve(
    assetCategory: AssetCategory,
    assetPath: String,
    assetSource: String
  ): Optional<String> =
    resolveAsset(assetCategory, assetPath, assetSource) { localAssetPath, remoteAssetUrl ->
      downloadAndGetAssetUrl(localAssetPath, remoteAssetUrl)
    }

  private fun resolveAsset(
    assetCategory: AssetCategory,
    assetPath: String,
    assetSource: String,
    resolveAsset: (Path, String) -> Optional<String>
  ): Optional<String> =
    constructLocalAssetPath(assetCategory, assetPath)
      .toOptional()
      .flatMap {
        val remoteAssetUrl = constructRemoteAssetUrl(
          assetCategory, assetPath, assetSource
        )
        resolveAsset(it, remoteAssetUrl)
      }

  private fun constructRemoteAssetUrl(
    assetCategory: AssetCategory,
    assetPath: String,
    assetSource: String
  ): String = when (assetCategory) {
    AssetCategory.STICKERS -> "$assetSource/${assetCategory.category}/jetbrains$assetPath"
    else -> "$assetSource/${assetCategory.category}/$assetPath"
  }

  private fun resolveTheAssetUrl(localAssetPath: Path, remoteAssetUrl: String): Optional<String> =
    when {
      hasAssetChanged(localAssetPath, remoteAssetUrl) ->
        downloadAndGetAssetUrl(localAssetPath, remoteAssetUrl)
      Files.exists(localAssetPath) ->
        localAssetPath.toUri().toString().toOptional()
      else -> Optional.empty()
    }

  fun constructLocalAssetPath(
    assetCategory: AssetCategory,
    assetPath: String
  ): Path =
    Paths.get(
      getLocalAssetDirectory(), assetCategory.category, assetPath
    ).normalize().toAbsolutePath()

  fun constructGlobalAssetPath(
    assetCategory: AssetCategory,
    assetPath: String
  ): Optional<Path> =
    getGlobalAssetDirectory()
      .map {
        Paths.get(
          it, assetCategory.category, assetPath
        )
      }

  private fun downloadAndGetAssetUrl(
    localAssetPath: Path,
    remoteAssetUrl: String
  ): Optional<String> {
    createDirectories(localAssetPath)
    val remoteAssetRequest = createGetRequest(remoteAssetUrl)
    return try {
      log.warn("Attempting to download asset $remoteAssetUrl")
      val remoteAssetResponse = httpClient.execute(remoteAssetRequest)
      if (remoteAssetResponse.statusLine.statusCode == 200) {
        remoteAssetResponse.entity.content.use { inputStream ->
          Files.newOutputStream(
            localAssetPath,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING
          ).use { bufferedWriter ->
            IOUtils.copy(inputStream, bufferedWriter)
          }
        }
        localAssetPath.toUri().toString().toOptional()
      } else {
        log.warn("Asset request for $remoteAssetUrl responded with $remoteAssetResponse")
        Optional.empty()
      }
    } catch (e: Throwable) {
      log.warn("Unable to get remote remote asset $remoteAssetUrl for raisins ${e.message}")
      Optional.empty()
    } finally {
      remoteAssetRequest.releaseConnection()
    }
  }

  private fun createGetRequest(remoteUrl: String): HttpGet {
    val remoteAssetRequest = HttpGet(remoteUrl)
    remoteAssetRequest.config = RequestConfig.custom()
      .setConnectTimeout(TimeUnit.MILLISECONDS.convert(5L, TimeUnit.SECONDS).toInt())
      .build()
    return remoteAssetRequest
  }
}
