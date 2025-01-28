/**************************************************************************/
// Práctica realizada por: Critóbal Jiménez Álvarez y Rafael Luque Framit
/**************************************************************************/

package indexadorP4;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVParserBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import com.opencsv.exceptions.CsvValidationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import java.util.Map;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
//import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;

import com.opencsv.exceptions.CsvException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.util.BytesRef;

import java.util.Scanner;
import org.apache.lucene.document.DoubleRange;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.facet.range.LongRange;
import org.apache.lucene.facet.range.LongRangeFacetCounts;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.util.NumericUtils;
//import org.apache.lucene.facet.range.RangeFacetBuilder;
import org.apache.lucene.facet.FacetsConfig;


/**
 *
 * @author cristobal jimenez y rafael luque
 */
public class IndexadorP4 {
    
    String indexPathGuiones = "./indexGuiones";
    String indexPathCapitulosUnidos = "./indexCapitulosUnidos";
    String facetPathGuiones = "./facetGuiones";
    String facetPathCapitulosUnidos = "./facetCapitulosUnidos";
    private IndexWriter indexWriterGuiones;
    private IndexWriter indexWriterCapitulosUnidos;
    private static boolean create;
    Analyzer analyzer = new StandardAnalyzer();
    private static String docPathGuiones ;
    private static String docPathCapitulosUnidos;
    FSDirectory taxoDir;
    DirectoryTaxonomyWriter taxoWriter1;
    DirectoryTaxonomyWriter taxoWriter2;
    FacetsConfig fconfig1;
    FacetsConfig fconfig2;    

    
    public static void main(String[] args) throws IOException {
        IndexadorP4 baseline = new IndexadorP4();
        Similarity similarity = new ClassicSimilarity();
        
        Scanner scanner = new Scanner(System.in);
        /************************/
        System.out.println("¿Deseas crear un nuevo índice o añadir a uno existente? (create/append)");
        String createOption = scanner.nextLine().trim().toLowerCase();
        
        while (!(createOption.equals("create") || createOption.equals("append"))) {
            System.out.println("Opción no válida. Debes elegir 'create' o 'append'.");
            createOption = scanner.nextLine().trim().toLowerCase();
        }

        create = createOption.equals("create");
        /************************/
        System.out.println("¿Deseas indexar Guiones, CapitulosUnidos, o ambos? (guiones/capitulosunidos/ambos)");
        String indexOption = scanner.nextLine().trim().toLowerCase();

        while (!(indexOption.equals("guiones") || indexOption.equals("capitulosunidos") || indexOption.equals("ambos"))) {
            System.out.println("Opción no válida. Debes elegir 'guiones', 'capitulosunidos' o 'ambos'.");
            indexOption = scanner.nextLine().trim().toLowerCase();
        }
        
        if (indexOption.equals("guiones") || indexOption.equals("ambos")) {
            System.out.println("Introduce la ruta del directorio de Guiones: \n(Presiona Enter para usar la ruta por defecto './documentos/Guiones')");
            String inputPath = scanner.nextLine().trim();
            docPathGuiones = inputPath.isEmpty() ? "./documentos/Guiones" : inputPath;
        }

        if (indexOption.equals("capitulosunidos") || indexOption.equals("ambos")) {
            System.out.println("Introduce la ruta del directorio de CapitulosUnidos: \n(Presiona Enter para usar la ruta por defecto './documentos/CapitulosUnidos')");
            String inputPath = scanner.nextLine().trim();
            docPathCapitulosUnidos = inputPath.isEmpty() ? "./documentos/CapitulosUnidos" : inputPath;
        }
        /************************/
        
        if (indexOption.equals("guiones") || indexOption.equals("ambos")) {
            // Creamos el índice
            baseline.configurarIndice1(similarity);
            baseline.configurarFaceta1();
            // Insertar los documentos
            try {
                baseline.indexarDocumentos1();
            } catch (IOException | CsvException e) {
                e.printStackTrace();
            }
            // Cerramos el indice
            baseline.close1();
            baseline.closeFacet1();
        }

        if (indexOption.equals("capitulosunidos") || indexOption.equals("ambos")) {
            // Creamos el índice
            baseline.configurarIndice2(similarity);
            baseline.configurarFaceta2();
            // Insertar los documentos
            try {
                baseline.indexarDocumentos2();
            } catch (IOException | CsvException e) {
                e.printStackTrace();
            }
            // Cerramos el indice
            baseline.close2();
            baseline.closeFacet2();
        }
        
        scanner.close();
    }
    
