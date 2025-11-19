package com.tomcvt.cvtcaptcha.service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.dtos.Point;
import com.tomcvt.cvtcaptcha.utility.SolutionParser;

@Service
public class CaptchaImageGenerator {
    private final String captchaDir;
    private final Random random = new Random();
    private final EmojiProvider emojiProvider;
    private final String emojiPath = "/emoji-noto-color/72/";

    public CaptchaImageGenerator(@Value("${app.captcha-dir}") String captchaDir,
                                 EmojiProvider emojiProvider) {
        this.captchaDir = captchaDir;
        this.emojiProvider = emojiProvider;
    }

    
    public File generateEmojiCaptchaImage(UUID requestId, String solution) {
        System.out.println("Generating emoji captcha image for requestId: " + requestId + " with solution: " + solution);
        List<Point> points = SolutionParser.parseCIOSolution(solution);
        List<String> emojis = emojiProvider.getRandomSet(points.size());
        List<String> emojiCodes = emojis.stream()
                                        .map(EmojiProvider::map)
                                        .toList();
        
        System.out.println("Parsed points: " + emojiCodes);

        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.setColor(Color.BLACK);
        int index = 0;
        for (Point point : points) {
            var oldTransform = g2d.getTransform();
            int x = (int) (point.x() * image.getWidth());
            int y = (int) (point.y() * image.getHeight());
            g2d.translate(x, y);
            float angle = (float) Math.toRadians(random.nextInt(360));
            g2d.rotate(angle);
            InputStream emojiStream = getClass().getResourceAsStream(emojiPath + "emoji_u" + emojiCodes.get(index) + ".png");
            index++;
            try {
                BufferedImage emojiImage = ImageIO.read(emojiStream);
                g2d.drawImage(emojiImage, -emojiImage.getWidth()/2, -emojiImage.getHeight()/2, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            g2d.setTransform(oldTransform);
        }
        g2d.dispose();
        //EmojiProvider.getRandomEmoji(); // Just to log available emojis
        try {
            File outputDir = new File(captchaDir);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            File outputFile = new File(outputDir, "captcha_" + requestId + ".png");
            ImageIO.write(image, "png", outputFile);
            return outputFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
