package com.example.fragebogen

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class FragmentList(private var mas: ArrayList<ArrayList<ObjectQuestions>>, private var count:Int, private var contextA:Context, private var student:StudentClass) : Fragment() {

    companion object {
        fun newInstance(mas:ArrayList<ArrayList<ObjectQuestions>>, count: Int,contextA:Context, student:StudentClass) = FragmentList(mas, count,contextA, student)

    }
    private lateinit var dataBase: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var masRandom:Array<Int>
    private lateinit var listView: ListView
    private lateinit var textView:TextView
    private lateinit var textAuthors:TextView
    private var idMasRandom = 0
    private lateinit var booleanMas:Array<Boolean>
    private var blockButtons = false
    private var i = 0
    private var countTrueAnswer = 0
    private lateinit var progressBarLitle:ProgressBar
    private lateinit var textViewResult:TextView
    private var time = 60 * 1000
    private var timeBetween = 10 * 1000
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    var timer = object: CountDownTimer(time.toLong(), 1) {
        override fun onTick(millisUntilFinished: Long) {
            progressBarLitle.progress = millisUntilFinished.toInt()

        }

        override fun onFinish() {
            checkAnswer()
        }
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    var timerBetween = object: CountDownTimer(timeBetween.toLong(), 1) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            idMasRandom++
            if(idMasRandom >= count)
            {
                showResult()
            }
            generateListView()
        }
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view =  inflater.inflate(R.layout.fragment_list_fragment, container, false)

        dataBase = Firebase.database
        myRef = dataBase.reference
        listView = view.findViewById(R.id.listView)
        textView = view.findViewById(R.id.textViewResult)
        textViewResult = view.findViewById(R.id.textViewResultMini)
        textAuthors = view.findViewById(R.id.textAuthors)
        progressBarLitle = view.findViewById(R.id.progressBarLitle)
        masRandom = Array(mas.count(), {i->-1})


        generateRandomMas()
        generateListView()

        progressBarLitle.getProgressDrawable().setColorFilter(
            Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);


        return view
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun generateListView() {
        if(idMasRandom >= count)
        {
            showResult()
        }else {
            progressBarLitle.max = time
            timer.start()
            textViewResult.text = "${idMasRandom+1}/${count}"
            i = masRandom[idMasRandom]
            blockButtons = false
            listView.adapter = Adapter(mas[i], contextA)
            booleanMas = Array(mas[i].count(), { i -> false })
            listView.setOnItemClickListener { parent, view, position, id ->
                if (position == listView.count - 1) {
                    if(hasConnection()) {
                        if (blockButtons) {
                            timerBetween.cancel()
                            idMasRandom++
                            if(idMasRandom >= count)
                            {
                                showResult()
                            }
                            generateListView()
                        } else {
                            val textView = view.findViewById<TextView>(R.id.textView)
                            textView.text = "Далее"
                            blockButtons = true
                            checkAnswer()
                        }
                    }else{
                        val t: Thread = object : Thread() {
                            override fun run() {
                                var flagThread = true
                                for(i in (0 .. 1500))
                                {
                                    if(hasConnection())
                                    {
                                        flagThread = false
                                    }else{
                                        Thread.sleep(10)
                                    }
                                }
                                if(flagThread)
                                    System.exit(0)
                            }
                        }
                        t.start()
                        alertDialog()
                    }


                } else if (!blockButtons && position != 0 && mas[i].count() != 3) {
                    if (booleanMas[position]) {
                        mas[i][position].idBackground = R.drawable.round_corner
                        view.background = contextA.getDrawable(mas[i][position].idBackground)
                        booleanMas[position] = false
                    } else {
                        mas[i][position].idBackground = R.drawable.round_corner_orange
                        view.background = contextA.getDrawable(mas[i][position].idBackground)
                        booleanMas[position] = true
                    }
                }
            }
        }

    }

    private fun showResult() {
        progressBarLitle.visibility = View.GONE
        textViewResult.visibility = View.GONE
        student.result="${countTrueAnswer} из ${count}"
        myRef.child("students").child(student.surname).setValue(student)
        listView.visibility = View.GONE
        textView.visibility = View.VISIBLE
        textAuthors.visibility = View.VISIBLE
        textView.text = "Ваш резульат: ${countTrueAnswer} из ${count}"
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun checkAnswer() {
        timer.cancel()
        timerBetween.start()
        var flag = mas[i].count() != 3
        var flagAnswer = false
        if(flag)
        {
            flagAnswer = true
            for(j in (1 until mas[i].count()-1))
            {
                if(booleanMas[j] && !mas[i][j].trueAnswer || mas[i][j].trueAnswer && !booleanMas[j])
                {
                    flagAnswer = false
                    mas[i][j].idBackground = R.drawable.roundcorner_red
                    getViewByPosition(j, listView)?.background = contextA.getDrawable(mas[i][j].idBackground)
                }
                if(mas[i][j].trueAnswer)
                {
                    mas[i][j].idBackground = R.drawable.roundcorner_green
                    getViewByPosition(j, listView)?.background = contextA.getDrawable(mas[i][j].idBackground)
                }
            }
        }else{
            val editText = getViewByPosition(1, listView)?.findViewById<EditText>(R.id.editText)
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
            editText?.isFocusable = false
            editText?.isLongClickable = false
            val textUser = editText?.text?.trim().toString()
            var trueAnswer = ""
            for(j in (1 until mas[i].count() - 1))
            {
                trueAnswer += mas[i][j].editText
                if(j != mas[i].count() - 2)
                {
                    trueAnswer +=", "
                }

                if(textUser == mas[i][j].editText)
                {
                    editText?.setText(textUser)
                    flagAnswer = true
                    break
                }
            }
            if(!flagAnswer)
            {
                editText?.setText("Ваш ответ: ${textUser}\nПравильный ответ: ${trueAnswer}")
                mas[i][1].idBackground = R.drawable.roundcorner_red
                getViewByPosition(1, listView)?.background = contextA.getDrawable(mas[i][1].idBackground)
            }else{
                mas[i][1].idBackground = R.drawable.roundcorner_green
                getViewByPosition(1, listView)?.background = contextA.getDrawable(mas[i][1].idBackground)
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

    fun hasConnection(): Boolean {
        val cm =
            contextA.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (wifiInfo != null && wifiInfo.isConnected) {
            return true
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (wifiInfo != null && wifiInfo.isConnected) {
            return true
        }
        wifiInfo = cm.activeNetworkInfo
        return wifiInfo != null && wifiInfo.isConnected
    }

    private fun alertDialog() {
        val builder = AlertDialog.Builder(contextA)
        builder.setTitle("Проверьте подключение к интернету!")
        builder.setMessage("Без подключения к интернету тестирование завершится через 15 секунд")
        builder.setPositiveButton("OK") { dialog, which ->

        }
        builder.show()
    }


}
