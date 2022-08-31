package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Main.class.getName());
        Properties prop = new Properties();


        try (var fileInputStream = new FileInputStream(args[0])){
            prop.load(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String mode = prop.getProperty("mode");
        String suffix = prop.getProperty("suffix");
        String files = prop.getProperty("files");

        if (!mode.equalsIgnoreCase("move") && !mode.equalsIgnoreCase("copy")) {
            logger.log(Level.SEVERE, "Mode is not recognized: " + mode);
        }
        if (suffix == null || suffix.isBlank()) {
            logger.log(Level.SEVERE, "No suffix is configured");
        }
        if (files == null || files.isBlank()) {
            logger.log(Level.WARNING, "No files are configured to be copied/moved");
            files = "";
        }

        for (String file: files.split(":(?!\\))")) { //split (":") won't work if have path C:\\...
            if (file == null || file.isBlank()) {
                logger.log(Level.WARNING, "No files are configured to be copied/moved");
                continue;
            }
            File f = new File(file);
            if (!f.exists()) {
                file = file.replace("\\", "/");
                logger.log(Level.SEVERE, "No such file: " + file);
                continue;
            }
            if (mode.equalsIgnoreCase("copy".trim())) {
                int at = file.lastIndexOf(".");
                String newFile = file.substring(0, at) +
                        suffix + file.substring(at);
                File dest = new File(newFile);

                try {
                    Files.copy(f.toPath(), dest.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                logger.log(Level.INFO, file.replace("\\", "/") + " -> " +
                        newFile.replace("\\", "/"));

            }
            if (mode.equalsIgnoreCase("move".trim())) {
                int at = file.lastIndexOf(".");
                int last = file.lastIndexOf("\\");
                String newFile = file.substring(0, at) +
                        suffix + file.substring(at);
                File dest = new File(newFile);

                try {
                    Files.move(f.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                logger.log(Level.INFO, file.replace("\\", "/") + " => " +
                        newFile.replace("\\", "/"));
            }
        }
    }
}