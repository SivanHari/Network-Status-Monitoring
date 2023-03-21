package com.sands.rd.networkstatus

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

interface NetworkStatusI {
    fun getNetworkStatus(s: String)
}

@AndroidEntryPoint
open class BaseActivity : AppCompatActivity(), NetworkStatusI {
    private val networkCheckViewModel: NetworkCheckViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        networkCheckViewModel.state.observe(this) {
            when (it) {
                is MyState.Online -> {
                    getNetworkStatus("Online")
                }
                is MyState.Error -> {
                    getNetworkStatus("offline")
                }
                is MyState.Lost -> {
                    getNetworkStatus("Offline")
                }
                is MyState.NoInternet -> {
                    getNetworkStatus("No Network !!!")
                }
                else -> {
                    getNetworkStatus("Else Fired")
                }
            }
        }


    }
    override fun getNetworkStatus(s: String) {
        println("s =$s")
    }

}