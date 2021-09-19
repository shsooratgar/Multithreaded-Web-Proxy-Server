import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Proxy {
    private static Proxy proxy;
    private ServerSocket serverSocket;
    private ArrayList<String> filters;
//    private

    public static Proxy getInstance() {
        return proxy;
    }

    public Proxy(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        filters = new ArrayList<>();
        proxy = this;
    }

    public void start() {
        new AdminFrame();
        while(true)
        {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("New Request Received");
                RequestHandler handler = new RequestHandler(socket);
                Thread thread = new Thread(handler);
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<String> getFilters() {
        return filters;
    }

    public boolean isFilter(String fullUrl) {
        String address = fullUrl.replaceFirst("htt((p)|(ps))://(www.)*","");
        for(String filter : filters)
        {
            if(fullUrl.contains(filter)) {

                return true;
            }
        }
        return false;
    }
}
