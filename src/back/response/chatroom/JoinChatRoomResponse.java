package back.response.chatroom;

import java.io.Serializable;

public record JoinChatRoomResponse(String uuid, int chatPort) implements Serializable {}
