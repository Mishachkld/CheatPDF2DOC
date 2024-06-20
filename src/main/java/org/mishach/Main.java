package org.mishach;

import org.mishach.Convertor.Convertor;

import static org.mishach.Tools.Constants.*;

public class Main {
    public static void main(String[] args){
        new Convertor(PATH_TO_DOCX_FILE, PATH_TO_PDF_FOLDER).generateCheat();
    }
}