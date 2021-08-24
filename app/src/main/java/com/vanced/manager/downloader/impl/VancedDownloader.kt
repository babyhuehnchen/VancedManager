package com.vanced.manager.downloader.impl

import com.vanced.manager.domain.model.App
import com.vanced.manager.downloader.api.VancedAPI
import com.vanced.manager.downloader.base.AppDownloader
import com.vanced.manager.installer.impl.VancedInstaller
import com.vanced.manager.preferences.holder.managerVariantPref
import com.vanced.manager.preferences.holder.vancedLanguagesPref
import com.vanced.manager.preferences.holder.vancedThemePref
import com.vanced.manager.preferences.holder.vancedVersionPref
import com.vanced.manager.ui.viewmodel.MainViewModel
import com.vanced.manager.util.arch
import com.vanced.manager.util.getLatestOrProvidedAppVersion
import com.vanced.manager.util.log

class VancedDownloader(
    vancedInstaller: VancedInstaller,
    private val vancedAPI: VancedAPI,
) : AppDownloader(
    appName = "vanced",
    appInstaller = vancedInstaller
) {

    private val theme by vancedThemePref
    private val version by vancedVersionPref
    private val variant by managerVariantPref
    private val languages by vancedLanguagesPref

    override suspend fun download(
        app: App,
        viewModel: MainViewModel
    ) {
        val correctVersion = getLatestOrProvidedAppVersion(
            version = version,
            app = app
        )
        val files = listOf(
            getFile(
                type = "Theme",
                apkName = "$theme.apk",
                version = correctVersion
            ),
            getFile(
                type = "Arch",
                apkName = "split_config.$arch.apk",
                version = correctVersion
            )
        ) + languages.map { language ->
            getFile(
                type = "Language",
                apkName = "split_config.$language.apk",
                version = correctVersion
            )
        }
        downloadFiles(
            files = files,
            viewModel,
            folderStructure = "$correctVersion/$variant",
            onError = {
                log("error", it)
            }
        )
    }

    private fun getFile(
        type: String,
        apkName: String,
        version: String,
    ) = File(
        call = vancedAPI.getFiles(
            version = version,
            variant = variant,
            type = type,
            apkName = apkName
        ),
        fileName = apkName
    )

}