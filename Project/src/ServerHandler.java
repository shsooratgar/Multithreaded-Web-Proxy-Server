import java.net.Socket;

public class ServerHandler implements Runnable{
    Socket proxy;
    public ServerHandler(Socket proxy) {
        this.proxy = proxy;
    }

    @Override
    public void run() {

    }
}
