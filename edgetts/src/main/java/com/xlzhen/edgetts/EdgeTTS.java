package com.xlzhen.edgetts;

import java.util.stream.Collectors;

import io.github.whitemagic2014.tts.TTS;
import io.github.whitemagic2014.tts.TTSVoice;
import io.github.whitemagic2014.tts.bean.Voice;

public class EdgeTTS {

    public static String textToMp3(String text, String audioPath, String language, boolean man) {
        String ttsName = getString(language, man);
        Voice voice = TTSVoice.provides().stream().filter(v -> v.getShortName().equals(ttsName)).collect(Collectors.toList()).get(0);
        // Set to true to resolve the rate limiting issue in certain regions..
        // You can customize the file name; if omitted, a random file name will be generated.
        // When the specified file name is the same, it will either overwrite or append to the file.
        // default mp3.
        return new TTS(voice, text)
                .findHeadHook()
                .isRateLimited(true) // Set to true to resolve the rate limiting issue in certain regions..
                .fileName(audioPath)// You can customize the file name; if omitted, a random file name will be generated.
                .overwrite(false) // When the specified file name is the same, it will either overwrite or append to the file.
                .formatMp3()  // default mp3.
                .trans();
    }

    private static String getString(String language, boolean man) {
        String ttsName;
        if (language.equals("ja-JP")) {
            ttsName = man ? "ja-JP-KeitaNeural" : "ja-JP-NanamiNeural";
        } else if (language.equals("en-US") || language.equals("zh-CN")) {
            ttsName = man ? "zh-CN-YunyangNeural" : "zh-CN-XiaoxiaoNeural";
        } else if (language.equals("pt-BR")) {
            ttsName = man ? "pt-BR-AntonioNeural" : "pt-BR-FranciscaNeural";
        } else if (language.equals("es-ES")) {
            ttsName = man ? "es-ES-AlvaroNeural" : "es-ES-ElviraNeural";
        } else if (language.equals("it-IT")) {
            ttsName = man ? "it-IT-DiegoNeural" : "it-IT-ElsaNeural";
        } else if (language.equals("de-DE")) {
            ttsName = man ? "de-DE-ConradNeural" : "de-DE-KatjaNeural";
        } else if (language.equals("fr-FR")) {
            ttsName = man ? "fr-FR-HenriNeural" : "fr-FR-EloiseNeural";
        } else {
            ttsName = man ? "zh-CN-YunyangNeural" : "zh-CN-XiaoxiaoNeural";
        }
        return ttsName;
    }
}
