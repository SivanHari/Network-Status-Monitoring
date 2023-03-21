package com.sands.rd.networkstatus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
||/////////////////////////////////////////////||
||                                             ||
||                                             ||
||      Created by HP on 14-03-2023.           ||
||                                             ||
||                                             ||
||/////////////////////////////////////////////||


 */

sealed class MyState {
    object Online : MyState()
    object Error : MyState()
    object Lost : MyState()
    object NoInternet : MyState()
}
@HiltViewModel
class NetworkCheckViewModel @Inject constructor(networkStatusTracker: NetworkStatusTracker) : ViewModel() {

    val state =
        networkStatusTracker.networkStatus
            .map(
                onUnavailable = { MyState.Error },
                onAvailable = { MyState.Online },
                Lost = { MyState.Lost },
                NoNet = { MyState.NoInternet },
            ).asLiveData(Dispatchers.IO)

}