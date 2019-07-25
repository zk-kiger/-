package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.UnknownHostException;

import Server.*;

/**
 * @ClassName LoginFrame
 * @Description 用户登录界面 - 需要实现登录之后跳转到聊天界面
 * @Author zk_kiger
 * @Date 2019/5/12 18:11
 * @Version 1.0
 */

public class LoginFrame extends JFrame implements ActionListener, KeyListener{
    //定义窗口大小
    private final int WIDTH = 550;
    private final int HEIGHT = 400;
    //用户名
    private JTextField jtfName;
    //IP地址
    private JTextField jtfIp;
    //端口号
    private JTextField jtfPort;
    //进入聊天室按钮
    private JButton jbtEnter;
    //退出聊天室按钮
    private JButton jbtExit;
    //客户端对象
    private Client client;

    public LoginFrame(Client client) {
        this.client = client;
        //设置窗口图片
        setIconImage(Toolkit.getDefaultToolkit().getImage("src/user.jpg"));
        //设置窗口标题
        setTitle("聊天室");
        //布局默认
        setLayout(null);
        //窗口大小
        setSize(WIDTH, HEIGHT);
        //窗口居中
        ServerFrameTool.setFrameCenter(this, WIDTH, HEIGHT);
        //不可缩放
        setResizable(false);
        //窗口关闭
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                jbtExit.doClick();
            }
        });

        //用户名标签
        JLabel jlbName = new JLabel("用户名:");
        jlbName.setFont(new Font("宋体", Font.PLAIN, 16));
        jlbName.setBounds(150, 60, 80, 31);
        this.add(jlbName);
        //用户名输入栏
        jtfName = new JTextField();
        jtfName.addKeyListener(this);    //添加监听器
        jtfName.setBounds(210, 60, 140, 31);
        this.add(jtfName);
        //设置输入栏大小
        jtfName.setColumns(10);

        //IP地址标签
        JLabel jlbIp = new JLabel("IP地址:");
        jlbIp.setFont(new Font("宋体", Font.PLAIN, 16));
        jlbIp.setBounds(150, 120, 80, 31);
        this.add(jlbIp);
        //IP地址输入栏
        jtfIp = new JTextField();
        jtfIp.addKeyListener(this);     //添加键盘事件
        jtfIp.setBounds(210, 120, 140, 31);
        this.add(jtfIp);
        //获取客户端IP地址
        try {
            String ip = (String) InetAddress.getLocalHost().getHostAddress();
            jtfIp.setText(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        jtfName.setColumns(10);

        //端口号标签
        JLabel jlbPort = new JLabel("端口号:");
        jlbPort.setFont(new Font("宋体", Font.PLAIN, 16));
        jlbPort.setBounds(150, 180, 80, 31);
        this.add(jlbPort);
        //端口号输入栏
        jtfPort = new JTextField();
        jtfPort.addKeyListener(this);
        jtfPort.setBounds(210, 180, 140, 31);
        this.add(jtfPort);
        jtfPort.setText("8888");
        jtfPort.setColumns(10);

        //进入聊天室按钮
        jbtEnter = new JButton("进入聊天室");
        jbtEnter.addActionListener(this);
        jbtEnter.addKeyListener(this);
        jbtEnter.setFont(new Font("宋体", Font.BOLD, 14));
        jbtEnter.setBounds(120, 240, 120, 40);
        this.add(jbtEnter);

        //退出聊天室按钮
        jbtExit = new JButton("退出聊天室");
        jbtExit.addActionListener(this);
        jbtExit.setFont(new Font("宋体", Font.BOLD, 14));
        jbtExit.setBounds(290, 240, 120, 40);
        this.add(jbtExit);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //退出聊天室，让该窗口不看见，并且关闭
        if(e.getSource() == jbtExit){
            setVisible(false);
            client.exitLogin();
        }
        //进入聊天室，获取用户名、服务器地址、端口号，用于登录
        if(e.getSource() == jbtEnter){
            String username = jtfName.getText();
            username.trim();
            String hostIp = jtfIp.getText();
            hostIp.trim();
            String hostPort = jtfPort.getText();
            hostPort.trim();
            if(!username.equals("")){
                if(!hostIp.equals("")){
                    if(!hostPort.equals("")){
                        String login_mess = client.login(username, hostIp, hostPort);
                        if(login_mess.equals("true")){
                            this.setVisible(false);
                            client.showChatFrame(username);
                        }else{
                            JOptionPane.showMessageDialog(this, login_mess);
                        }
                    }else{
                        JOptionPane.showMessageDialog(this, "服务器连接端口号不能为空！");
                    }
                }else{
                    JOptionPane.showMessageDialog(this, "服务器地址不能为空！");
                }
            }else{
                JOptionPane.showMessageDialog(this, "用户名不能为空！");
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        //如果用户敲击回车键，那么就模拟点击进入聊天室按键
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
            jbtEnter.doClick();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
