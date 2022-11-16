package com.app.lsnhdsteth.ui

import com.app.lsnhdsteth.model.ResponseJsonData
import com.app.lsnhdsteth.utils.Constant
import com.app.lsnhdsteth.utils.MySharePrefernce
import com.google.gson.Gson

class JsonDataUtility(var pref: MySharePrefernce?) {

    fun getItemSize():Int{
        var item_size = 0
        var response = pref?.getJsonData()
        if(response.equals("")) return 0
        var data_obj = Gson().fromJson(response, ResponseJsonData::class.java)
        if(data_obj.layout == null) return 0
        if(data_obj.layout[0].frame == null) return 0
        var frames = data_obj.layout[0].frame
        if(frames != null && frames.size>0){
            for (i in 0..frames.size-1){
                var items = frames[i].item
                if(items!=null && items.size>0){
                    for (j in 0..items.size-1) item_size = item_size+1
                }
            }
        }
        return item_size
    }

    fun isSsItemHdSteth():Boolean{
        var response = pref?.getJsonData()
        if(response.equals("")) return false
        var data_obj = Gson().fromJson(response, ResponseJsonData::class.java)
        if(data_obj.layout == null) return false
        if(data_obj.layout[0].frame == null) return false
        var frames = data_obj.layout[0].frame
        if(frames != null && frames.size>0){
            for (i in 0..frames.size-1){
                var items = frames[i].item
                if(items!=null && items.size>0){
                    for (j in 0..items.size-1){
                        if(items[j].type.equals(Constant.CONTENT_HD_STETH)){
                            if (items[j].ss == null) return false
                            if (items[j].ss != null && items[j].ss.size>0) return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun isItemHdSteth(pref: MySharePrefernce?):Boolean{
        var response = pref?.getJsonData()
        if(response.equals("")) return false
        var data_obj = Gson().fromJson(response, ResponseJsonData::class.java)
        if(data_obj.layout == null) return false
        if(data_obj.layout[0].frame == null) return false
        var frames = data_obj.layout[0].frame
        if(frames != null && frames.size>0){
            for (i in 0..frames.size-1){
                var items = frames[i].item
                if(items!=null && items.size>0){
                    for (j in 0..items.size-1){
                        if(items[j].type.equals(Constant.CONTENT_HD_STETH))return true
                    }
                }
            }
        }
        return false
    }

    fun isDeviceNotRegistered():Boolean{
        var response = pref?.checkRefreshData()
        return response.equals(Constant.REFRESH_FROM_NODEVICE)
    }

}