package com.example.fragebogen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.EditText
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ObjectQuestions(string: String, boolean: Boolean) {
    private var textPrivate:String? = null
    private var trueAnswerPrivate:Boolean = false

    private var bitmapTextPrivate: String? = null
    private var bitmapFlagPrivate = false

    private var editTextPrivate: String? = null
    private var editTextFlagPrivate = false


    init {
        if(boolean)
        {
            editTextPrivate = string
            editTextFlagPrivate = true

        }else {
            convertString(string)
        }
    }

    private fun convertString(str:String){
        var myStr = str
        val id = "id:"

        if(str.contains(id))
        {
            bitmapFlagPrivate = true
            bitmapTextPrivate = str.substring(str.indexOf(id) + id.length)
            myStr = str.substring(0,str.indexOf(id))
        }
        if(str[0] == '+')
        {
            trueAnswerPrivate = true
            myStr = str.substring(1)
        }
        textPrivate = myStr
    }


    var text = textPrivate
        get(){
            return field
        }
        set(value) {
            textPrivate = value
        }
    var trueAnswer = trueAnswerPrivate
        get(){
            return field
        }
        set(value) {
            trueAnswerPrivate = value
        }

    var bitmap = bitmapTextPrivate
        get(){
            return field
        }
        set(value) {
            bitmapTextPrivate = value
        }
    var bitmapFlag = bitmapFlagPrivate
        get(){
            return field
        }
        set(value) {
            bitmapFlagPrivate = value
        }
    var editText = editTextPrivate
        get(){
            return field
        }
        set(value) {
            editTextPrivate = value
        }
    var editTextFlag = editTextFlagPrivate
        get(){
            return field
        }
        set(value) {
            editTextFlagPrivate = value
        }

    var idBackground = R.drawable.round_corner
        get(){
            return field
        }
        set(value) {
            field = value
        }
}