package org.mishach.PDFScanner;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.mishach.Tools.Tools;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.mishach.Tools.Constants.*;
import static org.mishach.Tools.Tools.isCorrectPath;


public class PDFScanner {

    private static int numberOfPages = 0;  // переименовать в колличествоСтраниц
    private static int indexOfPage = 0; // индекс текущей страницы


    public static int countPageInPDFsFolder(String pdfFolderPath) throws IOException {
        if (numberOfPages == 0) {
            try (Stream<Path> filesStream = Files.walk(Paths.get(pdfFolderPath))) {
                filesStream.forEach(file -> {
                    if (isCorrectPath(file.getFileName().toString())) {
                        try {
                            int pages = countPageInPDF(file.toFile());
                            numberOfPages += pages;
                        } catch (IOException e) {
                            System.out.println("Не удалось посчитать страницы :(");
                        }
                    }
                });
            }
        }
        return numberOfPages;
    }

    public static int countPageInPDF(File file) throws IOException {
        PDDocument document = PDDocument.load(file);
        int pages = document.getNumberOfPages();
        document.close();
        return pages;
    }

    public static int convertPDFPagesToImages(File pdfFile, String pathToSaveImages) { // добавляем в папку temp скрины
        int countOfPages = 0;
        try {
            PDDocument document = PDDocument.load(pdfFile);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            countOfPages = document.getNumberOfPages();
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                String savePath = Tools.generatePathToFile("/page-", pageIndex, ".png");

                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(pageIndex, SCAN_RESOLUTION_DPI);
                setServiceInfoToImage(bufferedImage, pdfFile.getName());
                ImageIO.write(bufferedImage, "PNG", new File(pathToSaveImages + savePath));
            }
            document.close();
        } catch (IOException e) {
            System.out.println("PDFScanner.convertPDFPagesToImages() Файл не конвертируется :( :    " + e);
        }
        return countOfPages;
    }

    private static void setServiceInfoToImage(BufferedImage bim, String pdfName) {
        indexOfPage++;
        pdfName = Tools.cutString(pdfName, 48);
        Graphics graphics = bim.getGraphics();
        setUpGraphics(graphics);
        int xNumberPosition;
        int yNumberPosition;
        int xTextPosition = bim.getWidth() / 4;
        int yTextPosition = bim.getHeight() - 100;
        NumberPositionEnum indexPosition;
        if (indexOfPage % 2 == 1) {
            xNumberPosition = bim.getWidth() / 2 + bim.getWidth() / 4 + 150;
            yNumberPosition = bim.getHeight() - 100;
            indexPosition = NumberPositionEnum.RIGHT;
        } else {
            xNumberPosition = 150;
            yNumberPosition = bim.getHeight() - 100;
            indexPosition = NumberPositionEnum.LEFT;
        }
        String textImageServiceInfo = generateNumberPositionToText(indexPosition); // скорее можно просто удалить
        graphics.drawString(textImageServiceInfo, xNumberPosition, yNumberPosition); // записываем индекс картинки
        graphics.drawString(pdfName, xTextPosition, yTextPosition); // записываем название файла на картинку
    }


    private static String generateNumberPositionToText(NumberPositionEnum numberPosition) {
        StringBuilder builder = new StringBuilder();
        String splitString = "         ";
        switch (numberPosition) {
            case LEFT:
                builder.append(indexOfPage).append(splitString);
                break;
            case RIGHT:
                builder.append(splitString).append(indexOfPage);
                break;
        }
        return builder.toString();
    }

    private static void setUpGraphics(Graphics graphics) {
        Font font = new Font("Calibre", Font.PLAIN, 56);
        graphics.setFont(font);
        graphics.setColor(Color.BLACK);
    }

    private enum NumberPositionEnum {
        RIGHT, LEFT
    }
}
