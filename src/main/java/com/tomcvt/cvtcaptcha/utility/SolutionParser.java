package com.tomcvt.cvtcaptcha.utility;

import java.util.List;

import com.tomcvt.cvtcaptcha.dtos.Point;

public class SolutionParser {
    public static List<Point> parseCIOSolution(String solution) {
        List<Point> points = new java.util.ArrayList<>();
        String[] parts = solution.split(";");
        for (String part : parts) {
            String[] coords = part.split(",");
            float x = Float.parseFloat(coords[0]);
            float y = Float.parseFloat(coords[1]);
            points.add(new Point(x, y));
        }
        return points;
    }
}
