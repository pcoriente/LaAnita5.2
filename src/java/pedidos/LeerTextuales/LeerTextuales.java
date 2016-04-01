/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pedidos.LeerTextuales;

import Message.Mensajes;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import pedidos.dao.DAOCargaPedidos;
import pedidos.dominio.Textual;
//import pedidos.dominio.Chedraui;
import pedidos.dominio.Coma;
//import pedidos.dominio.ComercialMexicana;
import pedidos.dominio.Corvi;
////import pedidos.dominio.Imss;
//import pedidos.dominio.SamsClub;
import pedidos.dominio.WallMart;

/**
 *
 * @author Usuario
 */
public class LeerTextuales {

    public ArrayList<Textual> leerArchivoSams(String lectura) throws IOException {
        String registro;
        String anio;
        String mes;
        String dia;
        String fecha;

        ArrayList<Textual> lstSams = new ArrayList<>();
        FileInputStream fstream = new FileInputStream(lectura);
        try (DataInputStream entrada = new DataInputStream(fstream)) {
            BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));
            registro = buffer.readLine();
            while ((registro = buffer.readLine()) != null) {
                String[] pedidoArray;
                pedidoArray = registro.split(",");
                Textual sams = new Textual();
                sams.setOrdenCompra(pedidoArray[0]);
//                sams.setDepartamento(pedidoArray[1]);
                anio = pedidoArray[2].substring(0, 4);
                mes = pedidoArray[2].substring(4, 6);
                dia = pedidoArray[2].substring(6);
                fecha = anio + "-" + mes + "-" + dia;
                sams.setFechaEmbarque(Date.valueOf(fecha));
                anio = pedidoArray[3].substring(0, 4);
                mes = pedidoArray[3].substring(4, 6);
                dia = pedidoArray[3].substring(6);
                fecha = anio + "-" + mes + "-" + dia;
                sams.setFechaCancelacion(Date.valueOf(fecha));
                try {
                    sams.setCodigoTienda(Integer.parseInt(pedidoArray[0].substring(0, 4)));
                } catch (NumberFormatException e) {
                    Mensajes.mensajeAlert(e.getMessage());
                    break;
                }
                sams.setUpc(pedidoArray[5]);
                sams.setSku(pedidoArray[6]);
                try {
                    sams.setCantidad(Double.parseDouble(pedidoArray[11]));
                    sams.setCosto(Double.parseDouble(pedidoArray[12]));
                } catch (NumberFormatException e) {
                    Mensajes.mensajeAlert(e.getMessage());
                    break;
                }
                anio = pedidoArray[16].substring(0, 4);
                mes = pedidoArray[16].substring(4, 6);
                dia = pedidoArray[16].substring(6);
                fecha = anio + "-" + mes + "-" + dia;
                sams.setFechaElaboracion(Date.valueOf(fecha));
                sams.setNumeroProveedor(pedidoArray[15]);
//                try {
//                    sams.setEmpaque(Float.parseFloat(pedidoArray[19]));
//                } catch (NumberFormatException e) {
//                    Mensajes.mensajeAlert(e.getMessage());
//                    break;
//                }
                sams.setEmpaque(pedidoArray[19]);
                lstSams.add(sams);
            }
            entrada.close();
            return lstSams;
        }
    }
