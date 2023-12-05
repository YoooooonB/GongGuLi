package back.response.chatroom;

import java.io.Serializable;
import java.time.LocalDateTime;

public record GetChattingRoomResponse(int port, String region, String category, String title, String writerUuid,
                                      String lastUpdatedTime, LocalDateTime madeTime) implements Serializable {
}
