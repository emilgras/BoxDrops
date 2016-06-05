package com.emilgras.boxdrops.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.emilgras.boxdrops.extras.Util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by emilgras on 22/05/2016.
 */
public class BoxRecyclerView extends RecyclerView {

    private List<View> mNonEmptyViews = Collections.emptyList();
    private List<View> mEmptyViews = Collections.emptyList();

    private AdapterDataObserver mObserver = new AdapterDataObserver() {

        @Override
        public void onChanged() {
            toggleViews();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            toggleViews();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            super.onItemRangeChanged(positionStart, itemCount, payload);
            toggleViews();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            toggleViews();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            toggleViews();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            toggleViews();
        }
    };


    // Nr 1:    Used to instantiate a recyclerview from code
    public BoxRecyclerView(Context context) {
        super(context);
    }

    // Nr 2+3:  Used to instantiate a recyclerview from xml
    public BoxRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    // (this lets you define s custom style)
    public BoxRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (adapter!=null) {
            adapter.registerAdapterDataObserver(mObserver);
        }
        mObserver.onChanged();
    }

    public void hideIfEmpty(View ...views) {
        mNonEmptyViews = Arrays.asList(views);
    }

    public void showIfEmpty(View... emptyViews) {
        mEmptyViews = Arrays.asList(emptyViews);
    }

    private void toggleViews() {
        if (getAdapter() != null && !mEmptyViews.isEmpty() && !mNonEmptyViews.isEmpty()) {

            Log.d("Bobby", "Bobby: ");

            if(getAdapter().getItemCount() == 0) {
                //****** Adapter is empty

                // show all empty views
                Util.showViews(mEmptyViews);

                // hide the recyclerview
                setVisibility(View.GONE);

                // hide all non empty views
                Util.hideViews(mNonEmptyViews);

            } else {
                //****** Adapter has at least one item inside it

                // show all non empty views
                Util.showViews(mNonEmptyViews);

                // hide the recyclerview
                setVisibility(View.VISIBLE);

                // hide all empty views
                Util.hideViews(mEmptyViews);
            }
        }
    }

}
