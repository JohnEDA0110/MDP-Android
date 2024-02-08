package com.omkar.controller.ui.dashboard.models;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class ObstacleView extends View {

    private Direction direction;
    private boolean identified = false;
    private int id = 0;

    public ObstacleView(Context context) {
        super(context);
    }

    public ObstacleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObstacleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(direction == null || direction == Direction.NORTH){
            drawNorth(canvas);
        } else if(direction == Direction.SOUTH){
            drawSouth(canvas);
        } else if(direction == Direction.EAST){
            drawEast(canvas);
        } else if(direction == Direction.WEST){
            drawWest(canvas);
        }
    }

    public void drawSouth(Canvas canvas){
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1);

        // Calculate the height of the green portion (20% of the total height)
        int greenHeight = (int) (0.2 * getHeight());

        // Draw the bottom 80% in black
        canvas.drawRect(0, 0, getWidth(), getHeight(), getBlackPaint());

        // Draw the bottom 20% in green
        canvas.drawRect(0, getHeight() - greenHeight, getWidth(), getHeight(), getGreenPaint());

        canvas.drawRect(0, 0, getWidth(), getHeight(), borderPaint);

        if(id != 0){
            Paint textPaint = new Paint();
            textPaint.setColor(identified? Color.GREEN : Color.RED);
            textPaint.setTextSize(20);
            // draw the text in the center of the black region
            canvas.drawText(id + "", (getHeight()-greenHeight)/2, getWidth()/2, textPaint);
        }
    }

    public void drawWest(Canvas canvas){
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1);
        canvas.drawRect(0, 0, getWidth(), getHeight(), borderPaint);

        // Calculate the height of the green portion (20% of the total height)
        int greenWidth = (int) (0.2 * getWidth());

        canvas.drawRect(0, 0, getWidth(), getHeight(), getBlackPaint());

        canvas.drawRect(0, 0, greenWidth, getHeight(), getGreenPaint());

        if(id != 0){
            Paint textPaint = new Paint();
            textPaint.setColor(identified? Color.GREEN : Color.RED);
            textPaint.setTextSize(20);
            canvas.drawText(id + "", greenWidth * 1.05f, canvas.getHeight()/2, textPaint);
        }
    }

    public void drawNorth(Canvas canvas){
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1);
        canvas.drawRect(0, 0, getWidth(), getHeight(), borderPaint);

        // Calculate the height of the green portion (20% of the total height)
        int greenHeight = (int) (0.2 * getHeight());

        // Draw the top 20% in green
        canvas.drawRect(0, 0, getWidth(), greenHeight, getGreenPaint());

        // Draw the bottom 80% in black
        canvas.drawRect(0, greenHeight, getWidth(), getHeight(), getBlackPaint());

        if(id != 0){
            Paint textPaint = new Paint();
            textPaint.setColor(identified? Color.GREEN : Color.RED);
            textPaint.setTextSize(20);
            canvas.drawText(id + "", canvas.getWidth()/2, (greenHeight + ((getHeight() - greenHeight)/2))*1.05f, textPaint);
        }
    }

    public void drawEast(Canvas canvas){
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1);
        canvas.drawRect(0, 0, getWidth(), getHeight(), borderPaint);

        // Calculate the height of the green portion (20% of the total height)
        int greenWidth = (int) (0.2 * getWidth());

        canvas.drawRect(0, 0, getWidth(), getHeight(), getBlackPaint());

        canvas.drawRect(getWidth()-greenWidth, 0, getWidth(), getHeight(), getGreenPaint());

        if(id != 0){
            Paint textPaint = new Paint();
            textPaint.setColor(identified? Color.GREEN : Color.RED);
            textPaint.setTextSize(20);
            canvas.drawText(id + "", (canvas.getWidth() - greenWidth)/6, canvas.getHeight()/2, textPaint);
        }
    }

    public void redraw(Direction direction) {
        this.direction = direction;
        invalidate();
    }

    public void setId(int id){
        this.id = id;
        invalidate();
    }

    public void obstacleHasBeenIdentified(int id){
        this.identified = true;
        this.id = id;
        invalidate();
    }

    private Paint getGreenPaint() {
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        return paint;
    }

    private Paint getBlackPaint() {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        return paint;
    }
}
