package application;
	

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;



public class Main extends Application {
	
	public static ExecutorService threadpool;  // ExecutorService =  다양한 클라이언트 접속시 여러개의 스레드를 효율적으로 관리
	public static Vector<client> clients = new Vector<client>(); // 접속한 클라이언트 관리하게 하는 라이브러리 
	ServerSocket serverSocket;
	
	public void startserver(String ip,int port) { //서버를 구동시켜 클라이언트의 연결을 기다리는 메소드
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(ip,port)); // 포트랑 소켓 연결 
		}catch (Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed()) {
				stopserver();
			}return;
		}
		Runnable thread = new Runnable() { // 클라이언트가 접속할떄 까지 기다리는 스레드 
			
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new client(socket)); // 클라이언트 배열에 새로 접속한 클라이언트 추가하기 
						System.out.println("[클라이언트 접속성공]"
								+ socket.getRemoteSocketAddress() // 접속 클라이언트 주소 
							+": "	+ Thread.currentThread().getName()); // 해당 스레드 정보 
					}catch (Exception e) {
						if(!serverSocket.isClosed()) {
							stopserver();
						}break;
					}
				}
				
			}
		};
		threadpool = Executors.newCachedThreadPool(); // 스레드 풀 초기화 
		threadpool.submit(thread); // 스레드 풀에 클라이언트 접속을 기다리는 스레드 담기 
	}
	public void stopserver() { // 서버 작동을 중지시키는 함수
		try {
			Iterator<client> iterator = clients.iterator(); // 현재 작동하는 모든 소켓 닫기  
			while(iterator.hasNext()) { // 하나씩 접근함 
				client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			if(serverSocket != null && !serverSocket.isClosed()) { // 서버 소켓 닫기 
				serverSocket.close();
			}
			if(threadpool != null && !threadpool.isShutdown()) { // 스레드 풀 종료  자원 할당 해제 
				threadpool.shutdown();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void start(Stage primaryStage) { // ui 생성하고 프로그램 실제 동작 함수 
		BorderPane root = new BorderPane(); // 전체디자인 담을수잇는 틀 = 팬(pane) 생성 , 레이아웃 생성 
		root.setPadding(new javafx.geometry.Insets(5));
		
		javafx.scene.control.TextArea textarea = new javafx.scene.control.TextArea(); // 긴 텍스트 담길수있는 공간 = textarea 
		textarea.setEditable(false);  // 문장을 채울수만 잇고 수정 불가능 
		textarea.setFont(new Font("나눔고딕",15));
		root.setCenter(textarea);
		
		Button toggleButton = new Button("시작하기"); // toggle button = 스위치 
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1,0,0,0));
		root.setBottom(toggleButton);
		
		String ip = "127.0.0.1";
		int port = 9876;
		toggleButton.setOnAction(event -> { // 버튼 누를시 동작 실행 
			if(toggleButton.getText().equals("시작하기")) {
				startserver(ip, port);
				Platform.runLater(() -> { // gui 우리 화면을 출력  
					String message = String.format("[서버시작]\n",ip,port);
					textarea.appendText(message);
					toggleButton.setText("종료하기");
				});
			}else {
				stopserver();
				Platform.runLater(() -> {
					String message = String.format("[서버종료]\n",ip,port);
					textarea.appendText(message);
					toggleButton.setText("시작하기");
				});
			}
		});
		Scene scene = new Scene(root, 400,400);
		primaryStage.setTitle("채팅서버");
		primaryStage.setOnCloseRequest(event -> stopserver()); // 종료시에 stopserver 실행 
		primaryStage.setScene(scene);
		primaryStage.show();
				}
	
	public static void main(String[] args) {
		launch(args);
	}
}
