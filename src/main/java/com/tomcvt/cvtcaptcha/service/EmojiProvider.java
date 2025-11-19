package com.tomcvt.cvtcaptcha.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class EmojiProvider {
    private final static String[] emojis = {
        "😀", "😂", "😅", "😊", "😍", "😎", "😒", "😔", "😢", "😭",
        "😡", "😱", "👍", "👎", "🙏", "💪", "🎉", "🔥", "🌟", "🍀",
        "🍎", "🍕", "⚽", "🏀", "🚗", "✈️", "🏠", "📱", "💻", "🎧"
    };
    private final static int EMOJI_COUNT = emojis.length;
    private final Random random = new Random();

    public List<String> getRandomSet(int count) {
        List<String> selectedEmojis = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(EMOJI_COUNT);
            while (selectedEmojis.contains(emojis[index])) {
                index = random.nextInt(EMOJI_COUNT);
            }
            selectedEmojis.add(emojis[index]);
        }
        return selectedEmojis;
    }

    public static String map(String emoji) {
        int codePoint = emoji.codePointAt(0);
        return Integer.toHexString(codePoint);
    }
}
