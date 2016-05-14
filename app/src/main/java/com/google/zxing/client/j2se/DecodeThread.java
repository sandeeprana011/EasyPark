package com.google.zxing.client.j2se;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.commons.io.FilenameUtils;

final class DecodeThread extends Thread {
    private final Config config;
    private final Inputs inputs;
    private int successful;

    DecodeThread(Config config, Inputs inputs) {
        this.config = config;
        this.inputs = inputs;
    }

    public void run() {
        while (true) {
            String input = this.inputs.getNextInput();
            if (input != null) {
                File inputFile = new File(input);
                if (inputFile.exists()) {
                    try {
                        if (this.config.isMulti()) {
                            Result[] results = decodeMulti(inputFile.toURI(), this.config.getHints());
                            if (results != null) {
                                this.successful++;
                                if (this.config.isDumpResults()) {
                                    dumpResultMulti(inputFile, results);
                                }
                            }
                        } else {
                            Result result = decode(inputFile.toURI(), this.config.getHints());
                            if (result != null) {
                                this.successful++;
                                if (this.config.isDumpResults()) {
                                    dumpResult(inputFile, result);
                                }
                            }
                        }
                    } catch (IOException e) {
                    }
                } else {
                    try {
                        if (decode(new URI(input), this.config.getHints()) != null) {
                            this.successful++;
                        }
                    } catch (Exception e2) {
                    }
                }
            } else {
                return;
            }
        }
    }

    public int getSuccessful() {
        return this.successful;
    }

    private static void dumpResult(File input, Result result) throws IOException {
        String name = input.getCanonicalPath();
        int pos = name.lastIndexOf(46);
        if (pos > 0) {
            name = name.substring(0, pos);
        }
        writeStringToFile(result.getText(), new File(name + ".txt"));
    }

    private static void dumpResultMulti(File input, Result[] results) throws IOException {
        String name = input.getCanonicalPath();
        int pos = name.lastIndexOf(46);
        if (pos > 0) {
            name = name.substring(0, pos);
        }
        writeResultsToFile(results, new File(name + ".txt"));
    }

    private static void writeStringToFile(String value, File file) throws IOException {
        Writer out = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF8"));
        try {
            out.write(value);
        } finally {
            out.close();
        }
    }

    private static void writeResultsToFile(Result[] results, File file) throws IOException {
        String newline = System.getProperty("line.separator");
        Writer out = new OutputStreamWriter(new FileOutputStream(file), Charset.forName("UTF8"));
        try {
            for (Result result : results) {
                out.write(result.getText());
                out.write(newline);
            }
        } finally {
            out.close();
        }
    }

    private Result decode(URI uri, Map<DecodeHintType, ?> hints) throws IOException {
        try {
            BufferedImage image = ImageIO.read(uri.toURL());
            if (image == null) {
                System.err.println(uri.toString() + ": Could not load image");
                return null;
            }
            try {
                LuminanceSource source;
                if (this.config.getCrop() == null) {
                    source = new BufferedImageLuminanceSource(image);
                } else {
                    int[] crop = this.config.getCrop();
                    source = new BufferedImageLuminanceSource(image, crop[0], crop[1], crop[2], crop[3]);
                }
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                if (this.config.isDumpBlackPoint()) {
                    dumpBlackPoint(uri, image, bitmap);
                }
                Result result = new MultiFormatReader().decode(bitmap, hints);
                if (this.config.isBrief()) {
                    System.out.println(uri.toString() + ": Success");
                    return result;
                }
                ParsedResult parsedResult = ResultParser.parseResult(result);
                System.out.println(uri.toString() + " (format: " + result.getBarcodeFormat() + ", type: " + parsedResult.getType() + "):\nRaw result:\n" + result.getText() + "\nParsed result:\n" + parsedResult.getDisplayResult());
                System.out.println("Found " + result.getResultPoints().length + " result points.");
                for (int i = 0; i < result.getResultPoints().length; i++) {
                    ResultPoint rp = result.getResultPoints()[i];
                    System.out.println("  Point " + i + ": (" + rp.getX() + ',' + rp.getY() + ')');
                }
                return result;
            } catch (NotFoundException e) {
                System.out.println(uri.toString() + ": No barcode found");
                return null;
            }
        } catch (IllegalArgumentException e2) {
            throw new FileNotFoundException("Resource not found: " + uri);
        }
    }

