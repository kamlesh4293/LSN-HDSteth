package com.app.lsnhdsteth.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.util.DisplayMetrics
import android.view.KeyEvent
import android.view.Window
import android.widget.TextView
import com.app.lsnhdsteth.R
import com.app.lsnhdsteth.utils.Utility

class DialogView {

    companion object{

        fun showNotRegisterDialog(ctx:Activity,device_id:String) : Dialog{
            var dialog: Dialog? = null
            val metrics = DisplayMetrics()
            ctx.getWindowManager().getDefaultDisplay().getMetrics(metrics)
            val yInches = metrics.heightPixels / metrics.ydpi
            val xInches = metrics.widthPixels / metrics.xdpi
            val diagonalInches = Math.sqrt((xInches * xInches + yInches * yInches).toDouble())
            if (dialog == null || dialog?.isShowing == false) {
                dialog = Dialog(ctx)
                dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog?.setCancelable(false)
                dialog?.setContentView(R.layout.dialog_not_register)
                dialog?.findViewById<TextView>(R.id.tv_dialog_deviceid)?.text = device_id
                dialog?.findViewById<TextView>(R.id.text_dia_desc)?.text =
                    if (diagonalInches >= 6.5) "This Device is not registered to the L Squared Hub." // 6.5inch device or bigger
                    else "This Device is not registered to the \nL Squared Hub."

                dialog?.findViewById<TextView>(R.id.tv_dialog_detail)
                    ?.setText(Utility.getDetailsText(), TextView.BufferType.SPANNABLE)
                dialog?.setOnKeyListener { arg0, keyCode, event -> // TODO Auto-generated method stub
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog?.dismiss()
                        ctx.finish()
                    }
                    true
                }
            }
            return dialog
        }

    }
}