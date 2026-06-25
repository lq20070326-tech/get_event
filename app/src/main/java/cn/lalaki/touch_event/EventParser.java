package cn.lalaki.touch_event;

import java.util.ArrayList;
import java.util.List;

public class EventParser {
    // 暂存当前的坐标状态
    private int curX = 0, curY = 0;
    private int endx = 0 , endy = 0;
    //TODO 实际位置偏差大，需要修bug
    private final float maxx = 143999;
    private final float maxy = 319999;
    private final int resolusionx = 1440;
    private final int resolusiony = 3200;
    private boolean isTouching = false;
    long starttime;
    long endtime;
    int type;

    // 核心：处理单字节，如果凑齐一行且产生完整点击动作，返回 ActionModel
    public ActionModel handleCh(String line) {
        // 伪代码：解析 getevent 的 16 进制字符串
        // 格式通常是: [设备名]: [TYPE] [CODE] [VALUE]
        // 这里需要你根据你的 getevent 输出格式调整解析逻辑
        if (line.contains("0003 0035")) { // 假设这是 X 坐标代号
            if (curX == 0){
                curX = (int) ((parseHex(line)/maxx)*resolusionx) ;
                type = 1;
            }else{
                endx = (int) ((parseHex(line)/maxx)*resolusionx) ;
                type = 2;
            }
        } else if (line.contains("0003 0036")) { // 假设这是 Y 坐标代号
            if (curY == 0){
                curY = (int) ((parseHex(line)/maxy)*resolusiony) ;
            }else{
                endy = (int) ((parseHex(line)/maxy)*resolusiony) ;
            }
        } else if (line.contains("0001 014a 00000001")) { // 按下事件
            isTouching = true;
            starttime = System.currentTimeMillis();
        } else if (line.contains("0001 014a 00000000")) { // 松开事件
            endtime = System.currentTimeMillis();
            if (isTouching) {
                isTouching = false;
                // 触发了一个点击动作！
                if(type == 1) {
                    ActionModel actionModel = new ActionModel(curX, curY, 0, (int) (endtime - starttime), starttime);
                    curX = 0 ; curY = 0;
                    return actionModel;
                }else{
                    ActionModel actionModel = new ActionModel(curX, curY, endx, endy, 0, (int) (endtime - starttime), starttime);
                    curX = 0; curY = 0; endx = 0; endy = 0;
                    return actionModel;
                }
            }
        }
        return null;
    }

    private int parseHex(String line) {
        String hex = line.substring(line.lastIndexOf(" ") + 1);
        return Integer.parseInt(hex, 16);
    }
}