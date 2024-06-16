package org.mishach.Convertor;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.mishach.PDFScanner.PDFScanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.mishach.Constants.*;


public class Convertor {


    public static void generateCheat(String path) {
        generateDocFile(path);
        scanFolder();

    }

    private static void scanFolder() {
        try (Stream<Path> paths = Files.walk(Paths.get(PATH_TO_PDF_FOLDER))) {
            paths.forEach(streamFile -> {
                int count = PDFScanner.convertPDFPagesToImages(streamFile.toFile());
                writeInCheatFile(PATH_TO_DOCX_FILE, count);
            });
        } catch (IOException e) {
            System.out.println("File not found :(");
        }

    }

    private static void writeInCheatFile(String pathToDocxFile, int countOfFiles) {

        try {
            XWPFDocument docFile = new XWPFDocument(Files.newInputStream(Paths.get(pathToDocxFile)));
            for (int i = 0; i < countOfFiles; i++) {
                XWPFParagraph paragraph = docFile.createParagraph();
                paragraph.createRun().addPicture(Files.newInputStream(Paths.get("temp/page-" + i + ".png")),
                        XWPFDocument.PICTURE_TYPE_PNG,
                        "page-" + i + ".png", Units.toEMU(200), Units.toEMU(400));
                FileOutputStream out = new FileOutputStream(pathToDocxFile);
                docFile.write(out);
                out.close();
            }
            docFile.close();
        } catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private static void deleteDataInTemp(String pathFolder) {
        File folder = new File(pathFolder);
        if (folder.exists() && folder.isDirectory()) {
            try (Stream<Path> paths = Files.walk(Paths.get(pathFolder))) {
                paths.forEach(streamFile -> {
                    System.out.println(streamFile.toFile().getPath());
                    streamFile.toFile().delete();
                });
            } catch (IOException e) {
                System.out.println("Files not not deleted :(");
            }
        }
    }


    private static void generateDocFile(String pathFolder) {
        try {
            FileUtils.touch(new File(pathFolder));
            XWPFDocument fillFile = new XWPFDocument(); // танцы с бубном, чтобы файл создался не пустым
            FileOutputStream tempOut = new FileOutputStream(pathFolder);
            fillFile.write(tempOut);
            tempOut.close();
        } catch (IOException e) {
            System.out.println("Не удалось создать файл :(");
        }

    }

}
