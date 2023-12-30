package ar.edu.unju.fi.lucene;

import ar.edu.unju.fi.data.entity.ResultCard;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class Lucene {
    Searcher searcher;
    Indexer indexer;
    TextFileFilter textFileFilter;

    public Lucene(TextFileFilter textFileFilter) {
        this.textFileFilter = textFileFilter;
    }

    public void start() throws IOException {
        deleteDirectory(new File(LuceneConstant.INDEX_DIR));
        createDirectory(new File(LuceneConstant.INDEX_DIR));
        createIndex();
    }
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if(file.isDirectory()){
                        deleteDirectory(file);
                    }
                    else file.delete();
                }
            }
            directory.delete();
        }
    }
    private void createDirectory(File directory){
        if(!directory.exists()){
            if(directory.mkdirs())
                System.out.println("carpeta indice creada");
            else
                System.out.println("error al crear la carpeta indice");
        }
    }
    private void createIndex() throws IOException {
        indexer = new Indexer(LuceneConstant.INDEX_DIR);
        String path = getFolderPath();
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = indexer.createIndex(path, textFileFilter);
        long endTime = System.currentTimeMillis();
        indexer.close();
        System.out.println(numIndexed + " Archivo indexado, duracion: " + (endTime-startTime) + " ms");
    }

    public void updateIndex(String path) throws IOException {
        indexer = new Indexer(LuceneConstant.INDEX_DIR);
        String rootPath = getFolderPath();
        path = rootPath + "\\" + path;
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = indexer.updateIndex(path, textFileFilter);
        long endTime = System.currentTimeMillis();
        indexer.close();
        System.out.println(numIndexed + " Archivo indexado, duracion: " + (endTime-startTime) + " ms");
    }

    private static String getFolderPath() {
        String path = "";
        try (BufferedReader br = new BufferedReader(new FileReader(LuceneConstant.LAST_PATH))) {
            path = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    public ArrayList<ResultCard> search(String searchQuery) throws IOException, ParseException, InvalidTokenOffsetsException {
        searcher = new Searcher(LuceneConstant.INDEX_DIR);
        long startTime = System.currentTimeMillis();
        TopDocs hits = searcher.search(searchQuery);
        long endTime = System.currentTimeMillis();

        System.out.println(hits.totalHits + " documentos encontrados. Tiempo: " + (endTime-startTime));

        ArrayList<ResultCard> resultCards = new ArrayList<ResultCard>();
        for(ScoreDoc scoreDoc : hits.scoreDocs) {
            ResultCard resultCard = new ResultCard();
            Document doc = searcher.getDocument(scoreDoc);
            String fileName = doc.get(LuceneConstant.FILE_NAME);
            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);
            String cardImage = "";
            switch (fileExtension.toLowerCase()) {
                case "pdf" -> cardImage = LuceneConstant.IMAGE_DIR + "pdf.png";
                case "docx" -> cardImage = LuceneConstant.IMAGE_DIR + "doc.png";
                case "txt" -> cardImage = LuceneConstant.IMAGE_DIR + "txt.png";
            }

            resultCard.setName(doc.get(LuceneConstant.FILE_NAME));
            resultCard.setPath(doc.get(LuceneConstant.FILE_PATH));
            resultCard.setImage(cardImage);

            resultCards.add(resultCard);
        }
        return resultCards;
    }

}