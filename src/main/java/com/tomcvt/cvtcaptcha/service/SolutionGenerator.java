package com.tomcvt.cvtcaptcha.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.dtos.Point;

@Service
public class SolutionGenerator {
    private final List<Point> offsets = List.of(
        new Point(0.1f, 0.1f),
        new Point(0.6f, 0.55f),
        new Point(0.1f, 0.55f),
        new Point(0.6f, 0.1f)
    );
    private final Random random = new Random();


    public String generateCIOSolution() {
        List<Point> points = new ArrayList<>();
        for (Point offset : offsets) {
            float x = random.nextFloat() * 0.3f + offset.x();
            float y = random.nextFloat() * 0.25f + offset.y();
            points.add(new Point(x, y));
        }
        scrambleList(points);
        StringBuilder sb = new StringBuilder();
        for (Point point : points) {
            sb.append(String.valueOf(point.x()).substring(0, 5));
            sb.append(",");
            sb.append(String.valueOf(point.y()).substring(0, 5));
            sb.append(";");
        }
        sb.setLength(sb.length() - 1); 
        System.out.println("Generated CIO solution: " + sb.toString());
        return sb.toString();
    }



    private void scrambleList(List<Point> list) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Point temp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, temp);
        }
    }
}
