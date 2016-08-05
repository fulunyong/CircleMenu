package com.gxuwz.android.menu.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.gxuwz.android.menu.R;
import com.gxuwz.android.menu.listener.OnMenuItemClickListener;

import java.util.ArrayList;

/**
 * Created by fulunyong on 2016/7/29 16:48
 * email:fulunyong@qq.com
 */

public class SemicircleMenu extends ViewGroup implements View.OnClickListener {

    private final String TAG="SemicircleMenu";

    /**
     * 圆的半径
     */
    private int mRadius;

    private int mItemRadius;//菜单选择的半径大小

    private int parentWidth;//控件的宽度
    private int parentHeight;//控件的高度

    private int textWidth;//左边文本控件的宽度

    private int textSize;//左边文本大小

    private int itemMenuSize;//菜单控件大小

    private double mAngleDelay;//菜单间的弧度大小

    private ArrayList<TextView> textViews = new ArrayList<>();//存放左边的textView

    private ArrayList<ImageView> imageViews = new ArrayList<>();//存放菜单选项

    private ImageView circleLarge;//大圆
    private ImageView circleSmall;//小圆
    private ImageView circleEnter;//指针


    /**
     * 布局时的开始弧度
     */
    private double mStartAngle =Math.PI/2;

    /**
     * 菜单项的文本
     */
    private String[] mItemTexts;

    /**
     * 菜单选项的个数
     */
    private int mMenuItemCount;

    /**
     * 点击事件监听回调
     */
    private OnMenuItemClickListener itemListener;



    //上一次触摸的x坐标
    private float mLastX;

    //上一次触摸的y坐标
    private float mLastY;


    /**
     * 检测按下到抬起时使用的时间
     */
    private long mDownTime;
    /**
     * 是否正在触摸或者滑动
     */
    private boolean isTouch;


    /**
     * 位置修正
     */
    private CorrectionPositionRunnable correctionRunnable;
    //当前选择的下标
    private int tempIndex;


    /**
     * 添加点击事件监听器
     * @param itemListener
     */
    public void addItemClickListener(OnMenuItemClickListener itemListener) {
        this.itemListener = itemListener;
    }


    /**
     * @param resIds item图片资源
     * @param texts  文本内容
     * @param txIds  文本背景图片
     */
    public void setData(int[] resIds, String[] texts, int[] txIds) {
        mItemTexts = texts;
        // 参数检查
        if (resIds == null || texts == null || null == txIds) {
            throw new IllegalArgumentException("菜单图片、文本以及背景图片资源均不能为空！");
        }
        if (texts.length != txIds.length) {
            throw new IllegalArgumentException("文本个数与背景图片资源个数不相等！");
        }

        mMenuItemCount = Math.min(resIds.length, texts.length);

        mAngleDelay=2*Math.PI/mMenuItemCount;

        textViews.clear();
        imageViews.clear();
        //添加
        for (int index = 0; index < mMenuItemCount; index++) {
            TextView textView = new TextView(getContext());
            textView.setTag(index);
            textView.setBackgroundResource(txIds[index]);
            textView.setOnClickListener(this);
            textView.getPaint().setFakeBoldText(true);
            textView.setTextColor(Color.WHITE);
            textViews.add(textView);
            ImageView imageView = new ImageView(getContext());
            imageView.setTag(index);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setImageResource(resIds[index]);
            imageView.setOnClickListener(this);
            imageViews.add(imageView);
        }
        // 重新初始化一次数据
        invalidate();
    }


    private void inti(Context context) {
        setPadding(0, 0, 0, 0);
        circleLarge = new ImageView(context);
        circleLarge.setImageResource(R.mipmap.circle_large);
        circleLarge.setScaleType(ImageView.ScaleType.FIT_XY);
        circleSmall = new ImageView(context);
        circleSmall.setImageResource(R.mipmap.circle_small);
        circleSmall.setScaleType(ImageView.ScaleType.FIT_XY);
        circleEnter = new ImageView(context);
        circleEnter.setImageResource(R.mipmap.circle_enter);
        circleEnter.setOnClickListener(this);
        circleEnter.setScaleType(ImageView.ScaleType.CENTER);
        setOnClickListener(this);
    }


    public SemicircleMenu(Context context) {
        super(context);
        inti(context);
    }


