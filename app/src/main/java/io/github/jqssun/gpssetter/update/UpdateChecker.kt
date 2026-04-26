package io.github.jqssun.gpssetter.update

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UpdateChecker @Inject constructor() {
    data class Update(
        val version: String = "1.0",
        val assetUrl: String = "",
        val assetName: String = "app-release.apk"
    )

    companion object {
        const val TAG_NAME = "tag_name"
    }

    fun getLatestRelease(): Flow<Update?> = flow {
        emit(null)
    }

    fun clearCachedDownloads(context: Context) {
        // Build placeholder
    }
}
