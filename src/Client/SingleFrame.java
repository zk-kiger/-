package Client;

import Server.*;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ClassName SingleFrame
 * @Description TODO
 * @Author zk_kiger
 * @Date 2019/5/12 20:35
 * @Version 1.0
 */

public class SingleFrame extends JFrame implements ActionListener, KeyListener {
    private Client client;
    private String receiverUid;
    private String singler;
    //窗口大小
    private final int WIDTH = 550;
    private final int HEIGHT = 550;
    //私聊信息窗
    private JTextArea jtaSingleMessage;
    //私聊输入窗
    private JTextField jtfSingleInput;
    //发送按钮
    private JButton jbuSend;
    //文件上传按钮
    private JButton jbuFile;
    //表情按钮
    private JButton jbuFace;

    public SingleFrame(Client client, String singler, String receiverUid) {
        this.singler = singler;
        this.receiverUid = receiverUid;
        this.client = client;
        //设置窗口图片
        setIconImage(Toolkit.getDefaultToolkit().getImage("src/cxk.jpg"));
        //设置窗口居中
        ServerFrameTool.setFrameCenter(this, WIDTH, HEIGHT);
        //设置标题
        setTitle(singler);
        //设置窗口大小
        setSize(WIDTH, HEIGHT);
        //设置不可缩放
        setResizable(false);
        //设置窗口关闭
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeSingleFrame();
            }
        });
        //默认布局
        setLayout(null);

        //聊天信息窗
        jtaSingleMessage = new JTextArea();
        //设置不可编辑
        jtaSingleMessage.setEditable(false);
        //自动换行
        jtaSingleMessage.setLineWrap(true);
        //设置字体
        jtaSingleMessage.setFont(new Font("宋体", Font.BOLD, 14));
        //信息滚动窗
        JScrollPane jspSingleMessage = new JScrollPane(jtaSingleMessage);
        jspSingleMessage.setBounds(0, 0, 550, 450);
        //启用滚轮滑动
        jspSingleMessage.setWheelScrollingEnabled(true);
        jspSingleMessage.setBorder(BorderFactory.createTitledBorder("聊天信息"));
        //设置信息滚动窗水平属性
        jspSingleMessage.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //设置信息滚动窗垂直属性
        jspSingleMessage.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.add(jspSingleMessage);

        //信息输入框
        jtfSingleInput = new JTextField();
        jtfSingleInput.addKeyListener(this);   //添加回车发送信息功能
        jtfSingleInput.setBounds(3, 480, 460, 30);
        jtfSingleInput.setFont(new Font("楷体", Font.BOLD, 14));
        this.add(jtfSingleInput);
        jtfSingleInput.setColumns(10);

        //信息发送按钮
        jbuSend = new JButton("发送");
        jbuSend.setFont(new Font("宋体", Font.PLAIN, 14));
        jbuSend.setBounds(464, 480,75, 30);
        jbuSend.addActionListener(this);    //添加鼠标点击事件
        this.add(jbuSend);

        //文件上传按钮
        jbuFile = new JButton("文件");
        jbuFile.setFont(new Font("宋体", Font.PLAIN, 14));
        jbuFile.setBounds(3, 450, 75, 30);
        jbuFile.addActionListener(this);
        this.add(jbuFile);

        //表情按钮
        jbuFace = new JButton("表情包");
        jbuFace.setFont(new Font("宋体", Font.PLAIN, 14));
        jbuFace.setBounds(79, 450, 90, 30);
        jbuFace.addActionListener(this);
        this.add(jbuFace);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //如果是发送按钮
        if(e.getSource() == jbuSend) {
            //获取用户发送的信息
            String message = jtfSingleInput.getText();
            message.trim();
            jtfSingleInput.setText("");
            if("".equals(message)) {
                JOptionPane.showMessageDialog(this, "信息不能为空");
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd  hh:mm:ss");
                //在自己的界面显示聊天信息
                jtaSingleMessage.append(client.username + "  " + sdf.format(new Date())
                        + "\n" + message + "\n");
                //将信息发送到接收人的端口，需要拼接接收人的uid
                String content = "Chat/" + receiverUid + "/" + message;
                //向服务器发送信息
                client.sendMessage(content);
            }
        }
        //如果是文件按钮
        if(e.getSource() == jbuFile) {
            //选择文件，获取文件路径
            JFileChooser jFileChooser = new JFileChooser();
            //设置文件打开模式 - 只能选文件
            jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            //获取桌面路径，默认打开文件选择窗口时，在桌面路径
            FileSystemView fsv = FileSystemView.getFileSystemView();
            System.out.println(fsv.getHomeDirectory());
            jFileChooser.setCurrentDirectory(fsv.getHomeDirectory());
            //选择窗口标题信息
            jFileChooser.setDialogTitle("上传文件的路径");
            jFileChooser.setApproveButtonText("确定");
            //用于显示选择文件框
            int result = jFileChooser.showOpenDialog(this);
            //如果用户点击确认
            if(JFileChooser.APPROVE_OPTION == result) {
                //获得上传文件对象
                File file = jFileChooser.getSelectedFile();
                //拼接文件信息
                StringBuilder sb = new StringBuilder();
                sb.append("File/");
                sb.append(receiverUid);
                sb.append("/");
                sb.append(file.getName());
                sb.append("/");
                sb.append(file.length());
                // 需要关闭该客户端的接收线程
                client.setFlag_eixt(false);
                // 1.先将上传文件信息传给服务器
                try {
                    client.dos.writeUTF(sb.toString());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                // 2.读取文件对象,上传文件
                try (
                        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))
                ){
                    int len = -1;
                    byte[] bytes = new byte[1024];
                    while((len = bis.read(bytes)) != -1) {
                        client.dos.write(bytes, 0, len);
                    }
                    client.setFlag_eixt(true);
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            if(e.getSource() == jtfSingleInput){
                jbuSend.doClick();
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    @Override
    public void keyReleased(KeyEvent e) {}

    //关闭私聊窗口
    public void closeSingleFrame(){
        //从SingleMap中将对应的私聊端移除
        client.SingleMap.remove(receiverUid);
        setVisible(false);
    }

    public JTextArea getJtaSingleMessage() {
        return jtaSingleMessage;
    }
}
