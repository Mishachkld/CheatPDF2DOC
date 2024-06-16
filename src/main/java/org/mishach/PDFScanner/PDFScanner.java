package org.mishach.PDFScanner;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.mishach.Constants.*;


public class PDFScanner {
    // 1. Передаем путь к папке
    // 2. Метод сохранения
    // Алгоритм такой:
    // 2.1. считываем каждый файл из папки, передаем в фунцию, которая распиливает его на изображения (целиком в папку temp)
    // 2.2. далее располагаем эти файлы в doc для двустороней печати


    public static int convertPDFPagesToImages(File pdfFile) { // добавляем в папку temp скрины
        try {
            PDDocument document = PDDocument.load(pdfFile);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int countOfPages = document.getNumberOfPages();
            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(pageIndex, DPI);
                ImageIO.write(bim, "PNG", new File(PATH_TO_SAVED_PDF_IMAGES_FILES + "/page-" + pageIndex + ".png"));
            }
            document.close();
            return countOfPages;
        } catch (IOException e) {
            System.out.println("PDFScanner.convertPDFPagesToImages() Файл не конвертируется :(");
        }
        return 0;
    }

    public static void readImagesPDF() {

    }

}
