package com.example.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class LoadingUtil {

    private static Dialog loadingDialog;

    public static void show(Context context) {

        if (context == null) {
            return;
        }

        hide();

        loadingDialog =
                new Dialog(context);

        loadingDialog.requestWindowFeature(
                Window.FEATURE_NO_TITLE
        );

        loadingDialog.setCancelable(false);

        ImageView imageView =
                new ImageView(context);

        imageView.setImageResource(
                R.drawable.bluelock_loader
        );

        imageView.setScaleType(
                ImageView.ScaleType.CENTER_INSIDE
        );

        int size = dp(context, 110);

        loadingDialog.setContentView(
                imageView
        );

        Window window =
                loadingDialog.getWindow();

        if (window != null) {

            window.setBackgroundDrawable(
                    new ColorDrawable(
                            Color.TRANSPARENT
                    )
            );

            WindowManager.LayoutParams params =
                    new WindowManager.LayoutParams();

            params.copyFrom(
                    window.getAttributes()
            );

            params.width = size;
            params.height = size;
            params.gravity = Gravity.CENTER;
            params.dimAmount = 0.55f;

            window.setAttributes(params);

            window.addFlags(
                    WindowManager.LayoutParams
                            .FLAG_DIM_BEHIND
            );
        }

        RotateAnimation rotation =
                new RotateAnimation(
                        0f,
                        360f,
                        RotateAnimation.RELATIVE_TO_SELF,
                        0.5f,
                        RotateAnimation.RELATIVE_TO_SELF,
                        0.5f
                );

        rotation.setDuration(900);

        rotation.setInterpolator(
                new LinearInterpolator()
        );

        rotation.setRepeatCount(
                RotateAnimation.INFINITE
        );

        imageView.startAnimation(rotation);

        loadingDialog.show();
    }

    public static void hide() {

        if (loadingDialog != null) {

            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }

            loadingDialog = null;
        }
    }

    private static int dp(
            Context context,
            int value
    ) {

        return Math.round(
                value
                        * context.getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}