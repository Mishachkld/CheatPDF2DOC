package org.mishach.Convertor;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.mishach.PDFScanner.PDFScanner;
import org.mishach.Tools.Tools;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.mishach.Tools.Constants.*;
import static org.mishach.Tools.Tools.isCorrectPath;


public class POIConvertor implements Convertor {
    private static int countOfUsedPages = 0;
    /**
     * Костыль для подсчета страниц при создании оглавления
     **/
    private static int numeratedPageCounter = 1;
    private final String docxFilePath;
    private final String pdfFolderPath;

    /**
     * @param docxFilePath  путь, где будет рапололгаться DOCX файл
     * @param pdfFolderPath путь, до PDF файлов
     */
    public POIConvertor(String docxFilePath, String pdfFolderPath) {
        this.docxFilePath = docxFilePath;
        this.pdfFolderPath = pdfFolderPath;
    }

    public void generateCheat() {
        generateDocxFile();
        scanFolder(pdfFolderPath);
        numeratePDF(pdfFolderPath);
    }

    private void scanFolder(String pdfFolderPath) {
        try (Stream<Path> paths = Files.walk(Paths.get(pdfFolderPath))) {
            paths.forEach(streamFile -> {
                if (isCorrectPath(streamFile.getFileName().toString())) {
                    int countOfImageFiles = PDFScanner.convertPDFPagesToImages(streamFile.toFile(), PATH_TO_SAVED_PDF_IMAGES_FILES);
                    System.out.println("Converting: " + streamFile.getFileName());
                    try {
                        writeImagesUseSheetsInCheatFile(countOfImageFiles, countOfUsedPages); // костыль просто п
                    } catch (IOException | InvalidFormatException e) {
                        System.out.println("scanFolder() errror" + e);
                    }
                }
            });
        } catch (IOException e) {
            System.out.println("File not found :(");
        }
    }

    synchronized private void writeImagesUseSheetsInCheatFile(int countOfImageFiles, int countOfUsedPages) throws IOException, InvalidFormatException {
        Path pathToDocxFile = Paths.get(PATH_TO_DOCX_FILE);
        XWPFDocument document = new XWPFDocument(Files.newInputStream(pathToDocxFile));
        System.out.println("Adding images to DOCX file....");
        for (int indexPage = 0; indexPage < countOfImageFiles; indexPage++) {
            int indexTable = countOfUsedPages / 8 + countOfUsedPages / 8;
            int indexRow = countOfUsedPages % 8 / 4 % 2;
            int indexColumn = countOfUsedPages % 8 % 4 / 2;
            System.out.println(countOfUsedPages + " " + indexPage); // TODO это нужно убрать
            if (countOfUsedPages % 2 == 1) {
                indexTable++;
                indexColumn = (indexColumn == 1) ? 0 : 1;
            }
            XWPFTable table = document.getTables().get(indexTable);
            XWPFTableCell cell = table.getRows().get(indexRow).getCell(indexColumn);
            loadImageToDoc(document, cell, indexPage);
            countOfUsedPages++;
        }
        document.close();
    }

    private void loadImageToDoc(XWPFDocument document, XWPFTableCell cell, int indexPage) throws IOException, InvalidFormatException {
        String pathImage = Tools.generatePathToFile("temp/page-", indexPage, ".png");
        FileInputStream is = new FileInputStream(pathImage);

        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        XWPFRun run = paragraph.createRun();
        run.addPicture(is, Document.PICTURE_TYPE_PNG, pathImage,
                Units.toEMU(WIDTH_OF_IMAGE_PAGE),
                Units.toEMU(HIGH_OF_IMAGE_PAGE));
        is.close();
        countOfUsedPages++;
        document.write(Files.newOutputStream(Paths.get(docxFilePath)));
    }


    private static void creteTable(XWPFDocument document, int indexTable, int rows, int columns) {
        XWPFTable table = document.createTable(rows, columns);
        table.setWidth("100%");
        table.setCellMargins(100, 0, 0, 0);
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                XWPFParagraph cellParagraph = cell.getParagraphs().get(0);
                switch (indexTable % 2) {
                    case 0:
                        cellParagraph.setAlignment(ParagraphAlignment.RIGHT);
                        break;
                    case 1:
                        cellParagraph.setAlignment(ParagraphAlignment.LEFT);
                        break;
                }
            }
        }
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        paragraph.setPageBreak(true);
    }

    private void generateDocxFile() {
        try {
            FileUtils.touch(new File(docxFilePath)); // файл всегда пересоздается
            XWPFDocument document = new XWPFDocument();
            setMarginsToDocxFile(document);
            addTablesInDocxFile(document);
            FileOutputStream tempOut = new FileOutputStream(docxFilePath);
            document.write(tempOut);
            tempOut.close();
        } catch (IOException e) {
            System.out.println("Не удалось создать файл :( " + e);
        }
    }

    // устанвливаем нулевые отступы
    private void setMarginsToDocxFile(XWPFDocument document) {
        CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
        CTPageMar pageMargin = sectPr.addNewPgMar();
        pageMargin.setLeft(BigInteger.valueOf(0));
        pageMargin.setTop(BigInteger.valueOf(0));
        pageMargin.setRight(BigInteger.valueOf(0));
        pageMargin.setBottom(BigInteger.valueOf(0));
    }

    // добавляется таблица в файл
    private void addTablesInDocxFile(XWPFDocument document) throws IOException {
        int countOfAllPages = PDFScanner.countPageInPDFsFolder(pdfFolderPath);
        int countOfTables = (countOfAllPages / 4); // колличество таблиц
        for (int indexTable = 0; indexTable < countOfTables + 1; indexTable++) {
            creteTable(document, indexTable, 2, 2);
        }
    }

    public static void numeratePDF(String pdfFolderPath) {
        System.out.println("\n" + "---> Оглавление <---");
        try (Stream<Path> stream = Files.walk(Paths.get(pdfFolderPath))) {
            stream.forEach(file -> {
                try {
                    String path = file.getFileName().toString();
                    if (isCorrectPath(path)) {
                        System.out.println(numeratedPageCounter + " -- " + path);
                        numeratedPageCounter += PDFScanner.countPageInPDF(file.toFile());
                    }

                } catch (IOException e) {
                    System.out.println("numeratePDF(). Не получается посчитать страницы: " + e);
                }
            });
        } catch (IOException e) {
            System.out.println("numeratePDF(). Не возможно прочитать файлы в папке: " + e);
        }
    }

    @Override
    public void convert() {

    }
}