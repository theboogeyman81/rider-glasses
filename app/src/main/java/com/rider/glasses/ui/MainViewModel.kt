package com.rider.glasses.ui

import androidx.lifecycle.ViewModel
import com.rider.glasses.glasses.GlassesConnectionState
import com.rider.glasses.glasses.GlassesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val glassesManager: GlassesManager
) : ViewModel() {

    val glassesState: StateFlow<GlassesConnectionState> = glassesManager.state

    fun onTap() {}
    fun onHold() {}
}
