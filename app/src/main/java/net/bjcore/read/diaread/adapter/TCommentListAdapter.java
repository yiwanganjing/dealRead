package net.bjcore.read.diaread.adapter;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.facebook.drawee.view.SimpleDraweeView;
import net.bjcore.read.diaread.R;
import net.bjcore.read.diaread.entity.TComment;
import net.bjcore.read.diaread.holder.CommentHolder;
import net.bjcore.read.diaread.holder.HolderAdapter;
import java.util.List;

public class TCommentListAdapter<T> extends HolderAdapter<T,CommentHolder> {

    private Activity mContext;
    public TCommentListAdapter(Activity context, List<T> listData) {
        super(context, listData);
        mContext = context;
    }

    @Override
    public View initConvertView(LayoutInflater layoutInflater, ViewGroup parent) {
        return inflateWithParent(R.layout.item_story_comment, parent);
    }

    @Override
    public CommentHolder initHolder(View convertView) {
        CommentHolder viewHolder = new CommentHolder();
        viewHolder.avatar_view = (SimpleDraweeView) convertView.findViewById(R.id.avatar_view);
        viewHolder.name_view = (TextView) convertView.findViewById(R.id.name_view);
        viewHolder.tv_time = (TextView) convertView.findViewById(R.id.send_time_view);
        viewHolder.text_view = (TextView) convertView.findViewById(R.id.text_view); // 评论内容
        viewHolder.ll_content_reply = (LinearLayout) convertView.findViewById(R.id.ll_content_reply);

        return viewHolder;
    }

    @Override
    public void bindData(CommentHolder holder, final T ite) {
        final TComment item = (TComment) ite;

        holder.text_view.setText(item.content);
        holder.name_view.setText(item.user_name);
        if(item.user_avatar != null) {
            holder.avatar_view.setImageURI(Uri.parse(item.user_avatar));
        }else {
            holder.avatar_view.setImageURI(null);
        }
        holder.tv_time.setText(item.show_time);
        // 点击事件
        holder.ll_content_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

}