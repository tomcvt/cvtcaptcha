package com.tomcvt.cvtcaptcha.service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.dtos.Point;
import com.tomcvt.cvtcaptcha.utility.SolutionParser;

@Service
public class CaptchaImageGenerator {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CaptchaImageGenerator.class);
    private final String captchaDir;
    private final Random random = new Random();
    private final EmojiProvider emojiProvider;
    private final String emojiPath = "/emoji-noto-color/72/";
    private final int imageWidth; // default 400
    private final int imageHeight; // default 300

    public CaptchaImageGenerator(@Value("${app.captcha-dir}") String captchaDir,
            @Value("${app.image.width}") int imageWidth,
            @Value("${app.image.height}") int imageHeight,
            EmojiProvider emojiProvider) {
        this.captchaDir = captchaDir;
        this.emojiProvider = emojiProvider;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    public File generateEmojiCaptchaImage(UUID requestId, String solution) {
        List<Point> points = SolutionParser.parseCIOSolution(solution);
        List<String> emojis = emojiProvider.getRandomSet(points.size());
        List<String> emojiCodes = emojis.stream()
                .map(EmojiProvider::map)
                .toList();
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        int index = 0;
        for (Point point : points) {
            var oldTransform = g2d.getTransform();
            int x = (int) (point.x() * image.getWidth());
            int y = (int) (point.y() * image.getHeight());
            g2d.translate(x, y);
            float angle = (float) Math.toRadians(random.nextInt(360));
            g2d.rotate(angle);
            InputStream emojiStream = getClass()
                    .getResourceAsStream(emojiPath + "emoji_u" + emojiCodes.get(index) + ".png");
            index++;
            try {
                BufferedImage emojiImage = ImageIO.read(emojiStream);
                g2d.drawImage(emojiImage, -emojiImage.getWidth() / 2, -emojiImage.getHeight() / 2, null);
                // g2d.setColor(Color.RED);
                // g2d.drawOval(-emojiImage.getWidth() / 2, -emojiImage.getHeight() / 2, 72,
                // 72);
                // g2d.drawRect(0,0,1,1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            g2d.setTransform(oldTransform);
        }
        g2d.translate(16, 284);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Click in order", 0, 0);
        g2d.translate(150, 0);
        for (String emoji : emojiCodes) {
            InputStream emojiStream = getClass()
                    .getResourceAsStream(emojiPath + "emoji_u" + emoji + ".png");
            try {
                BufferedImage emojiImage = ImageIO.read(emojiStream);
                // AffineTransform scaleTransform = AffineTransform.getScaleInstance(0.5, 0.5);
                Image sei = emojiImage.getScaledInstance(
                        (int) (emojiImage.getWidth() * 0.5),
                        (int) (emojiImage.getHeight() * 0.5),
                        Image.SCALE_SMOOTH);
                g2d.drawImage(sei, -sei.getWidth(null) / 2, -sei.getHeight(null) / 2, null);
                g2d.translate(sei.getWidth(null) + 8, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        g2d.dispose();

        try {
            File outputDir = new File(captchaDir);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            File outputFile = new File(outputDir, "captcha_" + requestId + ".jpg");
            // Convert to RGB (JPEG does not support alpha)
            BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgbImage.createGraphics();
            g.drawImage(image, 0, 0, Color.BLACK, null);
            g.dispose();

            // Set JPEG compression
            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageOutputStream ios = ImageIO.createImageOutputStream(outputFile);
            jpgWriter.setOutput(ios);

            JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(
                    null);
            jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(0.7f);

            jpgWriter.write(null, new IIOImage(rgbImage, null, null), jpegParams);
            ios.close();
            jpgWriter.dispose();
            long fileSize = outputFile.length();
            String fileSizeKB = String.format("%.2f", fileSize / 1024.0);
            log.info("Generated CAPTCHA image: {} ({} KB)", outputFile.getAbsolutePath(), fileSizeKB);
            return outputFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
