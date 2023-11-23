package back.handler;

import back.ResponseCode;
import back.dao.GetInfoDAO;
import back.request.chatroom.Join_ChatRoom_Request;
import back.response.chatroom.Join_ChatRoom_Response;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ChatRoom_Handler extends Thread {
    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;

    public ChatRoom_Handler(Socket clientSocket) {
        try {

            InputStream inputStream = clientSocket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);

            OutputStream outputStream = clientSocket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Object readObj = objectInputStream.readObject();

            if (readObj instanceof  Join_ChatRoom_Request joinChatRoomRequest) {
                joinChatRoomMethod(joinChatRoomRequest);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void joinChatRoomMethod(Join_ChatRoom_Request joinChatRoomRequest) {
        try {
            //사용자에게 접속을 원하는 게시글 id, uuid 정보를 받아와서 처리할 거 처리하고 포트 정보 및 대화 내용 등 return 해주는 DAO 필요
            GetInfoDAO getInfoDAO = new GetInfoDAO();

            String nickName = getInfoDAO.getnickNameMethod(joinChatRoomRequest.uuid());
            int chatPort = getInfoDAO.getchatPortMethod(joinChatRoomRequest.selectRow());

            if (chatPort != -1) {
                objectOutputStream.writeObject(ResponseCode.JOIN_CHATROOM_SUCCESS);
                objectOutputStream.writeObject(new Join_ChatRoom_Response(nickName, chatPort));
            } else {
                objectOutputStream.writeObject(ResponseCode.JOIN_CHATROOM_FAILURE);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
