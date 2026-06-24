package cn.lalaki.touch_event;

import java.util.ArrayList;
import java.util.List;

public class EventParser {
    // 暂存当前的坐标状态
    private int curX = 0, curY = 0;
    private boolean isTouching = false;

    // 核心：处理单字节，如果凑齐一行且产生完整点击动作，返回 ActionModel
    public ActionModel handleCh(String line) {
        long recordtime = 0;
        // 伪代码：解析 getevent 的 16 进制字符串
        // 格式通常是: [设备名]: [TYPE] [CODE] [VALUE]
        // 这里需要你根据你的 getevent 输出格式调整解析逻辑
        //TODO 加入滑动的动作处理
        if (line.contains("0003 0035")) { // 假设这是 X 坐标代号
            curX = parseHex(line);
        } else if (line.contains("0003 0036")) { // 假设这是 Y 坐标代号
            curY = parseHex(line);
        } else if (line.contains("0001 014a 00000001")) { // 按下事件
            isTouching = true;
        } else if (line.contains("0001 014a 00000000")) { // 松开事件
            if (isTouching) {
                isTouching = false;
                // 触发了一个点击动作！
                recordtime = System.currentTimeMillis();
                return new ActionModel(curX, curY, 0, recordtime);
            }
        }
        return null;
    }

    private int parseHex(String line) {
        String hex = line.substring(line.lastIndexOf(" ") + 1);
        return Integer.parseInt(hex, 16);
    }
}