    private Result[] decodeMulti(URI uri, Map<DecodeHintType, ?> hints) throws IOException {
        try {
            BufferedImage image = ImageIO.read(uri.toURL());
            if (image == null) {
                System.err.println(uri.toString() + ": Could not load image");
                return null;
            }
            try {
                LuminanceSource source;
                if (this.config.getCrop() == null) {
                    source = new BufferedImageLuminanceSource(image);
                } else {
                    int[] crop = this.config.getCrop();
                    source = new BufferedImageLuminanceSource(image, crop[0], crop[1], crop[2], crop[3]);
                }
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                if (this.config.isDumpBlackPoint()) {
                    dumpBlackPoint(uri, image, bitmap);
                }
                Result[] results = new GenericMultipleBarcodeReader(new MultiFormatReader()).decodeMultiple(bitmap, hints);
                if (this.config.isBrief()) {
                    System.out.println(uri.toString() + ": Success");
                    return results;
                }
                for (Result result : results) {
                    ParsedResult parsedResult = ResultParser.parseResult(result);
                    System.out.println(uri.toString() + " (format: " + result.getBarcodeFormat() + ", type: " + parsedResult.getType() + "):\nRaw result:\n" + result.getText() + "\nParsed result:\n" + parsedResult.getDisplayResult());
                    System.out.println("Found " + result.getResultPoints().length + " result points.");
                    for (int i = 0; i < result.getResultPoints().length; i++) {
                        ResultPoint rp = result.getResultPoints()[i];
                        System.out.println("  Point " + i + ": (" + rp.getX() + ',' + rp.getY() + ')');
                    }
                }
                return results;
            } catch (NotFoundException e) {
                System.out.println(uri.toString() + ": No barcode found");
                return null;
            }
        } catch (IllegalArgumentException e2) {
            throw new FileNotFoundException("Resource not found: " + uri);
        }
    }

    private static void dumpBlackPoint(URI uri, BufferedImage image, BinaryBitmap bitmap) {
        int y;
        String inputName = uri.getPath();
        if (!inputName.contains(".mono.png")) {
            int offset;
            int x;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int stride = width * 3;
            int[] pixels = new int[(stride * height)];
            int[] argb = new int[width];
            for (y = 0; y < height; y++) {
                image.getRGB(0, y, width, 1, argb, 0, width);
                System.arraycopy(argb, 0, pixels, y * stride, width);
            }
            BitArray bitArray = new BitArray(width);
            for (y = 0; y < height; y++) {
                try {
                    BitArray row = bitmap.getBlackRow(y, row);
                    offset = (y * stride) + width;
                    for (x = 0; x < width; x++) {
                        if (row.get(x)) {
                            pixels[offset + x] = MatrixToImageConfig.BLACK;
                        } else {
                            pixels[offset + x] = -1;
                        }
                    }
                } catch (NotFoundException e) {
                    offset = (y * stride) + width;
                    for (x = 0; x < width; x++) {
                        pixels[offset + x] = -65536;
                    }
                }
            }
            y = 0;
            while (y < height) {
                try {
                    BitMatrix matrix = bitmap.getBlackMatrix();
                    offset = (y * stride) + (width * 2);
                    for (x = 0; x < width; x++) {
                        if (matrix.get(x, y)) {
                            pixels[offset + x] = MatrixToImageConfig.BLACK;
                        } else {
                            pixels[offset + x] = -1;
                        }
                    }
                    y++;
                } catch (NotFoundException e2) {
                }
            }
            writeResultImage(stride, height, pixels, uri, inputName, ".mono.png");
        }
    }

    private static void writeResultImage(int stride, int height, int[] pixels, URI uri, String inputName, String suffix) {
        int pos;
        Throwable th;
        BufferedImage result = new BufferedImage(stride, height, 2);
        result.setRGB(0, 0, stride, height, pixels, 0, stride);
        String resultName = inputName;
        if ("http".equals(uri.getScheme())) {
            pos = resultName.lastIndexOf(47);
            if (pos > 0) {
                resultName = FilenameUtils.EXTENSION_SEPARATOR + resultName.substring(pos);
            }
        }
        pos = resultName.lastIndexOf(46);
        if (pos > 0) {
            resultName = resultName.substring(0, pos);
        }
        resultName = resultName + suffix;
        OutputStream outStream = null;
        try {
            OutputStream outStream2 = new FileOutputStream(resultName);
            try {
                if (!ImageIO.write(result, "png", outStream2)) {
                    System.err.println("Could not encode an image to " + resultName);
                }
                if (outStream2 != null) {
                    try {
                        outStream2.close();
                    } catch (IOException e) {
                        outStream = outStream2;
                        return;
                    }
                }
                outStream = outStream2;
            } catch (FileNotFoundException e2) {
                outStream = outStream2;
                try {
                    System.err.println("Could not create " + resultName);
                    if (outStream != null) {
                        try {
                            outStream.close();
                        } catch (IOException e3) {
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (outStream != null) {
                        try {
                            outStream.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            } catch (IOException e5) {
                outStream = outStream2;
                System.err.println("Could not write to " + resultName);
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (IOException e6) {
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                outStream = outStream2;
                if (outStream != null) {
                    outStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            System.err.println("Could not create " + resultName);
            if (outStream != null) {
                outStream.close();
            }
        } catch (IOException e8) {
            System.err.println("Could not write to " + resultName);
            if (outStream != null) {
                outStream.close();
            }
        }
    }
}
