package org.mishach;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.mishach.Convertor.Convertor;
import org.mishach.PDFScanner.PDFScanner;
import org.mishach.Tools.Tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.mishach.Tools.Constants.*;


public class Main {
    public static void main(String[] args) throws IOException, InvalidFormatException {
        new Convertor(PATH_TO_DOCX_FILE).generateCheat();
    }
}