package com.step.pedometer.mystep.utils;

import android.app.Activity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Canvas;
import android.widget.LinearLayout;
import android.widget.Button;

import com.step.pedometer.mystep.R;

/**
 * Created by yuwwan on 7/25/2017.
 */

public class ImageView extends View {
    private Paint paint = null; //  画笔
    private Bitmap bitmap = null;   //  图片位图
    private Bitmap bitmapDisplay = null;
    private Matrix matrix = null;
    private int nBitmapWidth = 0;   //  图片的宽度
    private int nBitmapHeight = 0;  //  图片的高度
    private int nPosX = 0;    //  图片所在的位置X
    private int nPosY = 0; //  图片所在的位置Y
    private float fAngle = 0.0f;    //  图片旋转
    private float fScale = 1.0f;    //  图片缩放 1.0表示为原图

    public ImageView(Context context, AttributeSet attr) {
        super(context,attr);

        paint = new Paint();
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        matrix = new Matrix();

    }

    public void setImg(Bitmap _bitmap){
        bitmap = _bitmap;
        bitmapDisplay = bitmap;
        nBitmapWidth = bitmap.getWidth();
        nBitmapHeight = bitmap.getHeight();
    }

    public void setPos(int x, int y) {
        nPosX = x;
        nPosY = y;
    }
    //  设置旋转比例
    public void setAngle(float _fAngle) {
        matrix.reset();
        fAngle = _fAngle;
        matrix.setRotate(fAngle);
        bitmapDisplay = Bitmap.createBitmap(bitmap,0,0,nBitmapWidth,nBitmapHeight,matrix,true);

    }

    //set size
    public void setSize(int _width, int _height){
        bitmap = Bitmap.createScaledBitmap(bitmap,_width,_height,true);
        bitmapDisplay = bitmap;
        nBitmapWidth = bitmap.getWidth();
        nBitmapHeight = bitmap.getHeight();
    }


    // set scale
    public void setScale(float _fScale) {
        matrix.reset();
        fScale = _fScale;
        matrix.postScale(fScale, fScale);
        bitmap = Bitmap.createBitmap(bitmap,0,0,nBitmapWidth,nBitmapHeight,matrix,true);
        bitmapDisplay = bitmap;
        nBitmapWidth = bitmap.getWidth();
        nBitmapHeight = bitmap.getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);



        canvas.drawBitmap(bitmapDisplay, nPosX, nPosY, paint);
        invalidate();
    }
}
