package com.samsung.galaxyfmsdk.model

data class FmRdsData(
    val psName: String = "",
    val radioText: String = "",
    val pty: Int = 0,
    val piCode: Int = 0,
    val trafficProgram: Boolean = false,
    val trafficAnnouncement: Boolean = false
) {
    val ptyDescription: String
        get() = when (pty) {
            1 -> "News"
            2 -> "Current Affairs"
            3 -> "Information"
            4 -> "Sport"
            5 -> "Education"
            6 -> "Drama"
            7 -> "Culture"
            8 -> "Science"
            9 -> "Varied"
            10 -> "Pop Music"
            11 -> "Rock Music"
            12 -> "Easy Listening"
            13 -> "Light Classical"
            14 -> "Serious Classical"
            15 -> "Other Music"
            16 -> "Weather"
            17 -> "Finance"
            18 -> "Children's Programs"
            19 -> "Social Affairs"
            20 -> "Religion"
            21 -> "Phone In"
            22 -> "Travel"
            23 -> "Leisure"
            24 -> "Jazz Music"
            25 -> "Country Music"
            26 -> "National Music"
            27 -> "Oldies Music"
            28 -> "Folk Music"
            29 -> "Documentary"
            else -> "RDS Audio"
        }
}
