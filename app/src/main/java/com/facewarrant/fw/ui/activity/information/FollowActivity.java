package com.facewarrant.fw.ui.activity.information;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facewarrant.fw.R;
import com.facewarrant.fw.adapter.recycler.RecyclerCommonAdapter;
import com.facewarrant.fw.adapter.recycler.base.ViewHolder;
import com.facewarrant.fw.base.BaseActivity;
import com.facewarrant.fw.bean.FollowBean;
import com.facewarrant.fw.event.UpdateMesageEvent;
import com.facewarrant.fw.event.UpdateUserInfoEvent;
import com.facewarrant.fw.global.AccountManager;
import com.facewarrant.fw.net.RequestManager;
import com.facewarrant.fw.net.RetrofitCallBack;
import com.facewarrant.fw.net.RetrofitRequestInterface;
import com.facewarrant.fw.ui.personal.PersonalActivity;
import com.facewarrant.fw.util.CommonUtil;
import com.facewarrant.fw.util.LogUtil;
import com.facewarrant.fw.util.ToastUtil;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.makeramen.roundedimageview.RoundedImageView;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

/**
 * created  by  Alex
 * e-mail:15062859867@163.com
 */
public class FollowActivity extends BaseActivity {
    private static final String TAG = "FollowActivity";
    @BindView(R.id.iv_layout_top_back)
    ImageView mBackIV;
    @BindView(R.id.tv_layout_top_back_title)
    TextView mTitleTV;
    @BindView(R.id.rv_activity_follow)
    RecyclerView mRV;

    @BindView(R.id.tv_layout_top_back_setting)
    TextView mSettingTV;
    @BindView(R.id.trk)
    TwinklingRefreshLayout mRefreshLayout;

