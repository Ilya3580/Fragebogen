package com.example.fragebogen

class StudentClass(key: String, result:String, surname: String) {

    private var keyPrivate:String = key
    private var resultPrivate:String = result
    private var surnamePrivate:String = surname


    var surname:String
        get() {
            return surnamePrivate
        }
        set(value) {
            surnamePrivate = value
        }
    var key:String
        get() {
            return keyPrivate
        }
        set(value) {
            keyPrivate = value
        }
    var result:String
        get() {
            return resultPrivate
        }
        set(value) {
            resultPrivate = value
        }
}