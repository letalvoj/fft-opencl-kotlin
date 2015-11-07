package cz.letalvoj.gpgpu.equalizer

import javafx.application.Application

/**
 * Run with VM params: -Djava.library.path=./native  -Dcom.amd.aparapi.executionMode=%1
 */
fun main(args: Array<String>) {
    Application.launch(Equalizer::class.java, *args)
}