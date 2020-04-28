package encryptdecrypt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {

    static final String encryptionOption = "enc";
    static final String decryptionOption = "dec";

    static String mode = encryptionOption;
    static int key = 0;
    static String DEFAULT_MESSAGE_VALUE = "";
    static String message = DEFAULT_MESSAGE_VALUE;

    static String inputFile = "";
    static String outputFile = "";

    static EncryptionAlgorithm encryptionAlgorithm = new ShiftEncryptionAlgorithm();

    public static void main(String[] args) {

        try {
            parseCommandLineArgs(args);
        } catch (IOException e) {
            System.out.println(Arrays.asList(args));
            System.out.println("Error: parsing command line arguments.");
            e.printStackTrace();
            return;
        }

        switch (mode) {
            case encryptionOption:
                String encryptedMessage = encryptionAlgorithm.encryptMessage(message, key);
                sendResult(encryptedMessage, outputFile);
                break;
            case decryptionOption:
                String decryptedMessage = encryptionAlgorithm.decryptMessage(message, key);
                sendResult(decryptedMessage, outputFile);
                break;
            default:
                System.out.println("Error: Not supported mode.");
                break;
        }
    }

    private static void sendResult(String message, String outputFile) {
        if (outputFile.isEmpty()) {
            System.out.print(message);
        } else {
            writeProcessedMessageToOutputFile(message);
        }
    }

    private static void parseCommandLineArgs(String[] args) throws IOException {
        for (int i = 0; i < args.length; i += 2) {
            if (args[i].equals("-mode")) {
                mode = args[i + 1];
            } else if (args[i].equals("-alg")) {
                encryptionAlgorithm = assignEncryptionAlgorithm(args[i + 1]);
            } else if (args[i].equals("-key")) {
                key = Integer.valueOf(args[i + 1]);
            } else if (args[i].equals("-in")) {
                inputFile = args[i + 1];
            } else if (args[i].equals("-out")) {
                outputFile = args[i + 1];
            } else if (args[i].equals("-data")) {
                message = args[i + 1];
            }
        }

        areDataAndInputFileArgsPresent();

        if ( ! inputFile.isEmpty()) {
            message = readFileContent(inputFile);
        }
    }

    private static EncryptionAlgorithm assignEncryptionAlgorithm(String selectedAlgorithm) throws IOException {
        EncryptionAlgorithm algorithm;

        switch (selectedAlgorithm) {
            case "shift":
                algorithm = new ShiftEncryptionAlgorithm();
                break;
            case "unicode":
                algorithm = new UnicodeEncryptionAlgorithm();
                break;
            default:
                throw new IOException("Error: it's not allowed to have the '-data' arg and the '-in' arg at the same time.");
        }

        return algorithm;
    }

    private static String readFileContent(String inputFile) throws IOException {
        return Files.readString(Paths.get(inputFile));
    }

    private static void areDataAndInputFileArgsPresent() throws IOException {
        if (! message.equals(DEFAULT_MESSAGE_VALUE) && ! inputFile.isEmpty()) {
            throw new IOException("Error: it's not allowed to have the '-data' arg and the '-in' arg at the same time.");
        }
    }

    private static void writeProcessedMessageToOutputFile(String processedMessage) {
        try (PrintWriter writer = new PrintWriter(outputFile);) {
            writer.println(processedMessage);
        } catch (FileNotFoundException e) {
            System.out.print("Error: writing processing message to output file");
            e.printStackTrace();
        }
    }

}

interface EncryptionAlgorithm {
    String encryptMessage(String textToEncrypt, int key);
    String decryptMessage(String textToEncrypt, int key);
}

class ShiftEncryptionAlgorithm implements EncryptionAlgorithm {

    static final int intValueALowerCase = 'a';
    static final int intValueZLowerCase = 'z';

    static final int intValueAUpperCase = 'A';
    static final int intValueZUpperCase = 'Z';

    @Override
    public String encryptMessage(String textToEncrypt, int key) {
        return encryptDecryptMessage(textToEncrypt, key);
    }

    @Override
    public String decryptMessage(String textToEncrypt, int key) {
        return encryptDecryptMessage(textToEncrypt, key * -1);
    }

