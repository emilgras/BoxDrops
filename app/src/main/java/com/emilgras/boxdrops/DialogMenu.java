package com.emilgras.boxdrops;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.emilgras.boxdrops.beans.Drop;
import com.emilgras.boxdrops.widgets.CustomPickerView;

import io.realm.Realm;

/**
 * Created by emilgras on 20/05/2016.
 */
public class DialogMenu extends DialogFragment implements View.OnClickListener {

    private ImageButton mBtnClose;
    private EditText mInputWhat;
    private CustomPickerView mInputWhen;
    private Button mBtnAdd;

    public DialogMenu() {}


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_menu, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBtnClose = (ImageButton) view.findViewById(R.id.btn_close);
        mInputWhat = (EditText) view.findViewById(R.id.et_drop);
        mInputWhen = (CustomPickerView) view.findViewById(R.id.pv_date);
        mBtnAdd = (Button) view.findViewById(R.id.btn_add_it);

        mBtnClose.setOnClickListener(this);
        mBtnAdd.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btn_add_it:
                addAction();
                break;
        }

        dismiss();
    }

    private void addAction() {
        String what = mInputWhat.getText().toString();
        long currentTime = System.currentTimeMillis();
        Realm realm = Realm.getDefaultInstance();
        Drop drop = new Drop(what, currentTime, mInputWhen.getTime(), false);
        realm.beginTransaction();
        realm.copyToRealm(drop);
        realm.commitTransaction();
        realm.close();
    }
}
