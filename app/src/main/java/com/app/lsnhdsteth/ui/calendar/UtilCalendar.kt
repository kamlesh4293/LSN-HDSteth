package com.app.lsnhdsteth.ui.calendar

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class UtilCalendar {

    companion object{

        @JvmStatic
        fun getMonth(day:String,mon: Int) : MutableList<String> {

            var month = mon+1
            var cal = Calendar.getInstance()
//            val month = cal.get(Calendar.MONTH)
            var list = mutableListOf<String>()


            if(day.equals("SUNDAY")) for (i in 1..6) list.add("")
            if(day.equals("SATURDAY"))for (i in 1..5) list.add("")
            if(day.equals("FRIDAY"))for (i in 1..4) list.add("")
            if(day.equals("THURSDAY"))for (i in 1..3) list.add("")
            if(day.equals("WEDNESDAY"))for (i in 1..2) list.add("")
            if(day.equals("TUESDAY"))for (i in 1..1) list.add("")

            for (i in 1..28) list.add("$i")
            if(month == 4 ||month == 6 ||month == 9 ||month == 11){
                list.add("29")
                list.add("30")
            }
            if(month == 1 ||month == 3 ||month == 5 ||month == 7 ||month == 8 ||month == 10||month == 12 ){
                list.add("29")
                list.add("30")
                list.add("31")
            }
            for (i in list.size..34) list.add("")
            return list
        }

        @JvmStatic
        @RequiresApi(Build.VERSION_CODES.O)
        fun getFirstDay(year : Int,month: Int):String{
            var mon = (month+1).toString()
            if(mon.length==1) mon = "0$mon"
            var date = "01-$mon-$year"
            Log.d("TAG", "getFirstDay: Date - $date")
            val l = LocalDate.parse("01-$mon-$year", DateTimeFormatter.ofPattern("dd-MM-yyyy"))
            return l.dayOfWeek.toString()
        }

        fun getMonthName(no:Int) : String{
            var no = no+1
            if(no==1) return "January"
            else if(no==2) return "February"
            else if(no==3) return "March"
            else if(no==4) return "April"
            else if(no==5) return "May"
            else if(no==6) return "June"
            else if(no==7) return "July"
            else if(no==8) return "August"
            else if(no==9) return "September"
            else if(no==10) return "October"
            else if(no==11) return "November"
            else if(no==12) return "December"
            else return ""
        }


    }



}