package com.example.mintdemo.ui.demo1;

import android.widget.EditText;
import android.widget.Toast;

import com.example.mintdemo.R;
import com.example.mintdemo.base.mvp.BaseView;


public class LongActivity extends BaseView<LogPresenter,LoginContract.View> {
    private void initialize() {
        findViewById(R.id.main_go).setOnClickListener(v -> {
            EditText name = (EditText)findViewById(R.id.main_name);
            String names = name.getText().toString();
            EditText pwd = (EditText)findViewById(R.id.main_pwd);
            String pwds = pwd.getText().toString();
            p.getContract().loginPresenterRequest(names,pwds);
        });
    }

    @Override
    public LogPresenter getPresenter() {
        return new LogPresenter();
    }

    @Override
    public LoginContract.View getContract() {
        return new LoginContract.View() {
            @Override
            public void longViewResults(Boolean src) {
                Toast.makeText(LongActivity.this, src+"", Toast.LENGTH_SHORT).show();
            }

        };
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {
        initialize();
    }
}