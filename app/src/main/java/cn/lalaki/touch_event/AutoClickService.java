package cn.lalaki.touch_event;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.ArrayList;

public class AutoClickService extends AccessibilityService {
    static int index = 0;
    static ArrayList<ActionModel> actionModels = new ArrayList<>();

    // 静态变量，用来保存当前服务的全局单例
    public static AutoClickService mInstance;

    public static AutoClickService getInstance() {
        return mInstance;
    }

    // 当用户在系统设置里手动开启这个服务，并且系统成功连接它时，会触发这个方法
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mInstance = this; // 筑基成功，把自己的实例传给静态变量，方便别的界面直接调用
    }

    /**
     * 👑 核心驱动器：一个手势彻底完事了，才准跑下一个
     */
    public static void startSafePlayback(ArrayList<ActionModel> actionmodels) {
        Log.d("error","弹夹: "+actionmodels.size());
        if (mInstance == null) {
            Log.e("Playback", "无障碍服务未开启，无法回放");
            return;
        }
        if (actionmodels.isEmpty()) {
            Log.d("error","弹夹为空检测: "+actionmodels.size());
            State.recoverstate = false;
            return;
        }
        actionModels = actionmodels;

        // 吹响开工号角，开始消费第一个
        executeNextAction();
    }

    private static void executeNextAction() {
        Log.d("autostate"," index: "+index+" size: "+actionModels.size());
        if (index == actionModels.size()) {
            Log.d("Playback", "🏁 所有排队的手势已全部安全执行完毕！");
            FloatingWindowManager.loop--;
            index = 0;
            if(FloatingWindowManager.loop != 0) FloatingWindowManager.index = 0;
            Log.d("Autoxxxxxxxxxxx"," 状态： "+State.recoverstate+" loop: "+FloatingWindowManager.loop+" floatindex: "+FloatingWindowManager.index);
            State.executed = true;
            return;
        }
        // 1. 从你的线程安全阻塞队列里，弹出队头的一个动作
        final ActionModel action = actionModels.get(index);

        // 3. 构建当前动作的物理路径
        Path path = new Path();
        path.moveTo(action.startX, action.startY);
        if (action.type == 2) { // 如果是滑动，连线到终点
            path.lineTo(action.endX, action.endY);
        }

        GestureDescription.Builder builder = new GestureDescription.Builder();
//        int safeDuration = action.duration > 0 ? action.duration : 50;

        builder.addStroke(new GestureDescription.StrokeDescription(path, action.delay, action.duration));
        GestureDescription gesture = builder.build();

        // 4. 【核心灵魂】：利用系统的带有 ResultCallback 的接口开火
        Log.d("gesturepotion","准备开火，参数为：x:" + action.endX + "y:" + action.endY + "type:" + action.type + "delay:" + action.delay+"duration:"+action.duration);
        mInstance.dispatchGesture(gesture, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);

                // 👑 【链式递归】：当前手势彻底安全结束了，立刻去取下一个手势执行！
                index++;
                executeNextAction();
                Log.d("gesturepotion","开火成功，参数为：x:" + action.endX + "y:" + action.endY + "type:" + action.type + "delay:" + action.delay+"duration:"+action.duration);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.e("Playback", "❌ 系统通知：当前手势被异常拦截/取消了！");

                // 防死锁保底：就算被系统取消了（比如点到了不可点击区域），也得继续往下走，不能卡死在原地
                executeNextAction();
            }
        }, null);
    }

    public static void clear(){
        index = 0;
        actionModels.clear();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 监听系统窗口变化的。我们做连点器不需要管外界发生了什么，所以这里留空
    }

    @Override
    public void onInterrupt() {
        mInstance = null; // 服务被系统强行中断时清空单例
    }

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        mInstance = null; // 用户关闭服务时清空单例
        return super.onUnbind(intent);
    }
}