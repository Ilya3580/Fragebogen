package com.example.fragebogen

import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.Fragment
import org.w3c.dom.Text


class FragmentList(private var mas: ArrayList<ArrayList<String>>, private var count:Int, private var contextA:Context) : Fragment() {

    companion object {
        fun newInstance(mas:ArrayList<ArrayList<String>>, count: Int,contextA:Context) = FragmentList(mas, count,contextA)

    }
    private lateinit var masBool:Array<Boolean>
    private lateinit var listView:ListView
    private lateinit var masRandom:Array<Int>
    private lateinit var textViewResult:TextView
    private var idMasRandom = 0
    private var flagContinue = false
    private var countAnswerTrue = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var view =  inflater.inflate(R.layout.fragment_list_fragment, container, false)

        textViewResult = view.findViewById(R.id.textViewResult)
        listView = view.findViewById(R.id.listView)
        masRandom = Array(mas.count(), {i ->-1})
        generateRandomMas()


        generateListView()

        return view
    }

    private fun generateListView(){
        var i = masRandom[idMasRandom]
        masBool = Array(mas[i].size, {i -> false})

        listView.adapter = Adapter(mas[i], contextA)

        listView.onItemClickListener = OnItemClickListener { parent, itemClicked, position, id ->
            if(position == mas[i].size-1)
            {
                if(flagContinue)
                {
                    if(count == idMasRandom+1)
                    {
                        showResult()
                    }else {
                        this.idMasRandom++
                        generateListView()
                        flagContinue = false
                    }

                }else {
                    onClickLastElement()
                    var textView = parent[position].findViewById<TextView>(R.id.textView)
                    textView.text = "Далее"
                    flagContinue = true
                }


            }else if(position != 0) {
                if(masBool[position]) {
                    itemClicked.background =
                        contextA.getDrawable(R.drawable.round_corner)
                    masBool[position] = false
                }else{
                    itemClicked.background =
                        contextA.getDrawable(R.drawable.round_corner_orange)
                    masBool[position] = true
                }
            }

        }
    }

    private fun generateRandomMas() {
        var i = 0
        while (i<masRandom.count())
        {
            masRandom[i] = (0..masRandom.count()-1).random()
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
    private fun onClickLastElement() {
        var i = 0
        var count = 0
        var countM = 0
        var flag = true
        while (i< mas[masRandom[idMasRandom]].count())
        {
            if(masBool[i])
            {
                count++
                listView[i].background =
                    contextA.getDrawable(R.drawable.roundcorner_red)
                if(mas[masRandom[idMasRandom]][i][0] != '+')
                {
                    flag = false
                }
            }
            if(mas[masRandom[idMasRandom]][i][0] == '+')
            {
                countM++
                if(!masBool[i])
                {
                    flag = false
                }
                listView[i].background =
                    contextA.getDrawable(R.drawable.roundcorner_green)

            }

            i++
        }
        if(count == countM && flag)
        {
            countAnswerTrue++
        }

    }
    private fun showResult(){
        listView.visibility = View.GONE
        textViewResult.visibility = View.VISIBLE
        textViewResult.text =  "Ваш результат: ${countAnswerTrue} из ${count}"
    }





}
