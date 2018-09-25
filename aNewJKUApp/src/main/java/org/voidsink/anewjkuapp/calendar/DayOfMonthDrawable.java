/*
 *       ____.____  __.____ ___     _____
 *      |    |    |/ _|    |   \   /  _  \ ______ ______
 *      |    |      < |    |   /  /  /_\  \\____ \\____ \
 *  /\__|    |    |  \|    |  /  /    |    \  |_> >  |_> >
 *  \________|____|__ \______/   \____|__  /   __/|   __/
 *                   \/                  \/|__|   |__|
 *
 *  Copyright (c) 2014-2018 Paul "Marunjar" Pretsch
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */
package org.voidsink.anewjkuapp.calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import org.voidsink.anewjkuapp.R;

import androidx.annotation.NonNull;

/**
 * A custom view to draw the day of the month in the today button in the options menu
 */
public class DayOfMonthDrawable extends Drawable {
    private String mDayOfMonth = "1";
    private final Paint mPaint;
    private final Rect mTextBounds = new Rect();

    public DayOfMonthDrawable(Context c) {
//        TypedArray themeArray = c.getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary});
//        int mColor = themeArray.getColor(0, Color.WHITE);
//        themeArray.recycle();

        float mTextSize = c.getResources().getDimension(R.dimen.today_icon_text_size);
        mPaint = new Paint();
        mPaint.setAlpha(255);
        mPaint.setColor(Color.WHITE);
        mPaint.setTypeface(Typeface.DEFAULT_BOLD);
        mPaint.setTextSize(mTextSize);
        mPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        mPaint.getTextBounds(mDayOfMonth, 0, mDayOfMonth.length(), mTextBounds);
        int textHeight = mTextBounds.bottom - mTextBounds.top;
        Rect bounds = getBounds();
        canvas.drawText(mDayOfMonth, bounds.right / 2, ((float) bounds.bottom + textHeight + 1) / 2,
                mPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        // Ignore
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    public void setDayOfMonth(int day) {
        mDayOfMonth = Integer.toString(day);
        invalidateSelf();
    }
}