package com.app.lsnhdsteth.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.graphics.Point
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*
import android.os.Build.VERSION_CODES
import java.lang.Exception
import android.util.DisplayMetrics
import android.util.Log
import java.lang.StringBuilder
import android.net.wifi.WifiManager
import android.text.format.Formatter
import org.json.JSONObject

class DeviceInfo {

    companion object{

        // device id
        fun getDeviceId(ctx:Context):String{
            return Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)
        }

        // Screen resolution
        fun getDeviceResolution(activity: Activity):String{
            val display = activity.windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
//            val width = size.x
//            val height = size.y

            val metrics = DisplayMetrics()
            display.getMetrics(metrics)
            val width = metrics.widthPixels
            val height = metrics.heightPixels

            return "$width*$height"

        }

        // device name
        fun getDeviceName():String{
            return Build.MODEL;
        }

        // device ip address
        fun getLocalIpAddress(): String {
            try {
                val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf: NetworkInterface = en.nextElement()
                    val enumIpAddr: Enumeration<InetAddress> = intf.getInetAddresses()
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress: InetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress() && inetAddress is Inet4Address) {
                            return inetAddress.getHostAddress()
                        }
                    }
                }
            } catch (ex: SocketException) {
                ex.printStackTrace()
            }
            return ""
        }

        // total internal memory
        fun getTotalDiscSize(): Long {
            try{
                val stat = StatFs(Environment.getExternalStorageDirectory().path)
                val bytesAvailable: Long = if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
                    stat.blockSizeLong * stat.blockCountLong
                } else {
                    return 0
                }
                val megAvailable = bytesAvailable / 1024
                return megAvailable
            }catch (ex:RuntimeException){
                return 0
            }
        }

        // get available internal memory
        fun getUsedDiscSize(): Long {
            val stat = StatFs(Environment.getExternalStorageDirectory().path)
            val bytesAvailable: Long
            if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
                bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
            }else{
                return 0
            }
            val megAvailable = bytesAvailable / 1024
            return megAvailable
        }

        //  get RAM total size
        fun getTotalRAMSize(ctx: Context) : Long{
            val actManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            val availMemory = memInfo.availMem.toDouble()
            val totalMemory= memInfo.totalMem
            return totalMemory
        }

        //  get RAM  size
        fun getUsedRAMSize(ctx: Context) : Long{
            val actManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            val availMemory = memInfo.availMem
            return availMemory
        }

        fun getSerial(): String{
            try {
                if (Build.VERSION.SDK_INT >= VERSION_CODES.P)return Build.getSerial()
                else return Build.SERIAL
            }catch (ex : Exception){
                return ""
            }
        }

        fun getModelName(): String {
            return Build.DEVICE
        }

        fun getConnectedNetworkType(context: Context): String {
            val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            return networkInfo!!.typeName
        }

        fun getDeviceVersion() : String{
            return Build.VERSION.RELEASE.toString()
        }

        fun getWifiMacAddress(ctx:Context) : String{
            var ip_address = ""
            try {
                val wifiManager = ctx.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wInfo = wifiManager.connectionInfo
                ip_address = Formatter.formatIpAddress(wInfo.ipAddress)
                return ip_address
            }catch (ex: Exception){
                return ip_address
            }
        }

        fun deviceInfoData(ctx: Activity): JSONObject {
            val rootObject = JSONObject()
            rootObject.put("mac", getDeviceId(ctx))
            rootObject.put("app", com.app.lsnhdsteth.BuildConfig.VERSION_NAME)
            rootObject.put("watcher","")
            rootObject.put("os",7)
            rootObject.put("client",7)
            rootObject.put("res", getDeviceResolution(ctx))
            rootObject.put("computerName", getDeviceName())
            rootObject.put("local_addr", getLocalIpAddress())
            rootObject.put("appStart","")

            // info object
            val infoObject = JSONObject()
            infoObject.put("DiskTotal", getTotalDiscSize())
            infoObject.put("DiskUsed", getUsedDiscSize())
            infoObject.put("MemoryTotal", getTotalRAMSize(ctx))
            infoObject.put("MemoryUsed", getUsedRAMSize(ctx))
            infoObject.put("AndroidVersion", getDeviceVersion())
            infoObject.put("ModelSerialNumber", getSerial())
            infoObject.put("ModelName", getModelName())
            infoObject.put("ConnecteionType", getConnectedNetworkType(ctx))
            infoObject.put("WiFi-MAC", getWifiMacAddress(ctx))
            infoObject.put("AppType","HD Steth")

            rootObject.put("info",infoObject)
            return rootObject
        }



    }
}