package org.mishach;

import org.mishach.Convertor.Convertor;
import org.mishach.Convertor.POIConvertor;

import static org.mishach.Tools.Constants.*;

public class Main {
    public static void main(String[] args){
        Convertor convertor = new POIConvertor(PATH_TO_DOCX_FILE, PATH_TO_PDF_FOLDER);
        convertor.convert();
    }
}