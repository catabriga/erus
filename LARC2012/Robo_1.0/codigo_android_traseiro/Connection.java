//  Copyright (C) 2011 Lucas Catabriga Rocha <catabriga90@gmail.com>
//    
//  This file is part of Graphwar.
//
//  Graphwar is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  Graphwar is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.

//  You should have received a copy of the GNU General Public License
//  along with Graphwar.  If not, see <http://www.gnu.org/licenses/>.

package erus.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Connection
{
	private Socket socket;
	private OutputStream out;
	private InputStream in;
	
	private long lastReceivedTime;
	private long lastSentTime;
	
	public Connection(String ip, int port) throws IOException
	{		
		SocketAddress sockaddr = new InetSocketAddress(ip, port);
		socket = new Socket();
		
		socket.connect(sockaddr);
				
		out = socket.getOutputStream();
		in = socket.getInputStream();		
		
		lastReceivedTime = System.currentTimeMillis();
		lastSentTime = System.currentTimeMillis();
				
	}
		
	public Connection(Socket socket) throws IOException
	{
		this.socket = socket;
				
		out = socket.getOutputStream();
		in = socket.getInputStream();	
	}
	
	public void close() throws IOException
	{
		out.close();
		in.close();
		
		socket.close();
	}
	
	public String getIpAddress()
	{
		return socket.getInetAddress().getHostAddress();
	}
	
	public long getLastSentTime()
	{
		return this.lastSentTime;
	}
	
	public long getLastReceivedTime()
	{
		return this.lastReceivedTime;
	}
	
	public void sendMessage(byte message[], int offset, int length) throws IOException
	{
		out.write(message, offset, length);
		lastSentTime = System.currentTimeMillis();		
	}
	
	public int readMessage(byte buffer[], int offset, int length) throws IOException
	{			
		int readSize = in.read(buffer, offset, length);		
		lastReceivedTime = System.currentTimeMillis();
		
		return readSize;
	}
	
	public int readMessageNonBlocking(byte buffer[], int offset, int maxLength) throws IOException
	{			
		int readMaxSize = in.available();
		
		if(readMaxSize > maxLength)
		{
			readMaxSize = maxLength;
		}
		
		int readSize = in.read(buffer, offset, readMaxSize);		
		lastReceivedTime = System.currentTimeMillis();
		
		return readSize;
	}
}
