package com.google.zxing;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;

public final class HtmlAssetTranslator {
    private static final Pattern COMMA;

    /* renamed from: com.google.zxing.HtmlAssetTranslator.1 */
    static class C00301 implements FileFilter {
        C00301() {
        }

        public boolean accept(File file) {
            return file.isDirectory() && file.getName().startsWith("html-") && !"html-en".equals(file.getName());
        }
    }

    /* renamed from: com.google.zxing.HtmlAssetTranslator.2 */
    static class C00312 implements FilenameFilter {
        final /* synthetic */ Collection val$filesToTranslate;

        C00312(Collection collection) {
            this.val$filesToTranslate = collection;
        }

        public boolean accept(File dir, String name) {
            return name.endsWith(".html") && (this.val$filesToTranslate.isEmpty() || this.val$filesToTranslate.contains(name));
        }
    }

    static {
        COMMA = Pattern.compile(",");
    }

    private HtmlAssetTranslator() {
    }

    public static void main(String[] args) throws IOException {
        File assetsDir = new File(args[0]);
        Collection<String> languagesToTranslate = parseLanguagesToTranslate(assetsDir, args[1]);
        Collection<String> filesToTranslate = parseFilesToTranslate(args);
        for (String language : languagesToTranslate) {
            translateOneLanguage(assetsDir, language, filesToTranslate);
        }
    }

    private static Collection<String> parseLanguagesToTranslate(File assetsDir, CharSequence languageArg) {
        Collection<String> languages = new ArrayList();
        if ("all".equals(languageArg)) {
            for (File languageDir : assetsDir.listFiles(new C00301())) {
                languages.add(languageDir.getName().substring(5));
            }
        } else {
            languages.addAll(Arrays.asList(COMMA.split(languageArg)));
        }
        return languages;
    }

    private static Collection<String> parseFilesToTranslate(String[] args) {
        Collection<String> fileNamesToTranslate = new ArrayList();
        for (int i = 2; i < args.length; i++) {
            fileNamesToTranslate.add(args[i]);
        }
        return fileNamesToTranslate;
    }

    private static void translateOneLanguage(File assetsDir, String language, Collection<String> filesToTranslate) throws IOException {
        File targetHtmlDir = new File(assetsDir, "html-" + language);
        targetHtmlDir.mkdirs();
        File englishHtmlDir = new File(assetsDir, "html-en");
        String translationTextTranslated = StringsResourceTranslator.translateString("Translated by Google Translate.", language);
        for (File sourceFile : englishHtmlDir.listFiles(new C00312(filesToTranslate))) {
            translateOneFile(language, targetHtmlDir, sourceFile, translationTextTranslated);
        }
    }

    private static void translateOneFile(String language, File targetHtmlDir, File sourceFile, String translationTextTranslated) throws IOException {
        File destFile = new File(targetHtmlDir, sourceFile.getName());
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(sourceFile);
            Element rootElement = document.getDocumentElement();
            rootElement.normalize();
            Queue<Node> nodes = new LinkedList();
            nodes.add(rootElement);
            while (!nodes.isEmpty()) {
                Node node = (Node) nodes.poll();
                if (shouldTranslate(node)) {
                    NodeList children = node.getChildNodes();
                    for (int i = 0; i < children.getLength(); i++) {
                        nodes.add(children.item(i));
                    }
                }
                if (node.getNodeType() == (short) 3) {
                    String text = node.getTextContent();
                    if (text.trim().length() > 0) {
                        text = StringsResourceTranslator.translateString(text, language);
                        node.setTextContent(' ' + text + ' ');
                    }
                }
            }
            Node translateText = document.createTextNode(translationTextTranslated);
            Node paragraph = document.createElement("p");
            paragraph.appendChild(translateText);
            rootElement.getElementsByTagName("body").item(0).appendChild(paragraph);
            try {
                ((DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS")).createLSSerializer().writeToURI(document, destFile.toURI().toString());
            } catch (ClassNotFoundException cnfe) {
                throw new IllegalStateException(cnfe);
            } catch (InstantiationException ie) {
                throw new IllegalStateException(ie);
            } catch (IllegalAccessException iae) {
                throw new IllegalStateException(iae);
            }
        } catch (Throwable pce) {
            throw new IllegalStateException(pce);
        } catch (Throwable sae) {
            throw new IOException(sae);
        }
    }

    private static boolean shouldTranslate(Node node) {
        String textContent;
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            Node classAttribute = attributes.getNamedItem("class");
            if (classAttribute != null) {
                textContent = classAttribute.getTextContent();
                if (textContent != null && textContent.contains("notranslate")) {
                    return false;
                }
            }
        }
        if ("script".equalsIgnoreCase(node.getNodeName())) {
            return false;
        }
        textContent = node.getTextContent();
        if (textContent == null) {
            return false;
        }
        for (int i = 0; i < textContent.length(); i++) {
            if (Character.isLetter(textContent.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
