package Server;

import javax.swing.*;
import java.awt.*;

/**
 * @ClassName ServerFrameTool
 * @Description TODO
 * @Author zk_kiger
 * @Date 2019/5/10 18:33
 * @Version 1.0
 */

public class ServerFrameTool {
    private ServerFrameTool() {}

    /**
     * 窗体居中
     * @param jf
     */
    public static void setFrameCenter(JFrame jf, int WIDTH, int HEIGHT) {
        //获得屏幕的宽和高
        int width = Toolkit.getDefaultToolkit().getScreenSize().width;
        int height = Toolkit.getDefaultToolkit().getScreenSize().height;

        //设置窗体居中
        jf.setLocation((width - WIDTH)/2, (height - HEIGHT)/2);
    }
}
