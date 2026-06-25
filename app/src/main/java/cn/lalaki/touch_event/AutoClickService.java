package cn.lalaki.touch_event;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class AutoClickService extends AccessibilityService {

    // 静态变量，用来保存当前服务的全局单例
    private static AutoClickService mInstance;

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
     * 核心静态方法：对外开放的“开火接口”，传入 X 和 Y 坐标就能点一下屏幕
     */
    public static void click(int x, int y, int delay, int duration) {
        // 如果服务没开启，mInstance 为空，直接拦截，防止空指针崩溃
        if (mInstance == null) return;

        // 1. 创建一条点击路径。Path 在安卓里是画笔轨迹，我们要点击一个点，就把轨迹移到(x,y)
        Path path = new Path();
        path.moveTo(x, y);

        // 2. 构建手势描述器
        GestureDescription.Builder builder = new GestureDescription.Builder();

        // StrokeDescription 参数含义：
        // path: 点击的轨迹
        // 0: 收到指令后延迟 0 毫秒立刻执行
        // 50: 手指在屏幕上按压持续 50 毫秒（模拟正常人类戳屏幕的动作）
        builder.addStroke(new GestureDescription.StrokeDescription(path, delay, duration));
        GestureDescription gesture = builder.build();

        // 3. 让无障碍大管家把这个手势甩给屏幕，强行触发物理点击
        mInstance.dispatchGesture(gesture, null, null);
    }

    /**
     * 核心静态方法：让外部随时可以调用来模拟一次屏幕滑动
     * @param startX 起点 X
     * @param startY 起点 Y
     * @param endX   终点 X
     * @param endY   终点 Y
     * @param duration 滑动耗时（毫秒，比如滑动过程持续 300ms）
     */
    public static void swipe(int startX, int startY, int endX, int endY, int delay, int duration) {
        if (mInstance == null) return;

        // 1. 构建滑动轨迹：从起点 moveTo 连线 lineTo 到终点
        Path swipePath = new Path();
        swipePath.moveTo(startX, startY);
        swipePath.lineTo(endX, endY);

        GestureDescription.Builder builder = new GestureDescription.Builder();

        // 2. 关键参数设置：
        // swipePath: 滑动轨迹
        // 0: 收到指令立刻执行，不延迟
        // duration: 手指在屏幕上滑动的物理持续时间（划拉得快还是慢，全靠它控制）
        builder.addStroke(new GestureDescription.StrokeDescription(swipePath, delay, duration));
        GestureDescription gesture = builder.build();

        // 3. 甩出滑动指令
        mInstance.dispatchGesture(gesture, null, null);
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