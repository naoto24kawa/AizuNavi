package com.naoto24kawa.aizunavi.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.naoto24kawa.aizunavi.models.Consts;
import com.naoto24kawa.aizunavi.network.ApiContents;

import java.util.Locale;

public class InitialSettingActivity extends Activity {

    private static final String TAG = InitialSettingActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        if (Consts.LOCALE.equals(Locale.JAPAN)) {
            // 日本でない場合は終了
            finish();
        }

        // N2TTSがインストールされているか確認する
        if (!isInstalled(ApiContents.N2TTS_PACKAGE_NAME)) {
            // されていない場合はインストールさせる
            String targetUri = ApiContents.GOOGLEPLAY_BASE_URI + ApiContents.N2TTS_PACKAGE_NAME;
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(targetUri));
            try {
                this.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    /**
     * 対象のアプリケーションがインストールされているか確認する
     * @param targetPackage パッケージ名
     * @return 結果
     */
    public boolean isInstalled(String targetPackage) {
        try {
            // パッケージ名を指定してインストール状況を確認する
            PackageManager packageManager = this.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(
                    targetPackage,
                    PackageManager.GET_META_DATA);
            if (applicationInfo != null) {
                return true;
            }
        } catch (NameNotFoundException exception) {
            return false;
        }
        return false;
    }
}
