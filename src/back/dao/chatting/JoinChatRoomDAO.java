package back.dao.chatting;

import database.DBConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JoinChatRoomDAO {
    Connection conn = null;
    PreparedStatement pt = null;
    ResultSet rs = null;

    public boolean joinChatRoom(String uuid, int port) {
        boolean isJoined = false;

        try {
            String insertSQL = "INSERT INTO chattingmember(memberUuid, port) VALUES (?, ?)";
            conn = DBConnector.getConnection();
            pt = conn.prepareStatement(insertSQL);
            pt.setString(1, uuid);
            pt.setInt(2, port);

            if (!pt.execute()) {
                isJoined = true;
                System.out.println("채팅방 참여 DAO 성공");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isJoined;
    }
}
