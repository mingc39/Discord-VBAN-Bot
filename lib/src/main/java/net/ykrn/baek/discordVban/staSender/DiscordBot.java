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
// ������ PCM �����͸� ���ڵ�� ����
public class DiscordBot {
	
	private JDA bot = null; // �ڹ� ���ڵ� �� ��ü
	private VBANReceptor receptor; // VBANReceptor ��ü
	private int delay; // vban�� �� ������ ���� ������
	private Listener listener = null; // �� �׼Ǹ�����
	private String channel; // ä�� �̸�
	private byte[] buffer; // ���� ������� �ִ� ����
	private boolean connection = false; // ���� ����
	private int position; // ���ۿ��� ���� ��ġ
	
	public DiscordBot(String token, byte[] buffer, String channel, PrintStream ps) {
		
		// ����Ʈ ��Ʈ�� ����
		System.setOut(ps);
		System.setErr(ps);
		
		this.channel = channel;
		this.buffer = buffer;
		
		// �� ����
		try {
			listener = new Listener(); // ������ ����
			//bot = new JDABuilder(token).addEventListener(listener).build(); // �� ����
			bot = JDABuilder.createDefault(token).addEventListeners(listener).build(); // �� ����
			connection = true;
		} catch (LoginException e) {
			ps.println("login failed");
		}
		
	}
	
	public boolean checkConnection() {
		return connection;
	}
	
	// ����� ����
	public void startSendingAudio(VBANReceptor vban, int delay) {
		
		receptor = vban;
		this.delay = delay;
		
		// �־��� �ð���ŭ ��� �� ���� ����
		Thread thread = new Thread() {
			public void run() {
				try {
					sleep(1000); // ���
					
					// ���� �б� ���� ��ġ 
					position = vban.getPosition();
					position = position - delay;
					while(position < 0) position += buffer.length;
					System.out.println("buffer pos: " + position);
					
					System.out.println("start sending audio");
					listener.startSendingAudio(buffer, channel, bot); // ���� ���� ����
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
	
	// �� ������
	class Listener extends ListenerAdapter {
		
		// ����� ���� ����
		public void startSendingAudio(byte[] buffer, String channelName, JDA bot) {
			
			// ������ ���� ä�� ����
			VoiceChannel channel = bot.getVoiceChannelsByName(channelName, true).get(0);
			AudioManager manager = channel.getGuild().getAudioManager();
			
			// ����� ���� ����
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

		// ������ ����� ��ȯ
		@Override
		public ByteBuffer provide20MsAudio() {
			
			// ���ۿ��� ������ �о����
			for(int i = 0; i < audio.length; i++) {
				audio[i] = buffer[position];
				position++;
				if(position >= buffer.length) position = 0;
			}
			
			// ����� ���� (2����Ʈ ��Ʋ ����� -> 2����Ʈ �� �����)
			for(int i = 0; i < audio.length; i += 2) {
				t = audio[i];
				audio[i] = audio[i + 1];
				audio[i + 1] = t;
			}
			
			return ByteBuffer.wrap(audio);
			
		}
		
	}

}
