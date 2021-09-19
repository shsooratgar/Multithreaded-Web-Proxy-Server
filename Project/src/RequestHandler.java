import jdk.swing.interop.SwingInterOpUtils;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

public class RequestHandler implements Runnable{
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private String fullUrl;
    private String method;
    private String receivedRequest;

    public RequestHandler(Socket socket) throws IOException {
        this.socket = socket;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    @Override
    public void run() {
        try {
            receivedRequest = reader.readLine();
            if(receivedRequest == null)return;
            System.out.println(receivedRequest);
            String [] methodAndUrl = receivedRequest.split("\\s+");
            method = methodAndUrl[0];
            fullUrl = methodAndUrl[1];
            if(Proxy.getInstance().isFilter(fullUrl))
            {
                return;
            }
            if(!fullUrl.startsWith("http")){
                fullUrl = "http://" + fullUrl;
            }
            if(method.equals("CONNECT")){
                handleConnect();
            }
            else{
                handleRequest();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void handleRequest()  {
        trySendCachedRequest();
//        sendNewRequest();
    }

    private void trySendCachedRequest() {
        try {
            boolean cached = true;
            Cache cache = Cache.find(receivedRequest);
            if (cache == null || cache.getETag() == null)
                cached = false;
            String request = "";
            request += receivedRequest + "\r\n";
            String line;
            while (!(line = reader.readLine()).equals("")) {
                request += line + "\r\n";
                System.out.println(line);
            }
            if(cached)
                request += "If-None-Match: " + cache.getETag() + "\r\n";

            Socket connection = new Socket(new URL(fullUrl).getHost(), 80);
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream())));
            out.println();
            out.print(request);
            out.print("\r\n");
            out.flush();
            InputStream serverReader = connection.getInputStream();
            String answer = "";
            int temp;
            while((temp = serverReader.read()) != '\n')
            {
                answer += (char) temp;
            }
            System.out.println(answer);

            try{
                if(!answer.split("\\s+")[1].equals("304") || cache == null)
                    throw new NotCachedException();
                InputStream fileReader = cache.getInputStream();
                int byt;
                while ((byt = fileReader.read()) != -1) {
                    socket.getOutputStream().write(byt);
                }
                socket.getOutputStream().flush();
                socket.getOutputStream().close();
            } catch (NullPointerException | NotCachedException e) {
                e.printStackTrace();
                System.out.println("Sending none cached request");
                answer += "\n";
                socket.getOutputStream().write(answer.getBytes());
                cache = new Cache(receivedRequest);
                OutputStream fileCacheOutput = cache.getOutputStream();
                fileCacheOutput.write(answer.getBytes());
                int byt;
                boolean noMoreHeader = false;
                StringBuilder header = new StringBuilder();
                try{
                    while ((byt = serverReader.read()) != -1) {
                        if(!noMoreHeader) {
                            header.append((char) (byt));
                            if (byt == '\n') {
                                if (header.toString().isBlank()) {
                                    noMoreHeader = true;
                                }
                                String newHeader = header.toString();
                                if(newHeader.split(":")[0].equals("ETag"))
                                {
                                    noMoreHeader = true;
                                    String ETag = newHeader.split(":")[1].substring(1);
                                    System.out.println("Data has ETag: " + ETag);
                                    Cache.addCache(cache);
                                    cache.setETag(ETag);
                                }
                                header = new StringBuilder();
                            }
                        }
                        fileCacheOutput.write(byt);
                        socket.getOutputStream().write(byt);
                    }
                }
                catch (SocketException s)
                {
                    s.printStackTrace();
                    fileCacheOutput.close();
                    cache.getFile().delete();
                    return;
                }
                fileCacheOutput.close();
                if(cache.getETag() == null)
                {
                    cache.getFile().delete();
                }
                //Checking ETag Header:
                socket.getOutputStream().flush();
                serverReader.close();
                out.close();
                connection.close();
                socket.getInputStream().close();
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void sendCachedRequest() {
    }

    private void sendNewRequest() {
        try {
            String request = "";
            request += receivedRequest + "\r\n";
            String line;
            while (!(line = reader.readLine()).equals("")) {
                request += line + "\r\n";
                System.out.println(line);
            }
            Socket connection = new Socket(new URL(fullUrl).getHost(), 80);
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(connection.getOutputStream())));
            out.println();
            out.print(request);
            out.print("\r\n");
            out.flush();
            InputStream serverReader = connection.getInputStream();
            Cache cache = new Cache(receivedRequest);
            OutputStream fileCacheOutput = cache.getOutputStream();
            int byt;
            boolean noMoreHeader = false;
            StringBuilder header = new StringBuilder();
            try{
                while ((byt = serverReader.read()) != -1) {
                    if(!noMoreHeader) {
                        header.append((char) (byt));
                        if (byt == '\n') {
                            if (header.toString().isBlank()) {
                                noMoreHeader = true;
                            }
                            String newHeader = header.toString();
                            if(newHeader.split(":")[0].equals("ETag"))
                            {
                                noMoreHeader = true;
                                String ETag = newHeader.split(":")[1].substring(1);
                                System.out.println("Data has ETag: " + ETag);
                                Cache.addCache(cache);
                                cache.setETag(ETag);
                            }
                            header = new StringBuilder();
                        }
                    }
                    fileCacheOutput.write(byt);
                    socket.getOutputStream().write(byt);
                }
            }
            catch (SocketException s)
            {
                s.printStackTrace();
                fileCacheOutput.close();
                cache.getFile().delete();
                return;
            }
            fileCacheOutput.close();
            if(cache.getETag() == null)
            {
                cache.getFile().delete();
            }
            //Checking ETag Header:
            socket.getOutputStream().flush();
            serverReader.close();
            out.close();
            connection.close();
            socket.getInputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void handleConnect() throws IOException {
        String [] urlAndPort = fullUrl.replaceFirst("htt((p)|(ps))://" , "").split(":");
        String url = urlAndPort[0];
        int port = Integer.parseInt(urlAndPort[1]);
        InetAddress address = InetAddress.getByName(url);
        Socket proxy = new Socket(address, port);
        String line = "HTTP/1.0 200 Connection established\r\nProxy-Agent: JavaProxy/1.0\r\n\r\n";
        writer.write(line);
        writer.flush();
        DataForwarder forwarder1 = new DataForwarder(socket.getInputStream(), proxy.getOutputStream());
        Thread thread1 = new Thread(forwarder1);
        thread1.start();
        DataForwarder forwarder2 = new DataForwarder(proxy.getInputStream(), socket.getOutputStream());
        Thread thread2 = new Thread(forwarder2);
        thread2.start();
    }
}
