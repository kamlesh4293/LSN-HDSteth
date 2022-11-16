package com.app.lsnhdsteth.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.app.lsnhdsteth.model.Downloadable
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DataManager {


    companion object{

        val APP_DIRECTORY =  "LSN-HDSteth"
        val APP_SDSTETH_DIRECTORY =  "HDSteth"
        val APP_SCREEN_DIRECTORY =  APP_DIRECTORY+"/Screen"
        val APP_REPORT_DIRECTORY =  APP_DIRECTORY+"/Report"

        @JvmStatic
        fun getDirectory():String?{
            var file = MyApplication.applicationContext().getExternalFilesDir(APP_DIRECTORY)
            return file?.absolutePath
        }

        fun getScreenShotDirectory():String?{
            var file = MyApplication.applicationContext().getExternalFilesDir(APP_SCREEN_DIRECTORY)
            return file?.absolutePath
        }

        fun getReportDirectory():String?{
            var file = MyApplication.applicationContext().getExternalFilesDir(APP_REPORT_DIRECTORY)
            return file?.absolutePath
        }

        fun getReportFileList():Array<File> {
            var file = MyApplication.applicationContext().getExternalFilesDir(APP_REPORT_DIRECTORY)
            return file?.listFiles()!!
        }


        fun fileIsExist(downloadable: Downloadable):Boolean{
            var path = getDirectory()+File.separator+downloadable.name
            val file = File(path)
            return file.exists()
        }

        fun deleteFile(filename:String){
            var path = getDirectory()+File.separator+filename
            val file = File(path)
            if(file.exists())file.delete()
        }

        fun getAllDirectoryFiles() : Array<File>{
            val path = getDirectory()
            Log.d("DM Files", "Path: $path")
            val directory = File(path)
            val files = directory.listFiles()
//            Log.d("DM Files", "Size: " + files.size)
            return files
        }

        fun getListDownloadable (downloadable: List<Downloadable>?) : MutableList<String>{
            var list = mutableListOf<String>()
            if(downloadable!=null && downloadable.size>0){
                for (i in 0..downloadable.size-1)list.add(downloadable[i].name)
            }
            return list
        }

        @JvmStatic
        fun zipFolder(toZipFolder: File): File? {
            val ZipFile = File(toZipFolder.parent, String.format("%s.zip", toZipFolder.name))
//            val ZipFile = File(toZipFolder.parent, String.format("%s.zip", Utility.getOfflineZipFileName()))
            return try {
                val out = ZipOutputStream(FileOutputStream(ZipFile))
                zipSubFolder(out, toZipFolder, toZipFolder.path.length)
                out.close()
                ZipFile
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
        }

        @JvmStatic
        fun zipFolderOffline(toZipFolder: File): File? {
//            val ZipFile = File(toZipFolder.parent, String.format("%s.zip", toZipFolder.name))
            val ZipFile = File(toZipFolder.parent, String.format("%s.zip", Utility.getOfflineZipFileName()))
            return try {
                val out = ZipOutputStream(FileOutputStream(ZipFile))
                zipSubFolder(out, toZipFolder, toZipFolder.path.length)
                out.close()
                ZipFile
            } catch (ex: Exception) {
                ex.printStackTrace()
                null
            }
        }

        @JvmStatic
        @Throws(IOException::class)
        private fun zipSubFolder(out: ZipOutputStream, folder: File, basePathLength: Int) {
            val BUFFER = 2048
            val fileList = folder.listFiles()
            var origin: BufferedInputStream? = null
            for (file in fileList) {
                if (file.isDirectory) {
                    zipSubFolder(out, file, basePathLength)
                } else {
                    val data = ByteArray(BUFFER)
                    val unmodifiedFilePath = file.path
                    val relativePath = unmodifiedFilePath.substring(basePathLength + 1)
                    val fi = FileInputStream(unmodifiedFilePath)
                    origin = BufferedInputStream(fi, BUFFER)
                    val entry = ZipEntry(relativePath)
                    entry.time = file.lastModified() // to keep modification time after unzipping
                    out.putNextEntry(entry)
                    var count: Int
                    while (origin.read(data, 0, BUFFER).also { count = it } != -1) {
                        out.write(data, 0, count)
                    }
                    origin.close()
                    out.closeEntry()
                }
            }
        }

        // create file for saved data
        fun createReportFile(pref: MySharePrefernce?) {
            var data = pref?.getStoreReportdata()
            if(!data.equals("")){
                var filename = System.currentTimeMillis().toString()+".csv"
                try {
                    val gpxfile = File(getReportDirectory(), filename)
                    val writer = FileWriter(gpxfile)
                    writer.append(data.toString().trim())
                    writer.flush()
                    writer.close()
                    pref?.clearReportdata()
                } catch (e: Exception) {
                }
            }else{
                Log.d("TAG", "ReportFile: No data For create File")
            }
        }
    }


}