    public SemicircleMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        inti(context);
    }

    public SemicircleMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inti(context);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        /**
         * 根据传入的参数，分别获取测量模式和测量值
         */
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        /**
         * 如果宽或者高的测量模式非精确值
         */
        if (widthMode != MeasureSpec.EXACTLY
                || heightMode != MeasureSpec.EXACTLY) {
            // 主要设置为背景图的高度
            parentWidth = getSuggestedMinimumWidth();
            // 如果未设置背景图片，则设置为屏幕宽高的默认值
            parentWidth = parentWidth == 0 ? getDefaultWidth() : parentWidth;
        } else {
            // 如果都设置为精确值，则直接取小值；
            parentWidth = Math.min(width, height);

        }

        mRadius = parentWidth * 2 / 3;
        //高度设置为圆的直径
        parentHeight = 2 * mRadius;

        setMeasuredDimension(parentWidth, parentHeight);

        //计算左边默认的文本宽度
        textWidth = parentWidth / 15;
        //计算文本是字体
        textSize = parentWidth /23;

        //计算菜单控件的大小
        itemMenuSize = mRadius * 4 / 11;

        //计算选择菜单的真实半径
        mItemRadius = mRadius - (mRadius - mRadius * 6 / 11) / 2;

        removeAllViews();
        circleLarge.setLayoutParams(new LayoutParams(mRadius, parentHeight));
        circleLarge.setTag(-3);
        addView(circleLarge);
        circleSmall.setLayoutParams(new LayoutParams(mRadius * 6 / 11, mRadius));
        circleSmall.setTag(-2);
        addView(circleSmall);
        circleEnter.setLayoutParams(new LayoutParams(mRadius * 3 / 5, mRadius * 4 / 5));
        circleEnter.setTag(-1);
        addView(circleEnter);


        if (null != textViews && 0 < textViews.size()) {
            int textItemHeight = parentHeight / mMenuItemCount;
            for (TextView textView : textViews) {
                textView.setGravity(Gravity.CENTER|Gravity.CENTER_VERTICAL);
                textView.setLayoutParams(new LayoutParams(textWidth, textItemHeight));
                textView.setTextSize(px2sp(textSize));
                addView(textView);
            }
        }
        if (null != imageViews && 0 < imageViews.size()) {
            for (ImageView imageView : imageViews) {
                imageView.setLayoutParams(new LayoutParams(itemMenuSize, itemMenuSize));
                addView(imageView);
            }
        }


    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int height = bottom - top;
        int childCount = getChildCount();
        for (int index = 0; index < childCount; index++) {
            final View childAt = getChildAt(index);
            if (null != childAt) {
                int tag = (int) childAt.getTag();
                if (0 > tag) {//自有控件
                    int i = height - childAt.getLayoutParams().height;
                    switch (tag) {
                        case -1://enter 控件
                            childAt.layout(0,  i / 2, childAt.getLayoutParams().width, height - i / 2);
                            break;
                        case -2://小圆 白 控件
                            childAt.layout(0, i / 2, mRadius / 2, height - i / 2);
                            break;
                        case -3://大圆 灰 控件
                            childAt.layout(0, 0, mRadius, height);
                            break;
                    }
                } else {//菜单组件
                    mStartAngle %= 2*Math.PI;
                    if (childAt instanceof ImageView) {//图片处理
                        int x = (int) (mItemRadius * Math.sin(mStartAngle+tag*mAngleDelay));
                        int y = (int) (mItemRadius * Math.cos(mStartAngle+tag*mAngleDelay));
                        childAt.layout(x - itemMenuSize / 2, mRadius - y - itemMenuSize / 2, x + itemMenuSize / 2, mRadius - y + itemMenuSize / 2);
                    } else if (childAt instanceof TextView){//文本处理  在后面单独处理   此处不做处理

                    }
                }
            }
        }

        //文本矫正
        if (null!=textViews&&0<textViews.size()){
            for(TextView view:textViews){
                view.setText("");
                int viewTag = (int) view.getTag();
                view.layout(
                        right - view.getLayoutParams().width,
                        viewTag* view.getLayoutParams().height,
                        right + 5,
                        view.getLayoutParams().height * (viewTag + 1));
            }

            double tempAngle=mStartAngle;
            tempAngle-=Math.PI/2;
            tempAngle%=2*Math.PI;
            tempAngle+=2*Math.PI;
            tempAngle%=2*Math.PI;
            //tempAngle 肯定是一个正数

            tempIndex= (int)(tempAngle/mAngleDelay);
            tempIndex%=mMenuItemCount;
            if(mAngleDelay/2<tempAngle%mAngleDelay&&tempIndex!=mMenuItemCount-1){
                tempIndex++;
            }
            tempIndex+=textViews.size();
            tempIndex%=textViews.size();
            tempIndex=textViews.size()-tempIndex;
            tempIndex%=textViews.size();
            TextView textView = textViews.get(tempIndex);
            textView.setText(mItemTexts[tempIndex]);
            textView.layout(
                    right - 2*textView.getLayoutParams().width,
                    tempIndex* textView.getLayoutParams().height,
                    right + 5,
                    textView.getLayoutParams().height * (tempIndex + 1));
            textView.setGravity(Gravity.CENTER);
            int paddingTop = textView.getLayoutParams().height - mItemTexts[tempIndex].length() * sp2px(8);
            textView.setPadding(sp2px(10),paddingTop/sp2px(8),sp2px(10),0);
            textView.invalidate();

            //再次校验位置
            double temp=mStartAngle-Math.PI/2;
            temp%=Math.PI*2;
            temp+=Math.PI*2;
            temp%=Math.PI*2;

            if(mAngleDelay/3<Math.abs(temp-((int)textView.getTag())*mAngleDelay)&&!isTouch){
                if (null!=correctionRunnable){
                    removeCallbacks(correctionRunnable);
                }
                postDelayed(correctionRunnable=new CorrectionPositionRunnable(true),20);
            }
        }

    }

    /**
     * 获得默认该layout的尺寸
     *
     * @return
     */
    private int getDefaultWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return Math.min(outMetrics.widthPixels, outMetrics.heightPixels);
    }


    @Override
    public void onClick(View view) {
        if (circleEnter==view&&null!=itemListener){
            itemListener.onItemClick(view, tempIndex);
            return;
        }
        if (null!=view&&view instanceof TextView && null != view.getTag()){
            if(TextUtils.isEmpty(((TextView) view).getText())){//直接修正位置
                if (null!=correctionRunnable){
                    removeCallbacks(correctionRunnable);
                }
                mStartAngle=Math.PI/2+mAngleDelay*(mMenuItemCount-(int)view.getTag());
                requestLayout();
                return;
            }else if (null!=itemListener){
                itemListener.onItemClick(view, (Integer) view.getTag());
                return;
            }
        }
        if (null != view && null != itemListener && null != view.getTag()) {
            itemListener.onItemClick(view, (Integer) view.getTag());
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        float x = ev.getX();
        float y = ev.getY();

        switch (ev.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mDownTime = System.currentTimeMillis();
                if (null!=correctionRunnable){
                    removeCallbacks(correctionRunnable);
                }
                isTouch=true;
                break;
            case MotionEvent.ACTION_MOVE:
                isTouch=true;
                if (isInEnter(x,y)){//enter点击事件  跳过
                    return super.dispatchTouchEvent(ev);
                }
                long intervalTimeMove = System.currentTimeMillis() - mDownTime;
                float start = getAngle(mLastX,mLastY-mRadius);
                float end = getAngle(x,y-mRadius);

                float tempX = x - mLastX;
                float tempY = y - mLastY;
                double delay = (end - start)/(Math.PI*2);
                if(tempX>0){
                    if(tempY>0){
                        delay=Math.abs(delay);
                    }else {
                        delay=-Math.abs(delay);
                    }
                }else {
                    if(tempY>0){
                        delay=Math.abs(delay);
                    }else {
                        delay=-Math.abs(delay);
                    }
                }

                if (Math.abs(delay)<0.08||80>intervalTimeMove){//距离或者时间过短 判断为点击事件
                    return super.dispatchTouchEvent(ev);
                }
                mStartAngle+= delay;
                requestLayout();
                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                isTouch=false;
                mLastX = x;
                mLastY = y;
                long intervalTime = System.currentTimeMillis() - mDownTime;

                if (80>intervalTime){//时间过短  判断为点击事件
                    return super.dispatchTouchEvent(ev);
                }
                postDelayed(correctionRunnable=new CorrectionPositionRunnable(false),20);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 是否为点击事件
     * @param x
     * @param y
     * @return
     */
    private boolean isInEnter(float x,float y){
        LayoutParams layoutParams = circleEnter.getLayoutParams();
        int i = mRadius - layoutParams.height;
        if (x<=layoutParams.width&&y>=i/2&&y<mRadius+layoutParams.height/2){
            return true;
        }
        return false;
    }


    /**
     * 获取当前触摸点相对于圆心的角度
     * @param mTouchX 触摸点x坐标
     * @param mTouchY 触摸点y坐标
     * @return
     */
    private float getAngle(float mTouchX, float mTouchY)
    {
        double x = mTouchX - (mItemRadius / 2d);
        double y = mTouchY;
        return (float) (Math.asin(y/Math.hypot(x,y))*180/Math.PI);
    }

    /**
     * sp转px
     * @param spValue
     * @return
     */
    public int sp2px(double spValue){
        final float fontScale =getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    private int px2sp(float pxValue) {
        final float fontScale =getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 位置修正
     */
    private class  CorrectionPositionRunnable implements Runnable{
        //是否依据左边修正
        private boolean isLeftCorrection;

        public CorrectionPositionRunnable(boolean isLeftCorrection) {
            this.isLeftCorrection = isLeftCorrection;
        }

        @Override
        public void run(){
            double runAngle=mStartAngle;
            runAngle-=Math.PI/2;
            runAngle%=Math.PI*2;
            runAngle+=Math.PI*2;
            runAngle%=Math.PI*2;
            runAngle%=mAngleDelay;

            if(runAngle>mAngleDelay/2){//
                mStartAngle+=(mAngleDelay-runAngle);
            }else{
                mStartAngle-=runAngle;
            }

            if (isLeftCorrection){//依据左边下标修正
                isLeftCorrection=false;
            }
            //刷新布局
            if (Math.abs(runAngle)>mAngleDelay/50)
                requestLayout();
        }
    }
}