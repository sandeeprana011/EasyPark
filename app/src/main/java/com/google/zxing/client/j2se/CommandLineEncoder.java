package com.google.zxing.client.j2se;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import java.io.File;
import java.util.Locale;
import org.apache.commons.io.FilenameUtils;

public final class CommandLineEncoder {
    private static final BarcodeFormat DEFAULT_BARCODE_FORMAT;
    private static final int DEFAULT_HEIGHT = 300;
    private static final String DEFAULT_IMAGE_FORMAT = "PNG";
    private static final String DEFAULT_OUTPUT_FILE = "out";
    private static final int DEFAULT_WIDTH = 300;

    static {
        DEFAULT_BARCODE_FORMAT = BarcodeFormat.QR_CODE;
    }

    private CommandLineEncoder() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            printUsage();
            return;
        }
        BarcodeFormat barcodeFormat = DEFAULT_BARCODE_FORMAT;
        String imageFormat = DEFAULT_IMAGE_FORMAT;
        String outFileString = DEFAULT_OUTPUT_FILE;
        int width = DEFAULT_WIDTH;
        int height = DEFAULT_WIDTH;
        for (String arg : args) {
            if (arg.startsWith("--barcode_format")) {
                barcodeFormat = BarcodeFormat.valueOf(arg.split("=")[1]);
            } else if (arg.startsWith("--image_format")) {
                imageFormat = arg.split("=")[1];
            } else if (arg.startsWith("--output")) {
                outFileString = arg.split("=")[1];
            } else if (arg.startsWith("--width")) {
                width = Integer.parseInt(arg.split("=")[1]);
            } else if (arg.startsWith("--height")) {
                height = Integer.parseInt(arg.split("=")[1]);
            }
        }
        if (DEFAULT_OUTPUT_FILE.equals(outFileString)) {
            outFileString = outFileString + FilenameUtils.EXTENSION_SEPARATOR + imageFormat.toLowerCase(Locale.ENGLISH);
        }
        String contents = null;
        for (String arg2 : args) {
            if (!arg2.startsWith("--")) {
                contents = arg2;
                break;
            }
        }
        if (contents == null) {
            printUsage();
        } else {
            MatrixToImageWriter.writeToFile(new MultiFormatWriter().encode(contents, barcodeFormat, width, height), imageFormat, new File(outFileString));
        }
    }

    private static void printUsage() {
        System.err.println("Encodes barcode images using the ZXing library\n");
        System.err.println("usage: CommandLineEncoder [ options ] content_to_encode");
        System.err.println("  --barcode_format=format: Format to encode, from BarcodeFormat class. Not all formats are supported. Defaults to QR_CODE.");
        System.err.println("  --image_format=format: image output format, such as PNG, JPG, GIF. Defaults to PNG");
        System.err.println("  --output=filename: File to write to. Defaults to out.png");
        System.err.println("  --width=pixels: Image width. Defaults to 300");
        System.err.println("  --height=pixels: Image height. Defaults to 300");
    }
}
