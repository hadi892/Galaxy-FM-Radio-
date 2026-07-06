cmake_minimum_required(VERSION 3.22.1)

project("galaxy_fm_jni")

add_library(
        galaxy_fm_jni
        SHARED
        v4l2_fm_tuner.cpp
        galaxy_fm_jni.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        galaxy_fm_jni
        ${log-lib}
)
