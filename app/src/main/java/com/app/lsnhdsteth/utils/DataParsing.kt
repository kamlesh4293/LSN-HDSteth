package com.app.lsnhdsteth.utils

import com.app.lsnhdsteth.model.ResponseJsonData

class DataParsing {

    companion object{

        fun getInactiveItems(data_obj: ResponseJsonData) :MutableList<String>{
            var items: MutableList<String> = mutableListOf()
            var layout = data_obj.layout.get(0)
            if (layout.inactive != null && layout.inactive?.size > 0) {
                var inactive = layout.inactive
                for (i in 0..inactive.size - 1) {
                    if (inactive.get(i).item != null && inactive.get(i).item.size > 0) {
                        var items_array = inactive.get(i).item
                        for (j in 0..items_array.size - 1) {
                            items.add(items_array[j].fileName)
                        }
                    }
                }
            }
            return items
        }

    }


}