    boolean charIsLowerCase(char letter) {
        return (int)letter >= intValueALowerCase && (int)letter <= intValueZLowerCase;
    }

    private char getShifttedLetterWithMaxAtZ(char letter, int shiftValue) {
        char shiftedValue;

        int intValueA = charIsLowerCase(letter) ? intValueALowerCase : intValueAUpperCase ;
        int intValueZ = charIsLowerCase(letter) ? intValueZLowerCase : intValueZUpperCase;

        if ((int)letter + shiftValue > intValueZ) {
            int extraShift = (int) letter + shiftValue - intValueZ;
            shiftedValue = (char) (intValueA - 1 + extraShift);
        } else if ((int)letter + shiftValue < intValueA) {
            int extraShift = (int) letter + shiftValue - intValueA;
            shiftedValue = (char) (intValueZ + 1 + extraShift);
        } else {
            shiftedValue = (char)(letter + shiftValue);
        }
        return (char) shiftedValue;
    }

    private String encryptDecryptMessage(String textToEncrypt, int key) {
        String processedMessage = "";

        for (int i = 0; i < textToEncrypt.length(); i++) {
            char letter = textToEncrypt.charAt(i);

            if (Character.isLetter(letter)) {
                processedMessage += getShifttedLetterWithMaxAtZ(letter, key);
            } else {
                processedMessage += letter;
            }
        }

        return processedMessage;
    }

    private void encryptMessageOnlyLetters(String textToEncrypt, int key) {
        for (int i = 0; i < textToEncrypt.length(); i++) {
            char letter = textToEncrypt.charAt(i);

            if ( Character.isLetter(letter))  {
                System.out.print(getShifttedLetter(letter, key));
            } else {
                System.out.print(letter);
            }
        }
    }

    private char getShifttedLetter(char letter, int shiftValue) {
        char shiftedValue;

        shiftedValue = (char)(letter + shiftValue);

        return (char) shiftedValue;
    }
}

class UnicodeEncryptionAlgorithm implements EncryptionAlgorithm {

    @Override
    public String encryptMessage(String textToEncrypt, int key) {
        return encryptDecryptMessage(textToEncrypt, key);
    }

    @Override
    public String decryptMessage(String textToEncrypt, int key) {
        return encryptDecryptMessage(textToEncrypt, key * -1);
    }

    private String encryptDecryptMessage(String textToEncrypt, int key) {
        String processedMessage = "";

        for (int i = 0; i < textToEncrypt.length(); i++) {
            char letter = textToEncrypt.charAt(i);

            processedMessage += getShifttedLetter(letter, key);
        }

        return processedMessage;
    }

    private char getShifttedLetter(char letter, int shiftValue) {
        char shiftedValue;

        shiftedValue = (char)(letter + shiftValue);

        return (char) shiftedValue;
    }
}

class OppositeLetterEncryptionAlgorithm implements EncryptionAlgorithm {

    static final int intValueA = 'a';
    static final int intValueZ = 'z';

    @Override
    public String encryptMessage(String textToEncrypt, int key) {
        return encryptDecryptMessage(textToEncrypt, key);
    }

    @Override
    public String decryptMessage(String textToEncrypt, int key) {
        return encryptDecryptMessage(textToEncrypt, key * -1);
    }

    private String encryptDecryptMessage(String textToEncrypt, int key) {
        String processedMessage = "";

        for (int i = 0; i < textToEncrypt.length(); i++) {
            char letter = textToEncrypt.charAt(i);

            processedMessage += getOppositeLetter(letter);
        }

        return processedMessage;
    }

    private static char getOppositeLetter(char letter) {
        int oppositeIntValue = getOppositeInIntInterval(letter, intValueA, intValueZ);
        return (char) oppositeIntValue;
    }

    private static int getOppositeInIntInterval(int numericValue, int initialValueInterval, int endValueInterval) {

        // num - initialValueInterval = diff => endValueInterval - diff = opposite
        // 17 - 15 = 2 => 35- 2 = 33
        // 33 - 15 = 18 => 35 - 18 = 17

        // 25 - 15 = 10 => 35 - 10 = 25

        int diffToSubstract = numericValue - initialValueInterval;
        int oppositeValue = endValueInterval - diffToSubstract;
        return oppositeValue;
    }
}



