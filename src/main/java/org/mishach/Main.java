package org.mishach;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.mishach.Convertor.Convertor;

import java.io.*;

import static org.mishach.Constants.*;


public class Main {

    /// Считываем содержимое папки и в ней по очереди открываем каждый файл

    private static final String PATH_TO_FILE_EXAMPLE = "src/main/resources/big-example.pdf";

    public static void main(String[] args) throws IOException, InvalidFormatException {
//        PDFScanner.convertPDFPagesToImages(new File(PATH_TO_FILE_EXAMPLE));
        Convertor.generateCheat(PATH_TO_DOCX_FILE);





    }
}