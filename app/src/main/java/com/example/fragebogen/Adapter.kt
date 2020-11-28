package com.example.fragebogen

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class Adapter(items: ArrayList<ObjectQuestions>, context: Context)
    :ArrayAdapter<ObjectQuestions>(context, R.layout.fragment_listview, items){

    private lateinit var view: View
    private lateinit var objectQuestions:ObjectQuestions
    private lateinit var storage:FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var progressBar: ProgressBar
    private var flagAlertDialog = true
    private var editTextText = ""

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference
        objectQuestions = getItem(position)!!
        if(objectQuestions.bitmapFlag)
        {
            view = LayoutInflater.from(context).inflate(
                R.layout.fragment_listview_image,
                parent,
                false
            )
            progressBar = view.findViewById(R.id.progressBar)
            generateImageView(position)
            generateTextView(position)
        }else if(objectQuestions.editTextFlag){
            view = LayoutInflater.from(context).inflate(
                R.layout.fragment_listview_edit_text,
                parent,
                false
            )
            generateEditText(position)
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.fragment_listview, parent, false)
            generateTextView(position)
            if(position !=0) {
                if (position == count - 1) {
                    val textView = view.findViewById<TextView>(R.id.textView)
                    textView.background = ContextCompat.getDrawable(
                        context,
                        R.drawable.round_corner_orange
                    )
                } else {
                    view.background = ContextCompat.getDrawable(
                        context,
                        getItem(position)!!.idBackground
                    )
                }
            }
        }

        return view
    }

    private fun generateImageView(position: Int)
    {
        objectQuestions.bitmap?.let { getBitmap(it, progressBar, position) }
    }

    private fun generateEditText(position: Int)
    {
        var editText = view.findViewById<EditText>(R.id.editText)
        editText.background = ContextCompat.getDrawable(context, getItem(position)!!.idBackground)
        editText.setText(editTextText)
        editText.setOnKeyListener { view, i, keyEvent ->
            var consumed = false
            if (i == KEYCODE_ENTER) {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view?.windowToken, 0)
                editText.setText(editText.text.toString().replace("\n", ""))
                consumed = true //это если не хотим, чтобы нажатая кнопка обрабатывалась дальше видом, иначе нужно оставить false
            }
            return@setOnKeyListener consumed
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(editable: Editable) {
                editTextText = editable.toString()
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }
        })
    }

    private fun generateTextView(position: Int)
    {
        val textView = view.findViewById<TextView>(R.id.textView)
        if(position == count-1)
        {
            textView.setTextColor(Color.WHITE)
            textView.setPadding(0, 30, 0, 30)
            textView.textSize = 30F
            var param = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            param.setMargins(0, 0, 0, 50)
            textView.layoutParams = param
        }
        if(position == 0)
        {
            textView.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            textView.setPadding(10, 0, 0, 0)
        }
        textView.text = objectQuestions.text
    }
    private fun getBitmap(imageName: String, progressBar: ProgressBar, position: Int) {
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        if(hasConnection()) {
            if(getItem(position)?.idBitmapFlag!!)
            {
                progressBar.visibility = View.GONE
                imageView.visibility = View.VISIBLE
                imageView.setImageBitmap(getItem(position)?.idBitmap)
            }else {
                val pathReference = storageRef.child(imageName)
                val ONE_MEGABYTE: Long = 1024 * 1024
                pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeByteArray(it, 0, it.count())
                    progressBar.visibility = View.GONE
                    imageView.visibility = View.VISIBLE
                    imageView.setImageBitmap(bitmap)
                    getItem(position)?.idBitmap = bitmap
                    getItem(position)?.idBitmapFlag = true
                }.addOnFailureListener {
                    Log.d("TAGA", it.toString())
                }
            }
        }else{
            val t: Thread = object : Thread() {
                override fun run() {
                    var flagThread = true
                    for(i in (0 .. 1500))
                    {
                        if(hasConnection())
                        {
                            getBitmap(imageName, progressBar, position)
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

            if(flagAlertDialog) {
                flagAlertDialog = false
                alertDialog()
            }

        }
    }
    fun hasConnection(): Boolean {
        val cm =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
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
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Проверьте подключение к интернету!")
        builder.setMessage("Без подключения к интернету тестирование завершится через 15 секунд")
        builder.setPositiveButton("OK") { dialog, which ->

        }
        builder.show()
    }


}