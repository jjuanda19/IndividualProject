package com.example.remainderapplication



data class Member(
    var RemID: String="",
    var name: String="",
    var address: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var date: String="",
    var timeSetup: String="",
    var dateSetup: String="",
    var actStatus: Boolean= false,
    var reminderDay: String = "",
    var daySetup: String= "",
    var description: String ="",
    var weather: String=""

)
