package com.google.zxing;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringsResourceTranslator {
    private static final String APACHE_2_LICENSE = "<!--\n Copyright (C) 2011 ZXing authors\n\n Licensed under the Apache License, Version 2.0 (the \"License\");\n you may not use this file except in compliance with the License.\n You may obtain a copy of the License at\n\n      http://www.apache.org/licenses/LICENSE-2.0\n\n Unless required by applicable law or agreed to in writing, software\n distributed under the License is distributed on an \"AS IS\" BASIS,\n WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n See the License for the specific language governing permissions and\n limitations under the License.\n -->\n";
    private static final String API_KEY = "INSERT-YOUR-KEY";
    private static final Pattern ENTRY_PATTERN;
    private static final Map<String, String> LANGUAGE_CODE_MASSAGINGS;
    private static final Pattern STRINGS_FILE_NAME_PATTERN;
    private static final Pattern TRANSLATE_RESPONSE_PATTERN;
    private static final Charset UTF8;
    private static final Pattern VALUES_DIR_PATTERN;

    /* renamed from: com.google.zxing.StringsResourceTranslator.1 */
    static class C00331 implements FileFilter {
        C00331() {
        }

        public boolean accept(File file) {
            return file.isDirectory() && StringsResourceTranslator.VALUES_DIR_PATTERN.matcher(file.getName()).matches();
        }
    }

    static {
        UTF8 = Charset.forName("UTF-8");
        ENTRY_PATTERN = Pattern.compile("<string name=\"([^\"]+)\".*>([^<]+)</string>");
        STRINGS_FILE_NAME_PATTERN = Pattern.compile("values-(.+)");
        TRANSLATE_RESPONSE_PATTERN = Pattern.compile("translatedText\":\\s*\"([^\"]+)\"");
        VALUES_DIR_PATTERN = Pattern.compile("values-[a-z]{2}(-[a-zA-Z]{2,3})?");
        LANGUAGE_CODE_MASSAGINGS = new HashMap(3);
        LANGUAGE_CODE_MASSAGINGS.put("zh-rCN", "zh-cn");
        LANGUAGE_CODE_MASSAGINGS.put("zh-rTW", "zh-tw");
    }

    private StringsResourceTranslator() {
    }

    public static void main(String[] args) throws IOException {
        File resDir = new File(args[0]);
        File stringsFile = new File(new File(resDir, "values"), "strings.xml");
        Collection<String> forceRetranslation = Arrays.asList(args).subList(1, args.length);
        for (File translatedValuesDir : resDir.listFiles(new C00331())) {
            translate(stringsFile, new File(translatedValuesDir, "strings.xml"), forceRetranslation);
        }
    }

    private static void translate(File englishFile, File translatedFile, Collection<String> forceRetranslation) throws IOException {
        Throwable th;
        SortedMap<String, String> english = readLines(englishFile);
        SortedMap<String, String> translated = readLines(translatedFile);
        String parentName = translatedFile.getParentFile().getName();
        Matcher stringsFileNameMatcher = STRINGS_FILE_NAME_PATTERN.matcher(parentName);
        stringsFileNameMatcher.find();
        String language = stringsFileNameMatcher.group(1);
        String massagedLanguage = (String) LANGUAGE_CODE_MASSAGINGS.get(language);
        if (massagedLanguage != null) {
            language = massagedLanguage;
        }
        System.out.println("Translating " + language);
        File resultTempFile = File.createTempFile(parentName, ".xml");
        boolean anyChange = false;
        Writer out = null;
        try {
            Writer out2 = new OutputStreamWriter(new FileOutputStream(resultTempFile), UTF8);
            try {
                out2.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                out2.write(APACHE_2_LICENSE);
                out2.write("<resources>\n");
                for (Entry<String, String> englishEntry : english.entrySet()) {
                    String key = (String) englishEntry.getKey();
                    String value = (String) englishEntry.getValue();
                    out2.write("  <string name=\"");
                    out2.write(key);
                    out2.write(34);
                    if (value.contains("%s") || value.contains("%f")) {
                        out2.write(" formatted=\"false\"");
                    }
                    out2.write(62);
                    String translatedString = (String) translated.get(key);
                    if (translatedString == null || forceRetranslation.contains(key)) {
                        anyChange = true;
                        translatedString = translateString(value, language);
                    }
                    out2.write(translatedString);
                    out2.write("</string>\n");
                }
                out2.write("</resources>\n");
                out2.flush();
                quietClose(out2);
                if (anyChange) {
                    System.out.println("  Writing translations");
                    translatedFile.delete();
                    resultTempFile.renameTo(translatedFile);
                }
            } catch (Throwable th2) {
                th = th2;
                out = out2;
                quietClose(out);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            quietClose(out);
            throw th;
        }
    }

    static String translateString(String english, String language) throws IOException {
        if ("en".equals(language)) {
            return english;
        }
        String massagedLanguage = (String) LANGUAGE_CODE_MASSAGINGS.get(language);
        if (massagedLanguage != null) {
            language = massagedLanguage;
        }
        System.out.println("  Need translation for " + english);
        CharSequence translateResult = fetch(new URL("https://www.googleapis.com/language/translate/v2?key=INSERT-YOUR-KEY&q=" + URLEncoder.encode(english, "UTF-8") + "&source=en&target=" + language));
        Matcher m = TRANSLATE_RESPONSE_PATTERN.matcher(translateResult);
        if (m.find()) {
            String translation = m.group(1);
            System.out.println("  Got translation " + translation);
            return translation.replaceAll("\\\\u0026quot;", "\"").replaceAll("\\\\u0026#39;", "'").replaceAll("\\\\u200b", "").replaceAll("&amp;quot;", "\"").replaceAll("&amp;#39;", "'");
        }
        System.err.println("No translate result");
        System.err.println(translateResult);
        return english;
    }

    private static CharSequence fetch(URL translateURL) throws IOException {
        Throwable th;
        URLConnection connection = translateURL.openConnection();
        connection.connect();
        StringBuilder translateResult = new StringBuilder(200);
        Reader in = null;
        try {
            Reader in2 = new InputStreamReader(connection.getInputStream(), UTF8);
            try {
                char[] buffer = new char[AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT];
                while (true) {
                    int charsRead = in2.read(buffer);
                    if (charsRead > 0) {
                        translateResult.append(buffer, 0, charsRead);
                    } else {
                        quietClose(in2);
                        return translateResult;
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                in = in2;
                quietClose(in);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            quietClose(in);
            throw th;
        }
    }

    private static SortedMap<String, String> readLines(File file) throws IOException {
        Throwable th;
        SortedMap<String, String> entries = new TreeMap();
        BufferedReader reader = null;
        try {
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(file), UTF8));
            while (true) {
                try {
                    String line = reader2.readLine();
                    if (line != null) {
                        Matcher m = ENTRY_PATTERN.matcher(line);
                        if (m.find()) {
                            entries.put(m.group(1), m.group(2));
                        }
                    } else {
                        quietClose(reader2);
                        return entries;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    reader = reader2;
                }
            }
        } catch (Throwable th3) {
            th = th3;
            quietClose(reader);
            throw th;
        }
    }

    private static void quietClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }
}
