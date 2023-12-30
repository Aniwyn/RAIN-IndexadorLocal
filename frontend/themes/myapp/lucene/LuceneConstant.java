package ar.edu.unju.fi.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
public class LuceneConstant {
    public static final String CONTENTS = "contents";
    public static final String FILE_NAME = "filename";
    public static final String FILE_PATH = "filepath";
    public static final int MAX_SEARCH = 100; //revisar
    public static final String DATA_DIR = "D:\\LUCENE\\Prueba";
    public static final String INDEX_DIR = "D:\\LUCENE\\INDEX";
    public static final StandardAnalyzer analyzer = new StandardAnalyzer();
}
