package com.step.pedometer.mystep.utils;

/**
 * Created by jinglwa on 7/25/2017.
 */



import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PathDashPathEffect;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PathView extends View {

    Paint paint;
    Paint paint_arrow;
    Path path;
    Path arrow;


    public List<PointF> pts_  = null;
    Path pathShape;
    float phase;
    float advance;
    PathDashPathEffect.Style style;
    VectorPath vecPath_;
    private double viewRatioX_ = 1;
    private double viewRatioY_ = 1;
    private boolean isDebug = true;

    public enum Hint {
        HSTART, HEND, HLEFT, HRIGHT, HSTRAIGHT, HERROR
    }
    protected String getHint(Hint h) {
        switch (h) {
            case HSTART:
                return "出发！";
            case HEND:
                return "您已到达目标房间附近！";
            case HLEFT:
                return "前方请左转！";
            case HRIGHT:
                return "前方请右转！";
            case HSTRAIGHT:
                return "请直走！";
            case HERROR:
                return "您已偏离！";
            default:
                return "";
        }
    }
    public PathView(Context context) {
        super(context);
        init();
    }

    public PathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PathView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
//        paint.setStyle(Paint.Style.FILL);

        path = new Path();

        pathShape = new Path();
        pathShape.addCircle(10, 10, 10, Direction.CCW);

        phase = 0;
        advance = 30.0f;
        style = PathDashPathEffect.Style.ROTATE;
        // arrow
        paint_arrow = new Paint();
        paint_arrow = new Paint();
        paint_arrow.setColor(Color.RED);
        paint_arrow.setStrokeWidth(5);
        paint_arrow.setStyle(Paint.Style.FILL);
//        paint.setStyle(Paint.Style.FILL);
        arrow = new Path();
        arrow.setFillType(Path.FillType.EVEN_ODD);

        vecPath_ = new VectorPath();

//        paint2 = new Paint();
//        paint2.setColor(Color.YELLOW);
//        paint2.setStrokeWidth(15f);
    }

    public  void addPhase(int x) {
        phase += x;
    }

    public void setRatio(double xratio, double yratio) {
        viewRatioX_ = xratio;
        viewRatioY_ = yratio;
    }

    public void loadPoints(String str) {
        setPoints(vecPath_.readFromString(str, viewRatioX_, viewRatioY_));
    }

    protected void setPoints (List<PointF> pts) {
        pts_ = new ArrayList<>();
        for (int i = 0; i < pts.size(); ++i) {
            pts_.add(pts.get(i));
        }

        Log.e("test_ptlen", String.valueOf(pts.size()));
    }

    protected void drawPath (Canvas canvas, int color) {
        path.reset();
        if (pts_ == null) {
            return;
        }
        if (pts_.size() > 0) {
            path.moveTo(pts_.get(0).x, pts_.get(0).y);
        }
        for (int i = 1; i < pts_.size(); i++) {
            path.lineTo(pts_.get(i).x, pts_.get(i).y);
        }
        paint.setColor(color);
        canvas.drawPath(path, paint);
    }

    protected void drawArrow(Canvas canvas, int color) {
        arrow.reset();
        if (pts_ == null || pts_.size() < 3) {
            return;
        }
        PointF endp = new PointF(pts_.get(pts_.size()-1).x, pts_.get(pts_.size()-1).y);
        PointF prep = new PointF(pts_.get(pts_.size()-2).x, pts_.get(pts_.size()-2).y);
        float len = 20;
        arrow.moveTo(endp.x, endp.y);
        PointF dir = new PointF(endp.x - prep.x, endp.y - prep.y);
        double dir_len = Math.sqrt(dir.x * dir.x + dir.y * dir.y);
        dir.x /= dir_len;
        dir.y /= dir_len;
//        PointF startp = new PointF(endp.x - dir.x * len, endp.y - dir.y * len);
//        fillArrow(startp.x, startp.y, endp.x, endp.y);
//        Log.v("testdir", String.valueOf(prep)+"->"+String.valueOf(endp)+",dir"+String.valueOf(dir));

        double angle = 0.15 * Math.PI;
        double [][] rotateMat = new double[2][2];
        rotateMat[0][0] = Math.cos(angle );
        rotateMat[0][1] = -Math.sin(angle);
        rotateMat[1][0] = Math.sin(angle);
        rotateMat[1][1] = Math.cos(angle);
        PointF left_dir = new PointF((float) (rotateMat[0][0] * dir.x + rotateMat[0][1] * dir.y)
                , (float) (rotateMat[1][0] * dir.x + rotateMat[1][1] * dir.y));
        rotateMat[0][0] = Math.cos(- angle);
        rotateMat[0][1] = -Math.sin(- angle);
        rotateMat[1][0] = Math.sin(- angle);
        rotateMat[1][1] = Math.cos(- angle);
        PointF right_dir = new PointF((float) (rotateMat[0][0] * dir.x + rotateMat[0][1] * dir.y)
                , (float) (rotateMat[1][0] * dir.x + rotateMat[1][1] * dir.y));
        PointF left_pt = new PointF(endp.x - left_dir.x * len, endp.y - left_dir.y * len);
        PointF right_pt = new PointF(endp.x - right_dir.x * len, endp.y - right_dir.y * len);
//        Log.v("test_left", String.valueOf(left_dir));
//        Log.v("test_right", String.valueOf(right_dir));
        arrow.lineTo(left_pt.x, left_pt.y);
        arrow.lineTo(right_pt.x, right_pt.y);
        arrow.close();

        paint.setColor(color);
        canvas.drawPath(arrow, paint);
    }

    private class PairIdDist {
        int id = -1;
        double dist = Double.MAX_VALUE;
    }
    PairIdDist min_id_dist = new PairIdDist();
    protected int getNearestCorner(int x, int y, double threshold) {
        if (pts_ == null) {
            return -1;
        }
        double min_dist = Double.MAX_VALUE;
        int min_id = -1;
        for (int i = 0; i < pts_.size(); ++i) {
            double diff_x = pts_.get(i).x - x;
            double diff_y = pts_.get(i).y - y;
            double dist = Math.sqrt(diff_x * diff_x + diff_y * diff_y);
            if (dist < min_dist) {
                min_dist = dist;
                min_id = i;
            }
        }
//        Log.v("test_conerdist", String.valueOf(min_id) + ", dist=" + String.valueOf(min_dist));
        PointF cur_pt = new PointF(x, y);
        if (min_dist > threshold) {
            // compute current posistion to linesegment
            double dist1 = Double.MAX_VALUE;
            double dist2 = Double.MAX_VALUE;
            if (min_id > 0)
                dist1 = vecPath_.LineToPointDistance2D(pts_.get(min_id), pts_.get(min_id-1), cur_pt, true);
            if (min_id < pts_.size() - 1)
                dist2 = vecPath_.LineToPointDistance2D(pts_.get(min_id), pts_.get(min_id+1), cur_pt, true);
            min_id = -1;
            min_dist = Math.min(min_dist, dist1);
            min_dist = Math.min(min_dist, dist2);
        }
        min_id_dist.id = min_id;
        min_id_dist.dist = min_dist;

        Log.v("test_minid", String.valueOf(min_id) + ", dist=" + String.valueOf(min_dist));
        return min_id;
    }

    public String getCornerHint(int id, double threshold) {
        if (pts_ == null) {
            return "";
        }
        String res = "";
        if (id < 0) {
            if (min_id_dist.dist < threshold)
                res = getHint(Hint.HSTRAIGHT);
            else
                res = getHint(Hint.HERROR);
        } else if (id == 0) {
            res = getHint(Hint.HSTART);
        } else if (id == pts_.size() - 1) {
            res = getHint(Hint.HEND);
        } else {
            res = checkTurn(pts_.get(id-1), pts_.get(id), pts_.get(id+1));
        }
//        Log.v("test_reshint", res);
        return res;
    }

    public String getCurrentHint(int x, int y, double threshold) {
        int id = getNearestCorner(x, y, threshold);

        String res = getCornerHint(id, threshold);
//        Log.v("test_hintres", res);
        return res;
    }

    public String checkTurn(PointF a, PointF b, PointF c) {
        double v1x = b.x - a.x;
        double v1y = b.y - a.y;
        double v2x = c.x - b.x;
        double v2y = c.y - b.y;
        double res = v1x * v2y - v1y * v2x;
        if (res > Math.ulp(2.0))
            return getHint(Hint.HRIGHT); // left
        else if (res < - Math.ulp(2.0))
            return getHint(Hint.HLEFT); // right
        else
            return getHint(Hint.HSTRAIGHT); // straight
    }

    protected void drawCorner(Canvas canvas, int color) {
        int radius = 4;
        if (pts_ == null) return;
        paint.setColor(color);
        for (int i = 0; i < pts_.size(); ++i) {
            canvas.drawCircle(pts_.get(i).x, pts_.get(i).y, radius, paint);
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);

        drawPath(canvas, Color.RED);

        drawArrow(canvas, Color.RED);

        if (isDebug)
            drawCorner(canvas, Color.BLUE);
        invalidate();
    }
}