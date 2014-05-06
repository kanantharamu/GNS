/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.umass.cs.networktools;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 *
 * @author westy
 */
//this class is for testing the conectivity to a database
//actually it times the connection setup time
//in theory this can be used to find the closest database to the clien location.
public class Pinger {

  private static long NOCONNECTION = -1;

  public static boolean isReachable(String host, int port, int timeoutMs) {
    return checkConnection(host, port, timeoutMs) != NOCONNECTION;
  }
  public static long checkConnection(String host, int port, int timeoutMs) {
    long start = NOCONNECTION; //default check value
    long end = NOCONNECTION; //default check value
    long total = NOCONNECTION; // default for bad connection

    //make an unbound socket
    Socket theSock = new Socket();

    try {
      InetAddress address = InetAddress.getByName(host);

      SocketAddress sockaddr = new InetSocketAddress(address, port);

     // Create the socket with a timeout
     // when a timeout occurs, we will get timout exp.
     // also time our connection this gets very close to the real time
      start = System.currentTimeMillis();
      theSock.connect(sockaddr, timeoutMs);
      end = System.currentTimeMillis();
    } catch (UnknownHostException e) {
      start = NOCONNECTION;
      end = NOCONNECTION;
    } catch (SocketTimeoutException e) {
      start = NOCONNECTION;
      end = NOCONNECTION;
    } catch (IOException e) {
      start = NOCONNECTION;
      end = NOCONNECTION;
    } finally {
      if (theSock != null) {
        try {
          theSock.close();
        } catch (IOException e) {
        }
      }

      if ((start != NOCONNECTION) && (end != NOCONNECTION)) {
        total = end - start;
      }
    }

    return total; //returns NOCONNECTION if timeout
  }
  //This is a test class

  public static void main(String[] args) {

// byte[] addr1 = new byte[]{(byte)192,(byte)168,(byte)2,(byte)5};
    int port = 1521;
    int timeoutMs = 2000; // 2 seconds
    long value = checkConnection("ec2-23-22-192-183.compute-1.amazonaws.com", 22, timeoutMs);
    System.out.println(value);
  }
}