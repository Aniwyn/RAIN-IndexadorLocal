package ar.edu.unju.fi.lucene;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
public class LuceneConstant {
    public static final String CONTENTS = "contents";
    public static final String FILE_NAME = "filename";
    public static final String FILE_PATH = "filepath";
    public static final int MAX_SEARCH = 10;
    public static final String INDEX_DIR = "data/lucene_index";

    public static final String LAST_PATH = "data/dataPath";

    public static final String IMAGE_DIR = "images/";
    private static final CharArraySet stop_words = SpanishAnalyzer.getDefaultStopSet();
    public static final StandardAnalyzer analyzer = new StandardAnalyzer(stop_words);
}
