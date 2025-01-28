/**************************************************************************/
// Práctica realizada por: Critóbal Jiménez Álvarez y Rafael Luque Framit
/**************************************************************************/

package p6facetas;

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


public class BusquedaForm extends JFrame {

    private final busqueda busqueda;
    private JTextArea resultadoTextArea;

    public BusquedaForm(busqueda busqueda) {
        this.busqueda = busqueda;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 600);

        // Crear un JPanel personalizado con una imagen de fondo
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        // Configurar la imagen en la mitad izquierda
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 4;  // Ocupar tres filas
        JLabel imagenLabel = new JLabel(new ImageIcon("./imagenes/bart.png"));
        panel.add(imagenLabel, c);
        c.gridheight = 1;  // Restaurar la altura a 1

        // Tipo de Búsqueda
        c.gridx = 1;
        c.gridy = 0;
        panel.add(new JLabel("Tipo de Búsqueda:"), c);

        c.gridx = 2;
        JComboBox<String> tipoBusquedaComboBox = new JComboBox<>(new String[]{"Guiones", "Capítulos Unidos"});
        panel.add(tipoBusquedaComboBox, c);

        // Consulta
        c.gridx = 1;
        c.gridy = 1;
        panel.add(new JLabel("Consulta:"), c);

        c.gridx = 2;
        JTextField consultaTextField = new JTextField();
        panel.add(consultaTextField, c);

        // Botón Buscar
        c.gridx = 3;
        c.gridy = 1;
        c.gridheight = 1;  // Restaurar la altura a 1
        JButton buscarButton = new JButton("Buscar");
        panel.add(buscarButton, c);

        // JComboBox para mostrar las categorías
        c.gridx = 1;
        c.gridy = 2;  // Ahora, en la línea 2
        panel.add(new JLabel("Categorías (Facetas):"), c);
        c.gridx = 2;
        
        JComboBox<String> categoriasComboBox = new JComboBox<>();
        // Añadir la opción "None" al principio de la lista
        categoriasComboBox.addItem("None (Pulsar Buscar)");
        panel.add(categoriasComboBox, c);

        // Resultado
        c.gridx = 1;  // Cambiar a la columna 1
        c.gridy = 3;  // Cambiar a la fila 2
        c.gridwidth = 3;  // Ocupar tres columnas
        c.gridheight = GridBagConstraints.REMAINDER;  // Ocupar el resto de las filas
        c.fill = GridBagConstraints.BOTH;  // Ocupar tanto horizontal como verticalmente
        resultadoTextArea = new JTextArea();
        resultadoTextArea.setEditable(false);
        resultadoTextArea.setLineWrap(true);
        resultadoTextArea.setRows(20);
        JScrollPane scrollPane = new JScrollPane(resultadoTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollPane, c);

        // ActionListener para el JComboBox de categorías
        categoriasComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtener la categoría seleccionada
                String categoriaSeleccionada = (String) categoriasComboBox.getSelectedItem();

                // Verificar si se seleccionó "None"
                if ("None (Pulsar Buscar)".equals(categoriaSeleccionada)) {
                    // Realizar acciones específicas cuando se selecciona "None"
                    // Puedes dejarlo vacío o mostrar un mensaje, según tus necesidades
                    System.out.println("No se realizará ninguna acción");
                } else {
                    // Realizar acciones adicionales según la categoría seleccionada
                    // Puedes llamar a tu función FiltrarPorFacetas aquí
                    String resultado = busqueda.FiltrarPorFacetas(categoriaSeleccionada);
                    actualizarResultado(resultado);
                }
            }
        });
                    
                    
        buscarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String consulta = consultaTextField.getText();
                boolean esGuiones = tipoBusquedaComboBox.getSelectedItem().equals("Guiones");

                // Llamar al método correspondiente según la selección
                String resultado = busqueda.indexSearch(esGuiones, consulta);

                // Mostrar el resultado en el JTextArea
                actualizarResultado(resultado);
                // Llamar a la función MostrarFacetas para obtener las categorías
                
                ArrayList<String> categorias = busqueda.MostrarFacetas(esGuiones);

                // Añadir la opción "None" al principio de la lista
                categorias.add(0, "None (Pulsar Buscar)");

                // Actualizar el JComboBox con las categorías
                DefaultComboBoxModel<String> categoriasact = new DefaultComboBoxModel<>(categorias.toArray(new String[0]));
                categoriasComboBox.setModel(categoriasact);

                // Ajustar la interfaz
                revalidate();
                repaint();
            }
        });

        getContentPane().add(BorderLayout.CENTER, panel);
        setVisible(true);
    }
    
    // Método para actualizar el JTextArea con el resultado de la búsqueda
    private void actualizarResultado(String resultado) {
        resultadoTextArea.setText(resultado);

        // Actualizar la interfaz
        revalidate();
        repaint();
    }
/*
    public static void main(String[] args) {
        try {
            FSDirectory guionesDirectory = FSDirectory.open(Paths.get("./indexGuiones"));
            IndexWriterConfig guionesConfig = new IndexWriterConfig(new StandardAnalyzer());
            IndexWriter guionesWriter = new IndexWriter(guionesDirectory, guionesConfig);

            // Configuración del índice para capítulos unidos
            FSDirectory capitulosUnidosDirectory = FSDirectory.open(Paths.get("./indexCapitulosUnidos"));
            IndexWriterConfig capitulosUnidosConfig = new IndexWriterConfig(new StandardAnalyzer());
            IndexWriter capitulosUnidosWriter = new IndexWriter(capitulosUnidosDirectory, capitulosUnidosConfig);

            // Agregar documentos de ejemplo a los índices (esto puede variar según tu implementación)
            // guionesWriter.addDocument(...);
            // capitulosUnidosWriter.addDocument(...);

            // Cerrar los escritores de índices
            guionesWriter.close();
            capitulosUnidosWriter.close();

            // Crear instancias de Busqueda
            IndexReader guionesReader = DirectoryReader.open(guionesDirectory);
            IndexReader capitulosUnidosReader = DirectoryReader.open(capitulosUnidosDirectory);
            Analyzer analyzer = new StandardAnalyzer();
            busqueda busqueda = new busqueda(guionesReader, capitulosUnidosReader, analyzer);
            
            // Crear el formulario y mostrarlo
            SwingUtilities.invokeLater(() -> new BusquedaForm(busqueda));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

}