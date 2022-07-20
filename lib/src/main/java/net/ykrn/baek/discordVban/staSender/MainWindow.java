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
// ���� ������
public class MainWindow extends JFrame {
	
	private static final long serialVersionUID = 3632411744883168263L;
	private JTextField portTF; // VBAN ��Ʈ �ؽ�Ʈ �ʵ�
	private JTextField bufSizeTF; // ���� ������ �ؽ�Ʈ �ʵ�(byte)
	private JTextField delayTF; // ������ ������ �ؽ�Ʈ �ʵ�(byte)
	private JTextField channelTF; // ä�� �ؽ�Ʈ �ʵ�
	private JTextField tokenTF; // �� ��ū �ؽ�Ʈ �ʵ�
	private JTextArea log; // �α� �ؽ�Ʈ ������
	private boolean botStatus = false; // �� ���� true = �۵���, false = �۵� ������
	private byte[] buffer; // ����
	private PrintStream ps = null; // log �ؽ�Ʈ ����� ����ϴ� ��Ʈ��
	private JButton startBtn; // ���� ��ư
	private JLabel vbanPositionLabel; // VBANReceptor ���� ��ġ ǥ��
	private JLabel discordPositionLabel; // DiscordBot ���� ��ġ ǥ��
	
	// 
	public MainWindow() {
		
		// �� ���� ����
		botStatus = false;
		// â ����
		createWindow();
		// �α� ��� ��Ʈ�� ����
		createPrintStream();
		
		// �ʱ� ��� ����
		print("Discord VBAN Bot");
		print();
		print("Discord VBAN Bot v0.4");
		print("VBAN ��Ʈ���� �����Ͽ� ���ڵ� ���� ä�η� �����մϴ�.");
		print();
		print("���� ����");
		print("v0.5: ");
		print("  - ������ ������ msec���� ���� ��ġ�� ����");
		print("  - ���� ũ��, ������ �⺻�� ����");
		print("v0.4: ");
		print("  - ���� ������ ���ӵ� ��쿡�� ��� ����");
		print("v0.3: ");
		print("  - ��� â �ѱ� ��� ���� �ذ�");
		print("v0.2: ");
		print("  - �ʱ� �ȳ� ���� ����");
		print("  - ���� ��ġ ǥ�� �߰�");
		print("v0.1: ");
		print("  - ���� ����");
		print();
		print("��Ʈ: VBAN ��Ʈ���� ������ ��Ʈ ��ȣ");
		print("����: ������ ����� ���� ������ ũ��(byte)");
		print("������: VBAN ���Ű� ���ڵ� ���� ���� ������ ������(byte)");
		print("ä��: ������ ������ ���� ä�� �̸�");
		print("��ū: ������ ����� ��ū");
		print();
		print("���ŵǴ� VBAN ��Ŷ�� �ݵ�� 48000Hz 16Bits 2ch PCM �̾�� �մϴ�.");
		print();
		print("����� �� ������ Ȯ�� �� �ϴ��� ����! ��ư�� ���� ���� ������ �� �ֽ��ϴ�.");
		print();
		print("�Ҹ��� ������ �������� ��� ���� ����� �ϼ���.");
		print("������ص� �ذ���� �ʴ´ٸ� ���� ũ��� �����̸� �������ּ���.");
		print();
		
	}
	
