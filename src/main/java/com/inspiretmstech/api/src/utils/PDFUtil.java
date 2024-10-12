package com.inspiretmstech.api.src.utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class PDFUtil {

    public static PDDocument merge(List<byte[]> data) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        PDDocument merged = new PDDocument();

        for (byte[] uri : data) {
            PDDocument pdf = Loader.loadPDF(uri);
            merger.appendDocument(merged, pdf);
        }

        return merged;
    }

    public static PDDocument merge(byte[][] data) throws IOException {
        return PDFUtil.merge(Arrays.asList(data));
    }

    /**
     * Merge a list of PDFs, where each PDF is base64 data (or a base64 data URI)
     *
     * @param uris the list of pdfs to merge
     * @return the merged PDF
     * @throws IOException decoding error
     */
    public static PDDocument merge(String[] uris) throws IOException {
        List<byte[]> data = new ArrayList<>();
        for (String uri : uris) data.add(PDFUtil.decode(uri));
        return PDFUtil.merge(data);
    }

    public static PDDocument mergeFrom(List<URL> urls) throws IOException {
        List<byte[]> data = new ArrayList<>();
        for (URL url : urls) data.add(PDFUtil.decode(url));
        return PDFUtil.merge(data);
    }


    /**
     * Convert base64 data (either as a raw string or as a data uri) into a byte[]
     *
     * @param uri the data to decode
     * @return the decoded byte[]
     */
    private static byte[] decode(String uri) {

        // strip off the mime-type, if exists
        int commaIndex = uri.indexOf(',');
        if (commaIndex != -1) uri = uri.substring(commaIndex);

        // Decode the Base64 data to a byte array
        return Base64.getDecoder().decode(uri);
    }


    public static byte[] decode(URL url) throws IOException {
        try (InputStream inputStream = url.openStream();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) byteArrayOutputStream.write(buffer, 0, bytesRead);

            byteArrayOutputStream.close();
            return byteArrayOutputStream.toByteArray();
        }
    }

    public static String convertToBase64(PDDocument doc) throws IOException {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        doc.save(outputStream);
        doc.close();

        byte[] pdfBytes = outputStream.toByteArray();
        return Base64.getEncoder().encodeToString(pdfBytes);
    }

    public static String convertToBase64URI(PDDocument doc) throws IOException {
        return "data:application/pdf;base64," + PDFUtil.convertToBase64(doc);
    }


}
