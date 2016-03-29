package com.willing.android.xyz.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import com.willing.android.xyz.R;
import com.willing.android.xyz.entity.LrcLine;
import com.willing.android.xyz.entity.Lyric;
import com.willing.android.xyz.utils.LrcParser;


/**
 * Created by Willing on 2015/10/15 0015.
 */
public class LrcView extends View
{
    private static final int VELOCITY = 1000;

    private static final String NO_LYRIC = "无歌词信息";

    private Paint mPaint;
    private int mFontHeight;
    private int mLeading = 2; // 行间距
    private int mCurLrcColor = 0xff0000;


    private Scroller mScroller;
    private VelocityTracker mTracker;

    // 是否正在滚动
    private boolean mIsScrolling;
    // 当滚动歌词后，隔一段时间后，会把当前歌词行移动到中心，该变量表示滚动后，间隔的时间是否到达。
    private volatile boolean mIsOverTime = true;
    private int mDownY;


    private Lyric mLyric;
    private int mCurTime; //
    private int mTimeIndex; // 当前时间对应的索引值
    private String mPath;

    private Runnable mLrcCenterTask;
    private Handler mHandler;
    private String musicName;


    public LrcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LrcView(Context context) {
        this(context, null, 0);
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        TypedArray arr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.lrcView, defStyleAttr, 0);

        mLeading = (int) arr.getDimension(R.styleable.lrcView_leading, 2);

        mCurLrcColor = arr.getColor(R.styleable.lrcView_curLrcColor, 0xff0000);
        // 初始化
        mPaint = new Paint();
        mPaint.setTextSize(arr.getDimension(R.styleable.lrcView_textSize, 20));
        mPaint.setColor(arr.getColor(R.styleable.lrcView_textColor, 0));
        mPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetricsInt fontMetricsInt = mPaint.getFontMetricsInt();
        mFontHeight = fontMetricsInt.descent - fontMetricsInt.ascent + fontMetricsInt.leading + mLeading;

        mScroller = new Scroller(context);

