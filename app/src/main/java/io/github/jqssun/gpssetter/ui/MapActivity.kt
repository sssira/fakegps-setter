package io.github.jqssun.gpssetter.ui

import android.os.Bundle
import androidx.activity.viewModels
import io.github.jqssun.gpssetter.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapActivity : BaseMapActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // BaseMapActivity sudah mengatur setContentView dan map fragment
        
        // Di sini nanti kita bisa tambahkan observer untuk location updates
    }
}
