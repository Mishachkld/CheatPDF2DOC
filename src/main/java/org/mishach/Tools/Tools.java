package org.mishach.Tools;

public class Tools {
    public static String generatePathToFile(String attributes, int index, String typeOfFile) {
        return attributes + index + typeOfFile;
    }

    public static String cutString(String string, int cutSize) {
        if (string.length() > cutSize) {
            return string.substring(0, cutSize - 1);
        }
        return string;
    }

    public static boolean isCorrectPath(String path) {
        return path.contains(".pdf");
    }
}
