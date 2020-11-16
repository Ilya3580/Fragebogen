package com.example.fragebogen

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
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
    private lateinit var editTextKey: EditText
    private lateinit var textView:TextView
    private lateinit var fragment:Fragment
    private lateinit var questions:ArrayList<ArrayList<ObjectQuestions>>
    private lateinit var progressBar:ProgressBar
    private lateinit var globalStudent:StudentClass
    private var block= false
    private var blockKey = false
    private var flagEnd = true
    private var masterKey = ""
    private var masterFlag = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.container)
        questions = ArrayList()
        button = findViewById(R.id.button)
        editTextKey = findViewById(R.id.editTextKey)
        progressBar = findViewById(R.id.progressBar)
        textView = findViewById(R.id.textView)

        dataBase = Firebase.database
        myRef = dataBase.reference

        button.setOnClickListener {
            if(hasConnection()) {
                block = false
                progressBarStart()
                generateList()
            }else{
                Toast.makeText(this, "Нет подключения к интернету", Toast.LENGTH_LONG).show()
            }
        }


    }
    private fun progressBarStart()
    {
        textView.visibility = View.GONE
        button.visibility = View.GONE
        editTextKey.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }
    private fun progressBarStop()
    {
        textView.visibility = View.VISIBLE
        button.visibility = View.VISIBLE
        editTextKey.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }
    private fun pressButton()
    {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(editTextKey.windowToken, 0)
        var myKey = editTextKey.text.trim()
        if(myKey != "") {
            if (myKey == masterKey) {
                masterFlag = true
                generateFragment()
            } else {
                progressBarStart()
                blockKey = false
                checkKey()
            }
        }else{
            alertDialog("Введите ключ")
        }
    }

    private fun generateFragment() {
        progressBarStop()
        textView.visibility = View.GONE
        editTextKey.visibility = View.GONE
        var param = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        param.setMargins(50,50,50,50)
        button.layoutParams = param
        button.text = "начать"
        button.setOnClickListener {
            globalStudent.key = "+" + globalStudent.key
            myRef.child("students").child(globalStudent.surname).setValue(globalStudent)
            container.removeAllViews()
            fragment = Fragment.newInstance(questions, questions.size, this, globalStudent)
            supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
            flagEnd = false
        }

    }
    private fun generateList() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(!block) {
                    block = true
                    questions.clear()
                    for (ds in dataSnapshot.children) {
                        if(ds.key == "masterKey")
                        {
                            masterKey = ds.value.toString()
                        }
                        if(ds.key != "students") {
                            val arrayList = ArrayList<String>()
                            for (dsChild in ds.children) {
                                var flagIter = false
                                var flag = true
                                for (dsChildChild in dsChild.children) {
                                    arrayList.add(dsChildChild.value.toString())
                                    flag = false
                                    flagIter = true
                                }
                                if (flag) {
                                    arrayList.add(0, dsChild.value.toString())
                                }
                                if (flagIter) {
                                    arrayList.add("Проверить")
                                    questions.add(convertListObjectQuestion(arrayList))
                                }
                            }
                        }
                    }
                    pressButton()
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
    private fun convertListObjectQuestion(lst:ArrayList<String>) : ArrayList<ObjectQuestions> {
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
    private fun checkKey(){
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(!blockKey) {
                    var keyStr = editTextKey.text.trim().toString()
                    blockKey = true
                    for (ds in dataSnapshot.children) {
                        if (ds.key == "students") {
                            for (dsChild in ds.children) {
                                var student: StudentClass
                                var mas = ArrayList<String>()
                                for (dsChilChild in dsChild.children) {
                                    mas.add(dsChilChild.value.toString())
                                }
                                student = StudentClass(mas[0], mas[1], mas[2])
                                if (student.key == keyStr && student.key[0] != '+') {
                                    globalStudent = student
                                    generateFragment()
                                    return
                                } else if (student.key == "+" + keyStr && student.key[0] == '+') {
                                    alertDialog("Данный ключ уже использовали")
                                    return
                                }
                            }
                            alertDialog("Ключ не найден")
                        }
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w("TAGA", "Failed to read value.", error.toException())
            }
        })



        /*val ANDROID_ID: String = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )*/


    }
    private fun alertDialog(text:String) {
        textView.visibility = View.VISIBLE
        button.visibility = View.VISIBLE
        editTextKey.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        val builder = AlertDialog.Builder(this)
        builder.setTitle(text)
        builder.setPositiveButton("OK") { dialog, which ->
        }

        builder.show()
    }

}
