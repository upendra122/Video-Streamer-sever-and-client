# Video-Streamer-sever-and-client
A program for streaming a video that is on a remote computer (server) in the same network.
The program is only limited to transfering video.Although it can tranmit video of any container(mp4,avi etc.).
The program uses UDP(user datagram protocol) for tranfering the video frames.And control signal like puause, play sent through a TCP(transmission control protocol) connection.
For getting the frames from video stream of a container an external library () is used.
