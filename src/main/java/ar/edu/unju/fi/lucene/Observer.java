package ar.edu.unju.fi.lucene;

import com.vaadin.flow.component.UI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;

public class Observer implements Runnable {
    Lucene lucene;
    UI currentUI = UI.getCurrent();

    public Observer(Lucene lucene) {
        this.lucene = lucene;
    }

    @Override
    public void run() {
        try {
            String pathString = "";
            try (BufferedReader br = new BufferedReader(new FileReader(LuceneConstant.LAST_PATH))) {
                pathString = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Path path = Paths.get(pathString);

            WatchService watchService = FileSystems.getDefault().newWatchService();
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);

            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                        currentUI.access(() -> {
                            try {
                                lucene.updateIndex(event.context().toString());
                            } catch (IOException ignored) {
                            }
                        });
                        System.out.println("Archivo agregado: " + event.context());
                    } else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                        currentUI.access(() -> {
                            try {
                                lucene.start();
                            } catch (IOException ignored) {
                            }
                        });
                        System.out.println("Archivo eliminado: " + event.context());
                    }
                }
                key.reset();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException ignored) { }
    }
}