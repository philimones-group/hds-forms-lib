package org.philimone.hds.forms.listeners;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class OnSwipeListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    public OnSwipeListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener(){
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return this.gestureDetector.onTouchEvent(event);
    }

    public void onSwipeLeft() {

    }

    public void onSwipeRight() {

    }

    public void onSwipeBottom(){

    }

    public void onSwipeTop(){

    }


}
