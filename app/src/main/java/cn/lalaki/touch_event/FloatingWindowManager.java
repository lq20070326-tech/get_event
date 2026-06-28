package cn.lalaki.touch_event;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class FloatingWindowManager {
    private static WindowManager mWindowManager;
    private static View mFloatView;
    private static ArrayList<ActionModel> actionModels = new ArrayList<>();
    static int index = 0;
    static int size = 0;
    static int loop = 5;

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

        //1.录制按钮
        final Button btnRecord = new Button(context);
        btnRecord.setText("开始录制");
        btnRecord.setTextSize(12);
        btnRecord.setOnClickListener(view -> {
            if(!State.recordstate) {
                State.clearstate = false;
                State.recordstate = true;
                btnRecord.setText("⏳结束录制");
            }else{
                State.recordstate = false;
                size = ActionQueue.getInstance().queue.size();
                if (size == 1) return ;
                for (int i = 0; i < size-1; i++) {
                    actionModels.add(ActionQueue.getInstance().queue.poll());
                    Log.d("initializating","正在进行初始化，size："+ActionQueue.getInstance().queue.size());
                }
                ActionQueue.getInstance().queue.clear();
                Log.d("initialised","初始化成功，当前手势数量："+actionModels.size());
                btnRecord.setText("开始录制");
            }
        });
//        //2.停止按钮
//        final Button btnstop = new Button(context);
//        btnstop.setText("停止");
//        btnstop.setTextSize(12);
//        btnstop.setOnClickListener(view -> {
//            btnstop.setText("已停止");
//            index = 0;
//            State.recoverstate = false;
//            btnRecord.setText("开始录制");
//        });
        //3.回溯按钮
        final Button btnexec = new Button(context);
        btnexec.setText("执行");
        btnexec.setTextSize(12);
        btnexec.setOnClickListener(view -> {
            if(!State.recoverstate){
                btnexec.setText("停止执行");
                State.recoverstate = true;
                AutoClickService.index = 0;
                new Thread(() -> {
                    Log.d("xianc","线程已启动");
                    while(State.recoverstate && loop > 0){
                        while (State.executed) {
                            try {
                                AutoClickService.startSafePlayback(actionModels);
                                State.executed = false;
                                Log.d("floaxxxxxxxxxxx"," 状态： "+State.recoverstate+" loop: "+FloatingWindowManager.loop+" floatindex: "+FloatingWindowManager.index);
                            } catch (NullPointerException e){
                                State.recoverstate = false;
                            }
                        }
                    }
                }).start();
            }else{
                State.recoverstate = false;
                btnexec.setText("执行");
                index = 0;
                loop = 5;
                AutoClickService.index = 0;
            }
        });
        //3.5循环按钮
        final Button btnloop = new Button(context);
        btnloop.setText("循环");
        btnloop.setTextSize(12);
        btnloop.setOnClickListener(view -> {
            Context ctx = view.getContext();
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            EditText etNum = new EditText(ctx);
            etNum.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            builder.setView(etNum);
            builder.setTitle("设置循环次数");
            builder.setPositiveButton("确认", (dialog, which) -> {
                String numStr = etNum.getText().toString();
                if (!numStr.isEmpty()) {
                    loop = Integer.parseInt(numStr);
                }
            });
            AlertDialog dialog = builder.create();
            // 悬浮窗弹窗必须叠加悬浮层级
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            dialog.show();
        });
        //4.清除按钮
        final Button btnclear = new Button(context);
        btnclear.setText("清除");
        btnclear.setTextSize(12);
        btnclear.setOnClickListener(view -> {
            index = 0;
            State.clearstate = true;
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
        layout.addView(btnRecord, rowParams);
        layout.addView(btnloop, rowParams);
        layout.addView(btnexec, rowParams);
        layout.addView(btnclear, rowParams);

        // 👑 5. 调整一下悬浮窗 params 的初始弹窗位置，贴着左边更舒服
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 10;  // 离屏幕左边缘 10 像素
        params.y = 400; // 离屏幕顶端 400 像素

        mWindowManager.addView(layout, params);
    }
}
