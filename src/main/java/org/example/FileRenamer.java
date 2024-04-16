package org.example;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;

public class FileRenamer {
    public static void renameFilesByTime(String dirPath) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();

        if (files == null) {
            return;
        }

        Arrays.sort(files, Comparator.comparingLong(File::lastModified));

        int i = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                continue;
            }
            if (i == 0) {
                i++;
                continue;
            }

            String ext = getFileExtension(file);
            String newFilename = i + ext;
            File newFile = new File(dirPath, newFilename);

            try {
                Files.move(file.toPath(), newFile.toPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
        }
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf);
    }

    public static void main(String[] args) {
        String directoryPath = args[0];
        renameFilesByTime(directoryPath);
    }
}
