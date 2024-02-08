package com.omkar.controller;

import android.speech.tts.TextToSpeech;

import androidx.lifecycle.MutableLiveData;

public class SpeechProvider {

    private static MutableLiveData<TextToSpeech> speechAssistant = new MutableLiveData<>();

    public static MutableLiveData<TextToSpeech> getSpeechAssistant() {
        return speechAssistant;
    }

    public static void setSpeechAssistant(MutableLiveData<TextToSpeech> speechAssistant) {
        SpeechProvider.speechAssistant = speechAssistant;
    }

}
