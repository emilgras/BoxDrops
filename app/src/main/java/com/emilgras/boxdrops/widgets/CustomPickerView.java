package com.emilgras.boxdrops.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.emilgras.boxdrops.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by emilgras on 04/06/2016.
 */
public class CustomPickerView extends LinearLayout implements View.OnTouchListener {

    public static final int LEFT = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM = 3;
    public static final int DELAY = 150;

    private Calendar mCalendar;
    private TextView mTextDate;
    private TextView mTextMonth;
    private TextView mTextYear;
    private SimpleDateFormat mFormatter;
    private boolean mIncrement;
    private boolean mDecrement;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (mIncrement) {
                increment(mActiveId);
            }
            if (mDecrement) {
                decrement(mActiveId);
            }
            if (mIncrement || mDecrement) {
                mHandler.sendEmptyMessageDelayed(MESSAGE_WHAT, DELAY);
            }
            return true;
        }
    });
    private int MESSAGE_WHAT = 123;
    private int mActiveId;


    // Used when implementing only in code
    public CustomPickerView(Context context) {
        super(context);
        init(context);
    }

    // Required if this picker view should be created from xml
    public CustomPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    // Required if this picker view should be created from xml - with style attr.
    public CustomPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.picker_view, this);
        mCalendar = Calendar.getInstance(); // Initialize
        mFormatter = new SimpleDateFormat("MMM");
    }



    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("super", super.onSaveInstanceState());
        bundle.putInt("date", mCalendar.get(Calendar.DATE));
        bundle.putInt("month", mCalendar.get(Calendar.MONTH));
        bundle.putInt("year", mCalendar.get(Calendar.YEAR));
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Parcelable) {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("super");
            int date = bundle.getInt("date");
            int month = bundle.getInt("month");
            int year = bundle.getInt("year");
            update(date, month, year, 0, 0, 0);
        }
        super.onRestoreInstanceState(state);
    }



    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTextDate = (TextView) this.findViewById(R.id.tv_date);
        mTextMonth = (TextView) this.findViewById(R.id.tv_month);
        mTextYear = (TextView) this.findViewById(R.id.tv_year);

        // Touch listeners
        mTextDate.setOnTouchListener(this);
        mTextMonth.setOnTouchListener(this);
        mTextYear.setOnTouchListener(this);

        int date = mCalendar.get(Calendar.DATE);
        int month = mCalendar.get(Calendar.MONTH);
        int year = mCalendar.get(Calendar.YEAR);
        update(date, month, year, 0, 0, 0);
    }

    private void update(int date, int month, int year, int hour, int minute, int second) {
        mCalendar.set(Calendar.DATE, date);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.HOUR, hour);
        mCalendar.set(Calendar.MINUTE, minute);
        mCalendar.set(Calendar.SECOND, second);
        mTextDate.setText(date + "");
        mTextMonth.setText(mFormatter.format(mCalendar.getTime()));
        mTextYear.setText(year + "");
    }

    public long getTime() {
        return mCalendar.getTimeInMillis();
    }




    // MARK: - OnTouchListener
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (v.getId()) {

            case R.id.tv_date:
                processEventsFor(mTextDate, event);
                break;

            case R.id.tv_month:
                processEventsFor(mTextMonth, event);
                break;

            case R.id.tv_year:
                processEventsFor(mTextYear, event);
                break;

        }




        return true;
    }


    private void processEventsFor(TextView textView, MotionEvent event) {

        Drawable[] drawables = textView.getCompoundDrawables();

        if (hasDrawableTop(drawables) && hasDrawableBottom(drawables)) {

            Rect topBounds = drawables[TOP].getBounds();
            Rect bottomBounds = drawables[BOTTOM].getBounds();
            float x = event.getX();
            float y = event.getY();
            mActiveId = textView.getId();

            if (topDrawableHit(textView, topBounds.height(), x, y)) {

                if (isActionDown(event)) {

                    // Increment value

                    mIncrement = true;

                    increment(textView.getId());

                    mHandler.removeMessages(MESSAGE_WHAT);
                    mHandler.sendEmptyMessageDelayed(MESSAGE_WHAT, DELAY);
                    toggleDrawable(textView, true);

                }

                if (isActionUpOrCancel(event)) {

                    mIncrement = false;
                    toggleDrawable(textView, false);

                }

            }
            else if (bottomDrawableHit(textView, bottomBounds.height(), x, y)) {

                if (isActionDown(event)) {

                    // Decrement value

                    mDecrement = true;

                    decrement(textView.getId());

                    mHandler.removeMessages(MESSAGE_WHAT);
                    mHandler.sendEmptyMessageDelayed(MESSAGE_WHAT, DELAY);
                    toggleDrawable(textView, true);

                }

                if (isActionUpOrCancel(event)) {

                    mDecrement = false;
                    toggleDrawable(textView, false);

                }

            }
            else {

                mDecrement = false;
                mIncrement = false;
                toggleDrawable(textView, false);

            }

        }

    }


    private void increment(int id) {
        switch (id) {
            case R.id.tv_date:
                mCalendar.add(Calendar.DATE, 1);
                break;
            case R.id.tv_month:
                mCalendar.add(Calendar.MONTH, 1);
                break;
            case R.id.tv_year:
                mCalendar.add(Calendar.YEAR, 1);
                break;
        }

        set(mCalendar);
    }

    private void decrement(int id) {
        switch (id) {
            case R.id.tv_date:
                mCalendar.add(Calendar.DATE, -1);
                break;
            case R.id.tv_month:
                mCalendar.add(Calendar.MONTH, -1);
                break;
            case R.id.tv_year:
                mCalendar.add(Calendar.YEAR, -1);
                break;
        }

        set(mCalendar);
    }

    public void set(Calendar calendar) {
        int date = calendar.get(Calendar.DATE);
        int year = calendar.get(Calendar.YEAR);
        mTextDate.setText(date + "");
        mTextYear.setText(year + "");
        mTextMonth.setText(mFormatter.format(mCalendar.getTime()));
    }


    private boolean isActionDown(MotionEvent event) {
        return event.getAction() == MotionEvent.ACTION_DOWN;
    }

    private boolean isActionUpOrCancel(MotionEvent event) {
        return event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL;
    }



    private boolean topDrawableHit(TextView textView, int drawableHeight, float x, float y) {
        int xMin = textView.getPaddingLeft();
        int xMax = textView.getWidth() - textView.getPaddingRight();
        int yMin = textView.getPaddingTop();
        int yMax = textView.getPaddingTop() + drawableHeight;
        return x > xMin && x < xMax && y > yMin && y < yMax;
    }

    private boolean bottomDrawableHit(TextView textView, int drawableHeight, float x, float y) {
        int xMin = textView.getPaddingLeft();
        int xMax = textView.getWidth() - textView.getPaddingRight();
        int yMax = textView.getHeight() - textView.getPaddingBottom();
        int yMin = yMax - drawableHeight;
        return x > xMin && x < xMax && y > yMin && y < yMax;
    }


    private boolean hasDrawableTop(Drawable[] drawables) {
        return drawables[TOP] != null;
    }

    private boolean hasDrawableBottom(Drawable[] drawables) {
        return drawables[BOTTOM] != null;
    }

    private void toggleDrawable(TextView textView, boolean pressed) {
        if (pressed) {
            if (mIncrement) {
                textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.up_pressed, 0, R.drawable.down_normal);
            }
            if (mDecrement) {
                textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.up_normal, 0, R.drawable.down_pressed);
            }
        }
        else {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.up_normal, 0, R.drawable.down_normal);
        }
    }
}














