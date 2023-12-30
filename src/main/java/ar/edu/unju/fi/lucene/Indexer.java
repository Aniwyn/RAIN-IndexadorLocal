package ar.edu.unju.fi.lucene;

import com.vaadin.flow.component.notification.Notification;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

public class Indexer {
    public IndexWriter writer;
    public Indexer(String indexStoringPath) throws IOException {
        Directory indexDirectory = FSDirectory.open(Paths.get(indexStoringPath));
        IndexWriterConfig iwc = new IndexWriterConfig(LuceneConstant.analyzer);
        iwc.setRAMBufferSizeMB(512.0);
        writer = new IndexWriter(indexDirectory, iwc);
    }
    public void close() throws IOException {
        writer.close();
    }

    public int createIndex(String dataDirPath, FileFilter filter) throws IOException{
        File[] files = new File(dataDirPath).listFiles();

        for (File file : Objects.requireNonNull(files)) {
            if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead()) {
                if (filter.accept(file)) {
                    indexFile(file);
                }
            }
        }
        Notification.show("¡Listo!",3000, Notification.Position.BOTTOM_CENTER);
        return writer.getDocStats().numDocs;
    }

    public int updateIndex(String filePath, FileFilter filter) throws IOException{
        filePath = filePath.replace("/", "\\");
        File file = new File(filePath);
        if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead()) {
            if (filter.accept(file)) {
                indexFile(file);
            }
        }
        Notification.show("¡Listo!",3000, Notification.Position.BOTTOM_CENTER);
        return writer.getDocStats().numDocs;
    }

    private void indexFile(File file) throws IOException {
        Document document = getDocument(file);
        writer.addDocument(document);
    }

    private Document getDocument(File file) throws IOException{
        String content = extractText(file);

        Document lucene_document = new Document();
        TextField contentField = new TextField(LuceneConstant.CONTENTS, content, Field.Store.YES);
        TextField fileNameField = new TextField(LuceneConstant.FILE_NAME, file.getName(), Field.Store.YES);
        TextField filePathField = new TextField(LuceneConstant.FILE_PATH,file.getCanonicalPath(),Field.Store.YES);
        lucene_document.add(contentField);
        lucene_document.add(fileNameField);
        lucene_document.add(filePathField);
        return lucene_document;
    }

    private static String extractText(File file) throws IOException {
        String fileName = file.getName();
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

        switch (fileExtension.toLowerCase()) {
            case "pdf" -> {
                return PDFExtract(file);
            }
            case "docx" -> {
                return DOCXExtract(file);
            }
            case "txt" -> {
                return TXTExtract(file);
            }
            default -> {
                return "none";
            }
        }
    }
    private static String PDFExtract(File file) throws IOException {
        PDDocument pdf_document = PDDocument.load(file);
        PDFTextStripper textStripper = new PDFTextStripper();
        String content = textStripper.getText(pdf_document);
        pdf_document.close();
        return content;
    }

    private static String TXTExtract(File file) throws IOException {
        Scanner scanner = new Scanner(file);
        StringBuilder stringBuilder = new StringBuilder();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            stringBuilder.append(line).append(" ");
        }
        scanner.close();
        return stringBuilder.toString();
    }

    private static String DOCXExtract(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        XWPFDocument document = new XWPFDocument(fis);
        XWPFWordExtractor extractor = new XWPFWordExtractor(document);

        String content = extractor.getText();
        extractor.close();
        fis.close();
        return content;
    }
}