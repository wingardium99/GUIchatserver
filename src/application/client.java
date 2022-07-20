package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.MessageFormat;




public class client { // 한명의 클라이언트와 통신하게 해주는 client class 
	Socket socket;
	
	public client(Socket socket) { // 생성자 만듬 변수 초기화 하기 위해 
		this.socket = socket;
		receive();
	}
	
	public void receive() { // 반복적으로 클라이언트로 부터 메세지 받는 함수 
		Runnable thread = new Runnable() { // 스레드 사용시 runnable 사용함 
			
			@Override
			public void run() { // 하나의 스레드가 어떠한 모듈로 동작하는 함수 
				try {
					while(true) {
						InputStream in = socket.getInputStream(); // 내용을 전달받음
						byte[] buffer = new byte[512];
						int length = in.read(buffer);
						if(length == -1) throw new IOException(); // 오류시 오류 발생 
						System.out.println("[메세지 수신성공]"
											+ socket.getRemoteSocketAddress()
										+ ": "	+ Thread.currentThread().getName());
						String message = new String(buffer,0,length,"UTF-8"); // 버퍼를 문자열 string 에 담음 
						for(client client : Main.clients) { // 전달받은 메세지 다른 클라이언트에게 전송 
							client.send(message);
						}
					}
				}catch (Exception e) { // 예외 발생 
					try {
						System.out.println("[메세지 수신오류]"
								+ socket.getRemoteSocketAddress()
								+": " + Thread.currentThread().getName());
					}catch (Exception e2) {
						e2.printStackTrace();
					}
				}
				
			}
		};
		Main.threadpool.submit(thread); // 스레드 안정적 관리를 위해 만들어진 스레드를 스레드 풀에 전달 
	}
	
	public void send(String message) { // 클라이언트에게   메세지 전송하는 함수 
		Runnable thread= new Runnable() {
			
			@Override
			public void run() {
				try {
					OutputStream out = socket.getOutputStream(); //내용을 보냄 
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer); // 버퍼에 담긴 내용을 서버에서 클라이언트로 전송
					out.flush();   // 여기까지 전송한것을 알수잇음 
				} catch (Exception e) {
					try {
						System.out.println("[메세지 송신오류]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						Main.clients.remove(client.this); // 클라이언트 담는 배열에서 오류클라이언트 제거 
						socket.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
				
			}
		};
		Main.threadpool.submit(thread);
	}
}
