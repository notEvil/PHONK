/*
 * Part of Phonk http://www.phonk.io
 * A prototyping platform for Android devices
 *
 * Copyright (C) 2013 - 2017 Victor Diaz Barrales @victordiaz (Protocoder)
 * Copyright (C) 2017 - Victor Diaz Barrales @victordiaz (Phonk)
 *
 * Phonk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Phonk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Phonk. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package io.phonk.runner.apprunner.api;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.Map;

import io.phonk.runner.apidoc.annotation.PhonkClass;
import io.phonk.runner.apidoc.annotation.PhonkField;
import io.phonk.runner.apidoc.annotation.PhonkMethod;
import io.phonk.runner.apidoc.annotation.PhonkMethodParam;
import io.phonk.runner.apprunner.AppRunner;
import io.phonk.runner.apprunner.api.other.PProcessing;
import io.phonk.runner.apprunner.api.widgets.PAbsoluteLayout;
import io.phonk.runner.apprunner.api.widgets.PButton;
import io.phonk.runner.apprunner.api.widgets.PCustomView;
import io.phonk.runner.apprunner.api.widgets.PImage;
import io.phonk.runner.apprunner.api.widgets.PImageButton;
import io.phonk.runner.apprunner.api.widgets.PInput;
import io.phonk.runner.apprunner.api.widgets.PKnob;
import io.phonk.runner.apprunner.api.widgets.PLinearLayout;
import io.phonk.runner.apprunner.api.widgets.PList;
import io.phonk.runner.apprunner.api.widgets.PMap;
import io.phonk.runner.apprunner.api.widgets.PMatrix;
import io.phonk.runner.apprunner.api.widgets.PNumberPicker;
import io.phonk.runner.apprunner.api.widgets.PPlot;
import io.phonk.runner.apprunner.api.widgets.PLoader;
import io.phonk.runner.apprunner.api.widgets.PRadioButtonGroup;
import io.phonk.runner.apprunner.api.widgets.PScrollView;
import io.phonk.runner.apprunner.api.widgets.PSlider;
import io.phonk.runner.apprunner.api.widgets.PSpinner;
import io.phonk.runner.apprunner.api.widgets.PText;
import io.phonk.runner.apprunner.api.widgets.PTextList;
import io.phonk.runner.apprunner.api.widgets.PToggle;
import io.phonk.runner.apprunner.api.widgets.PToolbar;
import io.phonk.runner.apprunner.api.widgets.PTouchPad;
import io.phonk.runner.apprunner.api.widgets.PViewMethodsInterface;
import io.phonk.runner.apprunner.api.widgets.PViewPager;
import io.phonk.runner.apprunner.api.widgets.PWebView;
import io.phonk.runner.apprunner.api.widgets.StylePropertiesProxy;
import io.phonk.runner.base.utils.MLog;

@PhonkClass
public class PViewsArea extends ProtoBase {
    protected final AppRunner mAppRunner;
    protected Context mContext;

    // contains a reference of all views added to the absolute layout
    private ArrayList<View> viewArray = new ArrayList<>();

    // UI
    private StylePropertiesProxy mTheme;
    private boolean isScrollEnabled = false;
    protected PAbsoluteLayout uiAbsoluteLayout;
    private RelativeLayout uiHolderLayout;
    private PScrollView uiScrollView;

    public PViewsArea(AppRunner appRunner) {
        super(appRunner);
        mAppRunner = appRunner;
        mContext = appRunner.getAppContext();
    }

    public View initMainLayout() {
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        // this is the structure of the layout
        // uiHolderLayout (background color)
        // [scrollview] (isScrollEnabled)
        // [uiAbsoluteLayout]

        // set the holder
        uiHolderLayout = new RelativeLayout(getContext());
        uiHolderLayout.setLayoutParams(layoutParams);

        // We need to let the view scroll, so we're creating a scrollview
        uiScrollView = new PScrollView(getContext(), false);
        uiScrollView.setLayoutParams(layoutParams);
        // uiScrollView.setFillViewport(true);
        allowScroll(isScrollEnabled);

        // Create the main layout. This is where all the items actually go
        uiAbsoluteLayout = new PAbsoluteLayout(getAppRunner());
        uiAbsoluteLayout.setLayoutParams(layoutParams);
        uiScrollView.addView(uiAbsoluteLayout);
        uiHolderLayout.addView(uiScrollView);

        return uiHolderLayout;
    }

    public PAbsoluteLayout newAbsoluteLayout() {
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        PAbsoluteLayout pAbsoluteLayout = new PAbsoluteLayout(getAppRunner());
        pAbsoluteLayout.setLayoutParams(layoutParams);

        return pAbsoluteLayout;
    }

    /**
     * Changes the position cornerMode.
     * By default, Phonk places the widgets using normalized coordinates (0 to 1)
     * Sometimes we need a bit more control on how to create layouts so we can specify that the
     * views will lay out using pixel or dp (density independent pixels) units
     *
     * @param type pixels/dp/normalized
     * @exampleLink /examples/User Interface/Absolute Positioning
     * @advanced
     * @status OK
     */
    @PhonkMethod
    public void positionMode(String type) {
        uiAbsoluteLayout.mode(type);
    }

    /**
     * Adds a view created with new (newButton, newSlider, newText, etc) to the layout
     *
     * @param v view
     * @param x
     * @param y
     * @param w
     * @param h
     * @status TODO_EXAMPLE
     * @advanced
     */
    @PhonkMethod
    public View addView(View v, Object x, Object y, Object w, Object h) {
        boolean isAnimated = (boolean) mAppRunner.pUi.theme.get("animationOnViewAdd");

        if (isAnimated) {
            v.setAlpha(0);
            v.animate().alpha(1).setDuration(300).setStartDelay(100 * (1 + viewArray.size())).start();
        }

        viewArray.add(v);
        uiAbsoluteLayout.addView(v, x, y, w, h);

        return v;
    }

    public View addBtnView(Map props) {
        PViewMethodsInterface btn = (PViewMethodsInterface) newView("button");
        btn.setProps(props);

        float x1 = ((Number) btn.getProps().get("x")).floatValue();

        /*
        float y = ((Number) btn.getProps().get("y")).floatValue();
        float w = ((Number) btn.getProps().get("w")).floatValue();
        float h = ((Number) btn.getProps().get("h")).floatValue();
        */

        float x2 = (float) btn.getProps().get("x");
        MLog.d(TAG, "x1x2" + x1 + " " + x2);

        // this.addView((View) btn, x, y, w, h);

        return (View) btn;
    }

    /**
     * Remove all views in the layout
     *
     * @advanced
     * @status TODO_EXAMPLE
     */
    @PhonkMethod
    public void removeAllViews() {
        uiAbsoluteLayout.removeAllViews();
        viewArray.clear();
    }

    @PhonkMethod
    public void removeView(View v) {
        uiAbsoluteLayout.removeView(v);
    }

    /**
     * Allows the main interface to scroll up and down.
     * It's quite handy to lay out many widgets that cannot fit in a screen.
     *
     * @param scroll
     * @staus TODO_EXAMPLE
     */
    @PhonkMethod
    public void allowScroll(boolean scroll) {
        uiScrollView.setScrollingEnabled(scroll);
        isScrollEnabled = scroll;
    }

    /**
     * blablba
     *
     * @status TODO
     */
    @PhonkField
    public PToolbar toolbar;

    /**
     * Changes the background color using grayscale
     *
     * @param gray
     * @status TODO_EXAMPLE
     */
    @PhonkMethod
    public void background(int gray) {
        uiHolderLayout.setBackgroundColor(Color.rgb(gray, gray, gray));
    }

    /**
     * Changes the background color using RGB
     *
     * @param red
     * @param green
     * @param blue
     * @status TODO_EXAMPLE
     */
    @PhonkMethod
    public void background(int red, int green, int blue) {
        uiHolderLayout.setBackgroundColor(Color.rgb(red, green, blue));
    }

    /**
     * Changes the background color using RGB
     *
     * @param red
     * @param green
     * @param blue
     * @param alpha
     * @status TODO_EXAMPLE
     */
    @PhonkMethod
    public void background(int red, int green, int blue, int alpha) {
        uiHolderLayout.setBackgroundColor(Color.argb(alpha, red, green, blue));
    }

    /**
     * Changes the background color using Hexadecimal color
     *
     * @param c
     * @status TODO_EXAMPLE
     */
    @PhonkMethod
    public void background(String c) {
        uiHolderLayout.setBackgroundColor(Color.parseColor(c));
    }


    /**
     * Adds a button to the main screen
     *
     * @param label Text that appears in the button
     * @param x     Horizontal position
     * @param y     Vertical position
     * @param w     Width
     * @param h     Height
     * @return
     * @exampleLink /examples/User Interface/Basic Views
     * @status OK
     */
    @PhonkMethod
    public PButton addButton(String label, Object x, Object y, Object w, Object h) {
        PButton b = (PButton) newView("button");
        b.text(label);
        addView(b, x, y, w, h);
        return b;
    }

    @PhonkMethod
    public PButton addButton(String label, Object x, Object y) {
        PButton b = (PButton) newView("button");
        b.text(label);
        addView(b, x, y, -1, -1);
        return b;
    }

    private float toFloat(Object o) {
        return ((Number) o).floatValue();
    }

    /**
     * Adds a button to the main screen
     *
     * @param style
     * @return
     * @status TODO
    public PButton addButtonWithProperties(Map style) {
        PButton b = newButton("hi");
        b.setStyle(style);
        addViewAbsolute(b, toFloat(style.get("x")), toFloat(style.get("y")), toFloat(style.get("width")), toFloat(style.get("height")));

        return b;
    }
     */


    /**
     * Adds an imageButton
     *
     * @param imagePath
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @exampleLink /examples/User Interface/Basic Views
     * @status OK
     */
    @PhonkMethod
    public PImageButton addImageButton(String imagePath, Object x, Object y, Object w, Object h) {
        PImageButton pImageButton = (PImageButton) newView("imageButton");
        pImageButton.load(imagePath);
        addView(pImageButton, x, y, w, h);
        return pImageButton;
    }

    /**
     * @param imagePath
     * @param x
     * @param y
     * @return
     * @exampleLink /examples/User Interface/Basic Views
     * @status OK
     */
    @PhonkMethod
    public PImageButton addImageButton(String imagePath, Object x, Object y) {
        PImageButton pImageButton = (PImageButton) newView("imageButton");
        pImageButton.load(imagePath);
        addView(pImageButton, x, y, -1, -1);
        return pImageButton;
    }

    /**
     * Adds a text box.
     * If only x and y are provided, the bounding box will be as large as the text.
     * If x, y, w, h are provided, the text will accommodate to the bounding box defined.
     *
     * @param x
     * @param y
     * @return
     * @exampleLink /examples/User Interface/Basic Views
     * @status OK
     */
    @PhonkMethod(description = "", example = "")
    @PhonkMethodParam(params = {"label", "x", "y"})
    public PText addText(Object x, Object y) {
        PText tv = (PText) newView("text");
        addView(tv, x, y, -1, -1);
        return tv;
    }

    @PhonkMethod
    public PText addText(Object x, Object y, Object w, Object h) {
        PText tv = (PText) newView("text");
        addView(tv, x, y, w, h);
        return tv;
    }

    @PhonkMethod
    public PText addText(String text, Object x, Object y) {
        PText tv = (PText) newView("text");
        tv.setText(text);
        addView(tv, x, y, -1, -1);
        return tv;
    }

    @PhonkMethod
    public PText addText(String label, Object x, Object y, Object w, Object h) {
        PText tv = (PText) newView("text");
        tv.setText(label);
        addView(tv, x, y, w, h);
        return tv;
    }

    /**
     * Adds a text input
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @exampleLink /examples/User Interface/Basic Views
     * @status OK
     */
    @PhonkMethod
    public PInput addInput(Object x, Object y, Object w, Object h) {
        PInput et = (PInput) newView("input");
        addView(et, x, y, w, h);
        return et;
    }

    /**
     * Add a text area (which is the same as an input but multiline
     */
    @PhonkMethod
    public PInput addTextArea(Object x, Object y, Object w, Object h) {
        PInput et = (PInput) newView("textArea");
        addView(et, x, y, w, h);
        return et;
    }

    /**
     * Adds a toggle
     *
     * @param text
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @exampleLink /examples/User Interface/Basic Views
     * @status OK
     */
    @PhonkMethod
    public PToggle addToggle(final String[] text, Object x, Object y, Object w, Object h) {
        PToggle t = (PToggle) newView("toggle");

        if (text.length > 0) {
            t.text(text[0]);
            t.setTextOff(text[0]);
        }
        if (text.length > 1) {
            // tb.text(label[0]);
            t.setTextOn(text[1]);
        }
        addView(t, x, y, w, h);
        return t;
    }

    @PhonkMethod
    public PToggle addToggle(Object x, Object y, Object w, Object h) {
        PToggle t = (PToggle) newView("toggle");
        addView(t, x, y, w, h);
        return t;
    }

    @PhonkMethod
    public PToggle addToggle(String text, Object x, Object y, Object w, Object h) {
        PToggle t = (PToggle) newView("toggle");
        t.text(text);
        t.setTextOn(text);
        t.setTextOff(text);
        addView(t, x, y, w, h);
        return t;
    }

    /**
     * Creates a pager that contain slidable views
     *
     * @return
     * @status TODO
     * @advanced
     */
    @PhonkMethod(description = "Creates a new pager", example = "")
    public PViewPager addPager(Object x, Object y, Object w, Object h) {
        PViewPager pViewPager = (PViewPager) newView("pager");
        addView(pViewPager, x, y, w, h);
        return pViewPager;
    }

    public View getView() {
        return uiHolderLayout;
    }

    /**
     * Adds a slider. By default the range is [0, 1].
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @status TOREVIEW
     */
    @PhonkMethod
    public PSlider addSlider(Object x, Object y, Object w, Object h) {
        PSlider slider = (PSlider) newView("slider");
        addView(slider, x, y, w, h);
        return slider;
    }

    /**
     * Adds a knob
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @status TOREVIEW
     */
    @PhonkMethod
    public PKnob addKnob(Object x, Object y, Object w, Object h) {
        PKnob slider = (PKnob) newView("knob");
        addView(slider, x, y, w, h);
        return slider;
    }

    /**
     * Adds a matrix with M and N size. Useful when creating step sequencers and similar.
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @param m
     * @param n
     * @return
     * @status TOREVIEW
     */
    @PhonkMethod
    public PMatrix addMatrix(Object x, Object y, Object w, Object h, int m, int n) {
        PMatrix matrix = (PMatrix) newView("matrix");
        matrix.size(m, n);
        addView(matrix, x, y, w, h);
        return matrix;
    }

    /**
     * Add a loader
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @status TOREVIEW
     */
    @PhonkMethod
    public PLoader addLoader(Object x, Object y, Object w, Object h) {
        PLoader pb = (PLoader) newView("loader");
        addView(pb, x, y, w, -1);
        return pb;
    }

    /**
     * Adds a radio button group
     *
     * @param x
     * @param y
     * @return
     * @status TOREVIEW
     */
    @PhonkMethod
    public PRadioButtonGroup addRadioButtonGroup(Object x, Object y) {
        PRadioButtonGroup rbg = (PRadioButtonGroup) newView("radioButtonGroup");
        addView(rbg, x, y, -1, -1);
        return rbg;
    }

    @PhonkMethod
    public PRadioButtonGroup addRadioButtonGroup(Object x, Object y, Object w, Object h) {
        PRadioButtonGroup rbg = (PRadioButtonGroup) newView("radioButtonGroup");
        addView(rbg, x, y, w, h);
        return rbg;
    }

    /**
     * Adds an image
     *
     * @param x
     * @param y
     * @return
     * @status TOREVIEW
     */
    @PhonkMethod
    public PImage addImage(Object x, Object y) {
        final PImage iv = (PImage) newView("image");
        addView(iv, x, y, -1, -1);
        return iv;
    }

    @PhonkMethod
    public PImage addImage(Object x, Object y, Object w, Object h) {
        PImage iv = (PImage) newView("image");
        addView(iv, x, y, w, h);
        return iv;
    }

    @PhonkMethod
    public PImage addImage(String imagePath, Object x, Object y) {
        PImage iv = (PImage) newView("image");
        iv.load(imagePath);
        addView(iv, x, y, -1, -1);
        return iv;
    }

    @PhonkMethod
    public PImage addImage(String imagePath, Object x, Object y, Object w, Object h) {
        PImage iv = (PImage) newView("image");
        iv.load(imagePath);
        addView(iv, x, y, w, h);
        return iv;
    }

    /**
     * Add a choice box
     *
     * @param array
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @status TOREVIEW
     */
    @PhonkMethod
    public PSpinner addChoiceBox(final String[] array, Object x, Object y, Object w, Object h) {
        PSpinner pSpinner = (PSpinner) newView("choiceBox");
        pSpinner.setData(array);
        // pSpinner.setPrompt("qq");

        addView(pSpinner, x, y, w, h);
        return pSpinner;
    }

    /**
     * Adds a numberpicker
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @status TOREVIEW
     */
    @PhonkMethod
    public PNumberPicker addNumberPicker(Object x, Object y, Object w, Object h) {
        PNumberPicker pNumberPicker = (PNumberPicker) newView("numberPicker");
        addView(pNumberPicker, x, y, w, h);

        return pNumberPicker;
    }

    /*
    public Object addCam(Object x, Object y, Object w, Object h) {
        PCameraXOne cameraX = new PCameraXOne(getAppRunner());
        // cameraX.start();
        // addViewAbsolute((View) cameraX, x, y, w, h);
        // return cameraX;

        FrameLayout fl = new FrameLayout(getContext());
        fl.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        fl.setId(200 + (int) (200 * Math.random()));

        // Add the view
        addViewAbsolute(fl, x, y, w, h);

        // PCameraX p = new PCameraX();

        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.add(fl.getId(), cameraX, String.valueOf(fl.getId()));
        ft.commit();

        // return p;
        return null;
    }
     */

    /**
     * Adds a webview
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @status TOREVIEW
     */
    @PhonkMethod
    public PWebView addWebView(Object x, Object y, Object w, Object h) {
        PWebView webView = (PWebView) newView("webView");
        addView(webView, x, y, w, h);
        return webView;
    }

    /**
     * Adds a plot, by default the range is [0, 1]
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @status TOREVIEW
     */
    @PhonkMethod
    public PPlot addPlot(Object x, Object y, Object w, Object h) {
        PPlot pPlot = (PPlot) newView("plot");
        addView(pPlot, x, y, w, h);
        return pPlot;
    }

    /**
     * Creates a new touch pad
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @status TOREVIEW
     */
    @PhonkMethod
    public PTouchPad addTouchPad(Object x, Object y, Object w, Object h) {
        PTouchPad taV = (PTouchPad) newView("touchPad");
        addView(taV, x, y, w, h);
        return taV;
    }

    /**
     * Adds a canvas view
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @status TOREVIEW
     */
    @PhonkMethod
    public PCustomView addCanvas(Object x, Object y, Object w, Object h) {
        final PCustomView canvasView = (PCustomView) newView("canvas"); // (int) w, (int) h);
        addView(canvasView, x, y, w, h);
        return canvasView;
    }

    /**
     * Adds a map widget using OpenStreetMap
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @status TOREVIEW
     */
    @PhonkMethod(description = "", example = "")
    @PhonkMethodParam(params = {"x", "y", "w", "h"})
    public PMap addMap(Object x, Object y, Object w, Object h) {
        PMap mapView = (PMap) newView("map");
        addView(mapView, x, y, w, h);
        return mapView;
    }

    /**
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @status TOREVIEW
     */
    @PhonkMethod
    public PList addGrid(Object x, Object y, Object w, Object h, int numCols) {
        PList list = (PList) newView("list");
        list.numColumns(numCols);
        addView(list, x, y, w, h);

        return list;
    }

    /**
     * Adds a list of views
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @status TOREVIEW
     */
    @PhonkMethod
    public PList addList(Object x, Object y, Object w, Object h) {
        PList list = (PList) newView("list");
        list.numColumns(1);
        addView(list, x, y, w, h);

        return list;
    }

    public PTextList addTextList(Object x, Object y, Object w, Object h) {
        PTextList pTextList = new PTextList(mAppRunner);
        addView(pTextList, x, y, w, h);

        return pTextList;
    }


    /**
     * Adds a processing view.
     * This Processing view acts quite similar to Processing.org for Android.
     * In fact it is the same, although some things work a bit differently to play well with the rest of the framework.
     * <p>
     * - All the Processing functions prepend the Processing (p) object.
     * p.fill(255); p.stroke(); p.rect();
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @exampleLink /examples/Others/Processing
     */
    @PhonkMethod(description = "", example = "")
    @PhonkMethodParam(params = {"x", "y", "w", "h"})
    public PProcessing addProcessingCanvas(Object x, Object y, Object w, Object h) {
        // Create the main layout. This is where all the items actually go
        FrameLayout fl = new FrameLayout(getContext());
        fl.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        fl.setId(200 + (int) (200 * Math.random()));

        // Add the view
        addView(fl, x, y, w, h);

        PProcessing p = new PProcessing(getAppRunner());

        // TOFIX
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        // ft.add(fl.getId(), p, fl.getId());
        ft.add(fl.getId(), p, String.valueOf(fl.getId()));
        ft.commit();

        return p;
    }

    /**
     * Adds a linear layout
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     */
    @PhonkMethod
    public PLinearLayout addLinearLayout(Object x, Object y) {
        PLinearLayout pLinearLayout = (PLinearLayout) newView("linearLayout");
        addView(pLinearLayout, x, y, -1, -1);

        return pLinearLayout;
    }

    @PhonkMethod
    public PLinearLayout addLinearLayout(Object x, Object y, Object w, Object h) {
        PLinearLayout pLinearLayout = (PLinearLayout) newView("linearLayout");
        addView(pLinearLayout, x, y, w, h);

        return pLinearLayout;
    }


    /**
     * Adds a view created with newView
     *
     * @param v
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     * @advanced
     */
    @PhonkMethod
    public View add(View v, Object x, Object y, Object w, Object h) {
        addView(v, x, y, w, h);
        return v;
    }

    public View newView(String viewName) {
        switch (viewName) {
            case "linearLayout":
                return new PLinearLayout(mAppRunner);
            case "list":
                return new PList(mAppRunner);
            case "map":
                return new PMap(mAppRunner);
            case "canvas":
                return new PCustomView(mAppRunner);
            case "touchPad":
                return new PTouchPad(mAppRunner);
            case "plot":
                return new PPlot(mAppRunner);
            case "webView":
                return new PWebView(mAppRunner);
            case "numberPicker":
                return new PNumberPicker(mAppRunner);
            case "choiceBox":
                return new PSpinner(mAppRunner);
            case "image":
                return new PImage(mAppRunner);
            case "radioButtonGroup":
                return new PRadioButtonGroup(mAppRunner);
            case "loader":
                return new PLoader(mAppRunner);
            case "matrix":
                return new PMatrix(mAppRunner);
            case "knob":
                return new PKnob(mAppRunner);
            case "slider":
                return new PSlider(mAppRunner);
            case "pager":
                return new PViewPager(mAppRunner);
            case "toggle":
                return new PToggle(mAppRunner);
            case "input":
                PInput input = new PInput(mAppRunner);
                input.setMaxLines(1);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                return input;
            case "textArea":
                PInput textArea = new PInput(mAppRunner);
                textArea.setGravity(Gravity.TOP | Gravity.LEFT);
                return textArea;
            case "text":
                return new PText(mAppRunner);
            case "button":
                return new PButton(mAppRunner);
            case "imageButton":
                return new PImageButton(mAppRunner);
            default:
                return null;
        }
    }

    @Override
    public void __stop() {
    }
}
