package com.example.fragebogen

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.marginTop

class Adapter(items:ArrayList<String>, context: Context)
    :ArrayAdapter<String>(context,R.layout.fragment_listview, items){

    private lateinit var view: View
    private lateinit var textView:TextView

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        view = LayoutInflater.from(context).inflate(R.layout.fragment_listview, parent, false)

        textView = view.findViewById(R.id.textView)
        var text = getItem(position)
        if(text?.get(0) == '+')
        {
            text = text.substring(1)
        }
        textView.text = text
        if(position == count-1)
        {
            view.background = context.getDrawable(R.drawable.round_corner_orange)
            textView.setTextColor(Color.WHITE)
            textView.setPadding(0,30,0,30)
            textView.textSize = 30F
        }else if(position!=0)
        {
            view.background = context.getDrawable(R.drawable.round_corner)
        }



        return view
    }


}