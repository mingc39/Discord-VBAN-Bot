package net.ykrn.baek.discordVban.staSender;

import java.io.PrintStream;
import java.nio.ByteBuffer;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

// DiscordBot.java
// 버퍼의 PCM 데이터를 디스코드로 전송
public class DiscordBot {
	
	private JDA bot = null; // 자바 디스코드 봇 객체
	private VBANReceptor receptor; // VBANReceptor 객체
	private int delay; // vban과 봇 사이의 버퍼 딜레이
	private Listener listener = null; // 봇 액션리스너
	private String channel; // 채널 이름
	private byte[] buffer; // 보낼 오디오가 있는 버퍼
	private boolean connection = false; // 연결 상태
	private int position; // 버퍼에서 읽을 위치
	
	public DiscordBot(String token, byte[] buffer, String channel, PrintStream ps) {
		
		// 프린트 스트림 설정
		System.setOut(ps);
		System.setErr(ps);
		
		this.channel = channel;
		this.buffer = buffer;
		
		// 봇 생성
		try {
			listener = new Listener(); // 리스너 생성
			//bot = new JDABuilder(token).addEventListener(listener).build(); // 봇 생성
			bot = JDABuilder.createDefault(token).addEventListeners(listener).build(); // 봇 생성
			connection = true;
		} catch (LoginException e) {
			ps.println("login failed");
		}
		
	}
	
	public boolean checkConnection() {
		return connection;
	}
	
	// 오디오 전송
	public void startSendingAudio(VBANReceptor vban, int delay) {
		
		receptor = vban;
		this.delay = delay;
		
		// 주어진 시간만큼 대기 후 전송 시작
		Thread thread = new Thread() {
			public void run() {
				try {
					sleep(1000); // 대기
					
					// 버퍼 읽기 시작 위치 
					position = vban.getPosition();
					position = position - delay;
					while(position < 0) position += buffer.length;
					System.out.println("buffer pos: " + position);
					
					System.out.println("start sending audio");
					listener.startSendingAudio(buffer, channel, bot); // 음성 전송 시작
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}
	
	public int getPosition() {
		return position;
	}
	
	// 봇 리스너
	class Listener extends ListenerAdapter {
		
		// 오디오 전송 시작
		public void startSendingAudio(byte[] buffer, String channelName, JDA bot) {
			
			// 전송할 음성 채널 설정
			VoiceChannel channel = bot.getVoiceChannelsByName(channelName, true).get(0);
			AudioManager manager = channel.getGuild().getAudioManager();
			
			// 오디오 전송 시작
			manager.setSendingHandler(new Handler(buffer));
			manager.openAudioConnection(channel);
			
		}
		
	}
	
	class Handler implements AudioSendHandler {
		
		byte[] buffer;
		byte[] audio;
		byte t;
		
		public Handler(byte[] buffer) {
			this.buffer = buffer;
			this.audio  = new byte[3840];
		}

		@Override
		public boolean canProvide() {
			return true;
		}
		
		@Override
		public boolean isOpus() {
			return false;
		}

		// 전송할 오디오 반환
		@Override
		public ByteBuffer provide20MsAudio() {
			
			// 버퍼에서 데이터 읽어오기
			for(int i = 0; i < audio.length; i++) {
				audio[i] = buffer[position];
				position++;
				if(position >= buffer.length) position = 0;
			}
			
			// 엔디안 변경 (2바이트 리틀 엔디안 -> 2바이트 빅 엔디안)
			for(int i = 0; i < audio.length; i += 2) {
				t = audio[i];
				audio[i] = audio[i + 1];
				audio[i + 1] = t;
			}
			
			return ByteBuffer.wrap(audio);
			
		}
		
	}

}
