package com.emilgras.boxdrops.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.emilgras.boxdrops.AppBucketDrops;
import com.emilgras.boxdrops.R;
import com.emilgras.boxdrops.beans.Drop;
import com.emilgras.boxdrops.extras.Util;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by emilgras on 22/05/2016.
 */
public class AdapterDrops extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements SwipeListener {
    
    public static final String TAG = "AdapterDrops";

    public static final int COUNT_FOOTER = 1;
    public static final int COUNT_NO_ITEMS = 1;
    public static final int ITEM = 0;
    public static final int NO_ITEM = 1;
    public static final int FOOTER = 2;

    private LayoutInflater mInflator;
    private RealmResults<Drop> mResults;
    private Realm mRealm;
    private AddListener mAddListener;
    private MarkListener mMarkListener;
    private int mFilterOption;
    private Context mContext;
    private ResetListener mResetListener;


    public AdapterDrops(Context context, Realm realm, RealmResults<Drop> results, AddListener mAddListener, MarkListener mMarkListener) {
        mInflator = LayoutInflater.from(context);
        mRealm = realm;
        updateUI(results);
    }

    public AdapterDrops(Context context, Realm realm, RealmResults<Drop> results, AddListener listener, MarkListener markListener, ResetListener resetListener) {
        mContext = context;
        mInflator = LayoutInflater.from(context);
        mRealm = realm;
        updateUI(results);
        mAddListener = listener;
        mMarkListener = markListener;
        mResetListener = resetListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // OCVH is called only when there is enough holders to fill the whole screen. (maybe 6-10 times depending on screen size)
        // LayoutInflator objects are responsible for converting xml files to java code



        if (viewType == FOOTER) {

            View view = mInflator.inflate(R.layout.footer, parent, false);
            return new FooterHolder(view);

        } else if (viewType == NO_ITEM) {

            View view = mInflator.inflate(R.layout.no_item, parent, false);
            return new NoItemsHolder(view);

        } else {

            View view = mInflator.inflate(R.layout.row_drop, parent, false);
            return new DropHolder(view, mMarkListener);

        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        // This is where data is attached to the view holder at a given position
        // Will get called as many times as there is data


        if (holder instanceof DropHolder) {
            DropHolder dropHolder = (DropHolder) holder;
            Drop drop = mResults.get(position);
            if (drop.isValid()) {
                dropHolder.setWhat(drop.getWhat());
                dropHolder.setWhen(drop.getWhen());
                dropHolder.setBackground(drop.getComplete());
            }
        }

    }

    @Override
    public int getItemCount() {
        if (!mResults.isEmpty()) {
            // We have results to display
            return mResults.size() + COUNT_FOOTER;
        } else {
            // No results
            // Check sorting options
            // If complete/incomplete is used do extra check

            if (mFilterOption == Filter.LEAST_TIME_LEFT || mFilterOption == Filter.MOST_TIME_LEFT || mFilterOption == Filter.NONE) {
                // Then we know that the database is empty
                return 0;
            } else {
                // there could be something in the database
                return COUNT_NO_ITEMS + COUNT_FOOTER;
            }

        }
    }

    @Override
    public long getItemId(int position) {

        if (position < mResults.size()) {

            Drop drop = mResults.get(position);
            if (drop.isValid()) {
                return mResults.get(position).getAdded();
            }


        }

        return RecyclerView.NO_ID;
    }

    public void updateUI(RealmResults<Drop> results) {
        mResults = results;
        mFilterOption = AppBucketDrops.load(mContext);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {

        if (!mResults.isEmpty()) {
            // We have results

            if (position < mResults.size()) {

                return ITEM;

            } else {

                return FOOTER;

            }

        } else {
            // No results

            if (mFilterOption == Filter.COMPLETE || mFilterOption == Filter.INCOMPLETE) {

                if (position == 0) {

                    return NO_ITEM;

                } else {

                    return FOOTER;

                }

            } else {

                return ITEM;

            }

        }

    }

    @Override
    public void onSwipe(int position) {
        if (position < mResults.size()) {
            mRealm.beginTransaction();
            mResults.get(position).deleteFromRealm();
            mRealm.commitTransaction();
            notifyItemRemoved(position);
        }
        resetFilterIfEmpty();
    }

    private void resetFilterIfEmpty() {
        if (mResults.isEmpty() && (mFilterOption == Filter.COMPLETE || mFilterOption == Filter.INCOMPLETE)) {

            mResetListener.onReset();

        }
    }

    public void markComplete(int position) {
        if (position < mResults.size()) {
            mRealm.beginTransaction();
            mResults.get(position).setComplete(true);
            mRealm.commitTransaction();
            notifyItemChanged(position);
        }
    }


    public static class DropHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mTextWhat;
        TextView mTextWhen;
        MarkListener mMarkListener;
        Context mContext;
        View mItemView;

        public DropHolder(View itemView, MarkListener listener) {
            super(itemView);
            mItemView = itemView;
            itemView.setOnClickListener(this);
            mContext = itemView.getContext();
            mTextWhat = (TextView) itemView.findViewById(R.id.tv_what);
            mTextWhen = (TextView) itemView.findViewById(R.id.tv_when);
            mMarkListener = listener;
        }

        public void setWhat(String what) {
            mTextWhat.setText(what);
        }

        public void setWhen(long when) {
            mTextWhen.setText(DateUtils.getRelativeTimeSpanString(when, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL));
        }

        public void setBackground(boolean completed) {
            Drawable drawable;

            if (completed) {
                drawable = ContextCompat.getDrawable(mContext, R.color.bg_row_complete);
            } else {
                drawable = ContextCompat.getDrawable(mContext, R.drawable.bg_row_drop);
            }

            Util.setBackground(mItemView, drawable);

        }

        @Override
        public void onClick(View v) {
            mMarkListener.onMark(getAdapterPosition());
        }

    }


    public class NoItemsHolder extends RecyclerView.ViewHolder {

        public NoItemsHolder(View itemView) {
            super(itemView);
        }

    }


    public class FooterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Button mBtnAdd;

        public FooterHolder(View itemView) {
            super(itemView);
            mBtnAdd = (Button) itemView.findViewById(R.id.btn_footer);
            mBtnAdd.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mAddListener.add();
        }
    }

}
