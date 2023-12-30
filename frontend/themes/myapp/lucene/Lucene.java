package ar.edu.unju.fi.lucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;


public class Lucene {
    Searcher searcher;
    Indexer indexer;

    public Lucene() {

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
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = indexer.createIndex(LuceneConstant.DATA_DIR, new TextFileFilter());
        long endTime = System.currentTimeMillis();
        indexer.close();
        System.out.println(numIndexed + " Archivo indexado, duracion: " + (endTime-startTime) + " ms");
    }
    public void search(String searchQuery) throws IOException, ParseException, InvalidTokenOffsetsException {
        searcher = new Searcher(LuceneConstant.INDEX_DIR);
        long startTime = System.currentTimeMillis();
        TopDocs hits = searcher.search(searchQuery);
        long endTime = System.currentTimeMillis();

        System.out.println(hits.totalHits + " documentos encontrados. Tiempo: " + (endTime-startTime));

        for(ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = searcher.getDocument(scoreDoc);
            System.out.println("File: " + doc.get(LuceneConstant.FILE_PATH));
            System.out.println("Name: " + doc.get(LuceneConstant.FILE_NAME));
//            System.out.println("a: "+ doc.get(LuceneConstant.CONTENTS));
//            System.out.println("Archivo: "+doc.get(LuceneConstant.FILE_PATH));
//            System.out.println("Document ID: " + doc.get("id"));
//            System.out.println("Title: "+doc.get("title"));
//            System.out.println("Matching text: ");
//            System.out.println(doc.get(LuceneConstant.FILE_NAME));
        }
    }

}