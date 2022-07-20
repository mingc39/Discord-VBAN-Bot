package net.ykrn.baek.discordVban.staSender;

import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

// VBANReceptor.java
// VBAN 수신하여 버퍼에 저장
public class VBANReceptor extends Thread {
	
	// VBAN 패캣 설정 목록
	private static final int VBAN_SRList[] = {6000, 12000, 24000, 48000, 96000, 192000, 384000, 8000, 16000, 32000, 64000, 128000, 256000, 512000, 11025, 22050, 44100, 88200, 176400, 352800, 705600};
	private static final String VBAN_DATATYPE[] = {"BYTE8", "INT16", "INT24", "INT32", "FLOAT32", "FLOAT64", "12BITS", "10BITS"};
	private static final String VBAN_CODEC[] = {"PCM", "VBCA", "VBCV"};
	
	private byte[] buffer; // 수신 내용을 저장할 버퍼
	private PrintStream ps; // 정보 출력시 사용할 프린트스트림
	private int position = 0; // 버퍼에 쓸 위치
	private DatagramSocket ds;
	private DatagramPacket dp;
	private boolean connection = false; // 연결 상태
	
	// 시스템 콘솔 출력 스트림을 사용
	public VBANReceptor(int port, byte[] buf) {
		this(port, buf, System.out);
	}
	
	public VBANReceptor(int port, byte[] buf, PrintStream ps) {
		
		this.buffer = buf;
		this.ps = ps;
		
		// 초기화
		ps.println("Start VBANReceptor");
		position = 0;
		// 소켓 생성
		ps.println("Create Socket");
		ps.println("\tPort: " + port);
		try {
			ds = new DatagramSocket(port);
		} catch (SocketException e) {
			ps.println("Creating Socket Failed");
			return;
		}
		dp = new DatagramPacket(new byte[2048], 2048);
		connection = true;
		
		// 수신 시작
		this.start();
		
	}
	
	public boolean checkConnection() {
		return connection;
	}
	
	public int getPosition() {
		return position;
	}
	
	public void run() {
		while(true) {
			try {
				// receive packet
				ds.receive(dp);
				byte[] data = dp.getData();
				
				// check packet header
				String header = new String(data, 0, 4);
				if(header.equals("VBAN") == false) {
					ps.println("No VBAN Packet");
					throw new Exception("No VBAN Packet");
				} else if((data[4] & 0xE0) != 0x00) {
					ps.println("No audio packet");
					throw new Exception("No audio packet");
				} else if(VBAN_SRList[data[4]] != 48000) {
					ps.println("SR isn't 48000Hz");
					throw new Exception("SR isn't 48000Hz");
				} else if(data[6] + 1 != 2) {
					ps.println("Not 2 channel audio");
					throw new Exception("Not 2 channel audio");
				} else if((data[4] & 0x08) != 0x00) {
					ps.println("Bit resolution or CODEC isn't corrent");
					throw new Exception("Bit resolution or CODEC isn't corrent");
				} else if(VBAN_DATATYPE[data[7]].equals("INT16") == false) {
					ps.println("Bit resolution isn't INT16");
					throw new Exception("Bit resolution isn't INT16");
				} else if(VBAN_CODEC[data[4]>>>4].equals("PCM") == false) {
					ps.println("CODEC isn't correct");
					throw new Exception("CODEC isn't correct");
				}
				
				// copy data to buffer
				for(int i = 28; i < dp.getLength(); i++) {
					buffer[position] = data[i];
					position++;
					if(position >= buffer.length) position = 0;
				}
				
			} catch (Exception e) {
				ps.println("Reception failed");
			}
		}
		
	}

}
