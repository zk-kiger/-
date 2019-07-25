package Client;

import Server.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;

/**
 * @ClassName ChatFrame
 * @Description TODO
 * @Author zk_kiger
 * @Date 2019/5/12 20:35
 * @Version 1.0
 */

public class ChatFrame extends JFrame implements ActionListener, KeyListener, ListSelectionListener {
    private Client client;
    //窗口大小
    private final int WIDTH = 700;
    private final int HEIGHT = 500;
    //信息输入框
    private JTextField jtfInputMessage;
    //在线用户列表
    private JList jlsOnline;
    //聊天信息显示框
    private JTextArea jtaChatMessage;
    //发送按钮
    private JButton jbuSend;
    //退出聊天室按钮
    private JButton jbuExit;
    //清除聊天记录
    private JButton jbuClear;

    public ChatFrame(Client client, String title) {
        this.client = client;
        //设置聊天框图片
        setIconImage(Toolkit.getDefaultToolkit().getImage("src/timg.jpg"));
        //设置标题
        setTitle("聊天室" + "  " + title);
        //设置窗口大小
        setSize(WIDTH, HEIGHT);
        //窗口居中
        ServerFrameTool.setFrameCenter(this, WIDTH, HEIGHT);
        //不可缩放
        setResizable(false);
        //窗口关闭
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                jbuExit.doClick();
            }
        });
        //默认布局
        setLayout(null);

        //聊天信息窗
        jtaChatMessage = new JTextArea();
        //设置不可编辑
        jtaChatMessage.setEditable(false);
        //自动换行
        jtaChatMessage.setLineWrap(true);
        //设置字体
        jtaChatMessage.setFont(new Font("宋体", Font.BOLD, 14));
        //信息滚动窗
        JScrollPane jspChatMessage = new JScrollPane(jtaChatMessage);
        jspChatMessage.setBounds(15, 15, 424, 250);
        //启用滚轮滑动
        jspChatMessage.setWheelScrollingEnabled(true);
        jspChatMessage.setBorder(BorderFactory.createTitledBorder("聊天信息"));
        //设置信息滚动窗水平属性
        jspChatMessage.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //设置信息滚动窗垂直属性
        jspChatMessage.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.add(jspChatMessage);

        //信息输入框
        jtfInputMessage = new JTextField();
        jtfInputMessage.addKeyListener(this);   //添加回车发送信息功能
        jtfInputMessage.setBounds(15, 363, 288, 48);
        jtfInputMessage.setFont(new Font("楷体", Font.BOLD, 14));
        this.add(jtfInputMessage);
        jtfInputMessage.setColumns(10);

        //信息发送按钮
        jbuSend = new JButton("发送");
        jbuSend.setFont(new Font("宋体", Font.PLAIN, 14));
        jbuSend.setBounds(306, 363, 139, 48);
        jbuSend.addActionListener(this);    //添加鼠标点击事件
        this.add(jbuSend);

        //退出聊天室按钮
        jbuExit = new JButton("退出聊天室");
        jbuExit.setFont(new Font("宋体", Font.PLAIN, 14));
        jbuExit.setBounds(20, 285, 192, 55);
        jbuExit.addActionListener(this);
        this.add(jbuExit);

        //清除聊天记录
        jbuClear = new JButton("清除聊天记录");
        jbuClear.setFont(new Font("宋体", Font.PLAIN, 14));
        jbuClear.setBounds(237, 285, 202, 55);
        jbuClear.addActionListener(this);
        this.add(jbuClear);

        //在线用户列表
        jlsOnline = new JList();
        JScrollPane jspOnline = new JScrollPane(jlsOnline);
        jspOnline.setBounds(454, 15, 220, 394);
        jspOnline.setBorder(BorderFactory.createTitledBorder("在线用户"));
        //设置在线列表滚动窗水平属性：不出现
        jspOnline.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //设置在线列表滚动窗垂直属性：需要时出现
        jspOnline.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.add(jspOnline);

        //在线列表添加鼠标点击事件 - 双击打开私聊界面
        jlsOnline.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(jlsOnline.getSelectedIndex() != -1) {
                    //实现双击事件
                    if(e.getClickCount() == 2) {
                        //获得双击的对象 - 私聊用户的用户名
                        String singler = (String)jlsOnline.getSelectedValue();
                        //遍历userNameMap,获得对应的uid
                        Set<String> set = client.userNameMap.keySet();
                        //用来记录私聊用户的uid
                        String receiveUid = null;
                        for (String uid : set
                             ) {
                            String name = client.userNameMap.get(uid);
                            //如果当前uid对用的用户名和私聊用户的用户名相同，那么就记录该用户的uid
                            if(name.equals(singler)) {
                                receiveUid = uid;
                                break;
                            }
                        }
                        //判断这个用户是否有对应的私聊窗口
                        SingleFrame singleFrame = client.SingleMap.get(receiveUid);
                        if(singleFrame == null) {
                            //创建一个与该私聊用户的私聊窗口
                            singleFrame = new SingleFrame(client, singler, receiveUid);
                            //并记录
                            client.SingleMap.put(receiveUid, singleFrame);
                            singleFrame.setVisible(true);
                        }
                    }
                }
            }
        });
    }

    //按钮点击事件
    @Override
    public void actionPerformed(ActionEvent e) {
        //清除消息记录
        if(e.getSource() == jbuClear) {
            jtaChatMessage.setText("");
        }
        //发送信息
        if(e.getSource() == jbuSend) {
            //获取输入框内容
            String message = jtfInputMessage.getText();
            message.trim();
            //清除输入框
            jtfInputMessage.setText("");
            //如果内容为空
            if(message.equals("")) {
                JOptionPane.showMessageDialog(this, "不能发送空消息");
            } else {
                client.sendMessage("ChatGroup/" + message);
            }
        }
        //退出聊天室
        if(e.getSource() == jbuExit) {
            if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this,
                    "是否确定要退出聊天室？", "提示", JOptionPane.OK_CANCEL_OPTION)) {
                this.setVisible(false);
                client.exitChat();
                System.exit(0);
            }
        }

    }

    //输入栏回车输出信息
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (e.getSource() == jtfInputMessage) {
                jbuSend.doClick();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == jlsOnline) {
        }
    }

    public JTextArea getJtaChatMessage() {
        return jtaChatMessage;
    }

    public JList getJlsOnline() {
        return jlsOnline;
    }
}
