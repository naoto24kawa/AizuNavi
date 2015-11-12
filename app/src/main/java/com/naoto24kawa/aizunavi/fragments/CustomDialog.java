package com.naoto24kawa.aizunavi.fragments;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.naoto24kawa.aizunavi.R;
import com.naoto24kawa.aizunavi.entities.OriginalSpot;

public class CustomDialog extends DialogFragment {

    private View.OnClickListener onPositiveButtonClickListener;

    private View.OnClickListener onNegativeButtonClickListener;

    private OriginalSpot spot;

    private EditText mTitle;

    private EditText mDesc;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(getActivity());
        // タイトル非表示
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // フルスクリーン
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        dialog.setContentView(R.layout.dialog_custom);
        // 背景を透明にする
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if (spot != null) {
            mTitle = (EditText) dialog.findViewById(R.id.marker_title);
            mTitle.setText(getName(spot));

            mDesc = (EditText) dialog.findViewById(R.id.marker_description);
            mDesc.setText(spot.getDescription());
        }

        if (onPositiveButtonClickListener == null) {
            onPositiveButtonClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            };
        }
        // OK ボタンのリスナ
        dialog.findViewById(R.id.positive_button).setOnClickListener(onPositiveButtonClickListener);

        if (onNegativeButtonClickListener == null) {
            onNegativeButtonClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            };
        }
        // DELETE ボタンのリスナ
        dialog.findViewById(R.id.negative_button).setOnClickListener(onNegativeButtonClickListener);

        // Close ボタンのリスナ
        dialog.findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return dialog;
    }

    public void setOnPositiveButtonClickListener(View.OnClickListener onClickListener) {
        this.onPositiveButtonClickListener = onClickListener;
    }

    public void setOnNegativeButtonClickListener(View.OnClickListener onClickListener) {
        this.onNegativeButtonClickListener = onClickListener;
    }

    public void setSpot(OriginalSpot spot) {
        this.spot = spot;
    }

    public String getTitle() {
        return mTitle.getText().toString();
    }

    public String getDesc() {
        return mDesc.getText().toString();
    }

    private String getName(OriginalSpot spot) {
        String name = spot.getKanji();
        if (name == null || name.equals("")) {
            name = spot.getKana();
        }

        return name;
    }
}
