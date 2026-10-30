package br.uefs.umleditor.persistence;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * The most recently opened or saved diagram files, most recent first,
 * kept between executions (File > Recent Files, RF02).
 */
public class RecentFiles {

    public static final int MAX_FILES = 8;
    private static final String KEY = "recentFiles";
    private static final String SEPARATOR = "\n";

    private final Preferences store;
    private final List<Path> files = new ArrayList<>();

    public RecentFiles(Preferences store) {
        this.store = store;
        for (String entry : store.get(KEY, "").split(SEPARATOR)) {
            if (!entry.isBlank()) {
                files.add(Path.of(entry));
            }
        }
    }

    public List<Path> getFiles() {
        return List.copyOf(files);
    }

    public void add(Path file) {
        Path normalized = file.toAbsolutePath().normalize();
        files.remove(normalized);
        files.add(0, normalized);
        while (files.size() > MAX_FILES) {
            files.remove(files.size() - 1);
        }
        save();
    }

    public void remove(Path file) {
        files.remove(file.toAbsolutePath().normalize());
        save();
    }

    private void save() {
        store.put(KEY, String.join(SEPARATOR, files.stream().map(Path::toString).toList()));
    }
}
