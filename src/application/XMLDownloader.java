package application;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class XMLDownloader implements Runnable {
    String link;
    File out;
    public XMLDownloader (String link, File out) {
        this.link = link;
        this.out = out;
    }

    @Override
    public void run() {

        try {
            URL url = new URL(link);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            BufferedInputStream in = new BufferedInputStream(http.getInputStream());
            FileOutputStream fos = new FileOutputStream(this.out);
            BufferedOutputStream bout = new BufferedOutputStream(fos, 1024);
            byte[] buffer = new byte[1024];
            int read = 0;

            while((read = in.read(buffer, 0, 1024))>= 0) {
                bout.write(buffer, 0, read);
            }

            bout.close();
            in.close();
            System.out.println("Download complete.");
        }catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}