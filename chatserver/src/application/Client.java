package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
	
	Socket socket;
	
	public Client(Socket socket) {
		this.socket = socket;
		get();
	}
	
	//문자를 받았을 때
	public void get() {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();
						byte[] buffer = new byte[512];
						int length = in.read(buffer);
						while (length == -1) throw new IOException();
						System.out.println("[ "+socket.getRemoteSocketAddress()
						+" 로부터 메세지를 전달받았습니다! ]"
						+ " 사용된 쓰레드:" +Thread.currentThread().getName());
						String message = new String(buffer, 0, length, "UTF-8");
						for(Client client: Main.clients) {
							client.send(message);
						}
						
					}
				}catch(Exception e) {
					e.printStackTrace();
					try {
						System.out.println("[ 메세지를 받지 못했습니다 ]"
								+" 사용된 쓰레드:" + Thread.currentThread().getName());
					}catch(Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		Main.threadPool.submit(thread);
		 
	}
	
	//문자를 보낼 때
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer);
					out.flush();
				} catch (Exception e) {
					e.printStackTrace();
					try {
						System.out.println("[ 메세지를 보내지 못했습니다 ]"
								+socket.getRemoteSocketAddress()
								+" 사용된 쓰레드:" + Thread.currentThread().getName());
						Main.clients.remove(Client.this);
						socket.close();
					}catch(Exception e2) {
						e2.printStackTrace();
					}
				}
				
			}
		};
		Main.threadPool.submit(thread);
	}
}
