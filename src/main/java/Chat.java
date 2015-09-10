import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/chat/{roomId}/{nickname}")
public class Chat {
	/**
	 * 连接对象集合
	 */
	private static final Map<Integer, CopyOnWriteArraySet<Chat>> connections = new HashMap<Integer, CopyOnWriteArraySet<Chat>>();
//	private static int count = 1000;

	private String nickName;
	private int roomId;

	/**
	 * WebSocket Session
	 */
	private Session session;

	public Chat() {
	}

	/**
	 * 打开连接
	 * 
	 * @param session
	 * @param nickName
	 */
	@OnOpen
	public void onOpen(Session session, /*@PathParam(value = "nickName") String nickName,*/
			@PathParam(value = "roomId") int roomId,@PathParam(value = "nickname") String nickname) {
		
		this.session = session;
		this.nickName = nickname ;
		this.roomId = roomId;
		CopyOnWriteArraySet<Chat> chats = connections.get(roomId);
		if (chats == null) {
			chats = new CopyOnWriteArraySet<Chat>();
			connections.put(roomId, chats);
		}
		chats.add(this);
		String message = String.format("System=====> %s %s", this.nickName, " has joined.");
		Chat.broadCast(message,this.roomId);
	}

	/**
	 * 关闭连接
	 */
	@OnClose
	public void onClose() {

		connections.get(this.roomId).remove(this);
		String message = String.format("System=====> %s, %s", this.nickName, " has disconnection.");
		Chat.broadCast(message,this.roomId);
	}

	/**
	 * 接收信息
	 * 
	 * @param message
	 * @param nickName
	 */
	@OnMessage
	public void onMessage(String message, @PathParam(value = "nickName") String nickName) {
		Chat.broadCast(this.nickName + ">" + message,this.roomId);
	}

	/**
	 * 错误信息响应
	 * 
	 * @param throwable
	 */
	@OnError
	public void onError(Throwable throwable) {
		System.out.println(throwable.getMessage());
	}

	/**
     * 发送或广播信息
     * 
     * @param message
     */
    private static void broadCast(String message,int roomId) {
        for (Chat chat : connections.get(roomId)) {
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
                Chat.broadCast(String.format("System======> %s %s", chat.nickName,
                        " has bean disconnection."),roomId);
            }
        }
    }
}
