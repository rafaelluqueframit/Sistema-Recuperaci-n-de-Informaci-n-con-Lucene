/**************************************************************************/
// Práctica realizada por: Critóbal Jiménez Álvarez y Rafael Luque Framit
/**************************************************************************/


package p6facetas;

import org.apache.lucene.document.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

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

import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.DrillSideways;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LabelAndValue;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;



public class busqueda{

    private final IndexReader indexReaderGuiones;
    private final IndexReader indexReaderCapitulosUnidos;
    private final TaxonomyReader taxoReaderGuiones;
    private final TaxonomyReader taxoReaderCapitulosUnidos;
    
    private Analyzer analyzer = null;
    int DOCUMENTOS = 20;
    private IndexSearcher searcher=null;
    private FacetsCollector fcollector = null;
    private FacetsConfig fconfig = null;
    private Query query=null;
    private QueryBuilder builder = null;
    
    private String[] vector_facetas;
    private Map<String, String> map_faceta_season;
    private List<String> categorias;
    private long totalHits;
    private QueryParser parser;
    private TopDocs results=null;
    private TopDocs tdc=null;
    private DrillDownQuery ddq=null;
    
    public busqueda(IndexReader indexReaderGuiones, IndexReader indexReaderCapitulosUnidos, TaxonomyReader taxoReaderGuiones,TaxonomyReader taxoReaderCapitulosUnidos, Analyzer analyzer) {
        this.indexReaderGuiones = indexReaderGuiones;
        this.indexReaderCapitulosUnidos = indexReaderCapitulosUnidos;
        this.taxoReaderGuiones= taxoReaderGuiones; 
        this.taxoReaderCapitulosUnidos=taxoReaderCapitulosUnidos;
        this.analyzer = analyzer;
        this.categorias = new ArrayList<>();
    }

    
    public String ConsultaBooleanaGuiones(String[] campos, String operador) throws IOException{
        searcher = new IndexSearcher(indexReaderGuiones);
        fconfig = new FacetsConfig();
        fcollector = new FacetsCollector();
        
        ArrayList<Query> consultas = new ArrayList<Query>();
        
        Analyzer analyzer = null;
        QueryBuilder builder = null;
        
        BooleanQuery.Builder bqbuilder = new BooleanQuery.Builder();

        if (campos[0].compareTo("") != 0) {
            int[] values = Arrays.stream(campos[0].split(" "))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            Query queryid = IntPoint.newSetQuery("episode_id", values);
            consultas.add(queryid);
        }
        if (campos[1].compareTo("") != 0) {
            analyzer = new EnglishAnalyzer();
            QueryParser parser = new QueryParser("spoken_words", analyzer);
            try {
                Query queryspoken = parser.parse(campos[1]);
                consultas.add(queryspoken);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (campos[2].compareTo("") != 0) {
            analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("raw_location_text", analyzer);
            try {
                Query querylocation = parser.parse(campos[2]);
                consultas.add(querylocation);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (campos[3].compareTo("") != 0) {
            analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("raw_character_text", analyzer);
            try {
                Query queryraw = parser.parse(campos[3]);
                consultas.add(queryraw);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (campos[4].compareTo("") != 0) {
            int[] values = Arrays.stream(campos[4].split(" "))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            Query querynumber = IntPoint.newSetQuery("number", values);
            consultas.add(querynumber);
        }

        for (Query consulta : consultas) {
            BooleanClause.Occur occurType = BooleanClause.Occur.MUST; // Por defecto, AND
            if (operador.equalsIgnoreCase("OR")) {
                occurType = BooleanClause.Occur.SHOULD; // Si se especifica OR
            }
            bqbuilder.add(new BooleanClause(consulta, occurType));
        }
        query = bqbuilder.build();
        ArrayList<String> result = searchAndPrintResults(searcher, query);
        return String.join("\n", result);
    }
    
    public String ConsultaBooleanaCapitulosUnidos(String[] campos, String operador) throws IOException{
        searcher = new IndexSearcher(indexReaderCapitulosUnidos);
        fconfig = new FacetsConfig();
        fcollector = new FacetsCollector();
        ArrayList<Query> consultas = new ArrayList<Query>();
  

        Analyzer analyzer = null;
        QueryBuilder builder = null;
        BooleanQuery.Builder bqbuilder = new BooleanQuery.Builder();

        if (campos[0].compareTo("") != 0) {
            int[] values = Arrays.stream(campos[0].split(" "))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            Query queryid = IntPoint.newSetQuery("episode_id", values);
            consultas.add(queryid);
        }
        if (campos[1].compareTo("") != 0) {
            analyzer = new EnglishAnalyzer();
            QueryParser parser = new QueryParser("spoken_words", analyzer);
            try {
                Query queryspoken = parser.parse(campos[1]);
                consultas.add(queryspoken);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (campos[2].compareTo("") != 0) {
            analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("raw_character_text", analyzer);
            try {
                Query queryraw = parser.parse(campos[2]);
                consultas.add(queryraw);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        //PARA LOS CAMPOS EN LOS QUE QUEREMOS AÑADIR UN RANGO, AL NO EXISTIR LA FUNCIONALIDAD
        //PODEMOS CREAR UNA SUBCONSULTA BOOLEANA OR Y AÑADIRLA A LA CONSULTA GLOBAL.
        
        if (campos[3].compareTo("") != 0) {
            double[] values = Arrays.stream(campos[3].split(" "))
                            .mapToDouble(Double::parseDouble)
                            .toArray();

            BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

            for (double value : values) {
                double minValue = value - 0.2;
                double maxValue = value + 0.2;

                Query rangeQuery = DoublePoint.newRangeQuery("imdb_rating", minValue, maxValue);
                booleanQueryBuilder.add(rangeQuery, BooleanClause.Occur.SHOULD);
            }

            Query finalQuery = booleanQueryBuilder.build();
            consultas.add(finalQuery);
        }
        
        
        //ESTA ES LA SOLUCIÓN SIN INCLUIR RANGOS
        /*
        if (campos[3].compareTo("") != 0) {
            double[] values = Arrays.stream(campos[3].split(" "))
                            .mapToDouble(Double::parseDouble)
                            .toArray();
            query = DoublePoint.newSetQuery("imdb_rating",values);
            consultas.add(query);
        }
        //SOLUCIÓN ANTIGUA SIN PODER INTRODUCIR VARIOS VALORES
        if (campos[3].compareTo("") != 0) {
            // Convertir el valor del campo a un double
            double imdbRatingValue = Double.parseDouble(campos[3]);
            // Crear la consulta para el campo imdb_rating
            query = DoublePoint.newRangeQuery("imdb_rating", imdbRatingValue - 0.2, imdbRatingValue + 0.2);
            // Agregar la consulta al ArrayList
            consultas.add(query);
        }*/
        
        if (campos[4].compareTo("") != 0) {
            int[] values = Arrays.stream(campos[4].split(" "))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            Query querynumber = IntPoint.newSetQuery("number_in_season", values);
            consultas.add(querynumber);
        }
        
        if (campos[5].compareTo("") != 0) {
            analyzer = new KeywordAnalyzer();
            QueryParser parser = new QueryParser("original_air_date", analyzer);
            try {
                Query originalAirDateQuery = parser.parse(campos[5]);
                consultas.add(originalAirDateQuery);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (campos[6].compareTo("") != 0) {
            int[] values = Arrays.stream(campos[6].split(" "))
                    .mapToInt(Integer::parseInt)
                    .toArray();
            Query seasonQuery = IntPoint.newSetQuery("season", values);
            consultas.add(seasonQuery);
        }
        if (campos[7].compareTo("") != 0) {
            analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser("title", analyzer);
            try {
                Query titleQuery = parser.parse(campos[7]);
                consultas.add(titleQuery);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        //RANGO PARA INT
        if (campos[8].compareTo("") != 0) {
            int[] viewsValues = Arrays.stream(campos[8].split(" "))
                    .mapToInt(Integer::parseInt)
                    .toArray();

            BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
            for (int viewsValue : viewsValues) {
                int minRange = viewsValue - 5000;
                int maxRange = viewsValue + 5000;
                Query rangeQuery = IntPoint.newRangeQuery("views", minRange, maxRange);
                booleanQueryBuilder.add(rangeQuery, BooleanClause.Occur.SHOULD);
            }
            Query finalQuery = booleanQueryBuilder.build();
            consultas.add(finalQuery);
        }

        for (Query consulta : consultas) {
            BooleanClause.Occur occurType = BooleanClause.Occur.MUST; // Por defecto, AND
            if (operador.equalsIgnoreCase("OR")) {
                occurType = BooleanClause.Occur.SHOULD; // Si se especifica OR
            }
            bqbuilder.add(new BooleanClause(consulta, occurType));
        }
        query = bqbuilder.build();
        ArrayList<String> result = searchAndPrintResults(searcher, query);
        return String.join("\n", result);
    }
    
    public String indexSearch(boolean esGuiones, String consulta) {
        IndexReader reader = esGuiones ? indexReaderGuiones : indexReaderCapitulosUnidos;
        try {
            searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new BM25Similarity());
            fconfig = new FacetsConfig();
            fcollector = new FacetsCollector();
            String[] campos = esGuiones ?
                    new String[]{"raw_character_text", "spoken_words", "raw_location_text", "number", "episode_id", "file"} :
                    new String[]{"episode_id", "spoken_words", "raw_character_text", "imdb_rating", "number_in_season",
                            "original_air_date", "season", "title", "views", "file"};

            MultiFieldQueryParser parser = new MultiFieldQueryParser(campos, new StandardAnalyzer());
            String line = consulta.trim();
            if (line.length() == 0) {
                return "Consulta vacía";
            }
            query = parser.parse(line);
            ArrayList<String> result = searchAndPrintResults(searcher, query);
            return String.join("\n", result);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            return "Error en la búsqueda";
        }
    }
    
    public ArrayList<String> MostrarFacetas(boolean esGuiones) {
        vector_facetas = new String[4 * 100];
        map_faceta_season = new HashMap<String, String>();
        ArrayList<String> l = new ArrayList<String>();
        ddq = new DrillDownQuery(fconfig, query);
        categorias = new ArrayList<String>();
        int i = 0;
        try {
            //FacetsCollector fc1 = new FacetsCollector();
            tdc = FacetsCollector.search(searcher, ddq, 10, fcollector);
            Facets fcCount=null;
            if (!esGuiones){fcCount = new FastTaxonomyFacetCounts(taxoReaderCapitulosUnidos, fconfig, fcollector);}
            else{fcCount = new FastTaxonomyFacetCounts(taxoReaderGuiones, fconfig, fcollector);}
            
            List<FacetResult> allDims = fcCount.getAllDims(100);
            // Para cada categoria mostramos el valor de la etiqueta y su numero de ocurrencias
            for (FacetResult fr : allDims) {
                categorias.add(fr.dim);
                int cont = 0;
                // Almacenamos cada etiqueta en un vector de 3*TOP casillas para guardar todas las que mostramos
                for (LabelAndValue lv : fr.labelValues) {
                    vector_facetas[i] = new String(fr.dim + " (#n)-> " + lv.label + " (" + lv.value + ")");
                    map_faceta_season.put(lv.label, fr.dim);
                    l.add(fr.dim + ": " + lv.label + " (" + lv.value + ")");
                    cont++;
                    i++;
                }
            }
        } catch (IOException e) {
            System.out.println("Error al mostrar facetas. ");
        }
        return new ArrayList<>(l);
    }
    
 
    public String FiltrarPorFacetas(String valorFaceta) {
        // Inicializamos el DrillDownQuery con la consulta realizada
        DrillDownQuery ddq = new DrillDownQuery(fconfig, query);
        ArrayList<String> result = new ArrayList<>();
        try {
           // Dividir la entrada en tres partes
            String[] partes = valorFaceta.split(": |\\(");
            partes[2] = partes[2].substring(0, partes[2].length() - 1);//quitar )
            // Limpiar los espacios en blanco alrededor de las partes
            for (int i = 0; i < partes.length; i++) {
                partes[i] = partes[i].trim();
            }
            // Mostrar las partes
            for (String parte : partes) {
                System.out.println(parte);
            }
                // Realizamos operación AND entre la dimensión y el valor de la faceta
                ddq.add(partes[0], partes[1]);
                // Volvemos a hacer la búsqueda con el nuevo ddq que contiene las facetas.
                FacetsCollector fcollector = new FacetsCollector();
                TopDocs tdc = FacetsCollector.search(searcher, ddq, 10, fcollector);
                totalHits = tdc.totalHits.value;

                // Mostrar resultados (o haz lo que necesites con ellos)
                result = PrintFilteredResults(tdc);
                return String.join("\n", result);
        } catch (IOException e) {
            System.out.println("Error al filtrar facetas. ");
        }
        return String.join("\n", result);
    }

    private ArrayList<String> searchAndPrintResults(IndexSearcher searcher, Query query) throws IOException {
        ArrayList<String> resultList = new ArrayList<>();
        results = searcher.search(query, DOCUMENTOS);
        ScoreDoc[] hits = results.scoreDocs;
        long numTotalHits = results.totalHits.value;
        resultList.add(numTotalHits + " documentos encontrados");
        for (int j = 0; j < hits.length; j++) {
            Document doc = searcher.doc(hits[j].doc);
            // Construir la cadena con la información del documento
            Arrays.stream(doc.getFields().toArray())
                    .forEach(field -> {
                        resultList.add(((IndexableField) field).name() + ": " + doc.get(((IndexableField) field).name()));
                    });
            resultList.add("-----");
        }
        return resultList;
    }
    
    public ArrayList<String> PrintFilteredResults(TopDocs docs) throws IOException{
        ArrayList<String> resultList = new ArrayList<>();
        ScoreDoc[] hits = docs.scoreDocs;
        long numTotalHits = docs.totalHits.value;
        
        resultList.add(numTotalHits + " documentos encontrados");
        
        for(int j=0; j<hits.length; j++){

                try {
                Document doc = searcher.doc(hits[j].doc);
                // Construir la cadena con la información del documento
                Arrays.stream(doc.getFields().toArray())
                        .forEach(field -> {
                            resultList.add(((IndexableField) field).name() + ": " + doc.get(((IndexableField) field).name()));
                        });
                System.out.println(resultList.get(j));
            } catch (IOException e) {
                // Manejar la excepción (puede imprimir un mensaje, registrarla, etc.)
                System.out.println("Error al acceder al documento: " + e.getMessage());
            }
            resultList.add("-----");
        }
        return resultList;   
    }
}
