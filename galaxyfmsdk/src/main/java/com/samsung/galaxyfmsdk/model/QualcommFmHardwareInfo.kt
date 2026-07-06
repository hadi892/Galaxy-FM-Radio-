package com.samsung.galaxyfmsdk.model

data class QualcommFmHardwareInfo(
    val deviceModel: String = "Samsung Galaxy Tab A9+ 5G (SM-X216B)",
    val socPlatform: String = "Qualcomm Snapdragon 695 5G (SM6375)",
    val fmRadioHal: String = "WCN3990 / WCN6855 V4L2 FM Tuner",
    val linuxV4l2Node: String = "/dev/radio0",
    val antennaType: String = "Wired Headphone / AUX Cable Antenna Required",
    val supportsRds: Boolean = true,
    val supportsHardwareSeek: Boolean = true
)
