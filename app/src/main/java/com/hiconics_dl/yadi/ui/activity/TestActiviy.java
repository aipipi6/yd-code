package com.hiconics_dl.yadi.ui.activity;

import android.os.Bundle;

import com.freelink.library.widget.CircularProgressView;
import com.hiconics_dl.yadi.R;
import com.hiconics_dl.yadi.base.BaseToolBarActivity;

import butterknife.BindView;


public class TestActiviy extends BaseToolBarActivity {
    @BindView(R.id.CircularProgressView)
    CircularProgressView CircularProgressView;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_audio_update;
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        getHandler().postDelayed(runnable, 200);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getHandler().removeCallbacks(runnable);
    }

    int progress = 0;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            progress++;
            if(progress > 100) {
                progress = 0;
            }
            float fp = progress / 100f;
            CircularProgressView.setProgress(fp);
            getHandler().postDelayed(this, 200);
        }
    };

}
