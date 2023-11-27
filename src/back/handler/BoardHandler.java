package back.handler;

import back.ResponseCode;
import back.dao.board.DeleteBoardDAO;
import back.dao.board.ModifyMyPostDAO;
import back.dao.chatting.GetChatRoomPortDAO;
import back.dao.board.PostingDAO;
import back.dao.GetInfoDAO;

import back.request.board.DeleteBoardRequest;
import back.request.board.ModifyMyPostRequest;
import back.request.board.PostBoardRequest;
import back.response.board.PostBoardResponse;

import java.io.*;
import java.net.Socket;

public class BoardHandler extends Thread {
    private ObjectInputStream objectInputStream = null;
    private ObjectOutputStream objectOutputStream = null;

    public BoardHandler(Socket clientSocket) {
        try {
            InputStream inputStream = clientSocket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);

            OutputStream outputStream = clientSocket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);
        } catch (Exception exception) {
            exception.printStackTrace();

            try {
                objectInputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /*사용자 Request를 받는 메소드*/
    @Override
    public void run() {
        try {
            Object readObj = objectInputStream.readObject();

            if (readObj instanceof PostBoardRequest postBoardRequest) {
                postBoardMethod(postBoardRequest);
            } else if (readObj instanceof ModifyMyPostRequest modifyMyPostRequest) {
                modifyMyPostMethod(modifyMyPostRequest);
            } else if (readObj instanceof DeleteBoardRequest deleteBoardRequest) {
                deleteBoardMethod(deleteBoardRequest);
            }
        } catch (Exception exception) {
            exception.printStackTrace();

            try {
                objectInputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /*게시글 생성 Reponse를 보내는 메소드*/
    private void postBoardMethod(PostBoardRequest postBoardRequest) {
        try {
            PostingDAO postingDAO = new PostingDAO();
            GetInfoDAO getInfoDAO = new GetInfoDAO();
            GetChatRoomPortDAO getChatRoomPortDAO = new GetChatRoomPortDAO();

            if (postBoardRequest.title().isBlank()) {
                objectOutputStream.writeObject(ResponseCode.TITLE_MISSING);
            } else if (postBoardRequest.region().equals(" --")) {
                objectOutputStream.writeObject(ResponseCode.REGION_NOT_SELECTED);
            } else if (postBoardRequest.category().equals(" --")) {
                objectOutputStream.writeObject(ResponseCode.CATEGORY_NOT_SELECTED);
            } else if (postBoardRequest.maxPeopleNum().isBlank()) {
                objectOutputStream.writeObject(ResponseCode.PEOPLE_NUM_MISSING);
            } else if (postBoardRequest.content().isBlank()) {
                objectOutputStream.writeObject(ResponseCode.CONTENT_MISSING);
            } else {
                if (Integer.parseInt(postBoardRequest.maxPeopleNum()) > 30) {
                    objectOutputStream.writeObject(ResponseCode.PEOPLE_NUM_OVER_LIMIT);
                } else if (Integer.parseInt(postBoardRequest.maxPeopleNum()) <= 1) {
                    objectOutputStream.writeObject(ResponseCode.PEOPLE_NUM_UNDER_LIMIT);
                } else {
                    int port = getChatRoomPortDAO.assignChatRoomPort(); // 랜덤한 채팅방 포트를 할당한다.

                    if (postingDAO.posting(postBoardRequest, port)) {   //  DB로 게시글 생성 요청
                        objectOutputStream.writeObject(ResponseCode.POST_BOARD_SUCCESS); // 게시글 생성 성공 응답을 보낸다.
                        objectOutputStream.writeObject(new PostBoardResponse(port));
                    } else {
                        objectOutputStream.writeObject(ResponseCode.POST_BOARD_FAILURE);
                    }
                }
            }
        } catch (NumberFormatException numberFormatException) {
            try {
                objectOutputStream.writeObject(ResponseCode.PEOPLE_NUM_NOT_NUMBER);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            try {
                objectInputStream.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /*게시글을 수정하는 메소드*/
    private void modifyMyPostMethod(ModifyMyPostRequest modifyMyPostRequest) {
        try {
            ModifyMyPostDAO modifyMyPostDAO = new ModifyMyPostDAO();
            boolean isModified = modifyMyPostDAO.modifyMyPost(modifyMyPostRequest); //  DB로 수정 요청
            if (isModified) {
                objectOutputStream.writeObject(ResponseCode.MODIFY_MY_BOARD_SUCCESS); // 게시글 수정 성공 응답을 보낸다.
            } else {
                objectOutputStream.writeObject(ResponseCode.MODIFY_MY_BOARD_FAILURE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*게시글을 삭제하는 메소드*/
    private void deleteBoardMethod(DeleteBoardRequest deleteBoardRequest) {
        try {
            DeleteBoardDAO deleteBoardDAO = new DeleteBoardDAO();
            boolean isDeleted = deleteBoardDAO.deleteBoardMethod(deleteBoardRequest.port());
            if (isDeleted) {
                objectOutputStream.writeObject(ResponseCode.DELETE_MY_BOARD_SUCCESS);
            } else {
                objectOutputStream.writeObject(ResponseCode.MODIFY_MY_BOARD_FAILURE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
