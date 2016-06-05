package com.emilgras.boxdrops;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.emilgras.boxdrops.adapters.AdapterDrops;
import com.emilgras.boxdrops.adapters.AddListener;
import com.emilgras.boxdrops.adapters.CompleteListener;
import com.emilgras.boxdrops.adapters.Divider;
import com.emilgras.boxdrops.adapters.Filter;
import com.emilgras.boxdrops.adapters.MarkListener;
import com.emilgras.boxdrops.adapters.ResetListener;
import com.emilgras.boxdrops.adapters.SimpleTouchCallback;
import com.emilgras.boxdrops.beans.Drop;
import com.emilgras.boxdrops.extras.Util;
import com.emilgras.boxdrops.widgets.BoxRecyclerView;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class ActivityMain extends AppCompatActivity implements View.OnClickListener {

    Toolbar mToolbar;
    Button mBtnAdd;
    BoxRecyclerView mRecyclerView;
    Realm mRealm;
    RealmResults<Drop> mResults;
    AdapterDrops mAdapter;
    View mEmptyView;

    private AddListener mAddListener = new AddListener() {
        @Override
        public void add() {
            showDialogMenu();
        }
    };

    private CompleteListener mCompleteListener = new CompleteListener() {
        @Override
        public void onComplete(int position) {
            mAdapter.markComplete(position);
        }
    };

    private ResetListener mResetListener = new ResetListener() {
        @Override
        public void onReset() {
            AppBucketDrops.save(ActivityMain.this, Filter.NONE);
            loadResults(Filter.NONE);
        }
    };

    private MarkListener mMarkListener = new MarkListener() {
        @Override
        public void onMark(int position) {
            showDialogMark(position);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mEmptyView = findViewById(R.id.empty_drops);

        mBtnAdd = (Button) findViewById(R.id.btn_add);
        mBtnAdd.setOnClickListener(this);

        mRealm = Realm.getDefaultInstance();
        int filterOption = AppBucketDrops.load(this);
        loadResults(filterOption);

        mAdapter = new AdapterDrops(this, mRealm, mResults, mAddListener, mMarkListener, mResetListener);
        mAdapter.setHasStableIds(true);

        mRecyclerView = (BoxRecyclerView) findViewById(R.id.rv_drops);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.hideIfEmpty(mToolbar);
        mRecyclerView.showIfEmpty(mEmptyView);
        mRecyclerView.addItemDecoration(new Divider(this, LinearLayoutManager.VERTICAL));

        SimpleTouchCallback callback = new SimpleTouchCallback(mAdapter);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(mRecyclerView);

        initBackgroundImage();

        Util.scheduleAlarm(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        boolean handled = true;
        int filterOption = Filter.NONE;
        int id = item.getItemId();

        switch (id) {
            case R.id.action_add:
                showDialogMenu();
                break;

            case R.id.action_sort_descending_date:
                filterOption = Filter.LEAST_TIME_LEFT;
                mResults.addChangeListener(mChangeListener);
                break;

            case R.id.action_sort_ascending_date:
                filterOption = Filter.MOST_TIME_LEFT;
                mResults.addChangeListener(mChangeListener);
                break;

            case R.id.action_show_complete:
                filterOption = Filter.COMPLETE;
                mResults.addChangeListener(mChangeListener);
                break;

            case R.id.action_show_incomplete:
                filterOption = Filter.INCOMPLETE;
                mResults.addChangeListener(mChangeListener);
                break;

            default:
                handled = false;
                break;

        }

        AppBucketDrops.save(this, filterOption);
        loadResults(filterOption);
        return handled;
    }

    private void loadResults(int filterOption) {

        switch (filterOption) {
            case Filter.NONE:
                mResults = mRealm.where(Drop.class).findAllAsync();
                break;
            case Filter.LEAST_TIME_LEFT:
                mResults = mRealm.where(Drop.class).findAllSortedAsync("when", Sort.DESCENDING);
                break;
            case Filter.MOST_TIME_LEFT:
                mResults = mRealm.where(Drop.class).findAllSortedAsync("when", Sort.ASCENDING);
                break;
            case Filter.COMPLETE:
                mResults = mRealm.where(Drop.class).equalTo("complete", true).findAllAsync();
                break;
            case Filter.INCOMPLETE:
                mResults = mRealm.where(Drop.class).equalTo("complete", false).findAllAsync();
                break;
        }
        mResults.addChangeListener(mChangeListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mResults.addChangeListener(mChangeListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mResults.removeChangeListener(mChangeListener);
    }

    private void initBackgroundImage() {
        // Glide is a popular third party library that handles loading big images so that we dont get memory leaks
        ImageView background = (ImageView) findViewById(R.id.iv_background);
        Glide.with(this)
                .load(R.drawable.background)
                .centerCrop()
                .into(background);
    }




    private void showDialogMenu() {
        DialogMenu menu = new DialogMenu();
        menu.show(getSupportFragmentManager(), "Menu");
    }

    private void showDialogMark(int position) {
        DialogMark mark = new DialogMark();
        Bundle bundle = new Bundle();
        bundle.putInt("POSITION", position);
        mark.setArguments(bundle);
        mark.setCompleteListener(mCompleteListener);
        mark.show(getSupportFragmentManager(), "Mark");
    }

    @Override
    public void onClick(View v) {
        showDialogMenu();
    }




    private RealmChangeListener mChangeListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            mAdapter.updateUI(mResults);
        }
    };

}
