package com.example.mintdemo.ui.demo2.adapter;

import android.view.View;

public class ButtonsData {
    private String name = "";
    private boolean item = true;
    private View.OnClickListener clickStandard;


    public ButtonsData(String name, View.OnClickListener clickStandard) {
        this.name = name;
        this.clickStandard = clickStandard;
    }

    public View.OnClickListener getClickStandard() {
        return clickStandard;
    }

    public void setClickStandard(View.OnClickListener clickStandard) {
        this.clickStandard = clickStandard;
    }

    public boolean getItem() {
        return item;
    }

    public void setItem(boolean item) {
        this.item = item;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
