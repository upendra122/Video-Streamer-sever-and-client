package miniProFinal;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javafx.scene.control.ComboBox;
public class Gui  {
	static JFrame frame;
	static JLabel label;
	static ImageIcon icon;
	static BufferedImage image;
	static String movieList[];
	static JComboBox showMovie;
	static JButton forward10,backward10,play,pause;
	static String movieName=new String();
	static boolean isforwardB=false;
	static boolean isbackwardB=false;
	static boolean isplay=false;
	static boolean ispause=false;
	Gui(String []args)
	{
		movieList=args;
		frame =new JFrame("myStreamer");
		forward10=new JButton("Forward");
		backward10=new JButton("Backward");
		play=new JButton("play");
		pause=new JButton("pause");
		forward10.setBounds(400,550, 150, 30);
		backward10.setBounds(550,550,130,30);
		play.setBounds(200,550,100,30);
		pause.setBounds(300, 550,100,30);
		frame.setSize(1000,600);
		label=new JLabel();
		showMovie=new JComboBox(movieList);
	//	icon=new ImageIcon(image);
		frame.setLayout(null);
		//label.setIcon(icon);
		label.setBounds(0, 0, 1000, 400);
		showMovie.setBounds(0,520, 100,50);
		frame.add(label);
		frame.add(showMovie);
		frame.add(forward10);
		//frame.add(backward10);
		frame.add(play);
		frame.add(pause);
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		showMovie.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
				JComboBox temp=(JComboBox)e.getSource();
				Object tempM= temp.getSelectedItem();
				movieName=tempM.toString();
				
				System.out.println(movieName);
			}
		});
		forward10.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			
				isforwardB=true;
				System.out.println("forward");
			}
		});
		backward10.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg)
			{
				isbackwardB=true;
				System.out.println("backword");
			}
		});
		play.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				isplay=true;
				System.out.println("play");
			}
		});
		pause.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				ispause=true;
				System.out.println("pause");
			}
		});
		frame.setVisible(true);
	}
	void setIc(BufferedImage i)
	{
		icon=new ImageIcon(i);
		label.setIcon(icon);
	}
	
}