    public void indexarDocumentos1() throws IOException, CsvException {
        File GuionesFolder = new File(docPathGuiones);
        
        if (GuionesFolder.exists() && GuionesFolder.isDirectory()){
            File[] guiones = GuionesFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
            for (File guionFile : guiones) {
                try {
                    // Lee el archivo de diálogos CSV
                    try (CSVReader dialogosReader = new CSVReaderBuilder(new FileReader(guionFile))
                            .withSkipLines(1)
                            .withCSVParser(new CSVParserBuilder().withQuoteChar('"').build())
                            .build()) {
                        String[] discursoLinea;
                        while ((discursoLinea = dialogosReader.readNext()) != null) {
                            /*
                            Guiones:
                            ● ID: Primer campo que no tiene utilidad
                            ● episode_id: El número de episodio
                            ● number: Entero que representa la posición (orden) del diálogo en el episodio
                            ● timestamp_in_ms: tiempo desde el inicio del capítulo
                            ● raw_character_text: Personaje que habla
                            ● raw_location_text: Ubicación del discurso
                            ● spoken_words: El diálogo en sí.
                            */
                            Document doc = new Document();

                            doc.add(new IntPoint("episode_id", Integer.parseInt(discursoLinea[1])));
                            doc.add(new StoredField("episode_id", Integer.parseInt(discursoLinea[1])));


                            doc.add(new IntPoint("number", Integer.parseInt(discursoLinea[2])));
                            doc.add(new StoredField("number", Integer.parseInt(discursoLinea[2])));
                            
                            
                            doc.add(new LongPoint("timestamp_in_ms", Long.parseLong(discursoLinea[3])));
                            doc.add(new StoredField("timestamp_in_ms", Long.parseLong(discursoLinea[3])));

                            //No usamos stringField porque queremos usar un standard analyzer sobre ellos
                            doc.add(new TextField("raw_character_text", discursoLinea[4], Field.Store.YES));
                            doc.add(new TextField("raw_location_text", discursoLinea[5], Field.Store.YES));
                            doc.add(new TextField("spoken_words", discursoLinea[6], Field.Store.YES));
                            
                            // Obtén el nombre del archivo
                            String nombreArchivo = guionFile.getName()+"_number_"+discursoLinea[2];
                            doc.add(new TextField("file", nombreArchivo, Field.Store.YES));
                            
                            doc.add(new FacetField("raw_location_text", discursoLinea[5]));
                          
                            // Insertar el documento Lucene en el índice
                            indexWriterGuiones.addDocument(fconfig1.build(taxoWriter1, doc));
                        }
                     
                    } catch (CsvValidationException e) {
                        e.printStackTrace();
                    }
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            
        }
    }
    
    public void indexarDocumentos2() throws IOException, CsvException {
        File CapitulosUnidosFolder = new File(docPathCapitulosUnidos);
        
        if (CapitulosUnidosFolder.exists() && CapitulosUnidosFolder.isDirectory()){
            File[] capitulos = CapitulosUnidosFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
            for (File capituloFile : capitulos) {
                try {
                    // Lee el archivo de diálogos CSV
                    try (CSVReader capitulosReader = new CSVReaderBuilder(new FileReader(capituloFile))
                            .withSkipLines(1)
                            .withCSVParser(new CSVParserBuilder().withQuoteChar('"').build())
                            .build()) {
                        String[] discursoLinea;
                        while ((discursoLinea = capitulosReader.readNext()) != null) {
                            /*
                            Capitulos Unidos:
                                ● ID (código que no es de utilidad para nuestro propósito)
                                ● episode_id: número del episodio
                                ● spoken_words: Todos los diálogos de los que constan el episodio
                                ● raw_character_text: Listado de todos los personajes que intervienen en
                                el capítulo, en orden alfabético.
                                ● imdb_rating: La valoración media del capítulo en IMDB (Internet Movie
                                Data Base)
                                ● imdb_votes: Número de votos recibidos
                                ● number_in_season: número del episodio en la temporada (igual que
                                episode_id)
                                ● original_air_date: Fecha de emisión original
                                ● original_air_year: Año de emisión
                                ● season: Temporada del capítulo
                                ● title: Título del capítulo
                                ● us_viewers_in_millions: Número de espectadores en US (en millones)
                                ● views: Espectadores totales
                            */
                            Document doc = new Document();

                            doc.add(new IntPoint("episode_id", Integer.parseInt(discursoLinea[1])));
                            doc.add(new StoredField("episode_id", Integer.parseInt(discursoLinea[1])));
                            
                            doc.add(new TextField("spoken_words", discursoLinea[2], Field.Store.NO));
                            
                            doc.add(new TextField("raw_character_text", discursoLinea[3],Field.Store.YES));
                            
                            doc.add(new DoublePoint("imdb_rating", Double.parseDouble(discursoLinea[4])));
                            doc.add(new StoredField ("imdb_rating", Double.parseDouble(discursoLinea[4])));
                            
                            doc.add(new IntPoint("imdb_votes", Integer.parseInt(discursoLinea[5].replace(".0",""))));
                            doc.add(new StoredField("imdb_votes", Integer.parseInt(discursoLinea[5].replace(".0",""))));
                            
                            doc.add(new IntPoint("number_in_season", Integer.parseInt(discursoLinea[6])));
                            doc.add(new StoredField("number_in_season", Integer.parseInt(discursoLinea[6])));
                            
                            //no queremos tokenizarlo por ahora
                            doc.add(new StringField("original_air_date", discursoLinea[7], Field.Store.YES));
                            
                            doc.add(new IntPoint("original_air_year", Integer.parseInt(discursoLinea[8])));
                            
           
                            doc.add(new IntPoint("season", Integer.parseInt(discursoLinea[9])));
                            doc.add(new StoredField("season", Integer.parseInt(discursoLinea[9])));
                            
                            doc.add(new TextField("title", discursoLinea[10], Field.Store.YES));
                            
                            doc.add(new DoublePoint("us_viewers_in_millions", Double.parseDouble(discursoLinea[11])));
                            
                            doc.add(new IntPoint("views", Integer.parseInt(discursoLinea[12].replace(".0",""))));
                            doc.add(new StoredField("views", Integer.parseInt(discursoLinea[12].replace(".0",""))));
                            
                            
                            doc.add(new FacetField("season", discursoLinea[9]));
    
                            // Obtén el nombre del archivo
                            String nombreArchivo = capituloFile.getName();
                            doc.add(new TextField("file", nombreArchivo, Field.Store.YES));
                            // Insertar el documento Lucene en el índice
                            indexWriterCapitulosUnidos.addDocument(fconfig2.build(taxoWriter2, doc));
                        }
                    } catch (CsvValidationException e) {
                        e.printStackTrace();
                    } 
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    
    public void close1() {
        if (indexWriterGuiones != null) {
            try {
                indexWriterGuiones.commit();
                indexWriterGuiones.close();
                System.out.println("Indice Guiones cerrado");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("[ERROR] Error closing the index");
            }
        }
    }
    
    public void close2() {
        if (indexWriterCapitulosUnidos != null) {
            try {
                indexWriterCapitulosUnidos.commit();
                indexWriterCapitulosUnidos.close();
                System.out.println("Indice Capitulos Unidos cerrado");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("[ERROR] Error closing the index");
            }
        }
    }
       
    public void closeFacet1() {
        if (taxoWriter1 != null) {
            try {
                taxoWriter1.commit();
                taxoWriter1.close();
                System.out.println("Facetas Guiones cerrado");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("[ERROR] Error closing the index");
            }
        }
    }
    
    public void closeFacet2() {
        if (taxoWriter2 != null) {
            try {
                taxoWriter2.commit();
                taxoWriter2.close();
                System.out.println("Facetas Capitulos Unidos cerrado");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("[ERROR] Error closing the index");
            }
        }
    }
       
    public void configurarFaceta1() throws IOException {
        taxoDir = FSDirectory.open(Paths.get(facetPathGuiones));
        fconfig1 = new FacetsConfig();
        fconfig1.setMultiValued("raw_location_text", true);
        taxoWriter1 = new DirectoryTaxonomyWriter(taxoDir);
    }
    public void configurarFaceta2() throws IOException {
        taxoDir = FSDirectory.open(Paths.get(facetPathCapitulosUnidos));
        fconfig2 = new FacetsConfig();
        fconfig2.setMultiValued("season", true);
        taxoWriter2 = new DirectoryTaxonomyWriter(taxoDir);
    }
    public void configurarIndice1(Similarity similarity) throws IOException {
        PerFieldAnalyzerWrapper analizadorCampos1 = analizadorPorCampo1();
        IndexWriterConfig config1 = new IndexWriterConfig(analizadorCampos1);
        config1.setSimilarity(similarity);
        if (create){
            config1.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        } else{ config1.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);}
        Directory indexDir1 = FSDirectory.open(FileSystems.getDefault().getPath(indexPathGuiones));
        indexWriterGuiones = new IndexWriter(indexDir1, config1);
    }
    public void configurarIndice2(Similarity similarity) throws IOException {
        PerFieldAnalyzerWrapper analizadorCampos2 = analizadorPorCampo2();
        IndexWriterConfig config2 = new IndexWriterConfig(analizadorCampos2);
        config2.setSimilarity(similarity);
        if (create){
            config2.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        } else{ config2.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);}
        Directory indexDir2 = FSDirectory.open(FileSystems.getDefault().getPath(indexPathCapitulosUnidos));
        indexWriterCapitulosUnidos= new IndexWriter(indexDir2, config2);
    }
    
    public PerFieldAnalyzerWrapper analizadorPorCampo1() throws IOException{
        Map<String,Analyzer> analyzerPerField = new HashMap<>();
        analyzerPerField.put("episode_id", new KeywordAnalyzer());
        analyzerPerField.put("number", new KeywordAnalyzer());
        analyzerPerField.put("timestamp_in_ms", new KeywordAnalyzer());
        analyzerPerField.put("raw_character_text", new StandardAnalyzer());
        analyzerPerField.put("raw_location_text", new StandardAnalyzer());
        analyzerPerField.put("spoken_words", new EnglishAnalyzer());
        
        PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(analyzer,analyzerPerField);
        return aWrapper;
    }
    
    public PerFieldAnalyzerWrapper analizadorPorCampo2() throws IOException{
        Map<String,Analyzer> analyzerPerField = new HashMap<>();
        analyzerPerField.put("episode_id", new KeywordAnalyzer());
        analyzerPerField.put("spoken_words", new EnglishAnalyzer());
        analyzerPerField.put("raw_character_text", new StandardAnalyzer());
        analyzerPerField.put("imdb_rating", new KeywordAnalyzer());
        analyzerPerField.put("imdb_votes", new KeywordAnalyzer());
        analyzerPerField.put("number_in_season", new KeywordAnalyzer());
        analyzerPerField.put("original_air_date", new KeywordAnalyzer());
        analyzerPerField.put("original_air_year", new KeywordAnalyzer());
        analyzerPerField.put("season", new KeywordAnalyzer());
        analyzerPerField.put("title", new StandardAnalyzer());
        analyzerPerField.put("us_viewers_in_millions", new KeywordAnalyzer());
        analyzerPerField.put("views", new KeywordAnalyzer());        
        
        PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(analyzer,analyzerPerField);
        return aWrapper;
    }

}