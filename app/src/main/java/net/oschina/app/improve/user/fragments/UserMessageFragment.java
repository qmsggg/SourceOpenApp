package net.oschina.app.improve.user.fragments;

import com.google.gson.reflect.TypeToken;

import net.oschina.app.AppContext;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.improve.base.adapter.BaseRecyclerAdapter;
import net.oschina.app.improve.base.fragments.BaseRecyclerViewFragment;
import net.oschina.app.improve.bean.Message;
import net.oschina.app.improve.bean.base.PageBean;
import net.oschina.app.improve.bean.base.ResultBean;
import net.oschina.app.improve.user.adapter.UserMessageAdapter;

import java.lang.reflect.Type;

/**
 * Created by huanghaibin_dev
 * on 2016/8/16.
 */

public class UserMessageFragment extends BaseRecyclerViewFragment<Message> {
    public long authorId;

    @Override
    public void initData() {
        super.initData();
        authorId = Long.parseLong(String.valueOf(AppContext.getInstance().getLoginUid()));
    }

    @Override
    protected void requestData() {
        super.requestData();
        OSChinaApi.getMessageList(authorId, mIsRefresh ? null : mBean.getNextPageToken(), mHandler);
    }

    @Override
    protected BaseRecyclerAdapter<Message> getRecyclerAdapter() {
        return new UserMessageAdapter(this);
    }

    @Override
    protected Type getType() {
        return new TypeToken<ResultBean<PageBean<Message>>>() {
        }.getType();
    }
}
