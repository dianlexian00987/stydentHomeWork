package com.telit.zhkt_three.Adapter.QuestionAdapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.dialoge.LoadDialog;

import java.util.List;


/**
 * author: qzx
 * Date: 2019/5/23 16:30
 */
public class RVQuestionImgAdapter extends RecyclerView.Adapter<RVQuestionImgAdapter.RVQuestionImgViewHolder> {

    private List<String> urlImgs;
    private Context mContext;
    private LoadDialog loadDialog;

    public RVQuestionImgAdapter(Context context, List<String> list) {
        mContext = context;
        urlImgs = list;
    }

    @NonNull
    @Override
    public RVQuestionImgViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
      /*  loadDialog = new LoadDialog(mContext);
        loadDialog.show();*/

        return new RVQuestionImgViewHolder(LayoutInflater.from(mContext).inflate(R.layout.rv_question_img_item_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RVQuestionImgViewHolder rvQuestionImgViewHolder, int i) {
        Glide.with(mContext).load(urlImgs.get(i)).into(rvQuestionImgViewHolder.photoView);
        /*Glide.with(mContext).load(urlImgs.get(i)).into(new CustomViewTarget<SubsamplingScaleImageView, Drawable>(rvQuestionImgViewHolder.photoView) {
            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {

            }

            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                Bitmap bitmap = ImageUtils.DrawableToBitmap(resource);
                rvQuestionImgViewHolder.photoView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
                rvQuestionImgViewHolder.photoView.setImage(ImageSource.bitmap(bitmap));
            }

            @Override
            protected void onResourceCleared(@Nullable Drawable placeholder) {

            }
        });*/
    }

    @Override
    public int getItemCount() {
        return urlImgs != null ? urlImgs.size() : 0;
    }

    public class RVQuestionImgViewHolder extends RecyclerView.ViewHolder{

        private PhotoView photoView;

        public RVQuestionImgViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photoView_question_item);
            photoView.setEnabled(true);
        }
    }
}
