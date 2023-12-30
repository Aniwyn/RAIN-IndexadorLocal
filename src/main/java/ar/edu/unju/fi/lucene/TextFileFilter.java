package ar.edu.unju.fi.lucene;

import java.io.File;
import java.io.FileFilter;

public class TextFileFilter implements FileFilter {
    private boolean pdf = true;
    private boolean docx = true;
    private boolean txt = true;

    @Override
    public boolean accept(File pathname) {
        return (pathname.getName().toLowerCase().endsWith(".pdf") && pdf)
                || (pathname.getName().toLowerCase().endsWith(".txt") && txt)
                || (pathname.getName().toLowerCase().endsWith(".docx") && docx);
    }

    public void setPdf(boolean pdf) {
        this.pdf = pdf;
    }

    public void setDocx(boolean docx) {
        this.docx = docx;
    }

    public void setTxt(boolean txt) {
        this.txt = txt;
    }
}
