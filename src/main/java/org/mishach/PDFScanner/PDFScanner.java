package org.mishach.PDFScanner;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.mishach.Tools.Tools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.mishach.Tools.Constants.*;


public class PDFScanner {

    private static int numbersOfPage = 0;

    public static int countPageInPDFs() throws IOException {
        try(Stream<Path> filesStream = Files.walk(Paths.get(PATH_TO_PDF_FOLDER))){
            filesStream.forEach(file -> {
                try {
                    numbersOfPage += countPageInPDF(file.toFile());
                } catch (IOException e) {
                    System.out.println("Не удалось посчитать страницы :(");
                }
            });
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
                String savePath = Tools.generatePathToFile("/page-", pageIndex ,".png");
                BufferedImage bim = pdfRenderer.renderImageWithDPI(pageIndex, SCAN_DPI);
                ImageIO.write(bim, "PNG", new File(PATH_TO_SAVED_PDF_IMAGES_FILES + savePath));
            }
            document.close();
            return countOfPages;
        } catch (IOException e) {
            System.out.println("PDFScanner.convertPDFPagesToImages() Файл не конвертируется :(" + e);
        }
        return 0;
    }
}
