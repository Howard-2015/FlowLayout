package org.itheima.library;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by i 林海东 on 2015/11/27.
 */
public class FlowLayout
        extends ViewGroup
{
    private Line mCurrentLine;
    private int        mHorizontalSpace = 15;//水平间隙
    private int        mVerticalSpace   = 15;//垂直的间隙
    private List<Line> mLines           = new ArrayList<>();

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FlowLayout(Context context) {
        super(context);
    }

    public void setSpace(int horizontal,int vertical){
        this.mHorizontalSpace=horizontal;
        this.mVerticalSpace=vertical;
    }



    //复写测量的方法

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //清空行
        mLines.clear();
        mCurrentLine=null;

        int width         = MeasureSpec.getSize(widthMeasureSpec);
        int childMaxWidth = width - getPaddingLeft() - getPaddingRight();
        //测量孩子
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            //测量孩子
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            //将孩子添加记录到航中
            if (mCurrentLine == null) {
                mCurrentLine = new Line(childMaxWidth, mHorizontalSpace);
                //将line添加到布局中
                mLines.add(mCurrentLine);
                //将child添加到line中
                mCurrentLine.addChild(child);
            } else {
                if (mCurrentLine.canAdd(child)) {
                    mCurrentLine.addChild(child);
                } else {
                    mCurrentLine = new Line(childMaxWidth, mHorizontalSpace);
                    mLines.add(mCurrentLine);
                    mCurrentLine.addChild(child);
                }
            }
        }
        //设置自己的宽度和高度
        int height = getPaddingBottom() + getPaddingTop();
        for (int i = 0; i < mLines.size(); i++) {
            Line line = mLines.get(i);
            height += line.mLineHeight;

            if (i != mLines.size() - 1) {
                height += mVerticalSpace;
            }
        }

        setMeasuredDimension(width, height);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //测量孩子，以line为对象
        int top = getPaddingTop();
        for (int i = 0; i < mLines.size(); i++) {
            Line line = mLines.get(i);
            line.layout(getPaddingLeft(), top);
            top += line.mLineHeight + mVerticalSpace;
        }
    }

    private class Line {
        //用来记录每行中的控件和相关的属性,每行有的个数的view
        private List<View> mViews = new ArrayList<>();

        private int mLineHeight;
        private int mLineMaxWidth;
        private int mLineUsedWidth;
        private int mSpace;//控件之间的间隙,有设置设定

        public Line(int maxWidth, int space) {
            this.mLineMaxWidth = maxWidth;
            this.mSpace = space;
        }

        /**
         * 判断是否可以添加孩子
         * @param view
         * @return
         */
        public boolean canAdd(View view) {
            if (mViews.size() == 0) {
                return true;
            }
            int childWidth = view.getMeasuredWidth();
            if (mLineUsedWidth + childWidth + mSpace <= mLineMaxWidth) {
                return true;
            }
            return false;
        }

        /**
         * 调用方法之前，是否可以调用，通过canAdd方法判断。
         * @param view
         */

        public void addChild(View view) {
            int childWidth  = view.getMeasuredWidth();
            int childHeight = view.getMeasuredHeight();
            if (mViews.size() == 0) {
                //计算宽度
                mLineUsedWidth = childWidth;
                //计算高度
                mLineHeight = childHeight;
            } else {
                mLineUsedWidth += childWidth + mSpace;
                mLineHeight = mLineHeight > childHeight
                              ? mLineHeight
                              : childHeight;

            }


            mViews.add(view);//记录view
        }

        public void layout(int left, int top) {
            //判断是否有多余的空间
            int extraWidth=mLineMaxWidth-mLineUsedWidth;
            //获得每个view可以分到多少的平均值
            int avgWidth= (int) (extraWidth*1f/mViews.size()+0.5f);
            for (int i = 0; i < mViews.size(); i++) {
                View child = mViews.get(i);
                int childWidth=child.getMeasuredWidth();
                int childHeight=child.getMeasuredHeight();
                //先期望孩子的宽高
                if (avgWidth>0){
                    int childWidthSpec=MeasureSpec.makeMeasureSpec(childWidth+avgWidth,MeasureSpec.EXACTLY);
                    int childHeightSpec=MeasureSpec.makeMeasureSpec(childHeight,MeasureSpec.EXACTLY);

                    //设置期望值
                    child.measure(childWidthSpec,childHeightSpec);

                    childWidth=child.getMeasuredWidth();
                    childHeight=child.getMeasuredHeight();
                }
                int l = left;
                int t = (int) (top+(mLineHeight-childHeight)/2f+0.5f);
                int r = l + childWidth;
                int b = t + childHeight;

                child.layout(l, t, r, b);
                left += childWidth + mSpace;
            }
        }
    }
}
