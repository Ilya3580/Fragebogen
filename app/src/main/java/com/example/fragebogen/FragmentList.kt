package com.example.fragebogen

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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
    private lateinit var textView:TextView
    private var idMasRandom = 0
    private lateinit var booleanMas:Array<Boolean>
    private var blockButtons = false
    private var i = 0
    private var countTrueAnswer = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view =  inflater.inflate(R.layout.fragment_list_fragment, container, false)

        listView = view.findViewById(R.id.listView)
        textView = view.findViewById(R.id.textViewResult)
        masRandom = Array(mas.count(), {i->-1})


        generateRandomMas()
        generateListView()
        return view
    }

    private fun generateListView()
    {
        if(idMasRandom == mas.size)
        {
            showResult()
            return
        }
        i = masRandom[idMasRandom]
        blockButtons = false
        listView.adapter = Adapter(mas[i], contextA)
        booleanMas = Array(mas[i].count(), {i->false})
        listView.setOnItemClickListener{parent, view, position, id ->
            if(position == listView.count-1)
            {
                if(blockButtons)
                {
                    idMasRandom++
                    generateListView()
                }else{
                    val textView = view.findViewById<TextView>(R.id.textView)
                    textView.text = "Далее"
                    blockButtons = true
                    checkAnswer(mas[i].count() != 3)
                }


            }else if (!blockButtons && position !=0 && mas[i].count() != 3){
                if(booleanMas[position])
                {
                    view.background = contextA.getDrawable(R.drawable.round_corner)
                    booleanMas[position] = false
                }else{
                    view.background = contextA.getDrawable(R.drawable.round_corner_orange)
                    booleanMas[position] = true
                }
            }
        }

    }

    private fun showResult() {
        listView.visibility = View.GONE
        textView.visibility = View.VISIBLE
        textView.text = "Ваш резульат: ${countTrueAnswer} из ${mas.count()}"
    }


    private fun checkAnswer(flag:Boolean) {
        var flagAnswer = false
        if(flag)
        {
            flagAnswer = true
            for(j in (1 until mas[i].count()-1))
            {
                if(booleanMas[j] && !mas[i][j].trueAnswer || mas[i][j].trueAnswer && !booleanMas[j])
                {
                    flagAnswer = false
                    getViewByPosition(j, listView)?.background = contextA.getDrawable(R.drawable.roundcorner_red)
                }
                if(mas[i][j].trueAnswer)
                {
                    getViewByPosition(j, listView)?.background = contextA.getDrawable(R.drawable.roundcorner_green)
                }
            }
        }else{
            val editText = getViewByPosition(1, listView)?.findViewById<EditText>(R.id.editText)
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
            editText?.isFocusable = false
            editText?.isLongClickable = false
            val textUser = editText?.text
            var trueAnswer = ""
            for(j in (1 until mas[i].count() - 1))
            {
                trueAnswer += mas[i][j].editText
                if(j != mas[i].count() - 2)
                {
                    trueAnswer +=", "
                }

                if(editText?.text.toString().toLowerCase() == mas[i][j].editText)
                {
                    flagAnswer = true
                    break
                }
            }
            if(!flagAnswer)
            {
                editText?.setText("Ваш ответ: ${textUser}\nПравильный ответ: ${trueAnswer}")
                getViewByPosition(1, listView)?.background = contextA.getDrawable(R.drawable.roundcorner_red)
            }else{
                getViewByPosition(1, listView)?.background = contextA.getDrawable(R.drawable.roundcorner_green)
            }
        }
        if(flagAnswer)
        {
            countTrueAnswer++
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

    fun getViewByPosition(pos: Int, listView: ListView): View? {
        val firstListItemPosition = listView.firstVisiblePosition
        val lastListItemPosition = firstListItemPosition + listView.childCount - 1
        return if (pos < firstListItemPosition || pos > lastListItemPosition) {
            listView.adapter.getView(pos, null, listView)
        } else {
            val childIndex = pos - firstListItemPosition
            listView.getChildAt(childIndex)
        }
    }
}
