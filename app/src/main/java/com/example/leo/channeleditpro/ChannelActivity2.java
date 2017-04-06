package com.example.leo.channeleditpro;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Service;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by leo on 2017/3/1.
 */

public class ChannelActivity2 extends Activity implements View.OnClickListener {


    @BindView(R.id.btn_base_title_back)
    ImageView mBtnBaseTitleBack;
    @BindView(R.id.base_title)
    FrameLayout mBaseTitle;
    @BindView(R.id.my_category_tip_text)
    TextView mMyCategoryTipText;
    @BindView(R.id.btn_edit_channel)
    Button mBtnEditChannel;
    @BindView(R.id.rcv_drag)
    RecyclerView mRcvDrag;
    @BindView(R.id.rcv_nomal)
    RecyclerView mRcvNomal;
    private int clickItem = 0;

    private List<String> mUserList = new ArrayList<>();
    private List<String> mNomalList = new ArrayList<>();
    private DragRecycleAdapter mDragRecycleAdapter;
    private NomalRecycleAdapter mNomalRecycleAdapter;
    private AnimatorSet pathAnimator;
    private boolean mIsInanim;
    private Vibrator mVibrator;
    private View contentView;
    private View statusBar;


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentId());
        ButterKnife.bind(this);
        contentView = ButterKnife.findById(this, android.R.id.content);
        ScreenUtil.initScale(contentView);
        statusBar = ButterKnife.findById(this, R.id.statusBar);
        initStatusBar(statusBar);
        initView();
        initdata();
    }

    private void initStatusBar(View statusBar) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 透明状态栏
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        if (statusBar != null) {
            ViewGroup.LayoutParams layoutParams = statusBar.getLayoutParams();
            layoutParams.height = ScreenUtil.getStatusHeight(this);
            statusBar.setLayoutParams(layoutParams);
        }
    }

    private void initdata() {
        mUserList.addAll(Arrays.asList(getResources().getStringArray(R.array.newsTitle)));
        mDragRecycleAdapter.notifyDataSetChanged();
    }


    protected int getContentId() {
        return R.layout.activity_channel2;
    }


    protected void initView() {
        mBtnBaseTitleBack.setOnClickListener(this);
        mBtnEditChannel.setOnClickListener(this);
        mBtnEditChannel.setSelected(true);
        GridLayoutManager layout = new GridLayoutManager(this, 4);

        mRcvDrag.setLayoutManager(layout);
        mRcvNomal.setLayoutManager(new GridLayoutManager(this, 4));
        //mRcvNomal.getAdapter().get
        mDragRecycleAdapter = new DragRecycleAdapter(mUserList,this);
        mNomalRecycleAdapter = new NomalRecycleAdapter(mNomalList,this);

        mRcvDrag.setAdapter(mDragRecycleAdapter);
        mRcvNomal.setAdapter(mNomalRecycleAdapter);

        mRcvDrag.addOnItemTouchListener(new OnRecyclerItemClickListener(mRcvDrag) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                if (mDragRecycleAdapter.isDelIconShow()) {
                    if (!mIsInanim && vh.getLayoutPosition() != 0) {
                        startPahtAnim(vh.itemView, vh.getLayoutPosition(), mRcvDrag, mUserList, mRcvNomal, mNomalList);
                    }
                } else {
                    //如果不处于编辑状态 点击进入对应的新闻条目
                    clickItem = vh.getLayoutPosition();
                    finish();
                }
            }

            @Override
            public void onItemLongClick(RecyclerView.ViewHolder vh) {
                //判断被拖拽的是否是前两个，如果不是则执行拖拽
                if (vh.getLayoutPosition() != 0 ) {
                    dragHelper.startDrag(vh);
                    //获取系统震动服务
                    //震动70毫秒
                    if (!mDragRecycleAdapter.isDelIconShow()) {
                        mDragRecycleAdapter.notifyDelShow(true);
                        mBtnEditChannel.setText("完成");
                        mBtnEditChannel.setSelected(false);
                        mMyCategoryTipText.setText("拖动排序");
                    }
                    alertVibrate();
                }
            }
        });


        mRcvNomal.addOnItemTouchListener(new OnRecyclerItemClickListener(mRcvNomal) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                if (!mIsInanim) {

                    startPahtAnim(vh.itemView, vh.getLayoutPosition(),mRcvNomal,mNomalList,mRcvDrag,mUserList);
                }
            }

            @Override
            public void onItemLongClick(RecyclerView.ViewHolder vh) {

            }
        });

        dragHelper.attachToRecyclerView(mRcvDrag);

    }

    private void alertVibrate() {
        if (mVibrator == null) {
            mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        }
        mVibrator.vibrate(50);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // CubeUtils.showToast(this, "返回键");
                if (mDragRecycleAdapter.isDelIconShow()) {
                    alertVibrate();
                    mDragRecycleAdapter.notifyDelShow(false);
                    return true;
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startPahtAnim(final View tragetView, final int position, final RecyclerView fromView, List<String> fromList, final RecyclerView toView, final List<String> toList) {

        View lastCheckedChild = toView.getChildAt(toList.size() - 1);
       // ScreenUtil.initScale(lastCheckedChild);
        Rect rectLast = new Rect();
        int tragetX = 0;
        int tragetY = 0;
        int statusBarHeight = ScreenUtil.getStatusHeight(this);
        if (null != lastCheckedChild) {
            lastCheckedChild.getGlobalVisibleRect(rectLast);
            if (toList.size() % 4 == 0) {// 换行
                tragetX = toView.getLeft() + toView.getPaddingLeft() + 10;
                tragetY = rectLast.top + tragetView.getHeight() + 2 * 10 - statusBarHeight;
            } else {
                tragetX = rectLast.right + 10 * 2;
                tragetY = rectLast.top - statusBarHeight;
            }
        } else {
            rectLast.left = toView.getLeft() + toView.getPaddingLeft();
            rectLast.top = toView.getTop() + toView.getPaddingTop();
            tragetX = rectLast.left;
            tragetY = rectLast.top;
        }
        Rect rectTarget = new Rect();
        tragetView.getGlobalVisibleRect(rectTarget);
        final Point point = new Point();
        point.x = rectTarget.left;
        point.y = rectTarget.top - statusBarHeight;
        FloatItemViewManager.showFloatADwindow(this, tragetView, fromList.get(position), R.color.text_nomal_white, point);

        final Point updatePoint = new Point();
        ValueAnimator animX = ValueAnimator.ofFloat(point.x, tragetX);
        animX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float valueX = (float) animation.getAnimatedValue();
                updatePoint.x = (int) valueX;
                FloatItemViewManager.updateFloatViewPosition(updatePoint);
            }
        });
        ValueAnimator animY = ValueAnimator.ofFloat(point.y, tragetY);
        animY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float valueY = (float) animation.getAnimatedValue();
                updatePoint.y = (int) valueY;
                FloatItemViewManager.updateFloatViewPosition(updatePoint);
            }
        });
        final String removeItem = fromList.remove(position);
        pathAnimator = new AnimatorSet();
        pathAnimator.setDuration(200);
        pathAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
             //   tragetView.setVisibility(View.INVISIBLE);
                mIsInanim = true;
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                toList.add(removeItem);
                fromView.getAdapter().notifyItemRemoved(position);
               // tragetView.setVisibility(View.VISIBLE);
                FloatItemViewManager.removeFloawAdView(ChannelActivity2.this);
                toView.getAdapter().notifyItemInserted(toList.size() - 1);
                mIsInanim = false;
            }
            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        pathAnimator.play(animX).with(animY);
        pathAnimator.start();
    }


    ItemTouchHelper dragHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        /**
         * 是否处理滑动事件 以及拖拽和滑动的方向 如果是列表类型的RecyclerView的只存在UP和DOWN，如果是网格类RecyclerView则还应该多有LEFT和RIGHT
         * @param recyclerView
         * @param viewHolder
         * @return
         */
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
             int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
             int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
        }
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

            //得到当拖拽的viewHolder的Position
            int fromPosition = viewHolder.getAdapterPosition();
            //拿到当前拖拽到的item的viewHolder
            int toPosition = target.getAdapterPosition();

            if (fromPosition == 0) {
                return false;
            }
            if (toPosition == 0) {
                return false;
            }

            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mUserList, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mUserList, i, i - 1);
                }
            }
            mDragRecycleAdapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}
        /**
         * 重写拖拽可用
         * @return
         */
        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        /**
         * 长按选中Item的时候开始调用
         *
         * @param viewHolder
         * @param actionState
         */
        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder.getLayoutPosition() != 0) {
                super.onSelectedChanged(viewHolder, actionState);
                ViewCompat.setScaleX(viewHolder.itemView, 1.1f);
                ViewCompat.setScaleY(viewHolder.itemView, 1.1f);
                ((DragRecycleAdapter.DragHolder) viewHolder).mTextItem.setSelected(true);
            }
        }
        /**
         * 手指松开的时候还原
         * @param recyclerView
         * @param viewHolder
         */
        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.getLayoutPosition() == 0) {
                return;
            }
            super.clearView(recyclerView, viewHolder);
            ViewCompat.setScaleX(viewHolder.itemView, 1.0f);
            ViewCompat.setScaleY(viewHolder.itemView, 1.0f);
            ((DragRecycleAdapter.DragHolder) viewHolder).mTextItem.setSelected(false);

        }
        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            int position = viewHolder.getAdapterPosition();
            if (position != 0) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && position != 0) {
                float alpha = 1 - Math.abs(dX) / viewHolder.itemView.getWidth();
                viewHolder.itemView.setAlpha(alpha);
                viewHolder.itemView.setTranslationX(dX);
            }

        }
    });


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit_channel:

                changeItemState(mDragRecycleAdapter.isDelIconShow());

                break;
            case R.id.btn_base_title_back:

                if (mDragRecycleAdapter.isDelIconShow()) {
                    alertVibrate();
                    mDragRecycleAdapter.notifyDelShow(false);
                } else {
                    finish();
                }

                break;
        }
    }

    private void changeItemState(boolean delIconShow) {
        if (delIconShow) {
            //编辑状态
            mBtnEditChannel.setText("编辑");
            mBtnEditChannel.setSelected(true);
            mMyCategoryTipText.setText("切换频道");
            mDragRecycleAdapter.notifyDelShow(false);

        } else {
            //正常状态
            mBtnEditChannel.setSelected(false);
            mBtnEditChannel.setText("完成");
            mMyCategoryTipText.setText("拖动排序");
            alertVibrate();
            mDragRecycleAdapter.notifyDelShow(true);

        }

    }
}
