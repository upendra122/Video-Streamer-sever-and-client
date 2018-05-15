package miniProFinal;
	import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
	import java.io.*;
	import java.net.DatagramPacket;
	import java.net.DatagramSocket;
	import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayDeque;
	import java.util.Arrays;
	import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.Timer;

import java.nio.*;
	public class Client implements Runnable, ActionListener {

		static ArrayDeque<BufferedImage>images;
	    static int seqNo=0;
	    static byte []payload;
	    static byte buffer[];
	    static Timer timer;
	    static DatagramSocket pacsoc;
	    static Socket command;
	    static int framerate=60;
	    static Gui frame;
	    static String vidName=new String();
	    public Client() {
	    	timer=new Timer(framerate,this);
	    }
	    public static void main(String []args) throws  Exception{
	        //socket for passing commands
	        //socket for getting packets
	        PrintStream writerC; 
	        //for receiving packet
	        //connecting to server
	        frame=new Gui();
	        images = new ArrayDeque<BufferedImage>();	
	        command=new Socket("localhost",2500);
	        writerC=new PrintStream(command.getOutputStream());
	        Client temp=new Client();
	        Thread thread1=new Thread(temp);
	        thread1.start();
	        //get the list of video from server
	        //get the name of video user want to watch
	        vidName="Letme.mp4";
	        while(vidName.isEmpty()==true);        
	        String signal="start";
	        writerC.println(signal);
	        writerC.println(vidName);
	        thread1.join();
	        
	    }
	    public void actionPerformed(ActionEvent e)
	    {
	    	//showing an image according to framerate set by timer
	    	
	    	if(images.size()==0)
	    	{
	    			
	    				try {
							Thread.sleep(6000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
	    			
	    	}
	    	frame.setIc(images.remove());
	 //   	System.out.println("frames "+images.size());
	    }
		public void run()
		{
			//System.out.println("Test");
			int tempInt=0;
			BufferedImage tempImg;
			try {
				pacsoc=new DatagramSocket(2600);
				System.out.println("Test");
				buffer=new byte[1024*1024*8];
				payload=new byte[1024*1024*8];
				
				DatagramPacket packet=new DatagramPacket(buffer,1024*1024*8);
				while(true)
				{
					
					try {
						pacsoc.receive(packet);
						buffer=packet.getData();
						
						//extract seq no;
						//System.out.println("hello");
						if(seqNo==0)
						{
							timer.setInitialDelay(6000);
				    		//timer.setCoalesce(true);
				    		timer.start();
						}
						//remove it later
		//				System.out.println("seqNo"+seqNo);
						tempInt=seqNo+1;
						if(tempInt>seqNo)
						{
							//use payload later
							//frame.setIc(convertToimg(buffer));
							images.add((convertToimg(buffer)));
							//System.out.println("kyun nahi chal raha hai");
							seqNo=tempInt;
						}
						else
						{
							images.add(images.getLast());
						}
					} catch (Exception e) {}
				}
			} catch (SocketException e) {
				e.printStackTrace();
			}
		}
		 BufferedImage convertToimg(byte [] buff) throws Exception
		{
			 ByteArrayInputStream convert=new ByteArrayInputStream(buff);
			 return ImageIO.read(convert);
				
		}
	}
