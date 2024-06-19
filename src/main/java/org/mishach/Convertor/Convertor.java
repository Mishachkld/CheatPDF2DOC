package org.mishach.Convertor;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.mishach.PDFScanner.PDFScanner;
import org.mishach.Tools.Tools;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.mishach.Tools.Constants.*;


public class Convertor {
    private static int countOfUsedPages = 0;
    private final String docxFilePath;
    private List<Boolean> isAddedPageInTable = new ArrayList<>(Collections.nCopies(8, false));

    public Convertor(String docxFilePath) {
        this.docxFilePath = docxFilePath;
    }

    public void generateCheat() {
        generateDocFile();
        scanFolder();
    }

    private void scanFolder() {

        try (Stream<Path> paths = Files.walk(Paths.get(PATH_TO_PDF_FOLDER))) {
            paths.forEach(streamFile -> {
                int countOfPage = PDFScanner.convertPDFPagesToImages(streamFile.toFile());
                try {

                    writePagesUseSheetsInCheatFile(countOfPage, countOfUsedPages);
                } catch (IOException | InvalidFormatException e) {
                    System.out.println("scanFolder() errror" + e);
                }
            });
        } catch (IOException e) {
            System.out.println("File not found :(");
        }
    }


    // если (indexUsedPage % 2 == 0):
    //  Рассматриваем (tableIndex + 1), где tableIndex = (indexUsedPages / 8) (возможно даже не придется так делать)
    //  Смотрим ячейку cell.get(tableRow.size() - 1) for(i = 0; ; i++) идем по порядку, сверху вниз
    //                 cell.get(tableRow.size() - 1) for (j = tableRow.size(); ; j--) идем справа налево
    // если (indexPage % 2 == 1):
    //  Рассматриваем (tableIndex) и идем просто по порядку
    // Лучше написать функцию fillTable()
    synchronized private void writePagesUseSheetsInCheatFile(int countOfFiles, int countOfUsedPages) throws IOException, InvalidFormatException {
        Path pathToDocxFile = Paths.get(PATH_TO_DOCX_FILE);
        XWPFDocument document = new XWPFDocument(Files.newInputStream(pathToDocxFile));
        for (int indexPage = 0; indexPage < countOfFiles; indexPage++) {
            int indexTable = countOfUsedPages / 8 + countOfUsedPages / 8;
            int indexRow = countOfUsedPages % 8 / 4 % 2;
            int indexColumn = countOfUsedPages % 8 % 4 / 2;
            System.out.println(countOfUsedPages + " " + indexPage);
            if (countOfUsedPages % 2 == 1) {
                indexTable++;
                indexColumn = indexColumn == 1 ? 0 : 1;
            }
            XWPFTable table = document.getTables().get(indexTable);
            XWPFTableCell cell = table.getRows().get(indexRow).getCell(indexColumn);
            countOfUsedPages++;
            loadImageToDoc(document, cell, indexPage);
        }
        document.close();
    }

    private int findIndexOfEmptyCell() {
        int countAddedFileInTables = 0;
        int index = 0;
        for (int indexArray = 0; indexArray < isAddedPageInTable.size(); indexArray++) {
            if (isAddedPageInTable.get(indexArray)) {
                countAddedFileInTables++;
                if (countAddedFileInTables == 8) {
                    resetBooleanArray();
                    break;
                }
            } else {
                index = indexArray;
                isAddedPageInTable.set(index, true);
                break;
            }
        }
        return index;
    }
 /* if (!(cell.getParagraphs().size() == 1 && cell.getParagraphs().get(0).getRuns().isEmpty())) {
                indexTable++;
            }*/

