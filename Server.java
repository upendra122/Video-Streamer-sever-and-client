package miniProFinal;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.Timer;	
import com.sun.prism.Image;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.IContainer;

public class Server extends Thread implements ActionListener{
	static ServerSocket server;
	static Socket command;
	static byte buffer[];
	static DatagramSocket pacsoc;
	static DatagramPacket packet;
	static String state="init";
	static boolean pause=false;
	static boolean play=false;
	static boolean back10sec=false;
	static boolean forw10sec=false;
	static String videoName="";
	static Timer timer;
	static int frameRate=23;
	static FileInputStream readBuff;
	static boolean exit;
	static boolean vidAvail;
	static int seqNo=0;
	static int frameLen;
	static int frame10sec=0;
	static int clientSoc=2401;
	static InetAddress ip;
	static IContainer container;
	boolean first=true;
	static IMediaReader mediaReader;
	static ArrayDeque<BufferedImage> images=new ArrayDeque<BufferedImage>();
	Gui frame;
	Server()
	{
		timer=new Timer(frameRate,this);
	}
	//thread running for listening commands
	public void run()
	{
		try
		{
			Scanner sc=new Scanner(command.getInputStream());
			String temp=new String();
			while(true)
			{
				temp=sc.nextLine();
				if(temp.equals("start"))
				{
					videoName=sc.nextLine();
					vidAvail=true;
				}
				else if(temp.equals("play"))
				{
					play=true;
				}
				else if(temp.equals("pause"))
				{
					pause=true;
				}
				else if(temp.equals("backward"))
				{
					back10sec=true;
				}
				else if(temp.equals("forward"))
				{
					forw10sec=true;
				}
				else if(temp.equals("terminate"))
				{
					exit=true;
				}
			}
		}catch(Exception e) {};
	}
	public static void main(String args[]) throws Exception
	{
		server = new ServerSocket(2500);
        command=server.accept();
        ip=command.getInetAddress();
        System.out.println("client connected "+ip);
        Server tempObj=new Server();
        tempObj.start();
        pacsoc=new DatagramSocket();
        buffer=new byte[1024*1024*8];
        while( true)
        {
        	if(!vidAvail)
        	{
        		System.out.print("");
        	}
        	if(state.equals("init") && vidAvail)
        			
        	{
        		Thread.sleep(500);
        		//pre-process the video and all
        		//readBuff=new FileInputStream(new File(videoName));
        		//
        		container=IContainer.make();
        		if(container.open(videoName,IContainer.Type.READ,null)<0)
        		{
        			System.out.println("Cant not");
        		}
        		System.out.println(videoName);
        		mediaReader = ToolFactory.makeReader(container);
        		mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
        		mediaReader.addListener(new MediaListenerAdapter() {
        			
        			 public void onVideoPicture(IVideoPictureEvent Event)
        			{
        				
        				images.add(Event.getImage());
        				System.out.println("image added");
        			}
        		});
        		timer.setInitialDelay(0);
        		timer.setCoalesce(true);
        		state="ready";
        		play=true;
        		System.out.println("init");
        	}
        	if(state.equals("ready")&&play==true)
        	{
        		state="running";
        		timer.start();
        		System.out.println("running");
        		pause=false;
        	}
        	if(state.equals("running") && pause==true)
        	{
        		//pause
        		state="ready";
        		timer.stop();
        		System.out.println("ready");
        		play=false;
        	}
        	if((state.equals("running")||state.equals("ready"))&&back10sec==true)
        	{
        		//10 sec back
        		//calculate frame no
        		System.out.println("backed");
        		readBuff.close();
        		readBuff=new FileInputStream(new File(videoName));
        		for(int i=0;i<seqNo-frame10sec;i++)
        		{
        			readBuff.read(buffer,0,frameLen);
        		}
        		back10sec=false;
        	}
        	if((state.equals("running")||state.equals("ready"))&&forw10sec==true)
        	{
        		//10 sec later
        		//calculate next frame no
        		System.out.println("forward");
        		
        		for(int i=0;i<frame10sec;i++)
        		{
        			readBuff.read(buffer,0,frameLen);
        		}
        		forw10sec=false;
        	}
        	if((state.equals("ready")||state.equals("running"))&& exit==true)
        	{
        		command.close();
        		pacsoc.close();
        		server.close();
        		System.exit(0);
        	}
        }
	}
	public void actionPerformed(ActionEvent e)
	{
		//get the frame make it a packet send it two the client
		try {
			//ByteBuffer temp=ByteBuffer.allocate(5);
			//temp.putInt(seqNo);
			//buffer=temp.array();
			//readBuff.read(buffer,5,frameLen);
			//packet=new DatagramPacket(buffer,buffer.length,ip,clientSoc);
			
			/*if(first==true)
			{
				new Runnable() {
					public void run()
					{
						System.out.println("runable started");
						
						
						try {
						
						*/	mediaReader.readPacket();
							if(first==true)
							frame=new Gui();
						//	while(!images.isEmpty())
							//{
								frame.setIc(images.remove());
								//Thread.sleep(33);
							//}
						//} catch (InterruptedException e) {
							
							//e.printStackTrace();
						//}
						
						
					//}
				//};
		
			
				first=false;
			//}
		}catch(Exception t){};
	}

}
