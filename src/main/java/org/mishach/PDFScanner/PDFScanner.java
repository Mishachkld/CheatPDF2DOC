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


public class PDFScanner {

    private static int numbersOfPage = 0;  // переименовать в колличествоСтраниц
    private static int indexOfPage = 0; // индекс текущей страницы


    public static int countPageInPDFs() throws IOException {
        if (numbersOfPage == 0) {
            try (Stream<Path> filesStream = Files.walk(Paths.get(PATH_TO_PDF_FOLDER))) {
                filesStream.forEach(file -> {
                    try {
                        int pages = countPageInPDF(file.toFile());
                        numbersOfPage += pages;
                    } catch (IOException e) {
                        System.out.println("Не удалось посчитать страницы :(");
                    }
                });
            }
        }
        return numbersOfPage;
    }

    public static int countPageInPDF(File file) throws IOException {
        PDDocument document = PDDocument.load(file);
        int pages = document.getNumberOfPages();
        document.close();
        return pages;
    }

    public static int convertPDFPagesToImages(File pdfFile) { // добавляем в папку temp скрины
        int countOfPages = 0;
        try {
            PDDocument document = PDDocument.load(pdfFile);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            countOfPages = document.getNumberOfPages();
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                String savePath = Tools.generatePathToFile("/page-", pageIndex, ".png");

                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(pageIndex, SCAN_RESOLUTION_DPI);
                setServiceInfoToImage(bufferedImage, pdfFile.getName());
                ImageIO.write(bufferedImage, "PNG", new File(PATH_TO_SAVED_PDF_IMAGES_FILES + savePath));
            }
            document.close();
        } catch (IOException e) {
            System.out.println("PDFScanner.convertPDFPagesToImages() Файл не конвертируется :( :    " + e);
        }
        return countOfPages;
    }

    private static void setServiceInfoToImage(BufferedImage bim, String fileName) {
        indexOfPage++;
        fileName = cutString(fileName, 48);
        Graphics graphics = bim.getGraphics();
        setUpGraphics(graphics);
        int xPosition;
        int yPosition;
        NumberPositionEnum indexPosition;
        if (indexOfPage % 2 == 1) {
            xPosition = bim.getWidth() / 2 + bim.getWidth() / 4  + 150;
            yPosition = bim.getHeight() - 100;
            indexPosition = NumberPositionEnum.RIGHT;
        } else {
            xPosition = 150;
            yPosition = bim.getHeight() - 100;
            indexPosition = NumberPositionEnum.LEFT;
        }
        String textImageServiceInfo = generateText(indexPosition);
        graphics.drawString(textImageServiceInfo, xPosition, yPosition);
        graphics.drawString(fileName, bim.getWidth() / 4 , bim.getHeight() - 100);
    }

    private static String cutString(String string, int cutSize) {
        if (string.length() > cutSize) {
            return string.substring(0, cutSize - 1);
        }
        return string;
    }

    private static String generateText(NumberPositionEnum numberPosition) {
        StringBuilder builder = new StringBuilder();
        String splitString = "         ";
        switch (numberPosition) {
            case LEFT:
                builder.append(indexOfPage).append(splitString);
                break;
            case RIGHT :
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
