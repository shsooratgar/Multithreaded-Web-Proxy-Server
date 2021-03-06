import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataForwarder implements Runnable{
    private final InputStream input;
    private final OutputStream output;

    public DataForwarder(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[4096];
            int read = 1;
            while (read >= 0) {
                read = input.read(buffer);
                if (read > 0) {
                    output.write(buffer, 0, read);
                    if (input.available() < 1) {
                        output.flush();
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }
}
