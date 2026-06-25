package cn.lalaki.touch_event;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
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
        params.x = 90;
        params.y = 350;

        final LinearLayout layout = new LinearLayout(context);
        mFloatView = layout;

        //6.状态按钮
        final Button btnstate = new Button(context);
        btnstate.setText("状态栏：    ");

        //1.录制按钮
        final Button btnRecord = new Button(context);
        btnRecord.setText("开始录制");
        btnRecord.setOnClickListener(view -> {
            State.clearstate = false;
            State.recordstate = true;
            btnRecord.setText("⏳正在录制");
            btnstate.setText("状态栏：录制中");
        });
        //2.停止按钮
        final Button btnstop = new Button(context);
        btnstop.setText("停止");
        btnstop.setOnClickListener(view -> {
            State.recordstate = false;
            State.recoverstate = false;
            btnRecord.setText("开始录制");
            btnstate.setText("状态栏：录制成功");
        });
        //3.回溯按钮
        final Button btnexec = new Button(context);
        btnexec.setText("执行");
        btnexec.setOnClickListener(view -> {
            State.recoverstate = true;
            btnstate.setText("状态栏：执行中");
            new Thread(() -> {
                try {
                    while (State.recoverstate) {
                        // 消费者：如果没有动作，它会在这里自动阻塞等待，不占 CPU
                        ActionModel action = ActionQueue.getInstance().queue.take();
                        Log.d("actionmodel", "x:"+action.startX+"y:"+action.startY);
                        if(action.type == 1){
                            AutoClickService.click(action.startX,action.startY,action.delay,action.duration);
                        }else{
                            AutoClickService.swipe(action.startX,action.startY,action.endX,action.endY,action.delay,action.duration);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
        //4.清除按钮
        final Button btnclear = new Button(context);
        btnclear.setText("清除");
        btnclear.setOnClickListener(view -> {
            State.clearstate = true;
            ActionQueue.getInstance().queue.clear();
            btnstate.setText("状态栏：已清除");
        });
        //5.关闭按钮
        final Button btnClose = new Button(context);
        btnClose.setText("X");
        btnClose.setOnClickListener(v -> {
            if (mWindowManager != null && mFloatView != null) {
                mWindowManager.removeView(mFloatView);
                mFloatView = null;
            }
        });

        layout.addView(btnRecord);
        layout.addView(btnstop);
        layout.addView(btnexec);
        layout.addView(btnclear);
        layout.addView(btnClose);
        layout.addView(btnstate);

        mWindowManager.addView(layout, params);
    }
}
