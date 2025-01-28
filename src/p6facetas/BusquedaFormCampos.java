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
import org.apache.lucene.search.Query;


/**
 *
 * @author cristobaljimenez_
 */
public class BusquedaFormCampos extends JFrame {

    private final busqueda busqueda;
    private JTextArea resultadoTextArea;

    public BusquedaFormCampos(busqueda busqueda) {
        this.busqueda = busqueda;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200); // Aumentar el tamaño del JFrame

        // Crear un JPanel personalizado con una imagen de fondo
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        
        // Tipo de Búsqueda
        c.gridx = 1;
        c.gridy = 0;
        panel.add(new JLabel("Tipo de Búsqueda:"), c);

        JComboBox<String> tipoBusquedaComboBox = new JComboBox<>(new String[]{"Guiones", "Capítulos Unidos"});
        c.gridx = 2;
        panel.add(tipoBusquedaComboBox, c);

        // Lógica para campos adicionales según el tipo de búsqueda
        tipoBusquedaComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (tipoBusquedaComboBox.getSelectedItem().equals("Guiones")) {
                    // Elimina los componentes previos antes de agregar nuevos
                    panel.removeAll();

                    setSize(1200, 600);
                        
                    // Configurar la imagen en la izquierda
                    c.gridx = 0;
                    c.gridy = 0;
                    c.gridheight = GridBagConstraints.REMAINDER;  // Ocupar el resto de las filas
                    c.fill = GridBagConstraints.VERTICAL;  // Permitir que la imagen ocupe verticalmente
                    JLabel imagenLabel = new JLabel(new ImageIcon("./imagenes/bart.png"));
                    panel.add(imagenLabel, c);
                    c.gridheight = 1;  // Restaurar la altura a 1
                    c.fill = GridBagConstraints.NONE;  // Restaurar el fill a NONE

                    // Agregar campos adicionales para Guiones
                    String[] camposGuiones = {"episode_id", "spoken_words", "raw_location_text", "raw_character_text", "number"};
                    JTextField[] textFieldArray = new JTextField[camposGuiones.length];

                    // Configurar el GridBagConstraints para los campos
                    c.gridx = 1;
                    c.gridy = 0;  // Comenzar desde la fila 0
                    c.gridwidth = 1;
                    c.fill = GridBagConstraints.HORIZONTAL;

                    for (int i = 0; i < camposGuiones.length; i++) {
                        c.gridy = i + 1;  // Comenzar desde la fila 1
                        panel.add(new JLabel(camposGuiones[i] + ":"), c);

                        c.gridx = 2;
                        JTextField textField = new JTextField();
                        // Establecer la anchura de la caja de texto (por ejemplo, 20 columnas)
                        textField.setColumns(20);
                        // Agrega el JTextField al array
                        textFieldArray[i] = textField;
                        panel.add(textField, c);

                        // Restaurar el GridBagConstraints para el próximo JLabel
                        c.gridx = 1;
                    }

                    // Restaurar el GridBagConstraints para los componentes restantes
                    c.gridwidth = 1;
                    c.fill = GridBagConstraints.NONE;

                    // Botón Buscar
                    c.gridx = 3;
                    c.gridy = camposGuiones.length + 2;  // Ajustar según la cantidad de campos
                    panel.add(new JLabel(" "), c);  // Espaciador vertical
                    c.gridy = camposGuiones.length + 3;  // Ajustar según la cantidad de campos
                    JButton buscarButton = new JButton("Buscar");
                    panel.add(buscarButton, c);
                    
                    // JComboBox para seleccionar AND o OR
                    String[] operadores = {"AND", "OR"};
                    JComboBox<String> operadorComboBox = new JComboBox<>(operadores);
                    c.gridx = 2;
                    panel.add(operadorComboBox, c);
                    
                    // JComboBox para mostrar las categorías
                   
                    c.gridx = 1;
                    c.gridy = camposGuiones.length + 1; // Ajustar según la cantidad de campos
                    panel.add(new JLabel("Categorías (Facetas):"), c);
                    c.gridx = 2;
                    JComboBox<String> categoriasComboBox = new JComboBox<>();
                    // Añadir la opción "None" al principio de la lista
                    categoriasComboBox.addItem("None (Pulsar Buscar)");
                    panel.add(categoriasComboBox, c);
                    
                    // Resultado
                    c.gridx = 1;
                    c.gridy = camposGuiones.length + 4;  // Ajustar según la cantidad de campos
                    c.gridwidth = 3;
                    c.fill = GridBagConstraints.BOTH;
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
                    
                    // Agregar el ActionListener para el botón Buscar
                    buscarButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            
                            String operadorSeleccionado = (String) operadorComboBox.getSelectedItem();
                            
                            // Crear un array para almacenar los textos de los JTextField
                            String[] camposGuiones = new String[textFieldArray.length];

                            // Obtener los textos de los JTextField y almacenarlos en el array
                            for (int i = 0; i < textFieldArray.length; i++) {
                                camposGuiones[i] = textFieldArray[i].getText();
                            }

                            // Llamar a la función ConsultaBooleanaGuiones con el array de campos como parámetro
                            try {
                                
                                String resultado = busqueda.ConsultaBooleanaGuiones(camposGuiones,operadorSeleccionado);
                                actualizarResultado(resultado);
                                // Resto del código para manejar el resultado
                            } catch (IOException ex) {
                                // Manejar la excepción aquí, por ejemplo, imprimir un mensaje de error
                                ex.printStackTrace();
                            }
                            // Llamar a la función MostrarFacetas para obtener las categorías
                            ArrayList<String> categorias = busqueda.MostrarFacetas(true);

                            // Añadir la opción "None" al principio de la lista
                            categorias.add(0, "None (Pulsar Buscar)");

                            // Actualizar el JComboBox con las categorías
                            DefaultComboBoxModel<String> categoriasact = new DefaultComboBoxModel<>(categorias.toArray(new String[0]));
                            categoriasComboBox.setModel(categoriasact);

                            // Ajustar la interfaz
                            revalidate();
                            repaint();
                            // Mostrar el resultado en el JTextArea
                        }
                    });

                    // Ajustar la interfaz
                    revalidate();
                    repaint();
                }
            
                else if (tipoBusquedaComboBox.getSelectedItem().equals("Capítulos Unidos")){
                    // Elimina los componentes previos antes de agregar nuevos
                    panel.removeAll();

                    setSize(1000, 800);

                    // Configurar la imagen en la izquierda
                    c.gridx = 0;
                    c.gridy = 0;
                    c.gridheight = GridBagConstraints.REMAINDER;  // Ocupar el resto de las filas
                    c.fill = GridBagConstraints.VERTICAL;  // Permitir que la imagen ocupe verticalmente
                    JLabel imagenLabel = new JLabel(new ImageIcon("./imagenes/bart.png"));
                    panel.add(imagenLabel, c);
                    c.gridheight = 1;  // Restaurar la altura a 1
                    c.fill = GridBagConstraints.NONE;  // Restaurar el fill a NONE

                    // Agregar campos adicionales para Guiones
                    String[] camposCapitulosUnidos = {"episode_id", "spoken_words", "raw_character_text", "imdb_rating", "number_in_season","original_air_date (YYYY-MM-DD)","season","title","views"};
                    JTextField[] textFieldArray = new JTextField[camposCapitulosUnidos.length];

                    // Configurar el GridBagConstraints para los campos
                    c.gridx = 1;
                    c.gridy = 0;  // Comenzar desde la fila 0
                    c.gridwidth = 1;
                    c.fill = GridBagConstraints.HORIZONTAL;

                    for (int i = 0; i < camposCapitulosUnidos.length; i++) {
                        c.gridy = i + 1;  // Comenzar desde la fila 1
                        panel.add(new JLabel(camposCapitulosUnidos[i] + ":"), c);

                        c.gridx = 2;
                        JTextField textField = new JTextField();
                        // Establecer la anchura de la caja de texto (por ejemplo, 20 columnas)
                        textField.setColumns(20);
                        // Agrega el JTextField al array
                        textFieldArray[i] = textField;
                        panel.add(textField, c);

                        // Restaurar el GridBagConstraints para el próximo JLabel
                        c.gridx = 1;
                    }

                    // Restaurar el GridBagConstraints para los componentes restantes
                    c.gridwidth = 1;
                    c.fill = GridBagConstraints.NONE;

                    // Botón Buscar
                    c.gridx = 3;
                    c.gridy = camposCapitulosUnidos.length + 2;  // Ajustar según la cantidad de campos
                    panel.add(new JLabel(" "), c);  // Espaciador vertical
                    c.gridy = camposCapitulosUnidos.length + 3;  // Ajustar según la cantidad de campos
                    JButton buscarButton = new JButton("Buscar");
                    panel.add(buscarButton, c);
                    
                    // JComboBox para seleccionar AND o OR
                    String[] operadores = {"AND", "OR"};
                    JComboBox<String> operadorComboBox = new JComboBox<>(operadores);
                    c.gridx = 2;
                    panel.add(operadorComboBox, c);
                    
                    
                    // JComboBox para mostrar las categorías
                    c.gridx = 1;
                    c.gridy = camposCapitulosUnidos.length + 1; // Ajustar según la cantidad de campos
                    panel.add(new JLabel("Categorías (Facetas):"), c);
                    c.gridx = 2;
                    JComboBox<String> categoriasComboBox = new JComboBox<>();
                    
                    // Añadir la opción "None" al principio de la lista
                    categoriasComboBox.addItem("None (Pulsar Buscar)");
                    panel.add(categoriasComboBox, c);
                    
                    // Resultado
                    c.gridx = 1;
                    c.gridy = camposCapitulosUnidos.length + 4;  // Ajustar según la cantidad de campos
                    c.gridwidth = 3;
                    c.fill = GridBagConstraints.BOTH;
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
                    // Agregar el ActionListener para el botón Buscar
                    buscarButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            
                            String operadorSeleccionado = (String) operadorComboBox.getSelectedItem();
                            // Crear un array para almacenar los textos de los JTextField
                            String[] camposCapitulosUnidos = new String[textFieldArray.length];

                            // Obtener los textos de los JTextField y almacenarlos en el array
                            for (int i = 0; i < textFieldArray.length; i++) {
                                camposCapitulosUnidos[i] = textFieldArray[i].getText();
                            }
                            
                            
                            // Llamar a la función ConsultaBooleanaGuiones con el array de campos como parámetro
                            try {
                                String resultado = busqueda.ConsultaBooleanaCapitulosUnidos(camposCapitulosUnidos,operadorSeleccionado);
                                actualizarResultado(resultado);
                                // Resto del código para manejar el resultado
                            } catch (IOException ex) {
                                // Manejar la excepción aquí, por ejemplo, imprimir un mensaje de error
                                ex.printStackTrace();
                            }
                            // Llamar a la función MostrarFacetas para obtener las categorías
                            ArrayList<String> categorias = busqueda.MostrarFacetas(false);

                            // Añadir la opción "None" al principio de la lista
                            categorias.add(0, "None (Pulsar Buscar)");

                            // Actualizar el JComboBox con las categorías
                            DefaultComboBoxModel<String> categoriasact = new DefaultComboBoxModel<>(categorias.toArray(new String[0]));
                            categoriasComboBox.setModel(categoriasact);

                            
                            // Ajustar la interfaz
                            revalidate();
                            repaint();
                            
                            // Mostrar el resultado en el JTextArea
                            
                        }
                    });

                    // Ajustar la interfaz
                    revalidate();
                    repaint();
                }
            }
        });


        // Configurar el color de fondo del JFrame
        

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
            SwingUtilities.invokeLater(() -> new BusquedaFormCampos(busqueda));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
*/
}


