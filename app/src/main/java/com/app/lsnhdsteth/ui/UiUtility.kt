package com.app.lsnhdsteth.ui

import android.content.Context
import android.graphics.Color
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.VideoView
import com.app.lsnhdsteth.model.Item
import com.app.lsnhdsteth.model.LayoutView
import com.app.lsnhdsteth.model.ResponseJsonData
import com.app.lsnhdsteth.utils.Constant
import com.app.lsnhdsteth.utils.MySharePrefernce
import com.app.lsnhdsteth.utils.Utility
import com.google.gson.Gson

class UiUtility(var pref: MySharePrefernce,var ctx: Context) {

    // get list of Frames
    fun getFrames():MutableList<LayoutView> {

        var list = mutableListOf<LayoutView>()

        var response = pref?.getJsonData()
        if(response == null || response.equals("")) return list
        var data_obj = Gson().fromJson(response, ResponseJsonData::class.java)
        if(data_obj.layout == null) return list
        var layout = data_obj.layout.get(0)
        if(layout.frame==null || layout.frame?.size==0) return list
        var list_frame = Utility.getFilterFrameList(layout.frame)

        for (i in 0..list_frame.size - 1) {

            var rl = RelativeLayout(ctx)

            val params = RelativeLayout.LayoutParams(list_frame.get(i).w,list_frame.get(i).h)
            rl.layoutParams = params
            rl.x = list_frame.get(i).x.toFloat()
            rl.y = list_frame.get(i).y.toFloat()


            var screen_rl = RelativeLayout(ctx)
            screen_rl.layoutParams = params
            screen_rl.x = list_frame.get(i).x.toFloat()
            screen_rl.y = list_frame.get(i).y.toFloat()

            if(list_frame.get(i).bg.equals(""))rl.setBackgroundColor(Color.TRANSPARENT)
            else rl.setBackgroundColor(Color.parseColor(list_frame.get(i).bg))


            var video = VideoView(ctx)
            video.layoutParams = params
            video.visibility = View.GONE

            var image = ImageView(ctx)
            image.layoutParams = params
            image.visibility = View.GONE

            var video_image = ImageView(ctx)
            image.layoutParams = params
            image.visibility = View.GONE

            var myMediaMetadataRetriever = MediaMetadataRetriever()


            var web = WebView(ctx)
            web.layoutParams = params
            web.getSettings().setJavaScriptEnabled(true)
            web.setWebViewClient(WebViewClient())
            web.visibility = View.GONE

            rl.addView(video)
            rl.addView(image)
            rl.addView(web)
            rl.addView(video_image)

            // screenshot frame
            var screen_image = ImageView(ctx)
            screen_image.layoutParams = params

            screen_rl.addView(screen_image)
            list.add(LayoutView(rl,image,video_image,video,web,myMediaMetadataRetriever))
        }
        return list
    }


}