package back.handler;

import back.ResponseCode;
import back.dao.GetInfoDAO;
import back.request.chatroom.GetParticipantsChatRoomRequest;
import back.request.chatroom.JoinMessageChatRoomRequest;
import back.request.chatroom.KickChatRoomRequest;
import back.request.chatroom.MessageChatRoomRequest;
import back.response.chatroom.JoinMessageChatRoomResponse;
import back.response.chatroom.MessageChatRoomResponse;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServerHandler extends Thread {
	private ObjectOutputStream objectOutputStream = null;

	private ObjectInputStream objectInputStream = null;

	private ArrayList<ChatServerHandler> list = null;

	private Boolean master = false; //방장 권한

	public ChatServerHandler(Socket socket, ArrayList<ChatServerHandler> list) {
		try {
			this.list = list;

			//서버 -> 클라이언트 Output Stream
			OutputStream outputStream = socket.getOutputStream();
			objectOutputStream = new ObjectOutputStream(outputStream);

			//서버 <- 클라이언트 Input Stream
			InputStream inputStream = socket.getInputStream();
			objectInputStream = new ObjectInputStream(inputStream);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			JoinMessageChatRoomRequest joinMessageChatRoomRequest = (JoinMessageChatRoomRequest) objectInputStream.readObject();
			GetInfoDAO getInfoDAO = new GetInfoDAO();
			String nickName = getInfoDAO.getnickNameMethod(joinMessageChatRoomRequest.uuid());

			sendAll(new JoinMessageChatRoomResponse(nickName, "이(가) 입장했습니다."));

			if (list.size() == 1) { //처음으로 접속한 사람 즉 글쓴이에게 방장 권한 부여
				master = true;
			}

			while (true) {
				Object readObj = objectInputStream.readObject();

				if (readObj instanceof MessageChatRoomRequest messageChatRoomRequest) { //클라이언트 -> 서버 메세지 요청
					if (master) { // 방장이 메세지를 보냈을 때
						MessageChatRoomResponse messageChatRoomResponse = new MessageChatRoomResponse(
								nickName + "(방장)",
								messageChatRoomRequest.message()
						);

						sendAll(messageChatRoomResponse);
					} else { // 이외 사용자가 메세지를 보냈을 때
						MessageChatRoomResponse messageChatRoomResponse = new MessageChatRoomResponse(
								nickName,
								messageChatRoomRequest.message()
						);

						sendAll(messageChatRoomResponse);
					}
				} else if (readObj instanceof KickChatRoomRequest kickChatRoomRequest) { // 클라이언트 -> 서버 강퇴 요청
					if (master) { // 방장일 경우에만 강퇴 요청을 accept
						for (ChatServerHandler handler : list) {
							if (kickChatRoomRequest.target_nickName().equals(handler.getnickName())) {
								sendAll(new MessageChatRoomResponse("", kickChatRoomRequest.target_nickName() + "이(가) 강제퇴장 당했습니다."));

								handler.objectInputStream.close();
								list.remove(handler);
								break;
							}
						}

						objectOutputStream.writeObject(ResponseCode.KICK_CHATROOM_SUCCESS);
					} else {
						objectOutputStream.writeObject(ResponseCode.KICK_CHATROOM_FAILURE);
					}
				} else if (readObj instanceof GetParticipantsChatRoomRequest getParticipantsChatRoomRequest) {
					// 채팅방 참여자 명단을 가져오는 작업을 수행해야함.

				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/*메세지 전송 Response를 보내는 메소드*/
	private void sendAll(MessageChatRoomResponse messageChatRoomResponse) {
		try {
			for (ChatServerHandler handler : list) {
				handler.objectOutputStream.writeObject(messageChatRoomResponse);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/*새로운 유저 입장 Response를 보내는 메소드*/
	private void sendAll(JoinMessageChatRoomResponse joinMessageChatRoomResponse) {
		try {
			for (ChatServerHandler handler : list) {
				handler.objectOutputStream.writeObject(joinMessageChatRoomResponse);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	private String getnickName() {
		return getnickName();
	}
}