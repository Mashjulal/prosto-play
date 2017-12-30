package com.mashjulal.android.prostoplay;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mashjulal.android.prostoplay.prosto_pleer.AccessTokenModel;
import com.mashjulal.android.prostoplay.prosto_pleer.ProstoPleerClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateAccessTokenReceiver extends BroadcastReceiver {

    private static final String TAG = UpdateAccessTokenReceiver.class.getSimpleName();

    private static final long DELAY = 1000 * 60 * 55; // 55 minutes

    @Override
    public void onReceive(final Context context, Intent intent) {
        ProstoPleerClient.getInstance(context).authorize().enqueue(new Callback<AccessTokenModel>() {
            @Override
            public void onResponse(Call<AccessTokenModel> call, Response<AccessTokenModel> response) {
                AccessTokenModel model = response.body();
                if (model != null) {
                    String accessToken = model.getAccessToken();
                    ProstoPleerClient.setAccessToken(accessToken);
                }
                Intent i = new Intent(context, UpdateAccessTokenReceiver.class);
                PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                am.setExact(AlarmManager.RTC, System.currentTimeMillis() + DELAY, pi);
            }

            @Override
            public void onFailure(Call<AccessTokenModel> call, Throwable t) {
                Log.e(TAG, t.getMessage());
            }
        });
    }
}
