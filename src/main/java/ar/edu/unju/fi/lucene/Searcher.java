package ar.edu.unju.fi.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;


public class Searcher {
    IndexSearcher indexSearcher;
    QueryParser queryParser;
    Query query;

    public Searcher(String indexDirectoryPath) throws IOException {
        FSDirectory fsDir = FSDirectory.open(Paths.get(indexDirectoryPath));
        IndexReader fsDirReader = DirectoryReader.open(fsDir);
        indexSearcher = new IndexSearcher(fsDirReader);
        queryParser = new QueryParser(LuceneConstant.CONTENTS, LuceneConstant.analyzer);
    }

    public TopDocs search(String searchQuery) throws IOException, ParseException {
        query = queryParser.parse(searchQuery);
        return indexSearcher.search(query, LuceneConstant.MAX_SEARCH);
    }
    public Document getDocument(ScoreDoc scoreDoc) throws IOException {
        return indexSearcher.getIndexReader().document(scoreDoc.doc);
    }

}