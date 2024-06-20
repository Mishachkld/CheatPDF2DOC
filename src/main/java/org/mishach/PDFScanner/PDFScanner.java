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
    private static int indexOfPage = 1;

    public static int countPageInPDFs() throws IOException {
        if (numbersOfPage == 0) {
            try (Stream<Path> filesStream = Files.walk(Paths.get(PATH_TO_PDF_FOLDER))) {
                filesStream.forEach(file -> {
                    try {
                        numbersOfPage += countPageInPDF(file.toFile());
                    } catch (IOException e) {
                        System.out.println("Не удалось посчитать страницы :(");
                    }
                });
            }
        }
        return numbersOfPage;
    }

    private static int countPageInPDF(File file) throws IOException {
        return PDDocument.load(file).getNumberOfPages();
    }

    public static int convertPDFPagesToImages(File pdfFile) { // добавляем в папку temp скрины
        try {
            PDDocument document = PDDocument.load(pdfFile);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int countOfPages = document.getNumberOfPages();
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                String savePath = Tools.generatePathToFile("/page-", pageIndex, ".png");
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(pageIndex, SCAN_DPI);
                getServiceInfoToImage(bufferedImage, pdfFile.getName());
                ImageIO.write(bufferedImage, "PNG", new File(PATH_TO_SAVED_PDF_IMAGES_FILES + savePath));
            }
            document.close();
            return countOfPages;
        } catch (IOException e) {
            System.out.println("PDFScanner.convertPDFPagesToImages() Файл не конвертируется :(" + e);
        }
        return 0;
    }

    private static void getServiceInfoToImage(BufferedImage bim, String fileName) {
        Graphics graphics = bim.getGraphics();
        setUpGraphics(graphics);
        int xPosition;
        int yPosition;
        TextPositionEnum textPosition;
        if (indexOfPage % 2 == 1) {
            xPosition = bim.getWidth() / 2 + bim.getWidth() / 4;
            yPosition = bim.getHeight() - 100;
            textPosition = TextPositionEnum.LEFT;
        } else {
            xPosition = 150;
            yPosition = bim.getHeight() - 100;
            textPosition = TextPositionEnum.RIGHT;

        }
        String textImageServiceInfo = generateText(fileName, textPosition);
        graphics.drawString(textImageServiceInfo, xPosition, yPosition);
        indexOfPage++;
    }

    private static String generateText(String fileName, TextPositionEnum textPosition) {
        StringBuilder builder = new StringBuilder();
        String splitString = "         ";
        switch (textPosition) {
            case RIGHT:
                builder.append(indexOfPage).append(splitString).append(fileName);
                break;
            case LEFT:
                builder.append(fileName).append(splitString).append(indexOfPage);
                break;
        }
        return builder.toString();
    }

    private static void setUpGraphics(Graphics graphics) {
        Font font = new Font("Calibre", Font.PLAIN, 48);
        graphics.setFont(font);
        graphics.setColor(Color.BLACK);
    }

    private enum TextPositionEnum {
        RIGHT, LEFT
    }
}
