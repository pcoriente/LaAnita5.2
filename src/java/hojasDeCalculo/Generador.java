package hojasDeCalculo;

import formulas.dominio.Insumo;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 *
 * @author jesc
 */
public class Generador {
    // documento con las hojas de calculo
    private final Workbook libro;
 
    // la hoja de calculo
    private final Sheet hoja;
 
    // estilo de las celdas del encabezado (con el nombre de las columnas)
    private final CellStyle estiloTitulo;
 
    // estilo de las celdas con fórmula
    private final CellStyle estiloCeldaConFormula;
    
    private final CellStyle estiloCeldaNumero;
    
    private String nombreDeLaHoja;
    private int nTits;
    private int filas;
    
//    DecimalFormat df;
    
    public Generador() {
        this.nombreDeLaHoja="SinNombre";
        this.libro = new HSSFWorkbook();
        this.hoja = this.libro.createSheet(this.nombreDeLaHoja);
        this.estiloTitulo = getEstiloTitulo();
        this.estiloCeldaConFormula = getEstiloCeldaConFormula();
        this.estiloCeldaNumero = getEstiloNumero();
        this.nTits=0;
    }
    
    public Generador(String nombreDeLaHoja) {
        this.nombreDeLaHoja=nombreDeLaHoja;
        this.libro = new HSSFWorkbook();
        this.hoja = this.libro.createSheet(this.nombreDeLaHoja);
        this.estiloTitulo = getEstiloTitulo();
        this.estiloCeldaConFormula = getEstiloCeldaConFormula();
        this.estiloCeldaNumero = getEstiloNumero();
        this.nTits=0;
    }
    
    public void setCeldaFormatoNumero(Cell c) {
        c.setCellStyle(this.estiloCeldaNumero);
    }
    
    public Cell agregarColumnaFormula(int f, String formula) {
        Row fila=this.hoja.getRow(f-1);
        Cell celda=fila.createCell(fila.getLastCellNum());
        celda.setCellFormula(formula);
        return celda;
    }
    
    public void inicializaFilas() {
        this.filas=0;
    }
    
    // ajusta el ancho de las columnas en función de su contenido
    private void ajustaColumnas() {
        final short numeroColumnas = hoja.getRow(0).getLastCellNum();
        for (int i = 0; i < numeroColumnas; i++) {
            hoja.autoSizeColumn(i);
        }
    }
    
    // genera el documento
    public OutputStream generaDocumento() throws IOException {
        ajustaColumnas();
        final OutputStream outputStream = new FileOutputStream(nombreDeLaHoja+".xls");
        libro.write(outputStream);
        outputStream.close();
        return outputStream;
    }
    
    public void prueba(ArrayList<Cell> celdas) {
        
    }
    
    // añade la fórmula a una celda y añade el estilo de las celdas con fórmula
    private void anadeFormulaYEstiloACelda(Cell celda, String formula) {
        celda.setCellFormula(formula);
        celda.setCellStyle(estiloCeldaConFormula);
        celda.getCellStyle().setDataFormat(this.estiloCeldaNumero.getDataFormat());
    }
    
    // devuelve el rango de columnas sobre las que actuará la formula. Ej: (B2:F2)
    private static String generaRangoFormulaEnColumna(char columna, int nTits, int filas) {
        // la columna donde se situa el primer tiempo será la B (codigo ASCII 66) ya que en la A está el nombre del piloto)
        final int primeraFila = nTits;
        final int ultimaFila = primeraFila + filas - 1;
        return "(" + columna + primeraFila + ":" + columna + ultimaFila + ")";
    }
    
    private void generaFormulaSuma(Row f, String formula, char columna) {
        final byte posColumna = (byte) ((byte)columna - 65);
        int inicial=f.getRowNum()-this.filas+1;
        formula=formula+generaRangoFormulaEnColumna(columna, inicial, this.filas);
        anadeFormulaYEstiloACelda(f.createCell(posColumna), formula);
    }
    
//    // devuelve el rango de columnas sobre las que actuará la formula. Ej: (B2:F2)
//    private static String generaRangoFormulaEnFila(int numeroFila) {
//        // la columna donde se situa el primer tiempo será la B (codigo ASCII 66) ya que en la A está el nombre del piloto)
//        final byte columnaB = 66;
//        final char primeraColumna = (char)columnaB;
//        final char ultimaColumna = (char)columnaB + Insumo.NUMERO_VUELTAS_ENTRENAMIENTO - 1;
//        return "(" + primeraColumna + numeroFila + ":" + ultimaColumna + numeroFila + ")";
//    }
//    
//    // crea la celda con la fórmula de suma de tiempos correspondiente a una fila
//    private void generaFormulaSumaTiempos(Row f) {
//        final int numeroFila = f.getRowNum() + 1;
//        final String formula = "SUM" + generaRangoFormulaEnFila(numeroFila);
//        anadeFormulaYEstiloACelda(f.createCell(Insumo.NUMERO_VUELTAS_ENTRENAMIENTO + 1), formula);
//    }
    
//    // crea la celda con la fórmula de suma de tiempos correspondiente a una fila
//    private void generaFormulaSumaTiempos(Row f) {
//        final int numeroFila = f.getRowNum() + 1;
//        final String formula = "SUM" + generaRangoFormulaEnFila(numeroFila);
//        anadeFormulaYEstiloACelda(f.createCell(Insumo.NUMERO_VUELTAS_ENTRENAMIENTO + 1), formula);
//    }
//    // crea la celda con la fórmula de media de tiempos correspondiente a una fila
//    private void generaFormulaMediaTiempos(Row f) {
//        final int numeroFila = f.getRowNum() + 1;
//        final String formula = "AVERAGE" + generaRangoFormulaEnFila(numeroFila);
//        anadeFormulaYEstiloACelda(f.createCell(Insumo.NUMERO_VUELTAS_ENTRENAMIENTO + 2), formula);
//    }
//     // crea la celda con la fórmula de que calcula el mejor tiempo a una fila
//    private void generaFormulaMejorTiempo(Row f) {
//        final int numeroFila = f.getRowNum() + 1;
//        final String formula = "MIN" + generaRangoFormulaEnFila(numeroFila);
//        anadeFormulaYEstiloACelda(f.createCell(Insumo.NUMERO_VUELTAS_ENTRENAMIENTO + 3), formula);
//    }
    
