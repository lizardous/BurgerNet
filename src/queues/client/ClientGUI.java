package queues.client;

import java.awt.Button;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;

import queues.Message;

public class ClientGUI extends Frame implements ActionListener, WindowListener, Observer {

	private static final long serialVersionUID = 1L;
	
	private TextField tfTitle;
	private TextArea tfDesc;
	private TextField tfLat;
	private TextField tfLon;
	private Label lblNotice;
	
	private Button btnEmergency;
	private Button btnConfirm;
	private Button btnDetail;
	
	private Client client;
	
	void makeGUI(){
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		add(new Label("BurgerNet Client"));
		
		tfTitle = new TextField("Test ");
		add(tfTitle);
		tfDesc = new TextArea("");
		add(tfDesc);
		tfLat = new TextField("56");
		add(tfLat);
		tfLon = new TextField("6");
		add(tfLon);
		
		lblNotice = new Label("-");
		add(lblNotice);
		
		btnEmergency = new Button("Emergency");
		add(btnEmergency);
		btnEmergency.addActionListener(this);
		btnConfirm = new Button("Confirm");
		add(btnConfirm);
		btnConfirm.addActionListener(this);
		btnDetail = new Button("Add Details");
		add(btnDetail);
		btnDetail.addActionListener(this);
		
		addWindowListener(this);
		
		setTitle("BurgerNet Client");
		setSize(400, 800);
		setVisible(true);
	}
	
	public ClientGUI(final Client o){
		super();
		o.addObserver(this);
		this.client = o;
		
		makeGUI();
		
		(new Thread(){
			public void run(){ o.start(); }
		}).start();
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if((Button)evt.getSource() == btnConfirm){
			this.client.confirm(true);
		}
		
		if((Button)evt.getSource() == btnEmergency){
			this.client.updateLocation(Double.parseDouble(tfLat.getText()), Double.parseDouble(tfLon.getText()));
			this.client.emergency(tfTitle.getText(), tfDesc.getText() + "\n");
		}
		
		if((Button)evt.getSource() == btnDetail){
			this.client.detail(tfDesc.getText());
		}
	}

	@Override
	public void windowActivated(WindowEvent arg0) {}
	@Override
	public void windowClosed(WindowEvent arg0) {}
	@Override
	public void windowDeactivated(WindowEvent arg0) {}
	@Override
	public void windowDeiconified(WindowEvent arg0) {}
	@Override
	public void windowIconified(WindowEvent arg0) {}
	@Override
	public void windowOpened(WindowEvent arg0) {}
	@Override
	public void windowClosing(WindowEvent arg0) {
		System.exit(0);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		Message msg = (Message)arg;
		if(msg.getType() == Message.Types.RequestConfirm){
			lblNotice.setText("Please confirm this emergency");
		}
		if(msg.getType() == Message.Types.Detail || msg.getType() == Message.Types.Confirm){
			lblNotice.setText("Details added");
		}
		tfTitle.setText(msg.getTitle());
		tfDesc.setText(msg.getDescription());
		tfLat.setText(""+msg.getLat());
		tfLon.setText(""+msg.getLon());
	}

	public static void main(String[] args) throws IOException {
		new ClientGUI(new Client());
	}

}
