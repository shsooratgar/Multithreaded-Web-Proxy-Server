import java.io.*;
import java.util.ArrayList;
import java.util.UUID;

public class Cache {
    private String request;
    private String ETag;
    private File file;
    private static ArrayList<Cache> caches = new ArrayList<>();

    public Cache(String request) {
        this.request = request;
        file = new File(UUID.randomUUID().toString());
    }

    public static Cache find(String receivedRequest) {
        for(Cache cache : caches)
        {
            if(cache.request.equals(receivedRequest))
                return cache;
        }
        return null;
    }

    public String getETag() {
        return ETag;
    }

    public void setETag(String ETag) {
        this.ETag = ETag;
    }

    public static void addCache(Cache cache) {
        caches.add(cache);
    }

    public OutputStream getOutputStream() throws FileNotFoundException {
        return new FileOutputStream(file);
    }

    public InputStream getInputStream() throws FileNotFoundException {
        return new FileInputStream(file);
    }

    public File getFile() {
        return file;
    }
}
