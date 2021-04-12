package com.telit.zhkt_three.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.telit.zhkt_three.Activity.HomeWork.ExtraInfoBean;
import com.telit.zhkt_three.Activity.HomeWork.ItemHomeWorkView;
import com.telit.zhkt_three.Adapter.QuestionAdapter.RVQuestionImgAdapter;
import com.telit.zhkt_three.Adapter.QuestionAdapter.RVQuestionTvAnswerAdapter;
import com.telit.zhkt_three.CustomView.NoScrollRecyclerView;
import com.telit.zhkt_three.JavaBean.AutonomousLearning.QuestionBank;
import com.telit.zhkt_three.JavaBean.HomeWork.QuestionInfo;
import com.telit.zhkt_three.JavaBean.HomeWork.QuestionInfoByhand;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.Utils.QZXTools;

import java.util.ArrayList;
import java.util.List;

/**
 * author: qzx
 * Date: 2019/5/23 15:44
 */
public class VPHomeWorkDetailAdapter extends PagerAdapter {
    private Context mContext;
    //获取的数据
    private List<QuestionInfoByhand> mDatas;
    private final List<QuestionBank> bankList;
    private String taskStatus;
    private  int type;
    private String comType;
    private List<ItemHomeWorkView> itemHomeWorkViews=new ArrayList<>();

    private String flag;// 1、作业详情
    private String showAnswerDate;


    private RVQuestionTvAnswerAdapter rvQuestionAnswerAdapter;

    /**
     * 这里传入状态是给RVQuestionTvAnswerAdapter
     */
    public VPHomeWorkDetailAdapter(Context context, List<QuestionInfoByhand> list, List<QuestionBank> bankList,
                                   String status, int type, String comType) {
        mContext = context;
        mDatas = list;
        this.bankList = bankList;
        taskStatus = status;
        //0是互动 1是作业
        this.type = type;
        //判断作业是不是已经完成
        this.comType = comType;

        QZXTools.logE( "VPHomeWorkDetailAdapter: "+mDatas.size(),null);
    }

    private boolean needShowAnswer = false;

    /**
     * 提问专用：查看答案
     */
    public void needShowAnswer() {
        needShowAnswer = true;
    }

    @Override
    public int getCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.question_rv_layout, container, false);

        //分割线视图
        View divideView = view.findViewById(R.id.rv_divider);
        //右侧图片出题答题卡展示 或者 题库出题展示
        NoScrollRecyclerView rvAnswerRecycler = view.findViewById(R.id.rv_text_answer_question);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        rvAnswerRecycler.setLayoutManager(linearLayoutManager);
        rvAnswerRecycler.setNestedScrollingEnabled(false);
        rvAnswerRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);

        //左侧图片出题展示
        NoScrollRecyclerView imgsRecycler = view.findViewById(R.id.rv_img_question);
        imgsRecycler.setNestedScrollingEnabled(false);

        QuestionInfoByhand questionInfoByhand = mDatas.get(position);

        boolean isImageTask = false;

        String imgUrl = questionInfoByhand.getAttachment();
        if (TextUtils.isEmpty(imgUrl)) {
            imgsRecycler.setVisibility(View.GONE);
            divideView.setVisibility(View.GONE);
            isImageTask = false;
        } else {
            imgsRecycler.setVisibility(View.VISIBLE);
            divideView.setVisibility(View.VISIBLE);
            isImageTask = true;
            List<String> imgUrls = new ArrayList<>();
            imgUrls.add(imgUrl);
            RVQuestionImgAdapter rvQuestionImgAdapter = new RVQuestionImgAdapter(mContext, imgUrls);
            LinearLayoutManager linearLayoutManager_img = new LinearLayoutManager(mContext);
            imgsRecycler.setLayoutManager(linearLayoutManager_img);
            imgsRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);
            imgsRecycler.setAdapter(rvQuestionImgAdapter);
        }
        //右侧图片出题答题卡展示 或者 题库出题展示
        List<QuestionInfo> sheetList = questionInfoByhand.getSheetlist();
        rvQuestionAnswerAdapter = new RVQuestionTvAnswerAdapter(mContext, taskStatus,
                isImageTask, false,type,comType);

    /*    if (needShowAnswer) {
            rvQuestionAnswerAdapter.needShowAnswer();
        }*/


        rvAnswerRecycler.setItemViewCacheSize(10);

        rvAnswerRecycler.setDrawingCacheEnabled(true);//保存绘图，提高速度
        rvAnswerRecycler.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        //核心设置数据：题目数据、文本答案保存数据以及文件答案保存数据
        rvQuestionAnswerAdapter.setQuestionInfoList(sheetList, questionInfoByhand.getHomeworkId(), questionInfoByhand.getAnswerPublishDate());

        rvAnswerRecycler.setAdapter(rvQuestionAnswerAdapter);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    public void fromCameraCallback(String flag) {
        if (rvQuestionAnswerAdapter!=null){
            rvQuestionAnswerAdapter.fromCameraCallback(flag);
        }
    }

    public void fromBoardCallback(ExtraInfoBean extraInfoBean) {
        if (rvQuestionAnswerAdapter!=null){
            rvQuestionAnswerAdapter.fromBoardCallback(extraInfoBean);
        }
    }
}