//    public void leerArchivoCHedraui(String lectura) throws IOException {

    public ArrayList<Textual> leerArchivoImss(String lectura) throws IOException {
        String registro;
        String anio;
        String mes;
        String dia;
        String fecha;

        ArrayList<Textual> lstImss = new ArrayList<>();
        FileInputStream fstream = new FileInputStream(lectura);
        try (DataInputStream entrada = new DataInputStream(fstream)) {// Creamos el objeto de entrada
            BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));
            registro = buffer.readLine();
            while ((registro = buffer.readLine()) != null) {
                String[] pedidoArray;
                pedidoArray = registro.split(",");
                Textual imss = new Textual();
                imss.setOrdenCompra(pedidoArray[0]);
                anio = pedidoArray[1].substring(6);
                mes = pedidoArray[1].substring(3, 5);
                dia = pedidoArray[1].substring(0, 2);
                fecha = anio + "-" + mes + "-" + dia;
                imss.setFechaElaboracion(Date.valueOf(fecha));
                imss.setFechaEmbarque(Date.valueOf(fecha));
                anio = pedidoArray[2].substring(6);
                mes = pedidoArray[2].substring(3, 5);
                dia = pedidoArray[2].substring(0, 2);
                fecha = anio + "-" + mes + "-" + dia;
                imss.setFechaCancelacion(Date.valueOf(fecha));
                imss.setCodigoTienda(Integer.parseInt(pedidoArray[3]));
//            ims.setDescripcion(pedidoArray[12]);
                imss.setSku(pedidoArray[10]);
                imss.setUpc(pedidoArray[11]);
                imss.setCantidad((Float.parseFloat(pedidoArray[14].substring(0, 2))) * (Float.parseFloat(pedidoArray[16])));
//            ims.setEmpaque(Float.parseFloat(pedidoArray[14].substring(0, 2)));
                imss.setEmpaque(pedidoArray[14]);
                imss.setCosto(Float.parseFloat(pedidoArray[17]));
                imss.setNumeroProveedor(pedidoArray[5]);
                lstImss.add(imss);
            }
            entrada.close();
            return lstImss;
        }
    }

    public ArrayList<Textual> leerArchivoCHedraui(String lectura) throws IOException {
        String registro;
        String anio;
        String mes;
        String dia;
        String fecha;

        ArrayList<Textual> lstChedraui = new ArrayList<>();
        // Abrimos el archivo
        FileInputStream fstream = new FileInputStream(lectura);// 
        // Creamos el Buffer de Lectura
        try (DataInputStream entrada = new DataInputStream(fstream)) {// Creamos el objeto de entrada//
            // Creamos el Buffer de Lectura
            BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));
            // Leer el archivo linea por linea
            registro = buffer.readLine();
            while ((registro = buffer.readLine()) != null) {
                System.out.println(registro);
                String[] pedidoArray;
                pedidoArray = registro.split(",");
                Textual che = new Textual();
                che.setOrdenCompra(pedidoArray[0]);
                anio = pedidoArray[2].substring(0, 4);
                mes = pedidoArray[2].substring(4, 6);
                dia = pedidoArray[2].substring(6);
                fecha = anio + "-" + mes + "-" + dia;
                try {
                    che.setFechaEmbarque(Date.valueOf(fecha));
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
                anio = pedidoArray[3].substring(0, 4);
                mes = pedidoArray[3].substring(4, 6);
                dia = pedidoArray[3].substring(6);
                fecha = anio + "-" + mes + "-" + dia;
                try {
                    che.setFechaCancelacion(Date.valueOf(fecha));
                } catch (Exception e) {
                    System.err.println(e);
                }
                try {
                    che.setCodigoTienda(Integer.parseInt(pedidoArray[4].substring(0, 5)));
                } catch (NumberFormatException e) {
                    Message.Mensajes.mensajeError(e.getMessage());
                    break;
                }
                che.setUpc(pedidoArray[5].substring(1, 13));
                che.setEmpaque(pedidoArray[8]);
                try {
                    che.setCantidad(Double.parseDouble(pedidoArray[9]));
                } catch (NumberFormatException ex) {
                    Mensajes.mensajeError(ex.getMessage());
                }
                che.setCosto(Double.parseDouble(pedidoArray[10]));
                anio = pedidoArray[12].substring(0, 4);
                mes = pedidoArray[12].substring(4, 6);
                dia = pedidoArray[12].substring(6);
                fecha = anio + "-" + mes + "-" + dia;
                che.setFechaElaboracion(Date.valueOf(fecha));
                che.setNumeroProveedor(pedidoArray[13]);
                lstChedraui.add(che);
            }
            entrada.close();
            return lstChedraui;
        }
    }

    public ArrayList<Textual> leerArchivoComercialMexicana(String lectura, java.util.Date fechaEntrega, java.util.Date fechaCancelacion) throws IOException {
        String registro;
        String anio;
        String mes;
        String dia;
        String fecha;
        ArrayList<Textual> lstComercialMexicana = new ArrayList<>();
        FileInputStream fstream = new FileInputStream(lectura);
        try (DataInputStream entrada = new DataInputStream(fstream)) {
            BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));
            registro = buffer.readLine();
            while ((registro = buffer.readLine()) != null) {
                String[] pedidoArray;
                pedidoArray = registro.split(",");
//            Imss ims = new Imss();
                Textual comercial = new Textual();
                comercial.setOrdenCompra(pedidoArray[0]);
                anio = pedidoArray[1].substring(0, 4);
                mes = pedidoArray[1].substring(4, 6);
                dia = pedidoArray[1].substring(6);
                fecha = anio + "-" + mes + "-" + dia;
                comercial.setFechaElaboracion(Date.valueOf(fecha));
                comercial.setFechaEmbarque(new java.sql.Date(fechaEntrega.getTime()));
                comercial.setFechaCancelacion(new java.sql.Date(fechaCancelacion.getTime()));
//                comercial.setFechaEmbarque(fechaEmbarque);
//                comercial.setFechaCancelacion(fechaConcelacion);
                comercial.setNumeroProveedor(pedidoArray[4]);
                comercial.setUpc(pedidoArray[6].trim());
                if (comercial.getUpc().length() == 11) {
                    comercial.setUpc("0" + comercial.getUpc());
                }
//            if (anita == true) {
//                comercial.setUpc("0" + pedidoArray[6]);
//            } else {
//                comercial.setUpc(pedidoArray[6]);
//            }
                comercial.setCodigoTienda(Integer.parseInt(pedidoArray[7].substring(16, 19)));
                comercial.setCantidad(Float.parseFloat(pedidoArray[9]));
//            comercial.setCantidad((Float.parseFloat(pedidoArray[9]) + Float.parseFloat(pedidoArray[15])));
//            comercial.setPendientePorSurtir(Float.parseFloat(pedidoArray[15]));
//            comercial.setCamasPorPallet(Double.parseDouble(pedidoArray[16]));
//            comercial.setCajasPorCamas(Double.parseDouble(pedidoArray[17]));
                lstComercialMexicana.add(comercial);
            }
            entrada.close();
            return lstComercialMexicana;
        }
    }

