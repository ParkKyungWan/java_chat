package application;
	
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;


public class Main extends Application {
	
	public static ExecutorService threadPool;
	public static Vector<Client> clients = new Vector<Client>();
	
	ServerSocket serverSocket;
	
	//클라이언트와 연결을 기다리는 메소드
	public void startserver(String ip,int port) {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(ip,port));
		} catch (IOException e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {
				stopserver();
			}
			return;
		}
		
		//클라이언트가 접속할때까지 기다리는 쓰레드입니다.
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						System.out.println("[ 반가워요! ]"
								+socket.getRemoteSocketAddress()
								+":" + Thread.currentThread().getName());
					}catch(Exception e) {
						if(!serverSocket.isClosed()) {
							stopserver();
						}
						break;
					}
				}
				
			}
			
		};
		threadPool =  Executors.newCachedThreadPool();
		threadPool.submit(thread);
	}
	
	//서버 중지하는 메소드
	public void stopserver() {
		try {
			//작동중인 소켓 전부 닫음
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			//서버 소켓 객체 닫기
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			//쓰레드풀 종료하기
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("나눔고딕",15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button ("시작하기");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton,  new Insets(1,0,0,0));
		root.setBottom(toggleButton);
		
		String ip = "211.176.163.96";
		int port = 7197;
		
		toggleButton.setOnAction(event->{
			if(toggleButton.getText().equals("시작하기")) {
				startserver(ip, port);
				Platform.runLater(()->{
					String message = String.format("[서버를 시작합니다]\n", ip,port);
					textArea.appendText(message);
					toggleButton.setText("종료하기");
					
				});
			}else {
				stopserver();
				Platform.runLater(()->{
					String message = String.format("[서버를 종료합니다]\n", ip,port);
					textArea.appendText(message);
					toggleButton.setText("시작하기");
					
				});
				
			}
		});
		
		Scene scene = new Scene ( root, 400, 400);
		primaryStage.setTitle("복숭아 톡");
		primaryStage.setOnCloseRequest(event->stopserver());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	//프로그램의 진입점
	public static void main(String[] args) {
		launch(args);
	}
}
