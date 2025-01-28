/**************************************************************************/
// Práctica realizada por: Critóbal Jiménez Álvarez y Rafael Luque Framit
/**************************************************************************/


package p6facetas;
import p6facetas.BusquedaForm;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Paths;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.*;
import java.nio.file.Paths;
import java.util.ArrayList;


import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.analysis.CharArraySet;


import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.DirectoryReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.QueryBuilder;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;


public class MainFrame extends JFrame {

    public MainFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        // Crear un JPanel con un layout de cuadrícula
        JPanel panel = new JPanel(new GridLayout(2, 1));
        // Botón "Búsqueda Simple"
        JButton simpleSearchButton = new JButton("Búsqueda Simple");
        simpleSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Abrir el formulario de búsqueda simple
                abrirBusquedaForm(false);
            }
        });
        // Botón "Búsqueda por Campos"
        JButton fieldSearchButton = new JButton("Búsqueda por Campos");
        fieldSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Abrir el formulario de búsqueda por campos
                abrirBusquedaForm(true);
            }
        });
        // Agregar botones al panel
        panel.add(simpleSearchButton);
        panel.add(fieldSearchButton);
        // Agregar el panel al JFrame
        getContentPane().add(BorderLayout.CENTER, panel);
        // Hacer visible el JFrame
        setVisible(true);
    }

    // Método para abrir el formulario de búsqueda
    private void abrirBusquedaForm(boolean campos) {
        try {
            FSDirectory guionesDirectory = FSDirectory.open(Paths.get("./indexGuiones"));
            FSDirectory capitulosUnidosDirectory = FSDirectory.open(Paths.get("./indexCapitulosUnidos"));
            TaxonomyReader taxoReaderGuiones = new DirectoryTaxonomyReader(FSDirectory.open(Paths.get("./facetGuiones")));
            TaxonomyReader taxoReaderCapitulosUnidos= new DirectoryTaxonomyReader(FSDirectory.open(Paths.get("./facetCapitulosUnidos")));
            // Crear instancias de Busqueda
            IndexReader guionesReader = DirectoryReader.open(guionesDirectory);
            IndexReader capitulosUnidosReader = DirectoryReader.open(capitulosUnidosDirectory);
            Analyzer analyzer = new StandardAnalyzer();
            busqueda busqueda = new busqueda(guionesReader, capitulosUnidosReader,taxoReaderGuiones,taxoReaderCapitulosUnidos, analyzer);
            // Crear el formulario y mostrarlo
            if(campos == false)
                SwingUtilities.invokeLater(() -> new BusquedaForm(busqueda));
            if (campos == true)
                SwingUtilities.invokeLater(() -> new BusquedaFormCampos(busqueda));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}