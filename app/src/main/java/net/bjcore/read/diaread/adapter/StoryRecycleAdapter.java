package net.bjcore.read.diaread.adapter;

import android.app.Activity;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.bjcore.read.diaread.R;
import net.bjcore.read.diaread.StoryActivity;
import net.bjcore.read.diaread.entity.Story;
import net.bjcore.read.diaread.entity.TComment;
import net.bjcore.read.diaread.util.JsonUitl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StoryRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity mContext;
    private ArrayList<Story> mList = new ArrayList<>();
    private OnSmoothMoveLitener onSmoothMoveLitener;
    private int addPosition = -1;


    public StoryRecycleAdapter(OnSmoothMoveLitener litener) {
        onSmoothMoveLitener = litener;
        mContext = (Activity) litener;
        mFooterViews = new SparseArray<>();
    }

    public static enum ITEM_TYPE {
        ITEM_TYPE_ASIDE,
        ITEM_TYPE_RIGHT,
        ITEM_TYPE_LEFT,
        ITEM_TYPE_DIGLOG
    }

    public void setData(ArrayList<Story> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public int getAddPosition() {
        return addPosition;
    }

    public void setAddPosition(int addPosition) {
        this.addPosition = addPosition;
    }

    // 把弹框layout当做一个item
    public void removeDialog(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
        addPosition = -1;
        ((StoryActivity) mContext).setRecyclerViewScroll(true);
    }

    public void addDialog(int position) {
        addPosition = position;
        mList.add(position, null);
        notifyItemInserted(position);
    }

    private SparseArray<View> mFooterViews;
    // 基本的底部类型开始位置  用于viewType
    private static int BASE_ITEM_TYPE_FOOTER = 20000000;

    /**
     * 是不是底部类型
     */
    private boolean isFooterViewType(int viewType) {
        int position = mFooterViews.indexOfKey(viewType);
        return position >= 0;
    }

    /**
     * 创建底部的ViewHolder
     */
    private RecyclerView.ViewHolder createFooterViewHolder(View view) {
        return new RecyclerView.ViewHolder(view) {
        };
    }

    /**
     * 是不是底部位置
     */
    private boolean isFooterPosition(int position) {
        return position >= mList.size();
    }

    /**
     * 添加底部
     */
    public void addFooterView(View view) {
        int position = mFooterViews.indexOfValue(view);
        if (position < 0) {
            mFooterViews.put(BASE_ITEM_TYPE_FOOTER++, view);
        }
        notifyDataSetChanged();
    }

    /**
     * 移除底部
     */
    public void removeFooterView(View view) {
        int index = mFooterViews.indexOfValue(view);
        if (index < 0) return;
        mFooterViews.removeAt(index);
        notifyDataSetChanged();
    }

    /**
     * 根据具体的viewType绑定布局
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 先判断底部
        if (isFooterViewType(viewType)) {
            View footerView = mFooterViews.get(viewType);
            return createFooterViewHolder(footerView);
        }

        if (viewType == ITEM_TYPE.ITEM_TYPE_DIGLOG.ordinal()) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.item_dialog_comment, parent, false);
            // 评论框布局啊
            return new CommentViewHolder(v);
        }
        // 左右
        else if (viewType == ITEM_TYPE.ITEM_TYPE_RIGHT.ordinal()) {
            return new MsgViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_dialog_qq_right_text, parent, false));
        } else if (viewType == ITEM_TYPE.ITEM_TYPE_LEFT.ordinal()) {
            return new MsgViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_dialog_qq_left_text, parent, false));
        } else {
            return new AsideViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_dialog_weixin_aside, parent, false));
        }
    }

    private ArrayList<TComment> tcommentResult = new ArrayList<>();
    TCommentListAdapter dataAdapter;
    ListView mListView;
    View mEmptyView;
    private int lastPos;
    private String content_type_id;

    private void drawDiglogData(String type_id) {
        // 实际上draw的是前一条内容的 comment list
        dataAdapter = new TCommentListAdapter(mContext, tcommentResult);
        // 定位id是要拼出来的

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (isFooterPosition(position)) {
            return;
        }
        if (addPosition == position) {
            // 评论区 todo 显示评论
            ((CommentViewHolder) holder).cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 关闭窗口
                    if (addPosition != -1) {
                        removeDialog(addPosition);
                        onSmoothMoveLitener.setShouldScroll(true);
                    }
                }
            });
            // 评论框
            ((CommentViewHolder) holder).ll_add_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            // dialog item出现后,再初始化
            mListView = ((CommentViewHolder) holder).listView;
            mEmptyView = ((CommentViewHolder) holder).emptyView;
            mListView.setAdapter(dataAdapter);

            // 刷新数据
            fetchData();

        } else if (holder instanceof AsideViewHolder) {
            ((AsideViewHolder) holder).content.setText(mList.get(position).getContent());
        } else {
            ((MsgViewHolder) holder).text_view.setText(mList.get(position).getContent());
            ((MsgViewHolder) holder).name_view.setText(mList.get(position).getName());
            ((MsgViewHolder) holder).avatar_view.setImageURI(Uri.parse(mList.get(position).getHead()));
            int count = mList.get(position).getComment();
            if (count > 0) {
                ((MsgViewHolder) holder).comment_count.setVisibility(View.VISIBLE);
                ((MsgViewHolder) holder).comment_count.setText("" + count);
            } else {
                ((MsgViewHolder) holder).comment_count.setVisibility(View.GONE);
                ((MsgViewHolder) holder).comment_count.setText("");
            }

            //设置item的text_view的点击事件,其他位置不响应
            ((MsgViewHolder) holder).text_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    // 开关弹框item
                    if (addPosition != -1) {
                        removeDialog(addPosition);
                    } else {
                        lastPos = pos;
                        addDialog(pos + 1);
                        drawDiglogData("");
                        onSmoothMoveLitener.onSmoothMove(pos);
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mList.size() + mFooterViews.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isFooterPosition(position)) {
            // 直接返回position位置的key
            position = position - mList.size();
            return mFooterViews.keyAt(position);
        }
        if (addPosition == position) {
            return 3;
        }
        switch (mList.get(position).getRoleType()) {
            case 1:
                return ITEM_TYPE.ITEM_TYPE_RIGHT.ordinal();
            case 2:
                return ITEM_TYPE.ITEM_TYPE_LEFT.ordinal();
            case 0:
                return ITEM_TYPE.ITEM_TYPE_ASIDE.ordinal();
            default:
                return ITEM_TYPE.ITEM_TYPE_LEFT.ordinal();
        }
    }


    public static class AsideViewHolder extends RecyclerView.ViewHolder {

        TextView content;

        public AsideViewHolder(View itemView) {
            super(itemView);
            content = (TextView) itemView.findViewById(R.id.text_view);
        }
    }


    public static class CommentViewHolder extends RecyclerView.ViewHolder {

        LinearLayout ll_root;
        LinearLayout ll_comment;
        LinearLayout toolbar_layout;
        FrameLayout ll_frame;
        ImageView cancel;
        View emptyView;
        ListView listView;
        FrameLayout ll_add_comment;

        public CommentViewHolder(View itemView) {
            super(itemView);
            //ll_root = (LinearLayout) itemView.findViewById(R.id.ll_root);
            ll_comment = (LinearLayout) itemView.findViewById(R.id.ll_comment);
            toolbar_layout = (LinearLayout) itemView.findViewById(R.id.toolbar_layout);
            ll_frame = (FrameLayout) itemView.findViewById(R.id.ll_frame);
            emptyView = (View) itemView.findViewById(R.id.empty_view);
            listView = (ListView) itemView.findViewById(R.id.list_view);
            cancel = (ImageView) itemView.findViewById(R.id.cancel);
            ll_add_comment = (FrameLayout) itemView.findViewById(R.id.add_comment_layout);
        }
    }

    public static class MsgViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView avatar_view;
        TextView name_view;
        TextView text_view;
        ImageView image_view;
        TextView comment_count;

        public MsgViewHolder(View itemView) {
            super(itemView);
            avatar_view = (SimpleDraweeView) itemView.findViewById(R.id.avatar_view);
            name_view = (TextView) itemView.findViewById(R.id.name_view);
            text_view = (TextView) itemView.findViewById(R.id.text_view);
            image_view = (ImageView) itemView.findViewById(R.id.image_view);
            comment_count = (TextView) itemView.findViewById(R.id.dialog_comment_count);
        }
    }

    //设置item content点击监听
    public interface OnSmoothMoveLitener {
        void onSmoothMove(int position);

        void setShouldScroll(boolean scroll);
    }

    private void fetchData() {
        // 加载raw数据
        String json = JsonUitl.getJson(mContext, R.raw.s2);
        loadData(json);

    }

    private void loadData(String json) {

        tcommentResult.clear();
        try {
            JSONObject jsonObject = new JSONObject(json);
            if (jsonObject != null) {
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray comments = data.getJSONArray("comments");
                List<TComment> retList = new Gson().fromJson(comments.toString(), new TypeToken<List<TComment>>() {
                }.getType());
                if (retList != null && retList.size() > 0) {
                    tcommentResult.addAll(retList);
                    mListView.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);

                } else {
                    // 没有数据, 显示默认图片
                    mListView.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            } else {
                // 没有数据, 显示默认图片
/*                mListView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);*/
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        dataAdapter.notifyDataSetChanged();
    }
}
