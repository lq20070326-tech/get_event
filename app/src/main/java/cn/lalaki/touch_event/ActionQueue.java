package cn.lalaki.touch_event;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ActionQueue {
    private static final ActionQueue instance = new ActionQueue();
    // 线程安全的阻塞队列
    public final BlockingQueue<ActionModel> queue = new LinkedBlockingQueue<>();

    private ActionQueue() {}
    public static ActionQueue getInstance() { return instance; }
}