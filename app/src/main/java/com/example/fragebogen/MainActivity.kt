package com.example.fragebogen

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.lang.Exception


class MainActivity : AppCompatActivity() {

    private lateinit var dataBase: FirebaseDatabase
    private lateinit var myRef: DatabaseReference

    private lateinit var container:LinearLayout
    private lateinit var button:Button
    private lateinit var editTextKey: EditText
    private lateinit var editTextQuestion: EditText
    private lateinit var textView:TextView
    private lateinit var fragment:FragmentList
    private lateinit var questions:ArrayList<ArrayList<ObjectQuestions>>
    private lateinit var progressBar:ProgressBar
    private var count:Int = 0
    private var block= false
    private var flagEnd = true
    private var masterKey = ""
    private var key = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.container)
        questions = ArrayList()
        button = findViewById(R.id.button)
        editTextKey = findViewById(R.id.editTextKey)
        editTextQuestion = findViewById(R.id.editTextQuestion)
        textView = findViewById(R.id.textView)

        dataBase = Firebase.database
        myRef = dataBase.reference

        button.setOnClickListener {
            if(hasConnection()) {
                block = false
                textView.visibility = View.GONE
                button.visibility = View.GONE
                editTextQuestion.visibility = View.GONE
                editTextKey.visibility = View.GONE
                progressBar = findViewById(R.id.progressBar)
                progressBar.visibility = View.VISIBLE

                generateList()
            }else{
                Toast.makeText(this, "Нет подключения к интернету", Toast.LENGTH_LONG).show()
            }
        }


    }

    private fun pressButton(lst:ArrayList<String>)
    {

        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(editTextKey.windowToken, 0)
        inputMethodManager.hideSoftInputFromWindow(editTextQuestion.windowToken, 0)
        button.visibility = View.GONE
        editTextQuestion.visibility = View.GONE
        editTextKey.visibility = View.GONE
        progressBar.visibility = View.GONE

        if(editTextQuestion.text.toString() == "")
        {
            alertDialogKey("Ввены не все данные")

        }else {
            var tryFlag = true
            try {
                count = editTextQuestion.text.toString().toInt()
            }catch (e:Exception)
            {
                tryFlag = false
            }
            if(tryFlag) {
                if (count > questions.count() || count > 10) {
                    alertDialogKey("Введенное число слишком большое")
                } else if (count < 1) {
                    alertDialogKey("Введенное число слишком маленькое")
                } else {
                    var myKey = editTextKey.text.toString()
                    if (key == myKey || myKey == masterKey) {
                        if (checkId(lst, myKey == masterKey)) {
                            container.removeAllViews()
                            generateFragment()
                        }
                    } else {
                        alertDialogKey("Введеный ключ не верный! Попробуйте еще раз")
                    }
                }
            }else{
                alertDialogKey("Введены не верные данные")
            }
        }

    }

    private fun generateFragment() {
        fragment = FragmentList.newInstance(questions, count, this)
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
        flagEnd = false
    }
    private fun generateList() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                questions.clear()
                var lstA = ArrayList<String>()
                for (ds in dataSnapshot.children) {
                    if(ds.key == "masterKey")
                    {
                        masterKey = ds.value.toString()
                    }
                    if(ds.key == "key"){
                        for (dsChild in ds.children) {
                            if(dsChild.key.toString() == "key")
                            {
                                key = dsChild.value.toString()
                            }else {
                                lstA.add(dsChild.value.toString())
                            }
                        }
                    }
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
                    pressButton(lstA)
                    block = true
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
    private fun checkId(lst:ArrayList<String>, flagMasterKey:Boolean):Boolean {
        if(flagMasterKey)
        {
            return true
        }
        var flagId = true
        val ANDROID_ID: String = Settings.Secure.getString(
            applicationContext.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        for(n in lst)
        {
            if(n == ANDROID_ID)
            {
                flagId = false
                break
            }
        }
        if(flagId) {
            myRef.child("key").child(ANDROID_ID).setValue(ANDROID_ID)
            return true
        }else{
            alertDialogKey("Вы использовали данный ключ")
            return false
        }
    }
    private fun alertDialogKey(text:String) {
        textView.visibility = View.VISIBLE
        button.visibility = View.VISIBLE
        editTextQuestion.visibility = View.VISIBLE
        editTextKey.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        val builder = AlertDialog.Builder(this)
        builder.setTitle(text)
        builder.setPositiveButton("OK") { dialog, which ->
        }

        builder.show()
    }

}
