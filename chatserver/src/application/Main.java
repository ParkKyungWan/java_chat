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
	
	//Ŭ���̾�Ʈ�� ������ ��ٸ��� �޼ҵ�
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
		
		//Ŭ���̾�Ʈ�� �����Ҷ����� ��ٸ��� �������Դϴ�.
		Runnable thread = new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						System.out.println("[ �ݰ�����! ]"
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
	
	//���� �����ϴ� �޼ҵ�
	public void stopserver() {
		try {
			//�۵����� ���� ���� ����
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			//���� ���� ��ü �ݱ�
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			//������Ǯ �����ϱ�
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
		textArea.setFont(new Font("��������",15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button ("�����ϱ�");
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton,  new Insets(1,0,0,0));
		root.setBottom(toggleButton);
		
		String ip = "211.176.163.96";
		int port = 7197;
		
		toggleButton.setOnAction(event->{
			if(toggleButton.getText().equals("�����ϱ�")) {
				startserver(ip, port);
				Platform.runLater(()->{
					String message = String.format("[������ �����մϴ�]\n", ip,port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
					
				});
			}else {
				stopserver();
				Platform.runLater(()->{
					String message = String.format("[������ �����մϴ�]\n", ip,port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
					
				});
				
			}
		});
		
		Scene scene = new Scene ( root, 400, 400);
		primaryStage.setTitle("������ ��");
		primaryStage.setOnCloseRequest(event->stopserver());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	//���α׷��� ������
	public static void main(String[] args) {
		launch(args);
	}
}