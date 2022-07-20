package net.ykrn.baek.discordVban.staSender;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

// MainWindow.java
// 메인 윈도우
public class MainWindow extends JFrame {
	
	private static final long serialVersionUID = 3632411744883168263L;
	private JTextField portTF; // VBAN 포트 텍스트 필드
	private JTextField bufSizeTF; // 버퍼 사이즈 텍스트 필드(byte)
	private JTextField delayTF; // 딜레이 사이즈 텍스트 필드(byte)
	private JTextField channelTF; // 채널 텍스트 필드
	private JTextField tokenTF; // 봇 토큰 텍스트 필드
	private JTextArea log; // 로그 텍스트 에리어
	private boolean botStatus = false; // 봇 상태 true = 작동중, false = 작동 중지됨
	private byte[] buffer; // 버퍼
	private PrintStream ps = null; // log 텍스트 에리어에 출력하는 스트림
	private JButton startBtn; // 시작 버튼
	private JLabel vbanPositionLabel; // VBANReceptor 버퍼 위치 표시
	private JLabel discordPositionLabel; // DiscordBot 버퍼 위치 표시
	
	// 
	public MainWindow() {
		
		// 봇 상태 설정
		botStatus = false;
		// 창 생성
		createWindow();
		// 로그 출력 스트림 생성
		createPrintStream();
		
		// 초기 출력 내용
		print("Discord VBAN Bot");
		print();
		print("Discord VBAN Bot v0.4");
		print("VBAN 스트림을 수신하여 디스코드 음성 채널로 전송합니다.");
		print();
		print("버전 정보");
		print("v0.5: ");
		print("  - 딜레이 기준을 msec에서 버퍼 위치로 변경");
		print("  - 버퍼 크기, 딜레이 기본값 변경");
		print("v0.4: ");
		print("  - 여러 서버에 접속된 경우에도 사용 가능");
		print("v0.3: ");
		print("  - 출력 창 한글 출력 문제 해결");
		print("v0.2: ");
		print("  - 초기 안내 문구 수정");
		print("  - 버퍼 위치 표시 추가");
		print("v0.1: ");
		print("  - 최초 버전");
		print();
		print("포트: VBAN 스트림을 수신할 포트 번호");
		print("버퍼: 봇에서 사용할 사운드 버퍼의 크기(byte)");
		print("딜레이: VBAN 수신과 디스코드 음성 전송 사이의 딜레이(byte)");
		print("채널: 음성을 전송할 음성 채널 이름");
		print("토큰: 봇에서 사용할 토큰");
		print();
		print("수신되는 VBAN 패킷은 반드시 48000Hz 16Bits 2ch PCM 이어야 합니다.");
		print();
		print("상단의 봇 정보를 확인 후 하단의 시작! 버튼을 눌러 봇을 시작할 수 있습니다.");
		print();
		print("소리에 잡음이 섞여있을 경우 봇을 재시작 하세요.");
		print("재시작해도 해결되지 않는다면 버퍼 크기와 딜레이를 조정해주세요.");
		print();
		
	}
	
