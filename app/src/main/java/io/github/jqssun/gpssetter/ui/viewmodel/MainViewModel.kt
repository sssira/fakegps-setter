package io.github.jqssun.gpssetter.ui.viewmodel

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jqssun.gpssetter.BuildConfig
import io.github.jqssun.gpssetter.R
import io.github.jqssun.gpssetter.repository.FavoriteRepository
import io.github.jqssun.gpssetter.room.Favorite
import io.github.jqssun.gpssetter.update.UpdateChecker
import io.github.jqssun.gpssetter.utils.PrefManager
import io.github.jqssun.gpssetter.utils.ext.onIO
import io.github.jqssun.gpssetter.utils.ext.onMain
import io.github.jqssun.gpssetter.utils.ext.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class MainViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val prefManger: PrefManager,
    private val checkUpdates: UpdateChecker,
    private val downloadManager: DownloadManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val getLat  = prefManger.getLat
    val getLng  = prefManger.getLng
    val isStarted = prefManger.isStarted
    val mapType = prefManger.mapType

    private val _allFavList = MutableStateFlow<List<Favorite>>(emptyList())
    val allFavList : StateFlow<List<Favorite>> =  _allFavList
    
    fun doGetUserDetails(){
        onIO {
            favoriteRepository.getAllFavorites
                .catch { e ->
                    Timber.tag("Error").d(e.message.toString())
                }
                .collectLatest {
                    _allFavList.emit(it)
                }
        }
    }

    fun update(start: Boolean, la: Double, ln: Double)  {
        prefManger.update(start,la,ln)
    }

    private val _response = MutableLiveData<Long>()
    val response: LiveData<Long> = _response

    private fun insertNewFavorite(favorite: Favorite) = onIO {
        _response.postValue(favoriteRepository.addNewFavorite(favorite))
    }

    val isXposed = MutableLiveData<Boolean>(false)

    fun deleteFavorite(favorite: Favorite) = onIO {
        favoriteRepository.deleteFavorite(favorite)
    }

    private fun getFavoriteSingle(i : Int) : Favorite? {
        return try { favoriteRepository.getSingleFavorite(i.toLong()) } catch(e: Exception) { null }
    }

    private val _update = MutableStateFlow<UpdateChecker.Update?>(null)
    val update = _update.asStateFlow()

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                checkUpdates.clearCachedDownloads(context)
            }
            checkUpdates.getLatestRelease().collect {
                _update.emit(it)
            }
        }
    }

    fun getAvailableUpdate(): UpdateChecker.Update? = _update.value

    fun clearUpdate() {
        viewModelScope.launch { _update.emit(null) }
    }

    private var requestId: Long? = null
    private var _downloadState = MutableStateFlow<State>(State.Idle)
    private var downloadFile: File? = null
    val downloadState = _downloadState.asStateFlow()

    fun startDownload(context: Context, update: UpdateChecker.Update) {
        if(_downloadState.value is State.Idle) {
            downloadUpdate(context, update.assetUrl, update.assetName)
        }
    }

    private val downloadStateReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            viewModelScope.launch {
                val query = DownloadManager.Query().setFilterById(requestId ?: return@launch)
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    if (status == DownloadManager.STATUS_SUCCESSFUL && downloadFile != null) {
                        val outputUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", downloadFile!!)
                        _downloadState.emit(State.Done(outputUri))
                    } else {
                        _downloadState.emit(State.Failed)
                    }
                }
                cursor.close()
            }
        }
    }

    private val downloadObserver = object: ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            viewModelScope.launch {
                val query = DownloadManager.Query().setFilterById(requestId ?: return@launch)
                val c = downloadManager.query(query)
                if (c.moveToFirst()) {
                    val size = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val downloaded = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                    if (size > 0) {
                        val progress = (downloaded * 100.0 / size).roundToInt()
                        _downloadState.emit(State.Downloading(progress))
                    }
                }
                c.close()
            }
        }
    }

    private fun downloadUpdate(context: Context, url: String, fileName: String) {
        viewModelScope.launch {
            val downloadFolder = File(context.externalCacheDir, "updates").apply { mkdirs() }
            downloadFile = File(downloadFolder, fileName)
            context.registerReceiver(downloadStateReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            context.contentResolver.registerContentObserver(Uri.parse("content://downloads/my_downloads"), true, downloadObserver)
            
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setDestinationUri(Uri.fromFile(downloadFile!!))
            }
            requestId = downloadManager.enqueue(request)
        }
    }

    fun openPackageInstaller(context: Context, uri: Uri){
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            context.showToast("Update failed")
        }
    }

    sealed class State {
        object Idle: State()
        data class Downloading(val progress: Int): State()
        data class Done(val fileUri: Uri): State()
        object Failed: State()
    }

    fun storeFavorite(address: String, lat: Double, lon: Double) = onIO {
        var slot = 0L
        while (getFavoriteSingle(slot.toInt()) != null) { slot++ }
        insertNewFavorite(Favorite(id = slot, address = address, lat = lat, lng = lon))
    }
}
