package com.sands.rd.networkstatus

import android.content.Context
import android.net.*
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.net.HttpURLConnection
import java.net.URL
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


sealed class NetworkStatus {
    object Online : NetworkStatus()
    object Unavailable : NetworkStatus()
    object Lost : NetworkStatus()
    object NoInternet : NetworkStatus()

}
class NetworkStatusTracker @Inject constructor(@ApplicationContext context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    val networkStatus = callbackFlow {

        val networkStatusCallback = object : ConnectivityManager.NetworkCallback() {


            override fun onUnavailable() {
                println("networkStatusCallback onUnavailable")
                trySend(NetworkStatus.Unavailable).isSuccess
            }

            override fun onAvailable(network: Network) {
                println("networkStatusCallback onAvailable")


                val url = URL("https://www.google.com") // or your server address
                val conn: HttpURLConnection?

                try {

                    conn = url.openConnection() as HttpURLConnection
                    conn.setRequestProperty("Connection", "close")
                    conn.connectTimeout = 1000
                    println("conn.responseCode ->${conn.responseCode}")

                    val isOnline = conn.responseCode == 200
                    println("isOnline $isOnline")
                    trySend(NetworkStatus.Online).isSuccess


                } catch (E: Exception) {
                    E.printStackTrace()
                    println("e ->${E.message}")
                    trySend(NetworkStatus.NoInternet).isSuccess
                }

            }

            override fun onLost(network: Network) {
                println("networkStatusCallback onLost")
                trySend(NetworkStatus.Lost).isSuccess
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_BLUETOOTH)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_VPN)
            .build()

        connectivityManager.registerNetworkCallback(request, networkStatusCallback)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connectivityManager.requestNetwork(request, networkStatusCallback, 1000)
        } else {
            connectivityManager.requestNetwork(request, networkStatusCallback)
        }

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkStatusCallback)
        }
    }
        .flowOn(IO)


}
inline fun <Result> Flow<NetworkStatus>.map(
    crossinline onUnavailable: suspend () -> Result,
    crossinline onAvailable: suspend () -> Result,
    crossinline Lost: suspend () -> Result,
    crossinline NoNet: suspend () -> Result,
): Flow<Result> = map { status ->
    when (status) {

        NetworkStatus.Unavailable -> {
            println("inline Unavailable")
            onUnavailable()
        }
        NetworkStatus.Online -> {
            println("inline Available")
            onAvailable()
        }
        NetworkStatus.Lost -> {
            println("inline Lost")
            Lost()
        }
        NetworkStatus.NoInternet -> {
            println("inline NoNet")
            NoNet()
        }

    }
}





