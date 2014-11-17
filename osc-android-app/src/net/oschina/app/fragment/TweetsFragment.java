package net.oschina.app.fragment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.http.Header;

import net.oschina.app.AppContext;
import net.oschina.app.R;
import net.oschina.app.adapter.TweetAdapter;
import net.oschina.app.api.OperationResponseHandler;
import net.oschina.app.api.remote.OSChinaApi;
import net.oschina.app.base.BaseListFragment;
import net.oschina.app.base.ListBaseAdapter;
import net.oschina.app.bean.ListEntity;
import net.oschina.app.bean.Result;
import net.oschina.app.bean.ResultBean;
import net.oschina.app.bean.Tweet;
import net.oschina.app.bean.TweetsList;
import net.oschina.app.interf.OnTabReselectListener;
import net.oschina.app.ui.NavigationDrawerFragment;
import net.oschina.app.ui.dialog.CommonDialog;
import net.oschina.app.ui.dialog.DialogHelper;
import net.oschina.app.ui.empty.EmptyLayout;
import net.oschina.app.util.HTMLSpirit;
import net.oschina.app.util.TDevice;
import net.oschina.app.util.UIHelper;
import net.oschina.app.util.XmlUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

/**
 * @author HuangWenwei
 * 
 * @date 2014年10月10日
 */
public class TweetsFragment extends BaseListFragment implements OnItemLongClickListener, OnTabReselectListener {

	protected static final String TAG = TweetsFragment.class.getSimpleName();
	private static final String CACHE_KEY_PREFIX = "tweetslist_";

	private boolean mIsWatingLogin;
	
	class DeleteTweetResponseHandler extends OperationResponseHandler {

		DeleteTweetResponseHandler(Object... args) {
			super(args);
		}

		@Override
		public void onSuccess(int code, ByteArrayInputStream is, Object[] args)
				throws Exception {
			try {
				Result res = XmlUtils.toBean(ResultBean.class, is).getResult();
				if (res != null && res.OK()) {
					AppContext.showToastShort(R.string.delete_success);
					Tweet tweet = (Tweet) args[0];
					mAdapter.removeItem(tweet);
					mAdapter.notifyDataSetChanged();
				} else {
					onFailure(code, res.getErrorMessage(), args);
				}
			} catch (Exception e) {
				e.printStackTrace();
				onFailure(code, e.getMessage(), args);
			}
		}

		@Override
		public void onFailure(int arg0, Header[] arg1, byte[] arg2,
				Throwable arg3) {
			AppContext.showToastShort(R.string.delete_faile);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		IntentFilter filter = new IntentFilter(
				NavigationDrawerFragment.INTENT_ACTION_USER_CHANGE);
		getActivity().registerReceiver(mReceiver, filter);
	}

	@Override
	public void onResume() {
		if (mIsWatingLogin) {
			mCurrentPage = 0;
			mState = STATE_REFRESH;
			requestData(false);
		}
		super.onResume();
	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	@Override
	protected ListBaseAdapter getListAdapter() {
		return new TweetAdapter();
	}

	@Override
	protected String getCacheKeyPrefix() {
		return CACHE_KEY_PREFIX + tweetType;
	}

	@Override
	protected ListEntity parseList(InputStream is) throws Exception {
		TweetsList list = XmlUtils.toBean(TweetsList.class, is);
		return list;
	}

	@Override
	protected ListEntity readList(Serializable seri) {
		return ((TweetsList) seri);
	}

	@Override
	protected void sendRequestData() {
		OSChinaApi.getTweetList(tweetType, mCurrentPage, mHandler);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Tweet tweet = (Tweet) mAdapter.getItem(position);
		if (tweet != null)
			UIHelper.showTweetDetail(view.getContext(), tweet.getId());
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			setupContent();
		}
	};

	private void setupContent() {
		if (mErrorLayout != null) {
			mIsWatingLogin = true;
			mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
			mErrorLayout.setErrorMessage(getString(R.string.unlogin_tip));
		}
	}

	@Override
	protected void requestData(boolean refresh) {
		if (tweetType > 0) {
			if (AppContext.getInstance().isLogin()) {
				tweetType = AppContext.getInstance().getLoginUid();
				mIsWatingLogin = false;
				super.requestData(refresh);
			} else {
				mIsWatingLogin = true;
				mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
				mErrorLayout.setErrorMessage(getString(R.string.unlogin_tip));
			}
		} else {
			mIsWatingLogin = false;
			super.requestData(refresh);
		}
	}

	@Override
	public void initView(View view) {
		super.initView(view);
		mListView.setOnItemLongClickListener(this);
		mErrorLayout.setOnLayoutClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (AppContext.getInstance().isLogin()) {
					requestData(true);
				} else {
					UIHelper.showLoginActivity(getActivity());
				}
			}
		});
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		Tweet tweet = (Tweet) mAdapter.getItem(position);
		if (tweet != null) {
			handleLongClick(tweet);
			return true;
		}
		return false;
	}

	private void handleLongClick(final Tweet tweet) {
		String[] items = null;
		if (AppContext.getInstance().getLoginUid() == tweet.getAuthorid()) {
			items = new String[] { getResources().getString(R.string.copy),
					getResources().getString(R.string.delete) };
		} else {
			items = new String[] { getResources().getString(R.string.copy) };
		}
		final CommonDialog dialog = DialogHelper
				.getPinterestDialogCancelable(getActivity());
		dialog.setNegativeButton(R.string.cancle, null);
		dialog.setItemsWithoutChk(items, new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				dialog.dismiss();
				if (position == 0) {
					TDevice.copyTextToBoard(HTMLSpirit.delHTMLTag(tweet
							.getBody()));
				} else if (position == 1) {
					handleDeleteTweet(tweet);
				}
			}
		});
		dialog.show();
	}
	
	private void handleDeleteTweet(final Tweet tweet) {
		CommonDialog dialog = DialogHelper
				.getPinterestDialogCancelable(getActivity());
		dialog.setMessage(R.string.message_delete_tweet);
		dialog.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						OSChinaApi.deleteTweet(tweet.getAuthorid(), tweet.getId(),
								new DeleteTweetResponseHandler(tweet));
					}
				});
		dialog.setNegativeButton(R.string.cancle, null);
		dialog.show();
	}

	@Override
	public void onTabReselect() {
		onRefresh();
	}
}