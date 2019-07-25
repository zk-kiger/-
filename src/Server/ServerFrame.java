package Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @ClassName ServerFrame
 * @Description TODO
 * @Author zk_kiger
 * @Date 2019/5/9 11:44
 * @Version 1.0
 */

public class ServerFrame extends JFrame implements ActionListener {
    //定义窗口大小
    private final int WIDTH = 550;
    private final int HEIGHT = 550;
    //封装服务器窗口所要用到的组件
    //启动服务器按钮
    private JButton butStartServer;
    //停止服务器按钮
    private JButton butStopServer;
    //关闭服务器按钮
    private JButton butCloseServer;
    //信息通知窗口
    private JTextArea jtaMessage;
    //在线人数窗口
    private JList jltOnline;
    //服务器类
    private Server server;

    //定义服务器窗口属性
    public ServerFrame(Server server) {
        this.server = server;
        //设置标题
        setTitle("服务器");
        //设置窗口图片
        setIconImage(Toolkit.getDefaultToolkit().getImage("src/server.jpg"));
        //窗口大小
        setSize(WIDTH, HEIGHT);
        //窗口不可缩放
        setResizable(false);
        //窗口居中
        ServerFrameTool.setFrameCenter(this, WIDTH, HEIGHT);
        //设置窗口关闭
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                butCloseServer.doClick();
            }
        });
        //将布局设置为自定义布局
        setLayout(null);

        //启动服务器按钮
        butStartServer = new JButton("启动服务器");
        //设置位置和大小
        butStartServer.setBounds(35, 30, 150, 60);
        //设置按钮风格
        butStartServer.setFont(new Font("宋体", Font.BOLD, 15));
        //为按钮添加监听事件 - 因为该对象实现了监听接口
        butStartServer.addActionListener(this);
        this.add(butStartServer);

        //停止服务器按钮
        butStopServer = new JButton("停止服务器");
        butStopServer.setBounds(197, 30, 150, 60);
        //设置按钮风格
        butStartServer.setFont(new Font("宋体", Font.BOLD, 15));
        //先设置停止服务器按钮不能使用，启动服务器按钮可用
        butStopServer.setEnabled(false);
        butStopServer.addActionListener(this);
        this.add(butStopServer);

        //退出服务器按钮
        butCloseServer = new JButton("退出服务器");
        butCloseServer.setBounds(357, 30, 150, 60);
        //设置按钮风格
        butStartServer.setFont(new Font("宋体", Font.BOLD, 15));
        butCloseServer.addActionListener(this);
        this.add(butCloseServer);

        //信息窗
        jtaMessage = new JTextArea();
        //设置不可编辑
        jtaMessage.setEditable(false);
        //自动换行
        jtaMessage.setLineWrap(true);
        //设置字体
        jtaMessage.setFont(new Font("宋体", Font.BOLD, 14));
        //信息滚动窗
        JScrollPane jspMessage = new JScrollPane(jtaMessage);
        jspMessage.setBounds(20, 120, 270, 350);
        //启用滚轮滑动
        jspMessage.setWheelScrollingEnabled(true);
        jspMessage.setBorder(BorderFactory.createTitledBorder("操作信息"));
        //设置信息滚动窗水平属性
        jspMessage.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //设置信息滚动窗垂直属性
        jspMessage.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.add(jspMessage);

        //在线人数列表
        jltOnline = new JList();
        //设置字体
        jltOnline.setFont(new Font("宋体", Font.BOLD, 14));
        //列表滚动窗
        JScrollPane jspOnline = new JScrollPane(jltOnline);
        jspOnline.setBounds(310, 120, 210, 350);
        jspOnline.setBorder(BorderFactory.createTitledBorder("在线用户"));
        //设置在线列表滚动窗水平属性：不出现
        jspOnline.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //设置在线列表滚动窗垂直属性：需要时出现
        jspOnline.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.add(jspOnline);
    }

    /**
     * 重写鼠标点击事件方法
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        //e.getSource():获取该事件发生的对象
        if(e.getSource() == butStartServer) {
            //启动服务器按钮不可用
            butStartServer.setEnabled(false);
            //停止服务器按钮可用
            butStopServer.setEnabled(true);
            //启动服务器
            server.startServer();
        }
        if(e.getSource() == butStopServer) {
            int flag = JOptionPane.showConfirmDialog(this, "是否要停止服务器？", "",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(flag == JOptionPane.OK_OPTION){
                butStartServer.setEnabled(true);
                butStopServer.setEnabled(false);
                server.stopServer();
            }
        }
        if(e.getSource() == butCloseServer) {
            if(butStopServer.isEnabled()) {
                butStopServer.doClick();
                System.exit(0);
            }
            System.exit(0);
        }

    }

    public JList getJltOnline() {
        return jltOnline;
    }

    public JTextArea getJtaMessage() {
        return jtaMessage;
    }
}
