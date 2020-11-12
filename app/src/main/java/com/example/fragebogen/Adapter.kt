package com.example.fragebogen

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class Adapter(items:ArrayList<ObjectQuestions>, context: Context)
    :ArrayAdapter<ObjectQuestions>(context,R.layout.fragment_listview, items){

    private lateinit var view: View
    private lateinit var objectQuestions:ObjectQuestions
    private lateinit var storage:FirebaseStorage
    private lateinit var storageRef: StorageReference

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference
        objectQuestions = getItem(position)!!
        if(objectQuestions.bitmapFlag)
        {
            view = LayoutInflater.from(context).inflate(R.layout.fragment_listview_image, parent, false)
            generateImageView()
            generateTextView()
        }else if(objectQuestions.editTextFlag){
            view = LayoutInflater.from(context).inflate(R.layout.fragment_listview_edit_text, parent, false)
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.fragment_listview, parent, false)
            generateTextView()
        }
        return view
    }

    private fun generateImageView()
    {
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        objectQuestions.bitmap?.let { getBitmap(it, imageView) }
    }

    private fun generateTextView()
    {
        val textView = view.findViewById<TextView>(R.id.textView)
        textView.text = objectQuestions.text
    }
    private fun getBitmap(imageName:String, imageView:ImageView) {
        val pathReference = storageRef.child(imageName)
        val ONE_MEGABYTE: Long = 1024 * 1024
        pathReference.getBytes(ONE_MEGABYTE).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeByteArray(it, 0,it.count())
            imageView.setImageBitmap(bitmap)
        }.addOnFailureListener {
            Log.d("TAGA", it.toString())
        }
    }
}