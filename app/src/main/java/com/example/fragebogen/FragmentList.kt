package com.example.fragebogen

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.TextView
import androidx.core.view.get
import androidx.fragment.app.Fragment


class FragmentList(private var mas: ArrayList<ArrayList<ObjectQuestions>>, private var count:Int, private var contextA:Context) : Fragment() {

    companion object {
        fun newInstance(mas:ArrayList<ArrayList<ObjectQuestions>>, count: Int,contextA:Context) = FragmentList(mas, count,contextA)

    }

    private lateinit var masRandom:Array<Int>
    private lateinit var listView: ListView
    private var idMasRandom = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view =  inflater.inflate(R.layout.fragment_list_fragment, container, false)

        listView = view.findViewById(R.id.listView)
        masRandom = Array(mas.count(), {i->-1})
        generateRandomMas()
        generateListView()
        return view
    }

    private fun generateListView()
    {
        var i = masRandom[idMasRandom]
        listView.adapter = Adapter(mas[i], contextA)
        for(n in (1 until mas[i].count()))
        {
            if(!mas[i][n].editTextFlag)
            {
                listView[0].background = contextA.getDrawable(R.drawable.round_corner)
            }
            if(n == mas[i].count()-1)
            {
                listView[n].background = contextA.getDrawable(R.drawable.round_corner_orange)
            }
        }
    }

    private fun generateRandomMas() {
        var i = 0
        while (i<masRandom.count())
        {
            masRandom[i] = (0 until masRandom.count()).random()
            var j = 0
            while (j<i)
            {
                if(masRandom[i] == masRandom[j])
                {
                    i--
                    break
                }

                j++

            }
            i++
        }

    }
}
