package org.shirakumo.ocelot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.view.View;

import org.shirakumo.lichat.Payload;

public class PayloadView extends View {
    Bitmap image;
    Movie movie;
    long start = 0;

    public PayloadView(Context context, Payload payload){
        super(context);
        if(payload.contentType.equals("image/gif")){
            movie = Movie.decodeByteArray(payload.data, 0, payload.data.length);
        }else{
            image = BitmapFactory.decodeByteArray(payload.data, 0, payload.data.length, null);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(movie != null)
            setMeasuredDimension(movie.width()*2, movie.height()*2);
        else if(image != null)
            setMeasuredDimension(image.getWidth()*2, image.getHeight()*2);
        else
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawContent(canvas);
    }

    private void drawContent(Canvas canvas){
        canvas.scale(2f, 2f);
        if (movie != null) {
            long now = android.os.SystemClock.uptimeMillis();
            if (start == 0) { start = now; }

            int dur = movie.duration();
            if (dur <= 0) {
                dur = 1000;
            }

            movie.setTime((int)((now - start) % dur));

            movie.draw(canvas, 0, 0);
            invalidate();
        }else if(image != null){
            canvas.drawBitmap(image, 0, 0, null);
        }
    }
}
