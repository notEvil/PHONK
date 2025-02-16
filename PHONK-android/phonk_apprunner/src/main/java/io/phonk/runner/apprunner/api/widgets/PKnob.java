/*
 * Part of Phonk http://www.phonk.io
 * A prototyping platform for Android devices
 *
 * Copyright (C) 2013 - 2017 Victor Diaz Barrales @victordiaz (Protocoder)
 * Copyright (C) 2017 - Victor Diaz Barrales @victordiaz (Phonk)
 *
 * Phonk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Phonk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Phonk. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package io.phonk.runner.apprunner.api.widgets;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

import io.phonk.runner.apidoc.annotation.PhonkClass;
import io.phonk.runner.apprunner.AppRunner;
import io.phonk.runner.apprunner.api.common.ReturnInterface;
import io.phonk.runner.apprunner.api.common.ReturnObject;
import io.phonk.runner.base.utils.AndroidUtils;
import io.phonk.runner.base.views.CanvasUtils;

@PhonkClass
public class PKnob extends PCustomView implements PViewMethodsInterface, PTextInterface {
    private static final String TAG = PKnob.class.getSimpleName();

    public final StylePropertiesProxy props = new StylePropertiesProxy();
    public final KnobStyler styler;
    private final Typeface textStyle = Typeface.DEFAULT;
    private final boolean autoTextSize = false;
    private final DecimalFormat df;
    private ReturnInterface callbackDrag;
    private ReturnInterface callbackRelease;
    private Typeface textFont;
    private ArrayList touches;
    private float firstY;
    private float prevVal = 0;
    private float val = 0;
    private int mWidth;
    private int mHeight;
    private float mappedVal;
    private float unmappedVal;
    final OnDrawCallback mydraw = new OnDrawCallback() {
        @Override
        public void event(PCanvas c) {
            mWidth = c.width;
            mHeight = c.height;

            c.clear();
            c.cornerMode(false);

            int diameter = 0;
            if (c.width >= c.height) diameter = c.height;
            else diameter = c.width;
            // int halfdiameter = diameter / 2;

            int posX = c.width / 2;
            int posY = c.height / 2;

            c.noFill();

            c.strokeWidth(styler.knobBorderWidth);
            c.stroke(styler.knobBorderColor);
            c.ellipse(posX, posY, diameter - styler.knobBorderWidth, diameter - styler.knobBorderWidth);

            c.strokeWidth(AndroidUtils.dpToPixels(mAppRunner.getAppContext(), (int) styler.knobProgressWidth));
            c.stroke(styler.knobProgressColor); // styler.sliderBorderColor);

            float d = diameter - styler.knobBorderWidth - styler.knobProgressWidth - styler.knobProgressSeparation;
            c.arc(posX, posY, d, d, 180, unmappedVal, false);

            c.fill(styler.textColor);
            c.noStroke();
            c.typeface("monospace");

            df.setRoundingMode(RoundingMode.DOWN);
            if (autoTextSize) c.textSize((diameter / 5));
            else c.textSize(AndroidUtils.spToPixels(getContext(), (int) styler.textSize));

            c.drawTextCentered("" + df.format(mappedVal));
        }
    };
    private float rangeFrom = 0;
    private float rangeTo = 360;

    public PKnob(AppRunner appRunner, Map initProps) {
        super(appRunner, initProps);

        draw = mydraw;

        styler = new KnobStyler(appRunner, this, props);
        props.eventOnChange = false;
        props.put("knobBorderWidth", props, appRunner.pUtil.dpToPixels(1));
        props.put("knobProgressWidth", props, appRunner.pUtil.dpToPixels(2));
        props.put("knobProgressSeparation", props, appRunner.pUtil.dpToPixels(15));
        props.put("knobBorderColor", props, appRunner.pUi.theme.get("secondaryShade"));

        props.put("knobProgressColor", props, appRunner.pUi.theme.get("primary"));
        props.put("background", props, "#00FFFFFF");
        props.put("textColor", props, appRunner.pUi.theme.get("secondary"));
        props.put("textFont", props, "monospace");
        props.put("textSize", props, appRunner.pUtil.dpToPixels(4));
        Styler.fromTo(initProps, props);
        props.eventOnChange = true;
        styler.apply();

        df = new DecimalFormat("#.##");
        decimals(2);
    }

    public PKnob decimals(int num) {
        String formatString = "#.##";
        if (num > 0) formatString = "#." + new String(new char[num]).replace("\0", "#");
        df.applyPattern(formatString);
        df.setMinimumFractionDigits(num);
        df.setMinimumFractionDigits(num);
        return this;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                firstY = y;
                prevVal = val;
                return true;
            case MotionEvent.ACTION_MOVE:
                float delta = y - firstY;
                val = prevVal - delta;
                if (val < 0) val = 0;
                if (val > mHeight) val = mHeight;
                unmappedVal = CanvasUtils.map(val, 0, mHeight, 0, 360);
                mappedVal = CanvasUtils.map(val, 0, mHeight, rangeFrom, rangeTo);

                break;
            case MotionEvent.ACTION_UP:
                executeCallbackRelease();
                break;
            default:
                return false;
        }

        executeCallbackDrag();
        invalidate();

        return true;
    }

    private void executeCallbackRelease() {
        ReturnObject ret = new ReturnObject();
        ret.put("value", mappedVal);
        if (callbackRelease != null) callbackRelease.event(ret);
    }

    private void executeCallbackDrag() {
        ReturnObject ret = new ReturnObject();
        ret.put("value", mappedVal);
        if (callbackDrag != null) callbackDrag.event(ret);
    }

    public PKnob onChange(final ReturnInterface callbackfn) {
        this.callbackDrag = callbackfn;
        return this;
    }

    public PKnob onRelease(final ReturnInterface callbackfn) {
        this.callbackRelease = callbackfn;
        return this;
    }

    public PKnob range(float from, float to) {
        rangeFrom = from;
        rangeTo = to;

        return this;
    }

    public PKnob valueAndTriggerEvent(float val) {
        this.value(val);
        executeCallbackDrag();

        return this;
    }

    public PKnob value(float val) {
        this.mappedVal = val;
        this.unmappedVal = CanvasUtils.map(val, rangeFrom, rangeTo, 0, 360);

        this.invalidate();

        return this;
    }

    @Override
    public void set(float x, float y, float w, float h) {
        styler.setLayoutProps(x, y, w, h);
    }

    @Override
    public View textFont(Typeface font) {
        this.textFont = font;

        return this;
    }    @Override
    public void setProps(Map style) {
        styler.setProps(style);
    }

    @Override
    public View textSize(int size) {
        return null;
    }    @Override
    public Map getProps() {
        return props;
    }

    @Override
    public View textColor(String textColor) {
        return this;
    }

    @Override
    public View textColor(int textColor) {
        return this;
    }

    @Override
    public View textSize(float textSize) {
        return null;
    }

    @Override
    public View textStyle(int textStyle) {
        return this;
    }

    @Override
    public View textAlign(int alignment) {
        return this;
    }

    static class KnobStyler extends Styler {
        public float knobProgressSeparation;
        public float knobBorderWidth;
        public float knobProgressWidth;
        public int knobBorderColor;
        public int knobProgressColor;

        KnobStyler(AppRunner appRunner, View view, StylePropertiesProxy props) {
            super(appRunner, view, props);
        }

        @Override
        public void apply() {
            super.apply();

            knobProgressSeparation = toFloat(mProps.get("knobProgressSeparation"));
            knobBorderWidth = toFloat(mProps.get("knobBorderWidth"));
            knobProgressWidth = toFloat(mProps.get("knobProgressWidth"));
            knobBorderColor = Color.parseColor(mProps.get("knobBorderColor").toString());
            knobProgressColor = Color.parseColor(mProps.get("knobProgressColor").toString());
        }
    }




}
