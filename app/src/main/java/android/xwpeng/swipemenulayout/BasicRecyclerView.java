package android.xwpeng.swipemenulayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by xwpeng on 16-10-20.
 */

public class BasicRecyclerView extends RecyclerView {
    private ItemDecoration mItemDecoration;

    public BasicRecyclerView(Context context) {
        super(context);
        init();
    }

    public BasicRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BasicRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setDefaultLayoutManager();
        setFocusableInTouchMode(true);
        addOnScrollListener(new OnCScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    LinearLayoutManager llm = (LinearLayoutManager) getLayoutManager();
                    if (llm.findLastVisibleItemPosition() == llm.getChildCount() - 1) {
                        onBottom();
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    /**
     * 设置一个默认的{@link android.support.v7.widget.RecyclerView.LayoutManager LayoutManager}
     */
    public void setDefaultLayoutManager() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setLayoutManager(layoutManager);
    }

    public void removeDivider() {
        if (mItemDecoration != null){
            removeItemDecoration(mItemDecoration);
        }
    }

    public void setDivider(){
        setDivider(Color.GRAY);
    }

    public void setDivider(int color){
        setDivider(color, 1, 0, 0);
    }

    public void setDivider(int leftSpace, int rightSpace){
        setDivider(leftSpace, rightSpace, Color.GRAY);
    }

    public void setDivider(int leftSpace, int rightSpace, int color){
        setDivider(color, 1, leftSpace, rightSpace);
    }

    /**
     * 设置Divider
     * @param color 颜色
     * @param thickness 粗细
     * @param leftSpace 左边空白距离
     * @param rightSpace 右边空白距离
     */
    public void setDivider(int color, int thickness, final int leftSpace, final int rightSpace) {
        final Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(thickness);
        removeDivider();
        mItemDecoration = new RecyclerView.ItemDecoration() {

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDraw(c, parent, state);
            }

            @Override
            public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDrawOver(c, parent, state);
                for (int i = 0, size = parent.getChildCount() - 1; i < size; i++) {
                    View child = parent.getChildAt(i);
                    c.drawLine(child.getLeft() + leftSpace, child.getBottom(), child.getRight() - rightSpace, child.getBottom(), paint);
                }
            }

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
            }
        };
        addItemDecoration(mItemDecoration);
    }

    public static abstract class OnCScrollListener extends OnScrollListener {

        public void onBottom(){}
    }

}
