package com.emilgras.boxdrops;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.emilgras.boxdrops.adapters.CompleteListener;

/**
 * Created by emilgras on 03/06/2016.
 */
public class DialogMark extends DialogFragment {

    private ImageButton mBtnClose;
    private Button mBtnCompleted;
    private CompleteListener mCompleteListener;
    private View.OnClickListener mBtnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.btn_completed:
                    markAsComplete();
                    break;
            }
            dismiss();

        }
    };

    public DialogMark() {}

    private void markAsComplete() {
        Bundle args = getArguments();

        if (mCompleteListener != null && args != null) {
            int position = args.getInt("POSITION");
            mCompleteListener.onComplete(position);
        }

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogTheme);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_mark, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBtnClose = (ImageButton) view.findViewById(R.id.btn_close);
        mBtnCompleted = (Button) view.findViewById(R.id.btn_completed);
        mBtnClose.setOnClickListener(mBtnClickListener);
        mBtnCompleted.setOnClickListener(mBtnClickListener);

        Bundle args = getArguments();
        if (args != null) {
            int position = args.getInt("POSITION");
        }
    }

    public void setCompleteListener(CompleteListener listener) {
        mCompleteListener = listener;
    }
}