//    public ArrayList<Textual> leerArchivoSoriana(String lectura) throws IOException {
    public ArrayList<Textual> leerArchivoSoriana(String lectura) throws IOException {
        ArrayList<Textual> lstSoriana = new ArrayList<>();
        InputStream excelStream = null;
        try {
            excelStream = new FileInputStream(lectura);
            HSSFWorkbook workbook = new HSSFWorkbook(excelStream);
            HSSFSheet sheet = workbook.getSheetAt(0);
            HSSFRow fila;
            int totFilas = sheet.getLastRowNum();
            System.out.println("total de Registros "+totFilas);
            int r = 0;
            fila = sheet.getRow(r); 
            for ( r = 0; r < totFilas; r++) {
                fila = sheet.getRow(r);
                //String cellTda = fila.getCell(0).getStringCellValue();
                String cellTda = fila.getCell(0) == null ? ""
                        : (fila.getCell(0).getCellType() == Cell.CELL_TYPE_STRING) ? fila.getCell(0).getStringCellValue()
                                : (fila.getCell(0).getCellType() == Cell.CELL_TYPE_NUMERIC) ? "" + fila.getCell(0).getNumericCellValue() : "";
                            //String celltda = (fila.getCell(0).getCellType() == Cell.CELL_TYPE_STRING) ? fila.getCell(0).getStringCellValue();
                //hssfRow.getCell(c).getCellType() == Cell.CELL_TYPE_STRING)?hssfRow.getCell(c).getStringCellValue():
                //HSSFCell cellTda = fila.fila.getCell(r).;
                //(hssfRow.getCell(c).getCellType() == Cell.CELL_TYPE_STRING)?hssfRow.getCell(c).getStringCellValue():

                System.out.println("va la celda tienda " + cellTda+" numero de registro "+r);
            }
        } catch (IOException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }

//            int totFilas;
//            Workbook workbook = null;
//            try {
//                workbook = Workbook.getWorkbook(new File(lectura));
//            } catch (IOException | BiffException ex) {
//                Mensajes.mensajeError(ex.getMessage());
//                //Logger.getLogger(CargaGlnWallMart.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            Sheet sheet = workbook.getSheet(0);
//            totFilas = sheet.getRows();
//            System.out.println(totFilas);
//        String registro = null;
//        FileInputStream fstream = new FileInputStream(lectura);
//        DataInputStream entrada = new DataInputStream(fstream);
//
//        BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));
//        while ((registro = buffer.readLine()) != null) {
//            System.out.println(registro);
//        }
//        String registro = null;
//        String anio, mes, dia;
////        String mes;
////        String dia;
//        //String fecha;
//        String fechaElaboracion, fechaEmbarque, fechaCancelacion;
//        String numeroProveedor, ordenCompra;
////        String fechaEmbarque;
////        String fechaCancelacion;
//        String[] pedidoArray;
        
//        FileInputStream fstream = new FileInputStream(lectura);
//        try (DataInputStream entrada = new DataInputStream(fstream)) {
//            BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));
//            registro = buffer.readLine();
//            pedidoArray = registro.split(",");
////            sori.setNumeroProveedor(pedidoArray[1]);
//            numeroProveedor = pedidoArray[1];
////            sori.setOrdenCompra(pedidoArray[3]);
//            ordenCompra = pedidoArray[3];
//            registro = buffer.readLine();
//            pedidoArray = registro.split(",");
//            anio = pedidoArray[1].substring(6);
//            mes = pedidoArray[1].substring(3, 5);
//            dia = pedidoArray[1].substring(0, 2);
//            fechaElaboracion = anio + "-" + mes + "-" + dia;
//            //sori.setFechaElaboracion(Date.valueOf(fecha));
//            registro = buffer.readLine();
//            registro = buffer.readLine();
//            registro = buffer.readLine();
//            registro = buffer.readLine();
//            pedidoArray = registro.split(",");
//            anio = pedidoArray[0].substring(6);
//            mes = pedidoArray[0].substring(3, 5);
//            dia = pedidoArray[0].substring(0, 2);
//            fechaEmbarque = anio + "-" + mes + "-" + dia;
//            //sori.setFechaEmbarque(Date.valueOf(fecha));
//            anio = pedidoArray[1].substring(6);
//            mes = pedidoArray[1].substring(3, 5);
//            dia = pedidoArray[1].substring(0, 2);
//            fechaCancelacion = anio + "-" + mes + "-" + dia;
//            //sori.setFechaCancelacion(Date.valueOf(fecha));
//
//            registro = buffer.readLine();
//            while ((registro = buffer.readLine()) != null) {
//                pedidoArray = registro.split(",");
//                Textual sori = new Textual();
//                sori.setNumeroProveedor(numeroProveedor);
//                sori.setOrdenCompra(ordenCompra);
//                sori.setFechaElaboracion(Date.valueOf(fechaElaboracion));
//                sori.setFechaEmbarque(Date.valueOf(fechaEmbarque));
//                sori.setFechaEmbarque(Date.valueOf(fechaCancelacion));
//                sori.setCodigoTienda(Integer.parseInt(pedidoArray[2]));
//                sori.setUpc(pedidoArray[4]);
//                sori.setCantidad(Float.parseFloat(pedidoArray[3]));
//                sori.setCosto(Float.parseFloat(pedidoArray[6]));
//                System.out.println("Orden Compra "+sori.getOrdenCompra()+" numero tienda "+sori.getCodigoTienda()+" codigo "+sori.getUpc());
//                lstSoriana.add(sori);
//            }
            excelStream.close();
            return lstSoriana;
        }
