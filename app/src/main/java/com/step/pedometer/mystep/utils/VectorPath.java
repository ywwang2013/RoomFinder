package com.step.pedometer.mystep.utils;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jinglwa on 7/25/2017.
 */

public class VectorPath {
//    public List<Point> points_ = null;

    public List<PointF> readFromString(String str, double xratio, double yratio) {
        List<PointF> points = new ArrayList<>();

        String[] parts = str.split(Pattern.quote("|"));
//        Log.v("test_read", str);
//        Log.v("test_partlen", String.valueOf(parts.length));
        for (int i = 0; i < parts.length; ++i) {
            String [] xy = parts[i].split(",");
            if (xy.length < 2)
                continue;
            double x = (1714 - Integer.parseInt(xy[1])) * xratio;
            double y = Integer.parseInt(xy[0]) * yratio;

            points.add(new PointF((float)x, (float)y));
        }
        return points;
    }

    //Compute the dot product AB . AC
    private double DotProduct(PointF pointA, PointF pointB, PointF pointC)
    {
        double[] AB = new double[2];
        double[] BC = new double[2];
        AB[0] = pointB.x - pointA.x;
        AB[1] = pointB.y - pointA.y;
        BC[0] = pointC.x - pointB.x;
        BC[1] = pointC.y - pointB.y;
        double dot = AB[0] * BC[0] + AB[1] * BC[1];

        return dot;
    }

    //Compute the cross product AB x AC
    private double CrossProduct(PointF pointA, PointF pointB, PointF pointC)
    {
        double[] AB = new double[2];
        double[] AC = new double[2];
        AB[0] = pointB.x - pointA.x;
        AB[1] = pointB.y - pointA.y;
        AC[0] = pointC.x - pointA.x;
        AC[1] = pointC.y - pointA.y;
        double cross = AB[0] * AC[1] - AB[1] * AC[0];

        return cross;
    }

    //Compute the distance from A to B
    double Distance(PointF pointA, PointF pointB)
    {
        double d1 = pointA.x - pointB.x;
        double d2 = pointA.y - pointB.y;

        return Math.sqrt(d1 * d1 + d2 * d2);
    }

    //Compute the distance from AB to C
    //if isSegment is true, AB is a segment with endpoints, not a line.
    public double LineToPointDistance2D(PointF pointA, PointF pointB, PointF pointC,
                                        boolean isSegment)
    {
        double dist = CrossProduct(pointA, pointB, pointC) / Distance(pointA, pointB);
        if (isSegment)
        {
            double dot1 = DotProduct(pointA, pointB, pointC);
//            Log.v("test_point", String.valueOf(pointA)+"->"+String.valueOf(pointB)+"&"+String.valueOf(pointC));
//            Log.v("test_dot1", String.valueOf(dot1));
            if (dot1 > 0)
                return Distance(pointB, pointC);

            double dot2 = DotProduct(pointB, pointA, pointC);
//            Log.v("test_dot2", String.valueOf(dot2));
            if (dot2 > 0)
                return Distance(pointA, pointC);
        }
        return Math.abs(dist);
    }
}
