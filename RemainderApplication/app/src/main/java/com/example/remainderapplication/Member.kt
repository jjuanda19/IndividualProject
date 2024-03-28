package com.example.remainderapplication



data class Member(
    // Unique identifier for the reminder. Essential for database operations and identifying each reminder uniquely.
    var RemID: String="",
    var name: String="", // Name of the reminder.
    var address: String = "", // Physical address related to the reminder.
    var latitude: Double = 0.0, // Latitude part of the geographical coordinates.
    var longitude: Double = 0.0, // Longitude part of the geographical coordinates.
    var date: String="", // The date when the reminder is set to occur.
    var timeSetup: String="", // The time of day when the reminder is set up.
    var dateSetup: String="", // The date when the reminder was initially set up.
    // Activation status of the reminder, indicating whether the reminder is active (true) or inactive (false).
    var actStatus: Boolean= false,
    var reminderDay: String = "", // The day of the week for the reminder.
    var daySetup: String= "", // The day of the week when the reminder was set up.
    var description: String ="", // A more detailed description of the reminder.
    var weather: String="" // Information about the weather conditions expected at the time of the reminder.
)

