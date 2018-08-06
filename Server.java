package miniProFinal;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.Timer;	
import com.sun.prism.Image;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IContainerFormat;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.io.XugglerIO;

public class Server extends Thread implements ActionListener{
	static ServerSocket server;
	static Socket command,audio;
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
	static int frameRate=2;
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
	static ByteArrayOutputStream imgTobyte;
	static ArrayDeque<BufferedImage> images=new ArrayDeque<BufferedImage>();
	static ArrayList<String> vidList;
	static PrintStream writeS,writeAA;
	static DataOutputStream writeA;
	static int noOfmovies=0;
	static int for10count=0;
	static int back10count=0;
	//for calculating 10 sec  backword frame 
	static int framegone=0;
	static Path path;
	static PipedInputStream pis;
	static PipedOutputStream pos;
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
					String tempM=sc.nextLine();
					videoName="./media/"+tempM;
					path=Paths.get(videoName);
					streamToSource(  path );
					vidAvail=true;
					Thread t=new Thread()
							{
								public void run()
								{
									  int nRead = 0;
									    try {
											while ( ( nRead = pis.read( buffer ) ) != -1 ) {
											 
												writeA.write( buffer,0 , nRead );
												
											}
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									    try {
											pis.close( );
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}

									    System.out.println( "end : " + path );

								}
							};
					t.start();
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
        audio=server.accept();
        //send the movie list to the client
        writeS=new PrintStream(command.getOutputStream());
        writeA=new DataOutputStream(new BufferedOutputStream(audio.getOutputStream()));
        writeAA=new PrintStream(audio.getOutputStream());
        vidList=new ArrayList<String>();
        File[] files=new File("./media").listFiles();
        for(File file: files)
        {
        	if(file.isFile())
        	{
        		vidList.add(file.getName());
        		noOfmovies++;
        	}
        }
        while(vidList.isEmpty()==false)
        {
        	noOfmovies--;
        	String tempM=vidList.get(noOfmovies);
        	System.out.println(tempM);
        	writeS.println(tempM);
        	vidList.remove(noOfmovies);
        	
        }
        writeS.println("exit01");
        ip=command.getInetAddress();
        System.out.println("client connected "+ip);
        Server tempObj=new Server();
        tempObj.start();
        pacsoc=new DatagramSocket();
        buffer=new byte[1024*1024*8];
        imgTobyte =new ByteArrayOutputStream();
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
        			System.out.println("Can not");
        		}
        		System.out.println(videoName);
        		mediaReader = ToolFactory.makeReader(container);
        		mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
        		mediaReader.addListener(new MediaListenerAdapter() {
        			
        			 public void onVideoPicture(IVideoPictureEvent Event)
        			{
        				if(for10count==0)
        				images.add(Event.getImage());
        				else 
        					for10count--;
        				//System.out.println(images.getLast());
        				framegone++;
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
        		back10count=framegone-50;
        		framegone=0;
        		
        		back10sec=false;
        	}
        	if((state.equals("running")||state.equals("ready"))&&forw10sec==true)
        	{
        		//10 sec later
        		//calculate next frame no
        		for10count=50;
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
		
			//ByteBuffer temp=ByteBuffer.allocate(5);
			//temp.putInt(seqNo);
			//buffer=temp.array();
			//readBuff.read(buffer,5,frameLen);
			//packet=new DatagramPacket(buffer,buffer.length,ip,clientSoc);			
			if(images.size()>1000)
			{
				timer.stop();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					
					e1.printStackTrace();
				}
			}
			timer.start();
			mediaReader.readPacket();
			if(images.size()>1&& first==true)
			{
				Thread sendPack=new Thread()
						{
							public void run()
							{
								//System.out.println(images.size());
								
								try {
									
									ImageIO.write(images.remove(), "jpg",imgTobyte);
									imgTobyte.flush();
									
								} catch (IOException e) {
									e.printStackTrace();
								}
								
								buffer=imgTobyte.toByteArray();
							//	System.out.println(buffer.length);
								imgTobyte.reset();
								
								packet=new DatagramPacket(buffer,buffer.length,ip,2600);
								//to clear previous image from Byteoutputstream so
								//that it dont get add up in next packet
								
								try {
									if(buffer.length<60000)
									pacsoc.send(packet);
									
								} catch (IOException e) {
									
									e.printStackTrace();
								}
							}
							
						};
						sendPack.start();
			}
				
	}
	private static void streamToSource( Path path ) throws IOException {

	    byte[] buffer = new byte[4096];
	    pis = new PipedInputStream( );
	    pos = new PipedOutputStream( pis );
	    convertToMP3Xuggler( path, pos );

	    System.out.println( "start streaming" );
	  
	}

	private static void convertToMP3Xuggler( Path path, PipedOutputStream pos ) throws FileNotFoundException {
	    IMediaWriter mediaWriter = ToolFactory.makeWriter( XugglerIO.map( pos ) );
	    IContainerFormat containerFormat = IContainerFormat.make( );
	    containerFormat.setOutputFormat( "mp3", null, "audio/mp3" );
	    mediaWriter.getContainer( ).setFormat( containerFormat );
	    IContainer audioContainer = IContainer.make( );
	    audioContainer.open( path.toFile( ).toString( ), IContainer.Type.READ, null );
	    int audioStreamId = -1;


	    for ( int i = 0; i < audioContainer.getNumStreams( ); i++ ) {
	        IStream stream = audioContainer.getStream( i );
	        IStreamCoder coder = stream.getStreamCoder( );
	        if ( coder.getCodecType( ) == ICodec.Type.CODEC_TYPE_AUDIO ) {
	            audioStreamId = i;
	            break;
	        }
	    }
	    if ( audioStreamId < 0 ) {
	        throw new IllegalArgumentException( "cannot find audio stream in the current file : " + path.toString( ) );
	    }
	    System.out.println( "found audio stream = " + audioStreamId );

	    IStreamCoder coderAudio = audioContainer.getStream( audioStreamId ).getStreamCoder( );

	    if ( coderAudio.open( null, null ) < 0 ) {
	        throw new RuntimeException( "Cant open audio coder" );
	    }
	    coderAudio.setSampleFormat( IAudioSamples.Format.FMT_S16 );
	    int streamIndex = mediaWriter.addAudioStream( 0, 0, coderAudio.getChannels( ), coderAudio.getSampleRate( ) );
	    IStreamCoder writerCoder = mediaWriter.getContainer( ).getStream( streamIndex ).getStreamCoder( );
	    writerCoder.setFlag( IStreamCoder.Flags.FLAG_QSCALE, false );
	    int BITRATE=128;
		writerCoder.setBitRate( BITRATE * 1000 );
	    writerCoder.setBitRateTolerance( 0 );
	    System.out.println( "bitrate for output = " + writerCoder.getBitRate( ) );

	    IPacket packet = IPacket.make( );

	    runInThread( path, pos, mediaWriter, audioContainer, audioStreamId, coderAudio, streamIndex, packet );
	}
	private static void runInThread( Path path, PipedOutputStream pos, IMediaWriter mediaWriter, IContainer audioContainer, int audioStreamId, IStreamCoder coderAudio, int streamIndex, IPacket packet ) {

	    new Thread( ) {
	        @Override
	        public void run( ) {

	            while ( audioContainer.readNextPacket( packet ) >= 0 ) {
	                if ( packet.getStreamIndex( ) == audioStreamId ) {
	                    IAudioSamples samples = IAudioSamples.make( 4096, coderAudio.getChannels( ), IAudioSamples.Format.FMT_S16 );
	                    int offset = 0;
	                    while ( offset < packet.getSize( ) ) {
	                        int bytesDecoded = coderAudio.decodeAudio( samples, packet, offset );
	                        if ( bytesDecoded < 0 ) {
	                            System.out.println( "decode error in : " + path + " bytesDecoded =" + bytesDecoded + " offset=" + offset + " packet=" + packet );
	                            break;
	                        }

	                        offset += bytesDecoded;

	  	                        if ( samples.isComplete( ) ) {
	                            mediaWriter.encodeAudio( streamIndex, samples );
	                        }
	                    }
	                }
	            }
	            coderAudio.close( );
	            audioContainer.close( );
	            mediaWriter.close( );
	            try {
	                pos.close( );
	            } catch ( IOException e ) {
	                e.printStackTrace( );
	            }
	        }

	    }.start( );
	}
}