	// â ����
	private void createWindow() {
		
		// ������ ���̾ƿ� ����
		setLayout(new BorderLayout());
		
		// �г� ����
		JPanel upperPanel = new JPanel();
		JPanel mainPanel = new JPanel();
		JPanel lowerPanel = new JPanel();
		upperPanel.setLayout(new GridLayout(2, 1));
		mainPanel.setLayout(new BorderLayout(0, 0));
		lowerPanel.setLayout(new GridLayout(1, 5));
		
		// ��� �г�
		JPanel upperPanelSub = new JPanel();
		upperPanel.add(upperPanelSub);
		
		JPanel upperPanelSub1 = new JPanel();
		upperPanelSub1.add(new JLabel("��Ʈ"));
		portTF = new JTextField(4);
		upperPanelSub1.add(portTF);
		portTF.setText("6980");
		upperPanelSub.add(upperPanelSub1);
		
		JPanel upperPanelSub2 = new JPanel();
		upperPanelSub2.add(new JLabel("����"));
		bufSizeTF = new JTextField(4);
		upperPanelSub2.add(bufSizeTF);
		bufSizeTF.setText("40000");
		upperPanelSub.add(upperPanelSub2);
		
		JPanel upperPanelSub3 = new JPanel();
		upperPanelSub3.add(new JLabel("������"));
		delayTF = new JTextField(4);
		upperPanelSub3.add(delayTF);
		delayTF.setText("10000");
		upperPanelSub.add(upperPanelSub3);
		
		JPanel upperPanelSub4 = new JPanel();
		upperPanelSub4.add(new JLabel("ä��"));
		channelTF = new JTextField(6);
		upperPanelSub4.add(channelTF);
		channelTF.setText("����ä��");
		upperPanelSub.add(upperPanelSub4);
		
		JPanel upperPanelSub5 = new JPanel();
		upperPanelSub5.add(new JLabel("��ū"));
		tokenTF = new JTextField(33);
		upperPanelSub5.add(tokenTF);
		tokenTF.setText("");
		upperPanel.add(upperPanelSub5);
		
		// ���� �г�
		log = new JTextArea(15, 0);
		JScrollPane scrollPane = new JScrollPane(log);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		mainPanel.add(scrollPane);
		
		// �ϴ� �г�
		lowerPanel.add(new JLabel("VBAN ", SwingConstants.RIGHT));
		vbanPositionLabel = new JLabel("0");
		lowerPanel.add(vbanPositionLabel);
		startBtn = new JButton("����!");
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
		
		// �����ӿ� �гε� �߰�
		add(upperPanel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
		add(lowerPanel, BorderLayout.SOUTH);
		
		// ������ ����, ������ ǥ��
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
		print("���� �����մϴ�.");
		botStatus = true;
		startBtn.setEnabled(false);
		
		// ���� ����
		print("���۸� �����մϴ�.");
		print("���� ũ��: " + bufSizeTF.getText());
		try {
			buffer = new byte[Integer.parseInt(bufSizeTF.getText())];
		} catch(NumberFormatException e) {
			print("���� ������ �����Ͽ����ϴ�: ���� ũ�Ⱑ �߸��ƽ��ϴ�.");
			botStatus = false;
			startBtn.setEnabled(true);
			return;
		} catch(ArrayIndexOutOfBoundsException e) {
			print("���� ������ �����Ͽ����ϴ�: ���� ũ�Ⱑ �߸��Ǿ����ϴ�.");
			botStatus = false;
			startBtn.setEnabled(true);
			return;
		}
		print("���۸� �����Ͽ����ϴ�.");
		print();
		
		// VBAN ���� ����
		print("VBAN ��Ʈ�� ������ �����մϴ�.");
		print("VBAN ��Ʈ�� ���� ��Ʈ: " + portTF.getText());
		print("������: " + delayTF.getText());
		VBANReceptor receptor = null;
		try {
			receptor = new VBANReceptor(Integer.parseInt(portTF.getText()), buffer, getPrintStream());
			if(receptor.checkConnection() == true) print("VBAN ��Ʈ�� ������ ���۵Ǿ����ϴ�.");
			else {
				print("VBAN ��Ʈ�� ������ ���۵��� �ʾҽ��ϴ�.");
				botStatus = false;
				startBtn.setEnabled(true);
				return;
			}
		} catch(NumberFormatException e) {
			print("VBAN ��Ʈ�� ���� ���ۿ� �����Ͽ����ϴ�: ��Ʈ �Ǵ� �����̰� �߸��Ǿ����ϴ�.");
			botStatus = false;
			startBtn.setEnabled(true);
			return;
		}
		print();
		
		// ���ڵ� �� ����
		print("���ڵ� ���� �����մϴ�.");
		DiscordBot bot = new DiscordBot(tokenTF.getText(), buffer, channelTF.getText(), getPrintStream());
		if(bot.checkConnection() == true) print("���ڵ� ���� �����Ǿ����ϴ�.");
		else {
			print("���ڵ� ���� �������� �ʾҽ��ϴ�.");
			botStatus = false;
			startBtn.setEnabled(true);
			return;
		}
		try {
			int delay = Integer.parseInt(delayTF.getText());
			print("1�� �� ���� ���� ������ �����մϴ�.");
			bot.startSendingAudio(receptor, delay);
		} catch(NumberFormatException e) {
			print("���� ���� ������ �����Ͽ����ϴ�.");
			botStatus = false;
			startBtn.setEnabled(true);
			return;
		}
		print();
		
		new refreshPosition(receptor, bot);
		
	}
	
	// �α� ����Ʈ��Ʈ�� ����
	private PrintStream createPrintStream() {
		ps = new PrintStream(new OutputStream() {
			
			byte[] data = new byte[256];
			int position = 0;
			
			@Override
			public void write(int b) throws IOException {
				//log.append(String.valueOf((char) b)); // �� �ڵ�: �ѱ� ����
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
	
	// ����Ʈ��Ʈ�� ���� �α׿� ���� ���(�� �ٲ�)
	public void print() {
		print("");
	}
	
	// ����Ʈ��Ʈ�� ���� �α׿� ���� ���(����� �� �ٲ�)
	public void print(String s) {
		log.append(s + "\n");
		log.setCaretPosition(log.getDocument().getLength());
	}
	
	// VBAN, DiscordBot ���� ��ġ ǥ��
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
					sleep(100); // 0.1�ʸ��� ����
					if(vban != null) vbanPositionLabel.setText(Integer.toString(vban.getPosition()));
					if(bot != null) discordPositionLabel.setText(Integer.toString(bot.getPosition()));
				} catch (InterruptedException e) {
				}
			}
			
		}
		
	}

}
