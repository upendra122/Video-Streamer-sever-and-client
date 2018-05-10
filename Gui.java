package miniProFinal;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Gui  {
	static JFrame frame;
	static JLabel label;
	static ImageIcon icon;
	static BufferedImage image;
	Gui()
	{
		frame =new JFrame();
		frame.setSize(800,400);
		label=new JLabel();
	//	icon=new ImageIcon(image);
		frame.setLayout(null);
		//label.setIcon(icon);
		label.setBounds(0, 0, 800, 400);
		frame.add(label);
		
		frame.setVisible(true);
	}
	void setIc(BufferedImage i)
	{
		icon=new ImageIcon(i);
		label.setIcon(icon);
	}
	public static void main(String []args)
	{
		Gui obj1=new Gui();
	}
}
