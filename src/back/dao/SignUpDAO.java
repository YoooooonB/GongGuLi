package back.dao;

import back.request.account.SignUp_Request;
import database.DBConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SignUpDAO {
    Connection conn = null;
    PreparedStatement pt = null;
    ResultSet rs = null;

    public void signUp(SignUp_Request signUpInfo, String uuid) {
        conn = DBConnector.getConnection();
        String signInSQL = "INSERT INTO user (nickName, name, userId, password, region, phoneNum, birth, uuid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            pt = conn.prepareStatement(signInSQL);

            pt.setString(1, signUpInfo.nickName());
            pt.setString(2, signUpInfo.name());
            pt.setString(3, signUpInfo.userId());
            pt.setString(4, signUpInfo.password());
            pt.setString(5, signUpInfo.region());
            pt.setString(6, signUpInfo.phoneNumber());
            pt.setString(7, signUpInfo.birth());
            pt.setString(8, uuid);

            if (!pt.execute()) {
                System.out.println("회원가입 완료.");
            }

            pt.close();
            conn.close();
        } catch (Exception e) {
            System.out.println("회원가입 실패.");
            e.printStackTrace();
        }
    }
}
