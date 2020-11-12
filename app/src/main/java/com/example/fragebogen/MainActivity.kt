package com.example.fragebogen

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MainActivity : AppCompatActivity() {

    private lateinit var dataBase: FirebaseDatabase
    private lateinit var myRef: DatabaseReference

    private lateinit var container:LinearLayout
    private lateinit var button:Button
    private lateinit var fragment:FragmentList
    private lateinit var questions:ArrayList<ArrayList<ObjectQuestions>>
    private var count:Int = 0
    private var block= false
    private var flagEnd = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.container)
        questions = ArrayList()

        dataBase = Firebase.database
        myRef = dataBase.reference

        addButtonStart()
        button.setOnClickListener {
            if(hasConnection()) {

                generateList()
            }else{
                Toast.makeText(this, "Нет подключения к интернету", Toast.LENGTH_LONG).show()
            }
        }


    }
    private fun addButtonStart() {
        button = Button(this)
        val param:LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT)
        val margin = 120
        param.bottomMargin = margin
        param.topMargin = margin
        param.leftMargin = margin
        param.rightMargin= margin
        button.background = getDrawable(R.drawable.round_corner_orange)
        button.textSize = 50f
        button.setTextColor(Color.WHITE)
        button.text = "Начать"
        button.layoutParams = param
        container.addView(button)
    }
    private fun alertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Введите количество вопросов")
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)
        builder.setPositiveButton("OK") { dialog, which ->
            count = input.text.toString().toInt()
            if(count>questions.count() || count>10)
            {
                Toast.makeText(this, "Введенное число слишком большое", Toast.LENGTH_LONG).show()
                alertDialog()
            }else if(count<1){
                Toast.makeText(this, "Введенное число слишком маленькое", Toast.LENGTH_LONG).show()
                alertDialog()
            }else{
                container.removeAllViews()
                generateFragment()
            }
        }
        builder.show()
    }
    private fun generateFragment() {
        fragment = FragmentList.newInstance(questions, count, this)
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
        block = true
        flagEnd = false
    }
    private fun generateList() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                questions.clear()
                for (ds in dataSnapshot.children) {
                    val arrayList = ArrayList<String>()
                    for (dsChild in ds.children) {
                        var flagIter = false
                        var flag = true
                        for (dsChildChild in dsChild.children) {
                            arrayList.add(dsChildChild.value.toString())
                            flag = false
                            flagIter = true
                        }
                        if(flag)
                        {
                            arrayList.add(0,dsChild.value.toString())
                        }
                        if(flagIter)
                        {
                            arrayList.add("Проверить")
                            questions.add(convertListObjectQuestion(arrayList))
                        }
                    }
                }
                if(!block) {
                    alertDialog()
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("TAGA", "Failed to read value.", error.toException())
            }
        })
    }
    fun hasConnection(): Boolean {
        val cm =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
    override fun onBackPressed() {
        if(flagEnd) {
            super.onBackPressed()
        }else {
            alertDialogEnd()
        }
    }
    private fun alertDialogEnd() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Вы уверены, что хотите закрыть приложение")
        builder.setPositiveButton("Да") { dialog, which ->
            flagEnd = true
            onBackPressed()
        }
        builder.setNegativeButton("Нет") { dialog, which ->

        }
        builder.show()
    }
    private fun convertListObjectQuestion(lst:ArrayList<String>) : ArrayList<ObjectQuestions>
    {
        var mas:ArrayList<ObjectQuestions> = ArrayList()
        var flag = false
        for(n in lst)
        {
            if(n[0] == '+')
            {
                flag = true
                break
            }
        }
        for(n in (0 until lst.size))
        {
            if(n == 0 || n==lst.size-1) {
                mas.add(ObjectQuestions(lst[n], false))
            }else{
                mas.add(ObjectQuestions(lst[n], !flag))
            }
        }
        return mas
    }

}
