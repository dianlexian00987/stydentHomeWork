package com.telit.zhkt_three.Adapter.NewKnowQuestion;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.telit.zhkt_three.Constant.Constant;
import com.telit.zhkt_three.JavaBean.AutonomousLearning.QuestionBank;
import com.telit.zhkt_three.JavaBean.HomeWorkAnswerSave.AnswerItem;
import com.telit.zhkt_three.JavaBean.HomeWorkAnswerSave.LocalTextAnswersBean;
import com.telit.zhkt_three.JavaBean.NewKnowledge.SingleBean;
import com.telit.zhkt_three.JavaBean.WorkOwnResult;
import com.telit.zhkt_three.MyApplication;
import com.telit.zhkt_three.R;
import com.telit.zhkt_three.Utils.QZXTools;
import com.telit.zhkt_three.Utils.UserUtils;
import com.telit.zhkt_three.greendao.LocalTextAnswersBeanDao;

import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NewMulitChooseAdapter extends RecyclerView.Adapter<NewMulitChooseAdapter.ViewHolder> {
    private Context mContext;
    private QuestionBank questionBank;
    private String status;

    private List<SingleBean> singleBeans = new ArrayList<>();

    private HashMap<String,String> tags=new HashMap<>();

    private List<WorkOwnResult> ownList;

    public NewMulitChooseAdapter(Context mContext, QuestionBank questionBank, String status) {

        this.mContext = mContext;
        this.questionBank = questionBank;
        this.status = status;


        String optionJson = questionBank.getAnswerOptions();

        //解析选项   设置题中的内容
        if (!TextUtils.isEmpty(optionJson)) {
            Gson gson = new Gson();
            Map<String, String> optionMap = gson.fromJson(optionJson, new TypeToken<Map<String, String>>() {
            }.getType());
            Iterator<Map.Entry<String, String>> iterator = optionMap.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                SingleBean singleBean = new SingleBean();
                singleBean.setKeys(entry.getKey());
                singleBean.setWords(entry.getValue());
                singleBeans.add(singleBean);
            }
        }

        if (status.equals(Constant.Commit_Status) || status.equals(Constant.Review_Status)) {
            ownList = questionBank.getOwnList();

        }


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.judge_select_option_complete_layout, viewGroup, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.setIsRecyclable(false);
        viewHolder.option_do_tv.setText(singleBeans.get(i).getKeys());

        viewHolder.option_do_htv.setHtml(singleBeans.get(i).getWords(), new HtmlHttpImageGetter(viewHolder.option_do_htv));


/*        if (tags.contains(singleBeans.get(i).getKeys())) {
            viewHolder.option_do_tv.setSelected(true);
        } else {
            viewHolder.option_do_tv.setSelected(false);

        }*/


        if (status.equals(Constant.Todo_Status)) {

            //查询上次已经选中添加的多选题
                    LocalTextAnswersBean localTextAnswersBean1 = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                            .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionBank.getId() + ""),
                                    LocalTextAnswersBeanDao.Properties.HomeworkId.eq(questionBank.getHomeworkId()),
                                    LocalTextAnswersBeanDao.Properties.QuestionType.eq(questionBank.getQuestionChannelType()),
                                    LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();
                    if (localTextAnswersBean1 != null) {
//                            QZXTools.logE("Answer localTextAnswersBean=" + localTextAnswersBean, null);
                        List<AnswerItem> answerItems = localTextAnswersBean1.getList();
                        if (answerItems != null && answerItems.size() > 0) {
                            for (AnswerItem answerItem : answerItems) {
                                if (!TextUtils.isEmpty(answerItem.getContent())) {

                                    tags.put(answerItem.getContent(),answerItem.getContent());
                                }
                            }
                        }

                    }



            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (viewHolder.option_do_tv.isSelected()) {
                        viewHolder.option_do_tv.setSelected(false);
                        tags.remove(singleBeans.get(i).getKeys());
                    } else {
                        viewHolder.option_do_tv.setSelected(true);
                        tags.put(singleBeans.get(i).getKeys(),singleBeans.get(i).getWords());
                    }


                    QZXTools.logE("多选题数据的信息0001" +".......tags="+tags, null);

                 /*   if (tags.size() == 0){
                        //主要是多选题可以不选的判断
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().deleteByKey(questionBank.getId() + "");
                        return;
                    }*/
                    //保存作业的回显
                    LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                    localTextAnswersBean.setHomeworkId(questionBank.getHomeworkId());
                    localTextAnswersBean.setQuestionId(questionBank.getId() + "");
                    localTextAnswersBean.setUserId(UserUtils.getUserId());
                    localTextAnswersBean.setQuestionType(questionBank.getQuestionChannelType());
                    List<AnswerItem> answerItems = new ArrayList<>();
                    Iterator<Map.Entry<String, String>> entryIterator = tags.entrySet().iterator();
                    while (entryIterator.hasNext()){
                        Map.Entry<String, String> stringEntry = entryIterator.next();
                        String key = stringEntry.getKey();
                        //是否选中
                        AnswerItem answerItem = new AnswerItem();
                        answerItem.setContent(key);
                        answerItems.add(answerItem);

                    }

                    localTextAnswersBean.setList(answerItems);
                    MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);

                    QZXTools.logE("多选题数据的信息0001" + localTextAnswersBean+".......tags="+tags, null);
                }


            });

            //作业已经提交过了，回显
            //查询保存的答案,这是多选，所以存在多个答案
            LocalTextAnswersBean localTextAnswersBean = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                    .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionBank.getId() + ""),
                            LocalTextAnswersBeanDao.Properties.HomeworkId.eq(questionBank.getHomeworkId()),
                            LocalTextAnswersBeanDao.Properties.QuestionType.eq(questionBank.getQuestionChannelType()),
                            LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();


            QZXTools.logE("多选题数据的信息0002" + localTextAnswersBean, null);
            QZXTools.logE("多选题数据的信息0003" + "QuestionId" + questionBank.getId(), null);
            QZXTools.logE("多选题数据的信息0004" + "QuestionType" + questionBank.getQuestionChannelType(), null);

            //如果保存过答案回显
            if (localTextAnswersBean != null) {
//                            QZXTools.logE("Answer localTextAnswersBean=" + localTextAnswersBean, null);
                List<AnswerItem> answerItems = localTextAnswersBean.getList();
                QZXTools.logE("多选题数据的信息0005answerItems=" + answerItems, null);
                if (answerItems != null && answerItems.size() > 0) {
                    for (AnswerItem answerItem : answerItems) {
                        if (singleBeans.get(i).getKeys().equals(answerItem.getContent())) {
                            viewHolder.option_do_tv.setSelected(true);
                        }
                    }
                }else {
                    viewHolder.option_do_tv.setSelected(false);
                }

            }

        }

        if (status.equals(Constant.Commit_Status) || status.equals(Constant.Review_Status)
                || status.equals(Constant.Retry_Status)) {
            if (ownList != null && ownList.size() > 0) {
                for (int j = 0; j < ownList.size(); j++) {
                    WorkOwnResult workOwnResult = ownList.get(j);
                    String answerContent = workOwnResult.getAnswerContent();
                    if (answerContent.equals(singleBeans.get(i).getKeys())) {
                        viewHolder.option_do_tv.setSelected(true);
                    }
                }
            }
        } else if (status.equals(Constant.Save_Status)) {
            //查询上次已经选中添加的多选题
            LocalTextAnswersBean localTextAnswersBean1 = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                    .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionBank.getId() + ""),
                            LocalTextAnswersBeanDao.Properties.HomeworkId.eq(questionBank.getHomeworkId()),
                            LocalTextAnswersBeanDao.Properties.QuestionType.eq(questionBank.getQuestionChannelType()),
                            LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();
            if (localTextAnswersBean1 != null) {
//                            QZXTools.logE("Answer localTextAnswersBean=" + localTextAnswersBean, null);
                List<AnswerItem> answerItems = localTextAnswersBean1.getList();
                if (answerItems != null && answerItems.size() > 0) {
                    for (AnswerItem answerItem : answerItems) {
                        if (!TextUtils.isEmpty(answerItem.getContent())) {

                            tags.put(answerItem.getContent(),answerItem.getContent());
                        }
                    }
                }else {
                    tags.clear();
                }

            }


            //当前状态是保存
            List<WorkOwnResult> ownList = questionBank.getOwnList();
            if (ownList != null && ownList.size() > 0 && localTextAnswersBean1==null) {
                for (int j = 0; j < ownList.size(); j++) {
                    WorkOwnResult workOwnResult = ownList.get(j);
                    String answerContent = workOwnResult.getAnswerContent();
                    if (answerContent.equals(singleBeans.get(i).getKeys())) {
                        viewHolder.option_do_tv.setSelected(true);
                    }
                }
            }


            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (viewHolder.option_do_tv.isSelected()) {
                        viewHolder.option_do_tv.setSelected(false);
                        tags.remove(singleBeans.get(i).getKeys());
                    } else {
                        viewHolder.option_do_tv.setSelected(true);
                        tags.put(singleBeans.get(i).getKeys(),singleBeans.get(i).getWords());
                    }


                    QZXTools.logE("多选题数据的信息0001" +".......tags="+tags, null);

                 /*   if (tags.size() == 0){
                        //主要是多选题可以不选的判断
                        MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().deleteByKey(questionBank.getId() + "");
                        return;
                    }*/
                    //保存作业的回显
                    LocalTextAnswersBean localTextAnswersBean = new LocalTextAnswersBean();
                    localTextAnswersBean.setHomeworkId(questionBank.getHomeworkId());
                    localTextAnswersBean.setQuestionId(questionBank.getId() + "");
                    localTextAnswersBean.setUserId(UserUtils.getUserId());
                    localTextAnswersBean.setQuestionType(questionBank.getQuestionChannelType());
                    List<AnswerItem> answerItems = new ArrayList<>();
                    Iterator<Map.Entry<String, String>> entryIterator = tags.entrySet().iterator();
                    while (entryIterator.hasNext()){
                        Map.Entry<String, String> stringEntry = entryIterator.next();
                        String key = stringEntry.getKey();
                        //是否选中
                        AnswerItem answerItem = new AnswerItem();
                        answerItem.setContent(key);
                        answerItems.add(answerItem);

                    }

                    localTextAnswersBean.setList(answerItems);
                    MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao().insertOrReplace(localTextAnswersBean);

                    QZXTools.logE("多选题数据的信息0001" + localTextAnswersBean+".......tags="+tags, null);
                }


            });
            //查询保存的答案,这是多选，所以存在多个答案
            LocalTextAnswersBean localTextAnswersBean = MyApplication.getInstance().getDaoSession().getLocalTextAnswersBeanDao()
                    .queryBuilder().where(LocalTextAnswersBeanDao.Properties.QuestionId.eq(questionBank.getId() + ""),
                            LocalTextAnswersBeanDao.Properties.HomeworkId.eq(questionBank.getHomeworkId()),
                            LocalTextAnswersBeanDao.Properties.QuestionType.eq(questionBank.getQuestionChannelType()),
                            LocalTextAnswersBeanDao.Properties.UserId.eq(UserUtils.getUserId())).unique();
            if (localTextAnswersBean != null) {
//                            QZXTools.logE("Answer localTextAnswersBean=" + localTextAnswersBean, null);
                List<AnswerItem> answerItems = localTextAnswersBean.getList();
                QZXTools.logE("多选题数据的信息0005answerItems=" + answerItems, null);
                if (answerItems != null && answerItems.size() > 0) {
                    for (AnswerItem answerItem : answerItems) {
                        if (singleBeans.get(i).getKeys().equals(answerItem.getContent())) {
                            viewHolder.option_do_tv.setSelected(true);
                        }
                    }
                }else {
                    viewHolder.option_do_tv.setSelected(false);
                }

            }
        }

    }


    @Override
    public int getItemCount() {
        return singleBeans.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView option_do_tv;
        private HtmlTextView option_do_htv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            option_do_tv = itemView.findViewById(R.id.option_do_tv);
            option_do_htv = itemView.findViewById(R.id.option_do_htv);
        }
    }
}
