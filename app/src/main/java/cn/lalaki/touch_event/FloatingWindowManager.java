package cn.lalaki.touch_event;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class FloatingWindowManager {
    private static WindowManager mWindowManager;
    private static View mFloatView;

    public static void showFloatWindow(Context context){
        if (mFloatView != null) return;

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 300;

        final LinearLayout layout = new LinearLayout(context);
        mFloatView = layout;



        //4.关闭按钮
        final Button btnClose = new Button(context);

        btnClose.setText("X");
        btnClose.setOnClickListener(v -> {
            if (mWindowManager != null && mFloatView != null) {
                mWindowManager.removeView(mFloatView);
                mFloatView = null;
            }
        });

        layout.addView(btnClose);

        mWindowManager.addView(layout, params);
    }
}
