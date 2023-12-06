package back.dao.chatting;

import back.dao.GetInfoDAO;
import database.DBConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class GetParticipantsChatRoomDAO {
    Connection conn = null;
    PreparedStatement pt = null;
    ResultSet rs = null;
    public ArrayList<String> getParticipantsChatRoom(int port) {
        ArrayList<String> list = new ArrayList<>();
        try {
            GetInfoDAO getInfoDAO = new GetInfoDAO();
            conn = DBConnector.getConnection();
            String selectSQL = "SELECT memberUuid FROM chattingMember WHERE port = ?";
            pt = conn.prepareStatement(selectSQL);
            pt.setInt(1, port);
            rs = pt.executeQuery();
            while (rs.next()) {
                String nickName = getInfoDAO.getNickNameMethod(rs.getString("memberUuid"));
                list.add(nickName);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return list;
    }
}
