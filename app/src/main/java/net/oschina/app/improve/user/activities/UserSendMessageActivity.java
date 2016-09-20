package net.oschina.app.improve.user.activities;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.TextHttpResponseHandler;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.improve.base.activities.BaseRecyclerViewActivity;
import net.oschina.app.improve.base.adapter.BaseRecyclerAdapter;
import net.oschina.app.improve.bean.Message;
import net.oschina.app.improve.bean.base.PageBean;
import net.oschina.app.improve.bean.base.ResultBean;
import net.oschina.app.improve.bean.simple.Author;
import net.oschina.app.improve.behavior.KeyboardInputDelegation;
import net.oschina.app.improve.media.ImageGalleryActivity;
import net.oschina.app.improve.media.SelectImageActivity;
import net.oschina.app.improve.user.adapter.UserSendMessageAdapter;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import cz.msebera.android.httpclient.Header;

/**
 * 发送消息界面
 * Created by huanghaibin_dev
 * on 2016/8/18.
 */
public class UserSendMessageActivity extends BaseRecyclerViewActivity<Message> {

    @Bind(R.id.root)
    CoordinatorLayout mCoordinatorLayout;
    private KeyboardInputDelegation mDelegation;

    EditText mViewInput;
    private long authorId;
    private Author mReceiver;
    private ProgressDialog mDialog;
    private boolean isFirstLoading = true;
    private Map<String, Message> mSendQuent = new HashMap<>();

    public static void show(Context context, Author sender) {
        Intent intent = new Intent(context, UserSendMessageActivity.class);
        intent.putExtra("receiver", sender);
        context.startActivity(intent);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_user_send_message;
    }

    @Override
    protected void initData() {
        super.initData();
        mDialog = new ProgressDialog(this);
        mReceiver = (Author) getIntent().getSerializableExtra("receiver");
        setTitle(mReceiver.getName());
        authorId = Long.parseLong(String.valueOf(AppContext.getInstance().getLoginUid()));
        init();
    }

    /**
     * 下拉刷新为加载更多
     */
    @Override
    public void onRefreshing() {
        OSChinaApi.getMessageList(mReceiver.getId(), mBean.getNextPageToken(), mHandler);
    }

    /**
     * 去掉上拉加载
     */
    @Override
    public void onLoadMore() {

    }

    protected void init() {
        mDelegation = KeyboardInputDelegation.delegation(this, mCoordinatorLayout, null);
        mDelegation.showEmoji(getSupportFragmentManager());
        mDelegation.showPic(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImageActivity.showImage(UserSendMessageActivity.this, 1, true, null, new SelectImageActivity.Callback() {
                    @Override
                    public void doSelectDone(String[] images) {
                        File file = new File(images[0]);
                        String path = file.getPath();
                        if (mSendQuent.containsKey(path)) {
                            Toast.makeText(UserSendMessageActivity.this, "图片已经在发送队列", Toast.LENGTH_SHORT).show();
                        } else {
                            OSChinaApi.pubMessage(mReceiver.getId(), file, new CallBack(file.getPath()));
                            Message message = new Message();
                            message.setType(Message.TYPE_IMAGE);
                            Author author = new Author();
                            author.setId(AppContext.getInstance().getLoginId());
                            message.setSender(author);
                            message.setResource(file.getPath());
                            mSendQuent.put(file.getPath(), message);
                            mAdapter.addItem(message);
                            scrollToBottom();
                        }

                    }
                });
            }
        });
        mDelegation.setAdapter(new KeyboardInputDelegation.KeyboardInputAdapter() {
            @Override
            public void onSubmit(TextView v, String content) {
                content = content.replaceAll("[ \\s\\n]+", " ");
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(UserSendMessageActivity.this, "请输入文字", Toast.LENGTH_SHORT).show();
                    return;
                }
                mDialog.setMessage("正在发送中...");
                mDialog.show();
                OSChinaApi.pubMessage(mReceiver.getId(), content, new CallBack(null));
            }

            @Override
            public void onFinalBackSpace(View v) {

            }
        });
        mViewInput = mDelegation.getInputView();
    }

    @Override
    public void onItemClick(int position, long itemId) {
        Message message = mAdapter.getItem(position);
        if (Message.TYPE_IMAGE == message.getType()) {
            if (message.getId() == 0) {

            }else if (message.getId() == -1) { //重新发送
                message.setId(0);
                mAdapter.updateItem(position);
                File file = new File(message.getResource());
                OSChinaApi.pubMessage(mReceiver.getId(), file, new CallBack(file.getPath()));
            } else {
                ImageGalleryActivity.show(this, message.getResource());
            }
        }
    }

    @Override
    protected void setListData(ResultBean<PageBean<Message>> resultBean) {
        super.setListData(resultBean);
        if (isFirstLoading) {
            scrollToBottom();
            isFirstLoading = false;
        }

    }

    private void scrollToBottom() {
        mRecyclerView.scrollToPosition(mAdapter.getItems().size() - 1);
    }

    @Override
    protected Type getType() {
        return new TypeToken<ResultBean<PageBean<Message>>>() {
        }.getType();
    }

    @Override
    protected BaseRecyclerAdapter<Message> getRecyclerAdapter() {
        return new UserSendMessageAdapter(this);
    }

    private class CallBack extends TextHttpResponseHandler {
        private String filePath;

        public CallBack(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            if (filePath != null) {
                Message message = mSendQuent.get(filePath);
                message.setId(-1);
                mAdapter.updateItem(mAdapter.getItems().indexOf(message));
            }
            Toast.makeText(UserSendMessageActivity.this, "发送失败，请检查数据", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, String responseString) {
            Type type = new TypeToken<ResultBean<Message>>() {
            }.getType();
            try {
                ResultBean<Message> resultBean = AppContext.createGson().fromJson(responseString, type);
                if (resultBean.isSuccess()) {
                    if (filePath != null) {
                        Message message = mSendQuent.get(filePath);
                        if (message != null) {
                            mAdapter.removeItem(message);
                            mSendQuent.remove(filePath);
                        }
                    }
                    mAdapter.addItem(resultBean.getResult());
                    scrollToBottom();
                    mViewInput.setText("");
                } else {
                    if (filePath != null) {
                        Message message = mSendQuent.get(filePath);
                        message.setId(-1);
                        mAdapter.updateItem(mAdapter.getItems().indexOf(message));
                    }
                    Toast.makeText(UserSendMessageActivity.this, resultBean.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFinish() {
            super.onFinish();
            mDialog.dismiss();
        }
    }
}