        mLrcCenterTask = new LrcCenterTask(this);
        mHandler = new Handler(Looper.myLooper());
    }

    public void setPath(String path)
    {
        mPath = path;
        mLyric = null;

        invalidate();
    }
    public String getPath()
    {
        return mPath;
    }

    public Lyric getLyric()
    {
        return mLyric;
    }

    // 毫秒
    public void setCurTime(int time)
    {
        time = getTimeFromTime(time);
        if (time != mCurTime)
        {
            mCurTime = time;
            invalidate();
        }
    }

    // 必须在设置歌词前调用
    public void setTextSize(float size)
    {
        mPaint.setTextSize(size);
        Paint.FontMetricsInt fontMetricsInt = mPaint.getFontMetricsInt();
        mFontHeight = fontMetricsInt.descent - fontMetricsInt.ascent + fontMetricsInt.leading + mLeading;

    }
    public float getTextSize()
    {
        return mPaint.getTextSize();
    }

    public void setTextColor(int color)
    {
        mPaint.setColor(color);
    }
    public int getTextColor()
    {
        return mPaint.getColor();
    }

    public void setCurLrcColor(int color)
    {
        mCurLrcColor = color;
    }

    public int getCurLrcColor()
    {
        return mCurLrcColor;
    }

    public int getLeading()
    {
        return mLeading;
    }

    // 必须在设置歌词前调用
    public void setLeading(int leading)
    {
        mLeading = leading;
    }

    void setOverTime(boolean time)
    {
        mIsOverTime = time;
    }



    @Override
    protected void onDraw(Canvas canvas)
    {
        if (mLyric == null)
        {
            mLyric = LrcParser.parse(mPath, getWidth() - getPaddingLeft() - getPaddingRight(), mPaint);
            if (mLyric == null)
            {
                LrcParser.fetchFromNet(musicName, getWidth() - getPaddingLeft() - getPaddingRight(), mPaint, new Callback() {
                    @Override
                    public void onSuccess(Lyric lyric) {
                        mLyric = lyric;
                        postInvalidate();
                    }
                });
            }
        }
        int y = mFontHeight - mPaint.getFontMetricsInt().descent;
        int x = (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();

        int contentY = getHeight() - getPaddingBottom() - getPaddingTop();


        // 没有歌词时
        if (mLyric == null || mLyric.getLrcs().size() == 0)
        {
            Rect bounds = new Rect();
            mPaint.getTextBounds(NO_LYRIC, 0, NO_LYRIC.length(), bounds);

            y = (contentY - bounds.height()) / 2 + getPaddingTop() - mPaint.getFontMetricsInt().ascent;

            canvas.drawText(NO_LYRIC, 0, NO_LYRIC.length(), x, y, mPaint);

            return;
        }

        // 可以绘制的行数
        int drawTime = contentY / mFontHeight + 2;

        // lines当前已经绘制的行数
        int lines = 0;

        int startIndex = getScrollY() / mFontHeight;
        startIndex = Math.max(0, startIndex);


        if (!mIsScrolling && mIsOverTime)
        {
            // 计算中间行的索引
            startIndex = mTimeIndex - drawTime / 2;
            startIndex = Math.max(0, startIndex);
        }

        y = y + (getScrollY() / mFontHeight * mFontHeight) + getPaddingTop();

        // 防止绘制到内容区域以外
        canvas.clipRect(getPaddingLeft(), getPaddingTop() + getScrollY(), getWidth() - getPaddingRight(),
                contentY + getPaddingTop() + getScrollY());

        for (; startIndex < mLyric.getLrcs().size(); ++startIndex)
        {
            LrcLine line = mLyric.getLrcs().get(startIndex);

            if (line.getTime() == mCurTime)
            {
                int savedColor = mPaint.getColor();
                mPaint.setColor(mCurLrcColor);
                canvas.drawText(line.getLine(), 0, line.getLine().length(), x, y, mPaint);
                mPaint.setColor(savedColor);
            }
            else
            {
                canvas.drawText(line.getLine(), 0, line.getLine().length(), x, y, mPaint);
            }
            y += mFontHeight;

            lines++;
            if (lines >= drawTime)
            {
                break;
            }
        }

    }

    private int getTimeFromTime(int time)
    {
        if (mLyric == null)
        {
            return 0;
        }
        int lastFoundIndex = -1;
        int begin = 0;
        int end = mLyric.getLrcs().size() - 1;

        int mid = 0;
        while (begin <= end)
        {
            mid = (begin + end) / 2;

            if (time == mLyric.getLrcs().get(mid).getTime() + mLyric.getOffset())
            {
                lastFoundIndex = mid;
                end = mid - 1;
            }
            else if (time > mLyric.getLrcs().get(mid).getTime() + mLyric.getOffset())
            {
                begin = mid + 1;
            }
            else
            {
                end = mid - 1;
            }
        }

        if (lastFoundIndex == -1)
        {
            lastFoundIndex = Math.min(begin, mLyric.getLrcs().size() - 1);
        }
        while (lastFoundIndex > 0 && mLyric.getLrcs().get(lastFoundIndex).getTime() > time)
        {
            lastFoundIndex--;
        }
        mTimeIndex = lastFoundIndex;

        return mLyric.getLrcs().get(lastFoundIndex).getTime();
    }

    @Override
    public void computeScroll()
    {
        if (mScroller.computeScrollOffset())
        {
            scrollTo(0, mScroller.getCurrY());
            invalidate();
        }
        else
        {
            mIsScrolling = false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int action = event.getAction();
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:

                mDownY = (int) event.getY();

                if (mTracker == null)
                {
                    mTracker = VelocityTracker.obtain();
                }
                else
                {
                    mTracker.clear();
                }
                mTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                mTracker.addMovement(event);

                mIsScrolling = true;
                mIsOverTime = false;

                int detalY = (int) (mDownY - event.getY());


                int y = Math.min(getMaxScrollHeight(), detalY + getScrollY());
                y = Math.max(0, y);
                scrollTo(getScrollX(), y);

                mDownY = (int) event.getY();

                break;
            case MotionEvent.ACTION_UP:
                mTracker.addMovement(event);
                mTracker.computeCurrentVelocity(VELOCITY, ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity());

                if (mLyric == null)
                {
                    break;
                }
                if (Math.abs(mTracker.getYVelocity()) >= ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity())
                {
                    mScroller.fling(0, mScroller.getCurrY(), 0, -(int)mTracker.getYVelocity(),
                            0, 0, 0, getMaxScrollHeight());

                }
                else
                {
                    mIsScrolling = false;
                }

                mHandler.removeCallbacks(mLrcCenterTask);
                mHandler.postDelayed(mLrcCenterTask, 5000);

                mTracker.recycle();
                mTracker = null;
                invalidate();

                break;
            case MotionEvent.ACTION_CANCEL:
                mTracker.recycle();
                mTracker = null;
                mIsScrolling = false;
                break;
        }
        return true;
    }

    // 可以滚动的最大高度
    private int getMaxScrollHeight()
    {
        int maxHeight = 0;
        if (mLyric != null)
        {
            maxHeight = mFontHeight * mLyric.getLrcs().size() - getHeight() + getPaddingBottom() + mFontHeight * 2;
        }

        return maxHeight;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getMusicName() {
        return musicName;
    }


    private static class LrcCenterTask implements Runnable
    {
        private LrcView	mView;

        public LrcCenterTask(LrcView view)
        {
            mView = view;
        }

        @Override
        public void run()
        {

            if (mView != null)
            {
                mView.setOverTime(true);
            }

        }

    }

    public interface Callback
    {
        void onSuccess(Lyric lyric);
    }
}

