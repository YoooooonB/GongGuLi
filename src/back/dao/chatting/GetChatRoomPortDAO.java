package back.dao.chatting;

import serverStructure.ChatServer;

import java.net.ServerSocket;
import java.util.Random;

public class GetChatRoomPortDAO {
    private static final int MIN_PORT = 1029; //1029 ~ 49151에서 채팅방 서버가 생성됨
    private static final int MAX_PORT = 49151;

    public int assignChatRoomPort() {
        int port;
        while (true) {
            port = getRandomPortInRange(MIN_PORT, MAX_PORT);

            try {
                ServerSocket serversocket = new ServerSocket(port);
                System.out.println(port); // 생성된 채팅방 포트 확인용
                new ChatServer(serversocket).start();
                break; // 유효한 포트를 찾으면 루프 종료
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return port;
    }

    //    지정해둔 포트 범위 안에서 랜덤한 값을 추출하는 함수
    private static int getRandomPortInRange(int min, int max) {
        try {
            Random random = new Random();
            return random.nextInt(max - min + 1) + min;
        } catch (Exception exception) {
            exception.printStackTrace();
            return 0;
        }
    }
}