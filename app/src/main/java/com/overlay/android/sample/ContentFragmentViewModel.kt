package com.overlay.android.sample

import androidx.lifecycle.ViewModel
import com.google.android.exoplayer2.Player

class ContentFragmentViewModel : ViewModel() {
    var playerCreated = false
    var player: Player? = null
}