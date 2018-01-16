package net.bjcore.read.diaread;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.gson.Gson;

import net.bjcore.read.diaread.adapter.StoryRecycleAdapter;
import net.bjcore.read.diaread.entity.Content;
import net.bjcore.read.diaread.entity.Role;
import net.bjcore.read.diaread.entity.Story;
import net.bjcore.read.diaread.entity.StoryResult;
import net.bjcore.read.diaread.util.JsonUitl;
import net.bjcore.read.diaread.view.behavior.ControlScrollLinearlayoutManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class StoryActivity extends Activity implements View.OnClickListener, StoryRecycleAdapter.OnSmoothMoveLitener {


    private RecyclerView mRecyclerView;
    private CoordinatorLayout coordinatorLayout;
    private ControlScrollLinearlayoutManager mLayoutManager;
    private TextView title_view, collection_view;
    private StoryRecycleAdapter storyAdapter;
    private ArrayList<Story> mList = new ArrayList<>();

    float mPosX, mPosY = 0;
    float mCurPosX, mCurPosY = 0;
    private View mClickView;
    private int screenHeight;

    private FrameLayout toolBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        Fresco.initialize(this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.root_layout);
        title_view = ((TextView) findViewById(R.id.story_title_view));
        collection_view = ((TextView) findViewById(R.id.story_collection_view));

        // recycler
        mRecyclerView = ((RecyclerView) findViewById(R.id.recycler_view));
        mLayoutManager = new ControlScrollLinearlayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        toolBarLayout = (FrameLayout) findViewById(R.id.toolbar_layout);
        storyAdapter = new StoryRecycleAdapter(this);
        mRecyclerView.setAdapter(storyAdapter);
        storyAdapter.setData(mList);
        WindowManager wm = this.getWindowManager();

        screenHeight = wm.getDefaultDisplay().getHeight();
        // 点击区
        mClickView = LayoutInflater.from(this).inflate(R.layout.item_dialog_empty_footer, mRecyclerView, false);
        storyAdapter.addFooterView(mClickView);
        mClickView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 这里就是点击, 区分一下框在不在
                int addPosition = storyAdapter.getAddPosition();
                Log.e("GAO", "CLICK addPosition =" + addPosition);
                if (addPosition != -1) {
                    // 框在,关掉框的处理
                    storyAdapter.removeDialog(addPosition);
                } else {
                    // 框不在,追加item
                    Log.e("GAO", "CLICK");
                    drawData();
                }
            }
        });


