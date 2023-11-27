package front.mainPage;

import back.ResponseCode;
import back.request.board.PostBoardRequest;
import back.request.chatroom.JoinChatRoomRequest;
import back.response.board.PostBoardResponse;
import back.response.chatroom.JoinChatRoomResponse;
import front.ChatClient;
import front.FrontSetting;
import front.RoundedButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class NewPost extends JFrame{
    private FrontSetting frontSetting = new FrontSetting();

    private String uuid = null;
    private JFrame mainFrame;


    public NewPost(String uuid, JFrame mainframe) {
        this.uuid = uuid;
        this.mainFrame = mainframe;
        setPosting(uuid);
    }
    private void setPosting(String uuid) {
        JFrame newPostFrame = new JFrame("새 글");   // 새 글 프레임
        newPostFrame.setSize(500, 600);
        frontSetting.FrameSetting(newPostFrame);

        Container c = newPostFrame.getContentPane();  // 새 글 팝업창 패널
        c.setBackground(frontSetting.mainColor);
        c.setLayout(null);

        JLabel newPostLabel = new JLabel("새 글");  // 라벨
        newPostLabel.setFont(frontSetting.fb20);
        newPostLabel.setBounds(220, 20, 100, 40);

        JLabel titleLabel = new JLabel("  제목:");  // 제목 라벨
        titleLabel.setBounds(20, 80, 80, 35);
        titleLabel.setFont(frontSetting.f18);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(Color.WHITE);

        JTextField titleField = new JTextField();  // 제목 입력 필드
        titleField.setBounds(80, 80, 385, 35);
        titleField.setFont(frontSetting.f18);
        titleField.setBorder(null);

        JPanel whitePanel = new JPanel(null);  // 지역, 카테고리의 흰색 배경
        whitePanel.setBounds(20, 125, 445, 35);
        whitePanel.setBackground(Color.WHITE);

        JLabel regionLabel = new JLabel(" 지역: ");  // 지역 라벨
        regionLabel.setBounds(5, 3, 70, 30);
        regionLabel.setFont(frontSetting.f18);

        JComboBox regionField = new JComboBox(frontSetting.regionArr);  // 지역 콤보 박스
        regionField.setBounds(60, 3, 100, 30);
        regionField.setFont(frontSetting.f16);

        JLabel categoryLabel = new JLabel("카테고리: ");  // 카테고리 라벨
        categoryLabel.setBounds(170, 3, 100, 30);
        categoryLabel.setFont(frontSetting.f18);

        JComboBox categoryField = new JComboBox(frontSetting.categoryArr);  // 카테고리 콤보 박스
        categoryField.setBounds(255, 3, 100, 30);
        categoryField.setFont(frontSetting.f16);

        JLabel peopleNumLabel = new JLabel("인원: ");  // 인원 수 라벨
        peopleNumLabel.setBounds(365, 3, 50, 30);
        peopleNumLabel.setFont(frontSetting.f18);

        JTextField peopleNumField = new JTextField();
        peopleNumField.setBounds(410, 3, 25, 30);
        peopleNumField.setFont(frontSetting.f18);

        JTextArea contentArea = new JTextArea(445, 250);  // 내용 작성 필드
        contentArea.setFont(frontSetting.f16);

        JScrollPane contentScroll = new JScrollPane(contentArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentScroll.setBounds(20, 185, 445, 250);
        contentScroll.setBorder(null);

        RoundedButton postBtn = new RoundedButton("올리기");  // 올리기 버튼
        postBtn.setBounds(200, 480, 90, 50);
        postBtn.setFont(frontSetting.fb16);

        postBtn.addActionListener(new ActionListener() {  // 올리기 버튼 클릭 시
            @Override
            public void actionPerformed(ActionEvent e) {
                try (Socket clientSocket = new Socket("localhost", 1025);
                     OutputStream outputStream = clientSocket.getOutputStream();
                     ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
                     InputStream inputStream = clientSocket.getInputStream();
                     ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                ){
                    String title = titleField.getText();
                    String region = (String) regionField.getSelectedItem();
                    String category = (String) categoryField.getSelectedItem();
                    String maxPeopleNum = peopleNumField.getText();
                    String content = contentArea.getText();

                    PostBoardRequest PostBoardRequest = new PostBoardRequest(title, region, category, maxPeopleNum, content, uuid);

                    objectOutputStream.writeObject(PostBoardRequest);

                    ResponseCode responseCode = (ResponseCode) objectInputStream.readObject();

                    if (responseCode.getKey() == ResponseCode.POST_BOARD_SUCCESS.getKey()) { // 게시글 생성 성공
                        mainFrame.dispose();
                        new MainPage(uuid);
                        setSuccessPopUpFrame(" 글 작성 성공");
                        PostBoardResponse postBoardResponse = (PostBoardResponse) objectInputStream.readObject();
                        int port = postBoardResponse.port();
                        objectOutputStream.writeObject(new JoinChatRoomRequest(port, uuid));

                        responseCode = (ResponseCode) objectInputStream.readObject();

                        if (responseCode.getKey() == ResponseCode.JOIN_CHATROOM_SUCCESS.getKey()) { //채팅방 입장 성공
                            JoinChatRoomResponse joinChatRoomResponse = (JoinChatRoomResponse) objectInputStream.readObject();

                            //  여기서 닉네임 필요 없을거 같은데      보낸 문자 저장할 때만 닉네임 식으로 저장하는게 더 나을듯
                            new ChatClient(uuid, joinChatRoomResponse.chatPort());
                        } else if (responseCode.getKey() == ResponseCode.JOIN_CHATROOM_FAILURE.getKey()) { // 채팅방 입장 실패
                            showErrorDialog(responseCode.getValue());
                        }
                        newPostFrame.dispose();
                    } else { //게시글 생성 실패
                        showErrorDialog(responseCode.getValue());
                    }

                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        c.add(newPostLabel);
        c.add(titleLabel);
        c.add(titleField);
        c.add(whitePanel);
        whitePanel.add(regionLabel);
        whitePanel.add(regionField);
        whitePanel.add(categoryLabel);
        whitePanel.add(categoryField);
        whitePanel.add(peopleNumLabel);
        whitePanel.add(peopleNumField);
        c.add(contentScroll);
        c.add(postBtn);

        newPostFrame.setVisible(true);
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "안내", JOptionPane.ERROR_MESSAGE);
    }

    private void setSuccessPopUpFrame(String message) {  // 게시글 작성 성공 팝업
        JFrame notifyFrame = new JFrame();
        notifyFrame.setSize(300, 200);
        frontSetting.FrameSetting(notifyFrame);

        Container c = notifyFrame.getContentPane();
        c.setLayout(null);
        c.setBackground(frontSetting.mainColor);


        JLabel successLabel = new JLabel(message);
        successLabel.setFont(frontSetting.fb16);
        successLabel.setBounds(100, 35, 200, 40);

        RoundedButton okBtn = new RoundedButton("확인");
        okBtn.setFont(frontSetting.fb16);
        okBtn.setBounds(105, 95, 80, 30);

        okBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                notifyFrame.dispose();
                dispose();
            }
        });

        c.add(successLabel);
        c.add(okBtn);

        notifyFrame.setVisible(true);
    }
}