//    }
        //    public ArrayList<WallMart> leerArchivoWallMart(File archivoTexto) throws IOException, SQLException {
    //        DAOCargaPedidos dao = new DAOCargaPedidos();
    //        ArrayList<WallMart> lstWallMart = new ArrayList<WallMart>();
    //        BufferedReader in = new BufferedReader(new FileReader(archivoTexto));
    //        String registro;
    //        String anio;
    //        String mes;
    //        String dia;
    //        String fecha;
    //        HashMap aGln = null;
    //        try {
    //            aGln = dao.leeEntregasWallMart();
    //        } catch (SQLException ex) {
    //            Mensajes.mensajeError(ex.getMessage());
    //        }
    //        registro = in.readLine();
    //        while ((registro = in.readLine()) != null) {
    //            String[] pedidoArray;
    //            pedidoArray = registro.split(",");
    //            WallMart wallMart = new WallMart();
    //            wallMart.setOrdenCompra(pedidoArray[0]);
    //            wallMart.setDepartamento(pedidoArray[1]);
    //            anio = pedidoArray[2].substring(0, 4);
    //            mes = pedidoArray[2].substring(4, 6);
    //            dia = pedidoArray[2].substring(6);
    //            fecha = anio + "-" + mes + "-" + dia;
    //            wallMart.setFechaEmbarque(Date.valueOf(fecha));
    //            anio = pedidoArray[3].substring(0, 4);
    //            mes = pedidoArray[3].substring(4, 6);
    //            dia = pedidoArray[3].substring(6);
    //            fecha = anio + "-" + mes + "-" + dia;
    //            wallMart.setFechaCancelacion(Date.valueOf(fecha));
    //            Object obj = aGln.get(pedidoArray[4].substring(0, 5));
    //            if (obj != null) {
    //                try {
    //                    wallMart.setCodigoTienda(Integer.parseInt(obj.toString().trim()));
    //                } catch (NumberFormatException e) {
    //                    Mensajes.mensajeError(e.getMessage());
    //                }
    //            }
    //            wallMart.setUpc(pedidoArray[5]);
    //            wallMart.setSku(pedidoArray[6]);
    //            try {
    //                wallMart.setEmpaque(Float.parseFloat(pedidoArray[18]));
    //                wallMart.setCantidad(Float.parseFloat(pedidoArray[10]));
    //                wallMart.setCosto(Double.parseDouble(pedidoArray[11]));
    //            } catch (NumberFormatException e) {
    //                Mensajes.mensajeError(e.getMessage());
    //                break;
    //            }
    //            anio = pedidoArray[15].substring(0, 4);
    //            mes = pedidoArray[15].substring(4, 6);
    //            dia = pedidoArray[15].substring(6);
    //            fecha = anio + "-" + mes + "-" + dia;
    //            wallMart.setFechaElaboracion(Date.valueOf(fecha));
    //            wallMart.setNumeroProveedor(pedidoArray[14]);
    //            lstWallMart.add(wallMart);
    //        }
    //        return lstWallMart;
    //    }
    //    }

    public ArrayList leerArchivoComa(File archivoTexto) throws IOException {
        ArrayList<Coma> lstComa = new ArrayList<Coma>();
        BufferedReader in = new BufferedReader(new FileReader(archivoTexto));
        String registro;
        String anio;
        String mes;
        String dia;
        String fecha;
        registro = in.readLine();
        while ((registro = in.readLine()) != null) {
            String[] pedidoArray;
            pedidoArray = registro.split(",");
            Coma coma = new Coma();
            coma.setOrdenCompra(pedidoArray[0]);
            coma.setNumeroProveedor(pedidoArray[1]);
            anio = pedidoArray[2].substring(6);
            mes = pedidoArray[2].substring(3, 5);
            dia = pedidoArray[2].substring(0, 2);
            fecha = anio + "-" + mes + "-" + dia;
            coma.setFechaEmbarque(Date.valueOf(fecha));
            coma.setFechaElaboracion(Date.valueOf(fecha));
            anio = pedidoArray[3].substring(6);
            mes = pedidoArray[3].substring(3, 5);
            dia = pedidoArray[3].substring(0, 2);
            fecha = anio + "-" + mes + "-" + dia;
            coma.setFechaCancelacion(Date.valueOf(fecha));
            coma.setDescripcion(pedidoArray[5]);
            coma.setSku(pedidoArray[6]);
            try {
                coma.setEmpaque(Float.parseFloat(pedidoArray[7]));
                coma.setCantidad(Double.parseDouble(pedidoArray[10]));
                coma.setMercanciasSinCargo(Double.parseDouble(pedidoArray[11]));
                coma.setCosto(Double.parseDouble(pedidoArray[13]));
                coma.setCodigoTienda(Integer.parseInt(pedidoArray[17].substring(9)));
//            VERIFICAR LOS CALCULOS QUE ESTAN EN CAJAS.
                lstComa.add(coma);
            } catch (NumberFormatException e) {
                Mensajes.mensajeError(e.getMessage());
                break;
            }
        }
        return lstComa;
    }

    public ArrayList leerArchivoCorvi(File archivoTexto, Date fechaCancelacion) throws IOException {
        ArrayList<Corvi> lstComa = new ArrayList<Corvi>();
        BufferedReader in = new BufferedReader(new FileReader(archivoTexto));
        String registro;
        String anio;
        String mes;
        String dia;
        String fecha;
        registro = in.readLine();
        while ((registro = in.readLine()) != null) {
            String[] pedidoArray;
            pedidoArray = registro.split(",");
            Corvi corvi = new Corvi();
            corvi.setOrdenCompra(pedidoArray[0]);
            corvi.setCodigoTienda(Integer.parseInt(pedidoArray[1].substring(9)));
            anio = pedidoArray[3].substring(6);
            mes = pedidoArray[3].substring(3, 5);
            dia = pedidoArray[3].substring(0, 2);
            fecha = anio + "-" + mes + "-" + dia;
            corvi.setFechaElaboracion(Date.valueOf(fecha));
            anio = pedidoArray[4].substring(6);
            mes = pedidoArray[4].substring(3, 5);
            dia = pedidoArray[4].substring(0, 2);
            corvi.setFechaEmbarque(Date.valueOf(fecha));
            corvi.setUpc(pedidoArray[5].substring(2));
            corvi.setSku(pedidoArray[6]);
            corvi.setDescripcion(pedidoArray[7]);
            corvi.setCantidad(Double.parseDouble(pedidoArray[8]));
            corvi.setMercanciasSinCargo(Double.parseDouble(pedidoArray[9]));
            corvi.setCosto(Double.parseDouble(pedidoArray[12]));
            corvi.setFechaCancelacion(fechaCancelacion);
            lstComa.add(corvi);
        }
        return lstComa;
    }

}
