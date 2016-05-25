package net.oschina.app.fragment.general;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.gson.reflect.TypeToken;

import net.oschina.app.R;
import net.oschina.app.adapter.base.BaseListAdapter;
import net.oschina.app.adapter.general.QuesActionAdapter;
import net.oschina.app.adapter.general.QuestionAdapter;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.bean.base.PageBean;
import net.oschina.app.bean.base.ResultBean;
import net.oschina.app.bean.question.Question;
import net.oschina.app.fragment.base.BaseListFragment;
import net.oschina.app.util.UIHelper;

import java.lang.reflect.Type;

/**
 * 技术问答界面
 */
public class QuestionFragment extends BaseListFragment<Question> {

    private static final String TAG = "QuestionFragment";
    private GridView quesGridView = null;
    private View headView;


    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
        headView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_main_question_header, null);
        mListView.setOnItemClickListener(this);
        quesGridView = (GridView) headView.findViewById(R.id.gv_ques);

        final int[] positions = {1, 0, 0, 0, 0};
        final QuesActionAdapter quesActionAdapter = new QuesActionAdapter(getActivity(), positions);
        quesGridView.setAdapter(quesActionAdapter);
        quesGridView.setItemChecked(0, true);
        quesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < positions.length; i++) {
                    if (i == position) {
                        positions[position] = 1;
                    } else {
                        positions[i] = 0;
                    }
                }
                quesActionAdapter.notifyDataSetChanged();
            }
        });
        mListView.addHeaderView(headView);

    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected BaseListAdapter<Question> getListAdapter() {
        return new QuestionAdapter(this);
    }

    @Override
    protected void requestData() {
        super.requestData();
        OSChinaApi.getQuestionList(OSChinaApi.CATALOG_QUESTION_QUESTION,mIsRefresh ? mBeam.getPrevPageToken() : mBeam.getNextPageToken(), mHandler);
    }

    @Override
    protected Type getType() {
        return new TypeToken<ResultBean<PageBean<Question>>>() {
        }.getType();
    }

    @Override
    protected void setListData(ResultBean<PageBean<Question>> resultBean) {
        super.setListData(resultBean);
    }

    @Override
    protected void onItemClick(Question item, int position) {
        super.onItemClick(item, position);
        UIHelper.showPostDetail(getActivity(), (int) item.getId(),
                item.getCommentCount());
    }
}
