package net.ykrn.baek.discordVban.staSender;

import java.io.PrintStream;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.audio.AudioSendHandler;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;

// DiscordBot.java
// ������ PCM �����͸� ���ڵ�� ����
public class DiscordBot {
	
	private JDA bot = null; // �ڹ� ���ڵ� �� ��ü
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
			bot = new JDABuilder(token).addEventListener(listener).build(); // �� ����
			connection = true;
		} catch (LoginException e) {
			ps.println("login failed");
		}
		
	}
	
	public boolean checkConnection() {
		return connection;
	}
	
	// ����� ����
	public void startSendingAudio(int delay, int position) {
		
		// ���ۿ��� �б� ������ ��ġ
		this.position = position;
		
		// �־��� �ð���ŭ ��� �� ���� ����
		Thread thread = new Thread() {
			public void run() {
				try {
					sleep(delay); // ���
					System.out.println("start sending audio");
					listener.startSendingAudio(buffer, channel, bot); // ���� ���� ����
				} catch (InterruptedException e) {
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
			VoiceChannel channel = bot.getVoiceChannelByName(channelName, true).get(0);
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
		public byte[] provide20MsAudio() {
			
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
			
			return audio;
			
		}
		
	}

}
