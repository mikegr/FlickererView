/*
        Copyright 2016 BeeOne GmbH

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/

package at.beeone.flickerer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class FlickerView extends View {

    private static final String TAG = FlickerView.class.getSimpleName();
    Path triangle1;
    Path triangle2;
    private boolean[] state;
    private Paint whitePaint;
    private Paint blackPaint;
    private Rect rect1;
    private Rect rect2;
    private Rect rect3;
    private Rect rect4;
    private Rect rect5;
    private float xdpi;

    private static final double CM_IN_INCH = 2.54;
    private static final double DESIRED_WIDTH_IN_CM = 5.25;
    private static final double DESIRED_WIDTH_IN_INCH = DESIRED_WIDTH_IN_CM / CM_IN_INCH;
    private static final double MIN_INCH = 5 / CM_IN_INCH;
    private static final double BOX_WIDTH_IN_CM = 1.05;
    private static final double BOX_WIDTH_IN_INCH = BOX_WIDTH_IN_CM / CM_IN_INCH;
    private static final double BOX_HEIGHT_IN_CM = 2.0;
    private static final double BOX_HEIGHT_IN_INCH = BOX_HEIGHT_IN_CM / CM_IN_INCH;
    private static final double TRIAGLE_MIDDLE_DISTANCE_IN_INCH = BOX_WIDTH_IN_INCH / 2 + BOX_WIDTH_IN_INCH + BOX_WIDTH_IN_INCH *7/8;
    private static final double TRIAGLE_HEIGHT_IN_INCH = 0.5 /CM_IN_INCH;

    private boolean screenNotMinWidth;
    private boolean running = true;
    private int w;
    private int h;
    private int desiredWidth;
    private int desiredHeight;
    private int triangleHeight;

    public FlickerView(Context context) {
        super(context);
        init();
    }

    public FlickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FlickerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        defaultDisplay.getMetrics(dm);
        xdpi = dm.xdpi;
        int extraBottomPadding = (int) (10 * dm.density);

        triangleHeight = (int) (BOX_HEIGHT_IN_INCH/6 * xdpi);
        desiredWidth = (int) (xdpi * DESIRED_WIDTH_IN_INCH);
        desiredHeight = (int) (xdpi * BOX_HEIGHT_IN_INCH + triangleHeight + extraBottomPadding);

        Log.v(TAG, "widthPixels: " + dm.widthPixels);

        Log.v(TAG, "xdpi:" + xdpi);
        whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whitePaint.setStyle(Paint.Style.FILL);
        whitePaint.setColor(0xffffffff);
        whitePaint.setTextSize(24*xdpi/160);

        blackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blackPaint.setStyle(Paint.Style.FILL);
        blackPaint.setColor(0xff000000);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPaint(blackPaint);

        if (!running) {
            canvas.drawText("Stopped", 0, h/2, whitePaint);
            return;
        }

        if (screenNotMinWidth) {
            canvas.drawText("NOT ENOUGH SPACE", 0, h/2, whitePaint);
            return;
        }

        canvas.drawPath(triangle1, whitePaint);
        canvas.drawPath(triangle2, whitePaint);


        canvas.drawRect(rect1, state[0] ? whitePaint : blackPaint);
        canvas.drawRect(rect2, state[1] ? whitePaint : blackPaint);
        canvas.drawRect(rect3, state[2] ? whitePaint : blackPaint);
        canvas.drawRect(rect4, state[3] ? whitePaint : blackPaint);
        canvas.drawRect(rect5, state[4] ? whitePaint : blackPaint);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
        Log.v(TAG, "canvas width: " + w);

        double minWidth = MIN_INCH * xdpi;
        Log.v(TAG, "minWidth: " + minWidth);

        screenNotMinWidth = w < minWidth;

        int middleX = w/2;
        Log.v(TAG, "middleX: " + middleX);

        Log.v(TAG, "triangleHeight: " + triangleHeight);
        int triangleMiddleDistance = (int) (TRIAGLE_MIDDLE_DISTANCE_IN_INCH * xdpi);
        Log.v(TAG, "triangleMiddleDistance: " + triangleMiddleDistance);
        int triangleLength = (int) ((int) (triangleHeight * 2) / Math.sqrt(3));
        Log.v(TAG, "triangleLength: " + triangleLength);
        triangle1 = createTriangle(middleX - triangleMiddleDistance , triangleHeight, triangleLength);
        triangle2 = createTriangle(middleX + triangleMiddleDistance, triangleHeight, triangleLength);

        int boxWidth = (int) (BOX_WIDTH_IN_INCH * xdpi);
        Log.v(TAG, "boxWidth: " + boxWidth);
        int boxHeight = (int) (BOX_HEIGHT_IN_INCH * xdpi);
        Log.v(TAG, "boxHeight: " + boxHeight);

        int startX = (int) (middleX - boxWidth * 2.5);

        rect1 = createRect(startX, triangleHeight, boxWidth, boxHeight);
        rect2 = createRect(startX + boxWidth, triangleHeight, boxWidth, boxHeight);
        rect3 = createRect(startX + boxWidth * 2, triangleHeight, boxWidth, boxHeight);
        rect4 = createRect(startX + boxWidth * 3, triangleHeight, boxWidth, boxHeight);
        rect5 = createRect(startX + boxWidth * 4, triangleHeight, boxWidth, boxHeight);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }
        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    /**
     *
     * @param x x of top/left corner
     * @param y y of top/left corner
     * @param width length of square
     * @return
     */
    private Rect createRect(int x, int y, int width, int height) {
        return new Rect(x + width/8, y, x + width - width/8, y + height);
    }

    /**
     *
     * @param x bottom point
     * @param y bottom point
     * @param len lenght of one side
     * @return
     */
    private Path createTriangle(int x, int y, int len) {
        Path path = new Path();

        int h = ((int) (len*Math.sqrt(3)))/2;
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(x, y);
        path.lineTo(x - len / 2, y - h);
        path.lineTo(x + len/2, y - h);
        path.lineTo(x, y);
        path.close();
        Log.v(TAG, "Triangle: " + path.toString());
        return path;
    }

    public void setState(boolean[] state) {
        this.state = state;
        this.running = true;
        postInvalidate();
    }

    public void stopped() {
        this.running = false;
        postInvalidate();
    }
}
