import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;


@ServerEndpoint(value = "/chat/{nickName}")
public class Chat {
	 /**
     * ���Ӷ��󼯺�
     */
    private static final Set<Chat> connections = new CopyOnWriteArraySet<Chat>();

    private String nickName;

    /**
     * WebSocket Session
     */
    private Session session;

    public Chat() {
    }

    /**
     * ������
     * 
     * @param session
     * @param nickName
     */
    @OnOpen
    public void onOpen(Session session,
            @PathParam(value = "nickName") String nickName) {

        this.session = session;
        this.nickName = nickName;

        connections.add(this);
        String message = String.format("System> %s %s", this.nickName,
                " has joined.");
        Chat.broadCast(message);
    }

    /**
     * �ر�����
     */
    @OnClose
    public void onClose() {
        connections.remove(this);
        String message = String.format("System> %s, %s", this.nickName,
                " has disconnection.");
        Chat.broadCast(message);
    }

    /**
     * ������Ϣ
     * 
     * @param message
     * @param nickName
     */
    @OnMessage
    public void onMessage(String message,
            @PathParam(value = "nickName") String nickName) {
        Chat.broadCast(nickName + ">" + message);
    }

    /**
     * ������Ϣ��Ӧ
     * 
     * @param throwable
     */
    @OnError
    public void onError(Throwable throwable) {
        System.out.println(throwable.getMessage());
    }

    /**
     * ���ͻ�㲥��Ϣ
     * 
     * @param message
     */
    private static void broadCast(String message) {
        for (Chat chat : connections) {
            try {
                synchronized (chat) {
                    chat.session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                connections.remove(chat);
                try {
                    chat.session.close();
                } catch (IOException e1) {
                }
                Chat.broadCast(String.format("System> %s %s", chat.nickName,
                        " has bean disconnection."));
            }
        }
    }
}
