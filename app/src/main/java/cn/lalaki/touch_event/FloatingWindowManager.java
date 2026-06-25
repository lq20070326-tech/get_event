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

import java.util.ArrayList;

public class FloatingWindowManager {
    private static WindowManager mWindowManager;
    private static View mFloatView;
    private static ArrayList<ActionModel> actionModels= new ArrayList<>();
    private static int index = 0;

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
//        params.gravity = Gravity.TOP | Gravity.START;
//        params.x = 90;
//        params.y = 350;
//
//        final LinearLayout layout = new LinearLayout(context);
//        mFloatView = layout;
        // 👑 1. 创建最外层的垂直大盒子（这就是你想要的“一竖条”）
        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL); // 👈 核心：改成垂直排列
        layout.setPadding(10, 10, 10, 10);
        layout.setBackgroundColor(0xCC000000); // 加个 80% 透明度的暗黑背景，极具科技感
        mFloatView = layout;

        // 👑 2. 统一所有按钮的“身材”参数：宽度全部拉满占满这一竖条，高度自适应
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        );
        // 给按钮之间加点上下间距，防止粘在一起不好点
        rowParams.setMargins(0, 5, 0, 5);

        //6.状态按钮
        final Button btnstate = new Button(context);
        btnstate.setText("状态栏：    ");
        btnstate.setTextSize(12);

        //1.录制按钮
        final Button btnRecord = new Button(context);
        btnRecord.setText("开始录制");
        btnRecord.setTextSize(12);
        btnRecord.setOnClickListener(view -> {
            State.clearstate = false;
            State.recordstate = true;
            btnRecord.setText("⏳正在录制");
            btnstate.setText("状态栏：录制中");
        });
        //2.停止按钮
        final Button btnstop = new Button(context);
        btnstop.setText("停止");
        btnstop.setTextSize(12);
        btnstop.setOnClickListener(view -> {
            btnstop.setText("已停止");
            index = 0;
            State.recordstate = false;
            State.recoverstate = false;
            btnRecord.setText("开始录制");
        });
        //3.回溯按钮
        final Button btnexec = new Button(context);
        btnexec.setText("执行");
        btnexec.setTextSize(12);
        btnexec.setOnClickListener(view -> {
            for (int i = 0; i < ActionQueue.getInstance().queue.size(); i++) {
                actionModels.add(ActionQueue.getInstance().queue.poll());
            }
            State.recoverstate = true;
            btnstate.setText("状态栏：执行中");
            new Thread(() -> {
                Log.d("xianc","线程已启动");
                    while (State.recoverstate) {
                        try {
                            // 消费者：如果没有动作，它会在这里自动阻塞等待，不占 CPU
                            Log.d("try","try已启动");
                            ActionModel action = actionModels.get(index);
                            if (action.type == 1) {
                                AutoClickService.click(action.startX, action.startY, action.delay, action.duration);
                                Log.d("clickposition", "x:" + action.startX + "y:" + action.startY + "type:" + action.type);
                            } else {
                                AutoClickService.swipe(action.startX, action.startY, action.endX, action.endY, action.delay, action.duration);
                                Log.d("swipeposition", "x:" + action.endX + "y:" + action.endY + "type:" + action.type);
                            }
                            if(index == actionModels.size()) index = 0;
                            index++;
                        } catch(NullPointerException e){
                            State.recoverstate = false;
                        }
                    }
            }).start();
        });
        //4.清除按钮
        final Button btnclear = new Button(context);
        btnclear.setText("清除");
        btnclear.setTextSize(12);
        btnclear.setOnClickListener(view -> {
            State.clearstate = true;
            actionModels.clear();
            btnstate.setText("状态栏：已清除");
        });
        //5.关闭按钮
        final Button btnClose = new Button(context);
        btnClose.setText("X");
        btnClose.setTextSize(12);
        btnClose.setOnClickListener(v -> {
            if (mWindowManager != null && mFloatView != null) {
                mWindowManager.removeView(mFloatView);
                mFloatView = null;
            }
        });

        layout.addView(btnClose, rowParams);
        layout.addView(btnstate, rowParams);
        layout.addView(btnRecord, rowParams);
        layout.addView(btnstop, rowParams);
        layout.addView(btnexec, rowParams);
        layout.addView(btnclear, rowParams);

        // 👑 5. 调整一下悬浮窗 params 的初始弹窗位置，贴着左边更舒服
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 10;  // 离屏幕左边缘 10 像素
        params.y = 400; // 离屏幕顶端 400 像素

        mWindowManager.addView(layout, params);
    }
}
