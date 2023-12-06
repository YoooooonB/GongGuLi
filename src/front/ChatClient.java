package front;

import back.request.chatroom.*;
import back.response.ResponseCode;
import back.response.chatroom.ChattingMessageResponse;
import back.response.chatroom.GetParticipantsChatRoomResponse;
import back.response.chatroom.JoinMessageChatRoomResponse;
import back.response.chatroom.MessageChatRoomResponse;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class ChatClient extends JFrame implements Runnable{
    private Color c1 = new Color(255, 240, 227);
    private Color c3 = new Color(255, 255, 255);
    private Font f4 = new Font("SUITE", Font.PLAIN, 17);
    private Font f5 = new Font("SUITE", Font.PLAIN, 15);


    private ObjectOutputStream objectOutputStream = null;

    private ObjectInputStream objectInputStream = null;

    //Main Frame
    private JPanel chattingListPanel = null;
    private JTextField tf = null;
    private JTextArea chatTextArea = null;
    private JScrollPane scrollPane = null;

    //Participants Frame
    private JButton participantsButton = null;
    private JList<String> participantsList = null;
    private TimecheckWindow timecheckWindow = new TimecheckWindow(this);

    // 서버에서 각 클라이언트 이름을 받아오는 리스트
    private ArrayList<String> nameList = new ArrayList<>();
    private int port;
    // 사용자 uuid
    private String uuid = null;
    private final JButton leaveButton = null;

    // 처음 클라이언트가 생성되면 자동으로 로그인 메소드부터 실행 되도록 구현
    public ChatClient(int port, String uuid) {
        try {
            //포트 정보
            this.port = port;
            this.uuid = uuid;

            Socket socket = new Socket("localhost", port);

            //서버 -> 클라이언트 Output Stream
            OutputStream outputStream = socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);

            //서버 <- 클라이언트 Input Stream
            InputStream inputStream = socket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);

            createAndShowGUI();

            objectOutputStream.writeObject(new JoinMessageChatRoomRequest(uuid));
            List<ChattingMessageResponse> chattingMessageResponses = (List<ChattingMessageResponse>) objectInputStream.readObject();
            for (ChattingMessageResponse chattingMessageResponse : chattingMessageResponses) {
                getMessage(chattingMessageResponse);
            }

            Thread thread = new Thread(this);
            thread.start();

            setVisible(true);
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    dispose();
                }
            });
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void createAndShowGUI() {
        setTitle("채팅 목록");
        setSize(400, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        chattingListPanel = new JPanel(new BorderLayout());

        chatTextArea = new JTextArea();
        chatTextArea.setEditable(false); // 편집 불가능하도록 설정
        chatTextArea.setForeground(Color.BLACK);
        chatTextArea.setLineWrap(true);
        chatTextArea.setBackground(c3);
        chatTextArea.setFont(f4);
        scrollPane = new JScrollPane(chatTextArea);

        tf = new JTextField("");
        tf.setPreferredSize(new Dimension(300, 50));
        tf.setBackground(c1);
        tf.setBorder(BorderFactory.createLineBorder(new Color(200,200,200)));
        tf.setFont(f4);

        //Enter를 입력할 경우에 sendMessage 실행
        tf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // 참가자 문구 생성
        participantsButton = new JButton("참가자 : 0");
        participantsButton.setHorizontalAlignment(JButton.RIGHT);
        participantsButton.setBackground(c1);
        participantsButton.setPreferredSize(new Dimension(300, 35));
        participantsButton.setFont(f5);

        // 채팅방 나가기 버튼 생성
        JButton leaveButton = new RoundedButton("나가기");
        leaveButton.setBounds(0, 10, 50, 50);
        participantsButton.add(leaveButton);
        leaveButton.setBackground(c1);
        leaveButton.setFont(f5);




        leaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try (Socket clientSocket = new Socket("localhost", 1026);
                     OutputStream outputStream = clientSocket.getOutputStream();
                     ObjectOutputStream objectOutputStream2 = new ObjectOutputStream(outputStream);
                     InputStream inputStream = clientSocket.getInputStream();
                     ObjectInputStream objectInputStream2 = new ObjectInputStream(inputStream);
                ){
                    int choice = JOptionPane.showConfirmDialog(ChatClient.this,
                            "정말 나가시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);

                    if (choice == JOptionPane.YES_OPTION) {
                        LeaveChatRoomRequest leaveChatRoomRequest = new LeaveChatRoomRequest(port, uuid);
                        objectOutputStream.writeObject(leaveChatRoomRequest); // 채팅 서버와 연결 끊기
                        objectOutputStream2.writeObject(leaveChatRoomRequest);
                        showExitDialog();
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        chattingListPanel.add(participantsButton, BorderLayout.NORTH);
        chattingListPanel.add(scrollPane, BorderLayout.CENTER);
        chattingListPanel.add(tf, BorderLayout.SOUTH);
        add(chattingListPanel);

        setVisible(true);
    }
    private void showExitDialog() {dispose();}

    //Participants Frame
    private void showParticipants() {
        JFrame participantsFrame = new JFrame();
        participantsFrame.setLayout(new BorderLayout());
        participantsFrame.setSize(150, 300);
        participantsFrame.setLocationRelativeTo(null);
        participantsFrame.setResizable(false);

        DefaultListModel<String> participantListModel = new DefaultListModel<>();

        for (String n : nameList) {
            participantListModel.addElement(n);
        }

        participantsList = new JList<>(participantListModel);

        participantsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                try {
                    if (!e.getValueIsAdjusting()) {
                        String selected_name = participantsList.getSelectedValue();
                        if (selected_name != null) {
                            //kickRequest(selected_name);
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        JScrollPane participantsscrollPane = new JScrollPane(participantsList);

        participantsFrame.add(participantsscrollPane, BorderLayout.CENTER);

        participantsFrame.setVisible(true);
    }
    /*서버 Response를 받는 메소드*/
    @Override
    public void run() {
        try {
            while(true) {
                if (objectInputStream != null) {
                    Object readObj = objectInputStream.readObject();

                    if (readObj instanceof MessageChatRoomResponse messageChatRoomResponse) {
                        getMessage(messageChatRoomResponse);
                    } else if (readObj instanceof JoinMessageChatRoomResponse joinMessageChatRoomResponse) {
                        getParticipants();

                        getMessage(joinMessageChatRoomResponse);
                    } else if (readObj instanceof GetParticipantsChatRoomResponse getParticipantsChatRoomResponse) {
                        getParticipantsChatRoomResponse.list(); //이런식으로 list 안에 있는 원소들을 for문을 통해서 출력 해주도록 해야함
                    }

                    int pos = chatTextArea.getText().length();
                    chatTextArea.setCaretPosition(pos);
                } else break;
            }
        } catch(Exception exception) {
            chatTextArea.append("서버 통신 오류...");
        }
    }

    /*메세지 전송 Request를 보내는 메소드*/
    private void sendMessage() {
        try {
            String message = tf.getText();

            if (!message.isBlank()) {
                MessageChatRoomRequest messageChatRoomRequest = new MessageChatRoomRequest(message, uuid);

                objectOutputStream.writeObject(messageChatRoomRequest);

                tf.setText("");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /*메세지 출력 메소드*/
    private void getMessage(MessageChatRoomResponse messageChatRoomResponse) {
        chatTextArea.append(messageChatRoomResponse.nickName() + " : " + messageChatRoomResponse.message());
    }

    /*새로운 참여자 관련 메소드*/
    private void getMessage(JoinMessageChatRoomResponse joinMessageChatRoomResponse) {
        chatTextArea.append(joinMessageChatRoomResponse.nickName() + joinMessageChatRoomResponse.message());
    }

    private void getMessage(ChattingMessageResponse chattingMessageResponse) {
        chatTextArea.append(chattingMessageResponse.nickName() + " : " + chattingMessageResponse.message() + "\n");
    }

    /*강퇴 Request를 보내는 메소드*/
    private void kickRequest(String selected_name) {
        try {
            if (objectOutputStream != null) {
                KickChatRoomRequest kickChatRoomRequest = new KickChatRoomRequest(selected_name, uuid);

                objectOutputStream.writeObject(kickChatRoomRequest);

                ResponseCode responseCode = (ResponseCode) objectInputStream.readObject();

                if (responseCode.getKey() == ResponseCode.KICK_CHATROOM_SUCCESS.getKey()) {
                    // 성공적으로 강제퇴장을 했을 경우
                } else if (responseCode.getKey() == ResponseCode.KICK_CHATROOM_FAILURE.getKey()) {
                    // 방장 권한이 없어서 강제퇴장을 못 했을 경우
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /*채팅방 참여자 명단 Request를 보내는 메소드*/
    private void getParticipants() {
        try {
            objectOutputStream.writeObject(new GetParticipantsChatRoomRequest(port));

            Object object = objectInputStream.readObject();

            if (object instanceof  ResponseCode) {
                ResponseCode responseCode = (ResponseCode) object;
                if (responseCode.getKey() == ResponseCode.GET_PARTICIPANTS_SUCCESS.getKey()) {
                    GetParticipantsChatRoomResponse getParticipantsChatRoomResponse = (GetParticipantsChatRoomResponse) objectInputStream.readObject();
                    nameList = getParticipantsChatRoomResponse.list();
                    participantsButton.setText("참가자 : " + nameList.size());
                } else {
                    showErrorDialog(responseCode.getValue());
                }
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "안내", JOptionPane.ERROR_MESSAGE);
    }
}