    public int agregarTotales() {
        final Row fila = getNuevaFila();
        this.generaFormulaSuma(fila, "SUM", 'C');
        this.generaFormulaSuma(fila, "SUM", 'G');
        this.generaFormulaSuma(fila, "SUM", 'H');
        this.generaFormulaSuma(fila, "SUM", 'I');
        return fila.getRowNum()+1;
    }
    
    // crea una fila con los datos del piloto: nombre, tiempos, total, media y mejor tiempo
    public void agregaFila(Insumo insumo) {
//        this.df=new DecimalFormat("#####0.000000");
        final Row fila = getNuevaFila();
        int nFila=fila.getRowNum()+1;
        fila.createCell(0).setCellValue(insumo.getCod_pro());
        fila.createCell(1).setCellValue(insumo.getEmpaque());
        fila.createCell(2).setCellValue(Math.round(insumo.getCantidad()*1000000.00)/1000000.00);
        fila.createCell(3).setCellValue(Math.round((insumo.getCantidad()-insumo.getVariacion())*1000000.00)/1000000.00);
        fila.createCell(4).setCellValue(Math.round((insumo.getCantidad()+insumo.getVariacion())*1000000.00)/1000000.00);
        fila.createCell(5).setCellValue(Math.round(insumo.getCostoPromedio()*1000000.00)/1000000.00);
        fila.createCell(6).setCellFormula("C"+nFila+"*F"+nFila);
        fila.getCell(6).setCellStyle(this.estiloCeldaNumero);
//        fila.createCell(7).setCellValue(Math.round(insumo.getPtjeCtoParticipacion()*100000000.00)/1000000.00);
//        fila.createCell(8).setCellValue(Math.round(insumo.getPtjeCantParticipacion()*100000000.00)/1000000.00);
        this.filas++;
//        generaFormulaSumaTiempos(fila);
//        generaFormulaMediaTiempos(fila);
//        generaFormulaMejorTiempo(fila);
    }
    
    // crea una celda de encabezado (las del título) y añade el estilo
    private void creaCeldaEncabezado(Row filaEncabezado, int numeroCelda, String valor) {
        final Cell celdaEncabezado = filaEncabezado.createCell(numeroCelda);
        celdaEncabezado.setCellValue(valor);
        celdaEncabezado.setCellStyle(estiloTitulo);
    }
    
    // crea una nueva fila a continuación de la anterior
    private Row getNuevaFila() {
        return hoja.createRow(hoja.getPhysicalNumberOfRows());
    }
    
    // crea la fila y celdas del encabezado con el nombre de las columnas
    public void agregarFilaEncabezado(ArrayList<String> titulo) {
        final Row filaEncabezado = getNuevaFila();
        for (int c = 0; c < titulo.size(); c++) {
            creaCeldaEncabezado(filaEncabezado, c, titulo.get(c));
        }
        this.nTits++;
    }
    
    // devuelve el estilo que tendrán las celdas del título (negrita y color de fondo azul)
    private CellStyle getEstiloTitulo() {
        final CellStyle cellStyle = libro.createCellStyle();
        final Font cellFont = libro.createFont();
        cellFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        cellStyle.setFont(cellFont);
        cellStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        return cellStyle;
    }
     // devuelve el estilo que tendrán las celdas con fórmula (color de fondo gris claro)
    private CellStyle getEstiloCeldaConFormula() {
        final CellStyle cellStyle = libro.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        return cellStyle;
    }
    
    private CellStyle getEstiloNumero() {
        final CellStyle cs = libro.createCellStyle();
        cs.setDataFormat(libro.getCreationHelper().createDataFormat().getFormat("###,##0.000000"));
        return cs;
    }

    public int getFilas() {
        return filas;
    }

    public void setFilas(int filas) {
        this.filas = filas;
    }
}