/*
        recyclerview.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector b = new GestureDetector(StoryActivity.this, new MyGestureDetector());
            public final boolean onTouch(View paramAnonymousView, MotionEvent paramAnonymousMotionEvent) {
                this.b.onTouchEvent(paramAnonymousMotionEvent);
                return false;
            }
        });
*/

        findViewById(R.id.more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            public final boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        mPosX = motionEvent.getX();
                        mPosY = motionEvent.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        mCurPosX = motionEvent.getX();
                        mCurPosY = motionEvent.getY();
                        break;

                    case MotionEvent.ACTION_UP:
                        int addPosition2 = storyAdapter.getAddPosition();
                        if (mCurPosY - mPosY > 0 && (Math.abs(mCurPosY - mPosY) > 10)) {
                            //向下滑動
                            if (addPosition2 != -1) {
                                // 框在,禁止滑动
                                return true;
                            }
                            Log.e("GAO", "DOWN");
                            // 第一个可见位置
                            int firstItem = mRecyclerView.getChildPosition(mRecyclerView.getChildAt(0));
                            Log.e("GAO", "DOWN 第一个" + firstItem);
                            // 这里判断到达顶部时做向上翻页,结果集追加到list里面,coordinate应该记录为list最后一个的id
                           /* if(firstItem < 3 && contentResult.size()> 0 && contentResult.get(0).getId() != 1){
                                String start =  "" + (contentResult.get(0).getId() -20);
                                fetchContent(start,false);
                            }*/

                        } else if (mCurPosY - mPosY < 0
                                && (Math.abs(mCurPosY - mPosY) > 10)) {
                            //向上滑动
                            if (addPosition2 != -1) {
                                // 框在,禁止滑动
                                return true;
                            }
                            // 框不在,（且处于底部时）追加item

                            // 第一个可见位置
                            int firstItem = mRecyclerView.getChildPosition(mRecyclerView.getChildAt(0));
                            // 最后一个可见位置
                            int lastItem = mRecyclerView.getChildPosition(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1));
                            // 可见的个数
                            int childCount = mRecyclerView.getChildCount();
                            // 全部item个数
                            int itemCount = storyAdapter.getItemCount();
                            Log.e("GAO", String.format("( %d - %d )  , / %d / %d", firstItem, lastItem, childCount, itemCount));
                            if (lastItem > storyAdapter.getItemCount() - 2) {
                                drawData();
                            }

                        } else {
                            // 这里就是点击
                            if (addPosition2 != -1) {
                                // 框在,关掉框的处理
                                storyAdapter.removeDialog(addPosition2);
                                return true;
                            }
                            // 框不在,追加item
                            drawData();
                        }
                        break;

                }
                return false;
            }
        });

        initData();
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int addPosition = storyAdapter.getAddPosition();
                if (addPosition != -1) {
                    mLayoutManager.setScrollEnabled(false);
                } else {
                    mLayoutManager.setScrollEnabled(true);
                }

            }
        });
    }

    StoryResult storyResult = null;
    private int index = 0;

    private void initData() {

        index = 0;
        mList.clear();
        // 加载raw数据
        String json = JsonUitl.getJson(this, R.raw.s1);
        loadJson(json);
    }


    private void drawData() {

        if (storyResult == null || index == storyResult.list.size()) {
            // 末尾
            return;
        }
        Story story = new Story();
        Content content = storyResult.list.get(index);
        if (content.role_id == null) {
            story.setRoleType(0);
        } else {
            // 主角 副角 及姓名
            setStoryRole(content.role_id, story);
        }
        story.setContent(content.content);
        mList.add(story);
        // from Kevin
        storyAdapter.notifyItemInserted(mList.size() - 1);
        mRecyclerView.scrollToPosition(mList.size());

        index++;
    }


    private void setStoryRole(int roleId, Story story) {

        for (Role role : storyResult.roles) {
            if (role.id == roleId) {
                story.setName(role.name);
                story.setHead(role.head);
                story.setRoleType((role.lead != null && role.lead == 1) ? 1 : 2);
                story.setComment(new Random().nextInt(50) - 30);
                break;
            }
        }

    }

    //R.raw.s1 初始化几个数组  角色  内容
    public void loadJson(String json) {

        try {
            JSONObject object = new JSONObject(json);
            JSONObject data = object.getJSONObject("data");
            storyResult = new Gson().fromJson(data.toString(), StoryResult.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 初始化
        title_view.setText(storyResult.novel.title);
        collection_view.setText(storyResult.chapter.title);
        drawData();
    }

    @Override
    public void onClick(View v) {
    }

    // 手势- 咱不用
    private final class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        private MyGestureDetector() {
        }

        public final boolean onDown(MotionEvent paramMotionEvent) {
            Log.e("GAO", "onDown");
            // 这里就是点击, 区分一下框在不在
            int addPosition = storyAdapter.getAddPosition();
            if (addPosition != -1) {
                // 框在,关掉框的处理
                storyAdapter.removeDialog(addPosition);
                return true;
            }
            // 框不在,追加item
            drawData();
            return super.onDown(paramMotionEvent);
        }

        public final void onLongPress(MotionEvent paramMotionEvent) {

        }

//        public final boolean onScroll(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2){
//            int addPosition = storyAdapter.getAddPosition();
//            if (addPosition != -1) {
//                return true;
//            }
//            if (paramFloat2 < 0.0F) {
//                Log.e("GAO","########UP");
//            }else {
//                Log.e("GAO","########Down");
//                //drawData();
//            }
//            return false;
//        }

    }

    // from Kevin
    public void onSmoothMove(final int position) {
        int firstItem = mRecyclerView.getChildPosition(mRecyclerView.getChildAt(0));
        int lastItem = mRecyclerView.getChildPosition(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1));
        if (position < firstItem) {
            mRecyclerView.smoothScrollToPosition(position);
        } else if (position <= lastItem) {
            int movePosition = position - firstItem;
            if (movePosition >= 0 && movePosition < mRecyclerView.getChildCount()) {
                int top = mRecyclerView.getChildAt(movePosition).getTop();
                int viewHeight = mRecyclerView.getChildAt(movePosition).getHeight();
                //计算需要移动的高度 当前item距离顶部的高度 － （屏幕的高度 －（当前item的高度＋评论框的高度＋标题的高度））
                int moveTop = top - (screenHeight - viewHeight - dip2px(470) - toolBarLayout.getHeight());
                mRecyclerView.scrollBy(0, moveTop);
            }
        } else {
            mRecyclerView.smoothScrollToPosition(position);
        }
    }

    // 为了想禁止滚动,没起作用
    public void setShouldScroll(boolean scroll) {
//        mRecyclerView.setNestedScrollingEnabled(scroll);
    }

    public int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public void setRecyclerViewScroll(boolean isCanScroll) {
        mLayoutManager.setScrollEnabled(isCanScroll);
    }
}
