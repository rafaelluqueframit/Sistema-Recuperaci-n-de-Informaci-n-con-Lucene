# Implementación de un Sistema de Recuperación de Información usando Lucene

## Integrantes
- Rafael Luque Framit
- Cristóbal Jiménez Álvarez

---

## Índice
1. [Indexación](#indexación)
2. [Realización de la búsqueda](#realización-de-la-búsqueda)
3. [Búsqueda genérica](#búsqueda-genérica)
4. [Búsqueda booleana](#búsqueda-booleana)
    - [Guiones](#búsqueda-booleana-guiones)
    - [Capítulos Unidos](#búsqueda-booleana-capítulos-unidos)
5. [Muestra de facetas](#muestra-de-facetas)
6. [Filtrar por facetas](#filtrar-por-facetas)
7. [Posibles facetas](#posibles-facetas)
8. [Interfaz gráfica de usuario](#interfaz-gráfica-de-usuario)
9. [Trabajo en grupo](#trabajo-en-grupo)
10. [Manual de usuario](#manual-de-usuario)

---

## Indexación

### Índices creados
1. **Guiones**: Procesamiento de archivos CSV línea por línea, obteniendo:
    - `episode_id`: Número del episodio.
    - `number`: Posición del diálogo en el episodio.
    - `timestamp_in_ms`: Tiempo desde el inicio.
    - `raw_character_text`: Personaje que habla.
    - `raw_location_text`: Ubicación del discurso.
    - `spoken_words`: Diálogo en sí.

    Índice almacenado en `./indexGuiones`. El campo `raw_location_text` es también una faceta.

2. **Capítulos Unidos**: Procesamiento de diálogos agrupados en celdas:
    - `episode_id`, `spoken_words`, `raw_character_text` (alfabético).
    - Información adicional: `imdb_rating`, `season`, `title`, etc.

    Índice almacenado en `./indexCapitulosUnidos`. El campo `season` es una faceta.

---

## Realización de la búsqueda

La búsqueda se realiza utilizando `IndexReader` y `IndexSearcher`, con métodos clave como:
- `searcher.search(Query Q, int N)`: Devuelve los `N` documentos más relevantes.
- Compatibilidad con lógica booleana, búsquedas por campos y explicaciones de los escore.

---

## Búsqueda genérica

Se implementa una búsqueda estándar (similar a Google), manejando consultas de cualquier complejidad. Utiliza `StandardAnalyzer` para tokenizar las palabras y busca coincidencias en ambos índices (`Guiones` y `Capítulos Unidos`).

---

## Búsqueda booleana

### Guiones
Permite buscar simultáneamente por varios campos como:
- `episode_id`
- `spoken_words`
- `raw_character_text`
- Combina campos con operadores AND/OR.

### Capítulos Unidos
Campos disponibles:
- `episode_id`, `spoken_words`, `imdb_rating`, `season`, etc.
- Incluye soporte para búsquedas por rangos en campos como `views` e `imdb_rating`.

---

## Muestra de facetas

Se generan facetas sobre los campos `raw_location_text` (Guiones) y `season` (Capítulos Unidos). Estas se gestionan usando objetos como `TaxonomyReader`, `FacetsConfig` y `FacetsCollector`.

---

## Filtrar por facetas

El método `FiltrarPorFacetas` utiliza el objeto `DrillDownQuery` para aplicar filtros a los resultados obtenidos en la búsqueda inicial.

---

## Posibles facetas

1. **Facetas jerárquicas**: Organizar personajes en categorías como "Principales", "Secundarios" y "Extras".
2. **Facetas por rangos**: Para campos como `imdb_rating` y `views`.

---

## Interfaz gráfica de usuario

### Pantalla inicial
Opciones:
1. **Búsqueda Simple**: Selección de índice (Guiones o Capítulos Unidos).
2. **Búsqueda por Campos**: Introducir campos específicos y operadores AND/OR.

### Funcionalidades de la interfaz
- TextFields para consultas y JComboBox para seleccionar índices/facetas.
- Resultados mostrados en un JTextArea.
- Soporte para búsquedas simples y booleanas.

---

## Trabajo en grupo

- **Cristóbal Jiménez Álvarez**: Implementación de búsqueda simple, facetas y diseño de la interfaz.
- **Rafael Luque Framit**: Indexación, búsqueda booleana y redacción de la memoria.

---

## Manual de Usuario

### Pasos iniciales
1. **Apertura de la interfaz**:
    - Elige entre "Búsqueda Simple" o "Búsqueda por Campos".

### Búsqueda Simple
- Escribe la consulta.
- Filtra resultados por categorías:
  - `Localización` para Guiones.
  - `Season` para Capítulos Unidos.

### Búsqueda Booleana
- Especifica los campos y operador AND/OR.
- Filtra los resultados usando facetas específicas.

---

### Notas adicionales
- `imdb_rating` tiene un margen de error de ±0.2.
- `views` tiene un margen de ±5000.
- Introduce fechas como `YYYY-MM-DD`.

