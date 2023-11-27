package back.request.chatroom;

import java.io.Serializable;

public record MessageChatRoomRequest(String uuid, String message) implements Serializable {}