    private void loadImageToDoc(XWPFDocument document, XWPFTableCell cell, int indexPage) throws IOException, InvalidFormatException {
        String pathImage = Tools.generatePathToFile("temp/page-", indexPage, ".png");
        FileInputStream is = new FileInputStream(pathImage);

        XWPFParagraph paragraph = cell.addParagraph();
        XWPFRun run = paragraph.createRun();
        run.addPicture(is, Document.PICTURE_TYPE_PNG, pathImage,
                Units.toEMU(WIDTH_OF_IMAGE_PAGE),
                Units.toEMU(HIGH_OF_IMAGE_PAGE));
        is.close();
        countOfUsedPages++;
        document.write(Files.newOutputStream(Paths.get(docxFilePath)));
    }

    private void resetBooleanArray() {
        isAddedPageInTable = new ArrayList<>(Collections.nCopies(8, false));
    }

    private static void creteTableInDocument(XWPFDocument document, int rows, int columns) {
        XWPFTable table = document.createTable(rows, columns);
        table.setTableAlignment(TableRowAlign.CENTER);
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        paragraph.setPageBreak(true);
    }

    private void generateDocFile() {
        try {
            FileUtils.touch(new File(docxFilePath));
            XWPFDocument document = new XWPFDocument();
            int countOfAllPages = PDFScanner.countPageInPDFs();
            for (int i = 0; i < (countOfAllPages / 4) + 2; i++) {
                creteTableInDocument(document, 2, 2);
            }
            FileOutputStream tempOut = new FileOutputStream(docxFilePath);
            document.write(tempOut);
            tempOut.close();
        } catch (IOException e) {
            System.out.println("Не удалось создать файл :(");
        }
    }

    private void addImagesToDocument(int countOfFiles) throws IOException, InvalidFormatException {
        XWPFDocument document = new XWPFDocument(Files.newInputStream(Paths.get(PATH_TO_DOCX_FILE)));
        for (int indexPage = 0; indexPage < countOfFiles; indexPage++) {
            if (countOfUsedPages % 4 == 0) {
                XWPFParagraph paragraph = document.createParagraph();
                paragraph.setPageBreak(true);
            }
            XWPFTable table;
            if (countOfUsedPages % 4 == 0) {
                table = document.createTable(2, 2);
            } else {
                table = document.getTables().get(document.getTables().size() - 1);
            }

            int row = (countOfUsedPages % 4) / 2;
            int col = (countOfUsedPages % 4) % 2;

            XWPFTableCell cell = table.getRow(row).getCell(col);
            XWPFParagraph cellParagraph = cell.addParagraph();
            XWPFRun run = cellParagraph.createRun();

            String pathImage = Tools.generatePathToFile("temp/page-", indexPage, ".png");
            FileInputStream is = new FileInputStream(pathImage);
            run.addPicture(is,
                    XWPFDocument.PICTURE_TYPE_PNG,
                    pathImage,
                    Units.toEMU(WIDTH_OF_IMAGE_PAGE), Units.toEMU(HIGH_OF_IMAGE_PAGE)); // Adjust the image size as needed
            is.close();

            FileOutputStream out = new FileOutputStream(PATH_TO_DOCX_FILE);
            document.write(out);
            out.close();
            countOfUsedPages++;
        }
        document.close();
    }

    private static void writePagesUseParagraphsInCheatFile(String pathToDocxFile, int countOfFiles) {
        try {
            XWPFDocument docFile = new XWPFDocument(Files.newInputStream(Paths.get(pathToDocxFile)));
            for (int i = 0; i < countOfFiles; i++) {
                String pathToSavedImages = Tools.generatePathToFile("page-", i, ".png");

                XWPFParagraph paragraph = docFile.createParagraph();
                paragraph.createRun().addPicture(Files.newInputStream(Paths.get("temp/" + pathToSavedImages)),
                        XWPFDocument.PICTURE_TYPE_PNG,
                        pathToSavedImages, Units.toEMU(WIDTH_OF_IMAGE_PAGE), Units.toEMU(HIGH_OF_IMAGE_PAGE));
                FileOutputStream out = new FileOutputStream(pathToDocxFile);
                docFile.write(out);
                out.close();
            }
            docFile.close();
        } catch (IOException | InvalidFormatException e) {
            throw new RuntimeException(e);
        }
    }

}