	// 창 생성
	private void createWindow() {
		
		// 프레임 레이아웃 설정
		setLayout(new BorderLayout());
		
		// 패널 생성
		JPanel upperPanel = new JPanel();
		JPanel mainPanel = new JPanel();
		JPanel lowerPanel = new JPanel();
		upperPanel.setLayout(new GridLayout(2, 1));
		mainPanel.setLayout(new BorderLayout(0, 0));
		lowerPanel.setLayout(new GridLayout(1, 5));
		
		// 상단 패널
		JPanel upperPanelSub = new JPanel();
		upperPanel.add(upperPanelSub);
		
		JPanel upperPanelSub1 = new JPanel();
		upperPanelSub1.add(new JLabel("포트"));
		portTF = new JTextField(4);
		upperPanelSub1.add(portTF);
		portTF.setText("6980");
		upperPanelSub.add(upperPanelSub1);
		
		JPanel upperPanelSub2 = new JPanel();
		upperPanelSub2.add(new JLabel("버퍼"));
		bufSizeTF = new JTextField(4);
		upperPanelSub2.add(bufSizeTF);
		bufSizeTF.setText("40000");
		upperPanelSub.add(upperPanelSub2);
		
		JPanel upperPanelSub3 = new JPanel();
		upperPanelSub3.add(new JLabel("딜레이"));
		delayTF = new JTextField(4);
		upperPanelSub3.add(delayTF);
		delayTF.setText("10000");
		upperPanelSub.add(upperPanelSub3);
		
		JPanel upperPanelSub4 = new JPanel();
		upperPanelSub4.add(new JLabel("채널"));
		channelTF = new JTextField(6);
		upperPanelSub4.add(channelTF);
		channelTF.setText("빼액채널");
		upperPanelSub.add(upperPanelSub4);
		
		JPanel upperPanelSub5 = new JPanel();
		upperPanelSub5.add(new JLabel("토큰"));
		tokenTF = new JTextField(33);
		upperPanelSub5.add(tokenTF);
		tokenTF.setText("");
		upperPanel.add(upperPanelSub5);
		
		// 메인 패널
		log = new JTextArea(15, 0);
		JScrollPane scrollPane = new JScrollPane(log);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		mainPanel.add(scrollPane);
		
		// 하단 패널
		lowerPanel.add(new JLabel("VBAN ", SwingConstants.RIGHT));
		vbanPositionLabel = new JLabel("0");
		lowerPanel.add(vbanPositionLabel);
		startBtn = new JButton("시작!");
		startBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startBot();
			}	
		});
		lowerPanel.add(startBtn);
		lowerPanel.add(new JLabel("Discord ", SwingConstants.RIGHT));
		discordPositionLabel = new JLabel("0");
		lowerPanel.add(discordPositionLabel);
		
		// 프레임에 패널들 추가
		add(upperPanel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
		add(lowerPanel, BorderLayout.SOUTH);
		
		// 프레임 설정, 프레임 표시
		pack();
		setTitle("Discord VBAN Bot");
		getContentPane().setBackground(Color.LIGHT_GRAY);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		
	}
	
	private void startBot() {
		
		if(botStatus == true) return;
		
		print();
		print("---------------------------------------------------------------------");
		print();
		print("봇을 시작합니다.");
		botStatus = true;
		startBtn.setEnabled(false);
		
		// 버퍼 생성
		print("버퍼를 생성합니다.");
		print("버퍼 크기: " + bufSizeTF.getText());
		try {
			buffer = new byte[Integer.parseInt(bufSizeTF.getText())];
		} catch(NumberFormatException e) {
			print("버퍼 생성에 실패하였습니다: 버퍼 크기가 잘못됐습니다.");
			botStatus = false;
			startBtn.setEnabled(true);
			return;
		} catch(ArrayIndexOutOfBoundsException e) {
			print("버퍼 생성에 실패하였습니다: 버퍼 크기가 잘못되었습니다.");
			botStatus = false;
			startBtn.setEnabled(true);
			return;
		}
		print("버퍼를 생성하였습니다.");
		print();
		
		// VBAN 수신 시작
		print("VBAN 스트림 수신을 시작합니다.");
		print("VBAN 스트림 수신 포트: " + portTF.getText());
		print("딜레이: " + delayTF.getText());
		VBANReceptor receptor = null;
		try {
			receptor = new VBANReceptor(Integer.parseInt(portTF.getText()), buffer, getPrintStream());
			if(receptor.checkConnection() == true) print("VBAN 스트림 수신이 시작되었습니다.");
			else {
				print("VBAN 스트림 수신이 시작되지 않았습니다.");
				botStatus = false;
				startBtn.setEnabled(true);
				return;
			}
		} catch(NumberFormatException e) {
			print("VBAN 스트림 수신 시작에 실패하였습니다: 포트 또는 딜레이가 잘못되었습니다.");
			botStatus = false;
			startBtn.setEnabled(true);
			return;
		}
		print();
		
		// 디스코드 봇 생성
		print("디스코드 봇을 생성합니다.");
		DiscordBot bot = new DiscordBot(tokenTF.getText(), buffer, channelTF.getText(), getPrintStream());
		if(bot.checkConnection() == true) print("디스코드 봇이 생성되었습니다.");
		else {
			print("디스코드 봇이 생성되지 않았습니다.");
			botStatus = false;
			startBtn.setEnabled(true);
			return;
		}
		try {
			int delay = Integer.parseInt(delayTF.getText());
			print("1초 후 봇이 음성 전송을 시작합니다.");
			bot.startSendingAudio(receptor, delay);
		} catch(NumberFormatException e) {
			print("음성 전송 시작을 실패하였습니다.");
			botStatus = false;
			startBtn.setEnabled(true);
			return;
		}
		print();
		
		new refreshPosition(receptor, bot);
		
	}
	
	// 로그 프린트스트림 생성
	private PrintStream createPrintStream() {
		ps = new PrintStream(new OutputStream() {
			
			byte[] data = new byte[256];
			int position = 0;
			
			@Override
			public void write(int b) throws IOException {
				//log.append(String.valueOf((char) b)); // 구 코드: 한글 깨짐
				data[position++] = (byte) b;
				if(position >= data.length || b == '\n') {
					flush();
				}
			}
			
			@Override
			public void flush() throws IOException {
				log.append(new String(data, 0, position));
				position = 0;
				log.setCaretPosition(log.getDocument().getLength());
			}
		});
		return ps;
	}
	
	public PrintStream getPrintStream() {
		return ps;
	}
	
	// 프린트스트림 없이 로그에 직접 출력(줄 바꿈)
	public void print() {
		print("");
	}
	
	// 프린트스트림 없이 로그에 직접 출력(출력후 줄 바꿈)
	public void print(String s) {
		log.append(s + "\n");
		log.setCaretPosition(log.getDocument().getLength());
	}
	
	// VBAN, DiscordBot 버퍼 위치 표시
	class refreshPosition extends Thread {
		
		VBANReceptor vban;
		DiscordBot bot;
		
		public refreshPosition(VBANReceptor vban, DiscordBot bot) {
			this.vban = vban;
			this.bot = bot;
			this.start();
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					sleep(100); // 0.1초마다 갱신
					if(vban != null) vbanPositionLabel.setText(Integer.toString(vban.getPosition()));
					if(bot != null) discordPositionLabel.setText(Integer.toString(bot.getPosition()));
				} catch (InterruptedException e) {
				}
			}
			
		}
		
	}

}
