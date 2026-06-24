package cn.lalaki.touch_event;

public class ActionModel {
    public int type;      // 1 代表点击，2 代表滑动
    public int startX;    // 起点 X (点击和滑动共用)
    public int startY;    // 起点 Y (点击和滑动共用)
    public int endX;      // 终点 X (仅滑动需要)
    public int endY;      // 终点 Y (仅滑动需要)
    public long recordtime;
    public int duration;  // 该动作的持续时间（滑动耗时，或者点击按压时间）
    public int delay;     // 执行完这步后，休息多久再执行下一步（动作间隔延迟）

    // 快捷构建点击动作的构造函数
    public ActionModel(int x, int y, int delay, long recordtime) {
        this.type = 1;
        this.startX = x;
        this.startY = y;
        this.recordtime = recordtime;
        this.duration = 50; // 默认戳屏幕 50ms
        this.delay = delay;
    }

    // 快捷构建滑动动作的构造函数
    public ActionModel(int sx, int sy, int ex, int ey, int delay, int duration, long recordtime) {
        this.type = 2;
        this.startX = sx;
        this.startY = sy;
        this.endX = ex;
        this.endY = ey;
        this.recordtime = recordtime;
        this.duration = duration;
        this.delay = delay;
    }
}