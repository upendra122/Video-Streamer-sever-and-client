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
	    static Socket command,audio;
	    static int framerate=85;
	    static Gui frame;
	    static String vidName=new String();
	    static String vidList[]=new String[10];
	    static Scanner readC,readAA;
	    static DataInputStream readA;
	    static int noOfmovie=0;
	    static PrintStream writerC; 
	    static OutputStream temp;
	    static byte[] bufferA = new byte[4096];
	    static int len;
	    static boolean musicOn=false;
	    public Client() {
	    	timer=new Timer(framerate,this);
	    }
	    public static void main(String []args) throws  Exception{
	        //socket for passing commands
	        //socket for getting packets
	        
	        //for receiving packet
	        //connecting to server
	        
	        images = new ArrayDeque<BufferedImage>();	
	        command=new Socket("localhost",2500);
	        audio=new Socket("localhost",2500);
	        writerC=new PrintStream(command.getOutputStream());
	        readC=new Scanner(command.getInputStream());
	        readA=new DataInputStream(new BufferedInputStream(audio.getInputStream()));
	        readAA=new Scanner(audio.getInputStream());
	        temp=new FileOutputStream("temp.mp3");
	      //get the list of video list from server
	        String tempM;
	        while(true)
	        {
	        	tempM=readC.nextLine();
	        	//System.out.println(tempM);
	        	if(tempM.equals("exit01"))
	        	{
	        		break;
	        	}
	        	vidList[noOfmovie]=tempM;
	        	noOfmovie++;
	        	
	        }
	        frame=new Gui(vidList);
	        //get the name of video user want to watch
	        while(vidName.isEmpty()==true)
	        {
	        	vidName=frame.movieName;
	        	System.out.println(frame.movieName);
	        	
	        }
	        Thread t1=new Thread()
			{
				public void run()
				{
					while(true)
					{
					try {
						//readAA.nextInt(len);
						len=readA.read(bufferA);
						System.out.println(len);
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						temp.write(bufferA, 0,len);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				}
			};
			t1.start();
	        System.out.println("1");
	        Client temp=new Client();
	        Thread thread1=new Thread(temp);
	        thread1.start();
	        
	        String signal="start";
	        writerC.println(signal);
	        writerC.println(vidName);
	        thread1.join();
	        
	    }
	    public void actionPerformed(ActionEvent e)
	    {
	    	//showing an image according to framerate set by timer
	    	
	    	
	    	if(images.size()<=4)
	    	{
	    			
	    				try {
							Thread.sleep(2000);
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
					if(frame.isforwardB==true)
					{
						
						frame.isforwardB=false;
						writerC.println("forward");
						System.out.println("forward");
						timer.stop();
						images.clear();
						timer.setInitialDelay(12000);
						timer.restart();
						
					}
					if(frame.ispause==true)
					{
						frame.ispause=false;
						timer.stop();
						writerC.println("pause");
						//System.out.println("pahunch gyaa bhai ");
						while(frame.isplay==false) 
						{
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {}
						}
						
						writerC.println("play");
						timer.start();
						
						frame.isplay=false;
						
					}					
					try {
						pacsoc.receive(packet);
						buffer=packet.getData();
						//extract seq no;
						//System.out.println("hello");
						if(seqNo==0)
						{
							timer.setInitialDelay(3000);
				    		//timer.setCoalesce(true);
				    		timer.start();
						}
						//remove it later
						//System.out.println("seqNo"+seqNo);
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