    private int mDataStatus = STATUS_REFRESH;
    private int mPage = 1;
    private static final int STATUS_REFRESH = 1;
    private static final int STATUS_LOAD = 2;
    private RecyclerCommonAdapter<FollowBean> mAdapter;
    private List<FollowBean> mList = new ArrayList<>();
    private Drawable mAdd;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_follow;
    }

    @Override
    public void initData() {
        mTitleTV.setText(R.string.follow_me);
        mSettingTV.setText(R.string.all_read);
        mAdd = ContextCompat.getDrawable(mActivity, R.drawable.add_follow);
        mAdd.setBounds(0, 0, mAdd.getMinimumWidth(), mAdd.getMinimumHeight());
        getInfoList();
        CommonUtil.setRefreshStyle(mRefreshLayout, mActivity);

    }

    @Override
    public void initEvent() {
        mBackIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSettingTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readAllMessage();
            }
        });
        mRefreshLayout.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(TwinklingRefreshLayout refreshLayout) {
                super.onRefresh(refreshLayout);
                mPage = 1;
                mDataStatus = STATUS_REFRESH;
                getInfoList();
            }

            @Override
            public void onLoadMore(TwinklingRefreshLayout refreshLayout) {
                super.onLoadMore(refreshLayout);
                mPage++;
                mDataStatus = STATUS_LOAD;
                getInfoList();
            }
        });
    }

    private void readAllMessage() {
        final Map<String, String> map = new HashMap<>();
        map.put("userId", AccountManager.sUserBean.getId());
        map.put("messageType", "AT");
        map.put("type", 1 + "");
        LogUtil.e(TAG, map.toString());
        RequestManager.mRetrofitManager
                .createRequest(RetrofitRequestInterface.class)
                .deleteAllMessage(RequestManager.encryptParams(map))
                .enqueue(new RetrofitCallBack() {
                    @Override
                    public void onSuccess(String response) {
                        LogUtil.e(TAG, response.toString());
                        try {
                            JSONObject data = new JSONObject(response);
                            if (data.getInt("resultCode") == 200) {
                                mRefreshLayout.startRefresh();
                                ToastUtil.showShort(mActivity, "全部已读成功！");
                                EventBus.getDefault().post(new UpdateMesageEvent());
                            } else {
                                ToastUtil.showShort(mActivity, data.getString("resultDesc"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(Throwable t) {

                    }
                });
    }


    private void showRecyclerView() {
        if (mAdapter == null) {
            mAdapter = new RecyclerCommonAdapter<FollowBean>(mActivity, R.layout.item_follow, mList) {
                @Override
                protected void convert(ViewHolder holder, final FollowBean followBean, final int position) {
                    RoundedImageView topRIV = holder.getView(R.id.riv_item_follow);
                    Glide.with(mActivity).load(followBean.getTopUrl()).into(topRIV);
                    holder.setText(R.id.tv_item_follow_name, followBean.getName());
                    holder.setText(R.id.tv_item_follow_time, followBean.getTime());
                    ImageView readIV = holder.getView(R.id.iv_item_follow_read);
                    TextView followTV = holder.getView(R.id.tv_item_follow_follow);
                    LinearLayout followLL = holder.getView(R.id.ll_item_follow_follow);
                    switch (followBean.getStatus()) {
                        case 0:
                            readIV.setVisibility(View.VISIBLE);
                            break;
                        case 1:
                            readIV.setVisibility(View.GONE);
                            break;
                    }
                    switch (followBean.getAttentionVisible()) {
                        case 0:
                            followTV.setVisibility(View.GONE);
                            followLL.setVisibility(View.GONE);
                            break;
                        case 1:
                            followTV.setVisibility(View.VISIBLE);
                            followLL.setVisibility(View.VISIBLE);
                            break;
                    }
                    switch (followBean.getAttention()) {
                        case 0:
                            followTV.setText(R.string.follow);
                            followTV.setTextColor(ContextCompat.getColor(mActivity, R.color.colorFont));
                            followTV.setCompoundDrawables(mAdd, null, null, null);
                            break;
                        case 1:
                            followTV.setText(R.string.followed);
                            followTV.setTextColor(ContextCompat.getColor(mActivity, R.color.colorFontHint));
                            followTV.setCompoundDrawables(null, null, null, null);
                            break;
                    }
                    followTV.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getAttention(followBean.getFromUserId(), followBean.getAttention() + "", position);
                        }
                    });
                    holder.getConvertView().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            readOneMessage(followBean.getId(), followBean.getFromUserId());
                        }
                    });
                }
            };
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mActivity);
            mRV.setLayoutManager(linearLayoutManager);
            mRV.setAdapter(mAdapter);
            mRV.addItemDecoration(new DividerItemDecoration(mActivity, DividerItemDecoration.VERTICAL));
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void getInfoList() {
        final Map<String, String> map = new HashMap<>();
        map.put("userId", AccountManager.sUserBean.getId());
        map.put("messageType", "AT");
        map.put("page", mPage + "");
        map.put("rows", 15 + "");
        RequestManager.mRetrofitManager.createRequest(RetrofitRequestInterface.class).getInfoList(RequestManager.encryptParams(map)).enqueue(new RetrofitCallBack() {
            @Override
            public void onSuccess(String response) {
                LogUtil.e(TAG, response.toString());
                switch (mDataStatus) {
                    case STATUS_REFRESH:
                        mRefreshLayout.finishRefreshing();
                        mList.clear();
                        break;
                    case STATUS_LOAD:
                        mRefreshLayout.finishLoadmore();
                        break;
                }
                try {
                    JSONObject data = new JSONObject(response);
                    if (data.getInt("resultCode") == 200) {
                        if (!data.getString("result").equals("null")) {
                            JSONObject result = data.getJSONObject("result");
                            int noReadCount = result.getInt("messagesCount");
                            if (noReadCount == 0) {
                                mSettingTV.setTextColor(ContextCompat.getColor(mActivity, R.color.colorFontHint));
                                mSettingTV.setClickable(false);
                            } else if (noReadCount > 0) {
                                mSettingTV.setTextColor(ContextCompat.getColor(mActivity, R.color.common_red));
                                mSettingTV.setClickable(true);
                            }
                            JSONArray List = result.getJSONArray("messagesResponseDtoList");
                            for (int i = 0; i < List.length(); i++) {
                                JSONObject resultItem = List.getJSONObject(i);
                                FollowBean followBean = new FollowBean();
                                followBean.setId(resultItem.getString("messageId"));
                                followBean.setTopUrl(resultItem.getString("headUrl"));
                                followBean.setTime(resultItem.getString("createTime"));
                                followBean.setName(resultItem.getString("fromUser"));
                                followBean.setStatus(resultItem.getInt("status"));
                                followBean.setAttention(resultItem.getInt("isAttention"));
                                followBean.setAttentionVisible(resultItem.getInt("hasAttention"));
                                followBean.setFromUserId(resultItem.getString("fromUserId"));
                                mList.add(followBean);

                            }
                            showRecyclerView();
                        }
                    } else {
                        ToastUtil.showShort(mActivity, data.getString("resultDesc"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable t) {
                switch (mDataStatus) {
                    case STATUS_REFRESH:
                        mRefreshLayout.finishRefreshing();
                        break;
                    case STATUS_LOAD:
                        mRefreshLayout.finishLoadmore();
                        break;
                }

            }
        });
    }


    private void getAttention(String id, final String attention, final int position) {
        final Map<String, String> map = new HashMap<>();
        map.put("userId", AccountManager.sUserBean.getId());
        map.put("faceId", id);
        map.put("isAttention", attention);
        RequestManager.mRetrofitManager
                .createRequest(RetrofitRequestInterface.class)
                .attention(RequestManager.encryptParams(map))
                .enqueue(new RetrofitCallBack() {
                    @Override
                    public void onSuccess(String response) {
                        LogUtil.e(TAG, response.toString());
                        try {
                            JSONObject data = new JSONObject(response);
                            if (data.getInt("resultCode") == 200) {
                                switch (attention) {
                                    case "0":
                                        mList.get(position).setAttention(1);
                                        break;
                                    case "1":
                                        mList.get(position).setAttention(0);
                                        break;

                                }
                                mAdapter.notifyDataSetChanged();
                                EventBus.getDefault().post(new UpdateUserInfoEvent());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Throwable t) {

                    }
                });
    }

    private void readOneMessage(String id, final String userId) {
        final Map<String, String> map = new HashMap<>();
        map.put("userId", AccountManager.sUserBean.getId());
        map.put("messageId", id);
        map.put("type", 1 + "");
        LogUtil.e(TAG, map.toString());
        RequestManager.mRetrofitManager.createRequest(RetrofitRequestInterface.class)
                .readOneMessage(RequestManager.encryptParams(map))
                .enqueue(new RetrofitCallBack() {
                    @Override
                    public void onSuccess(String response) {
                        LogUtil.e(TAG, response.toString());
                        try {
                            JSONObject data = new JSONObject(response);
                            if (data.getInt("resultCode") == 200) {
                                mList.clear();
                                mPage = 1;
                                getInfoList();
                                PersonalActivity.open(mActivity, userId);
                                EventBus.getDefault().post(new UpdateMesageEvent());
                            } else {
                                ToastUtil.showShort(mActivity, data.getString("resultDesc"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(Throwable t) {

                    }
                });
    }

}