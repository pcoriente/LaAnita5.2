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
import java.io.InputStreamReader;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import pedidos.dao.DAOCargaPedidos;
import pedidos.dominio.Chedraui;
import pedidos.dominio.Coma;
import pedidos.dominio.ComercialMexicana;
import pedidos.dominio.Corvi;
import pedidos.dominio.Imss;
import pedidos.dominio.SamsClub;
import pedidos.dominio.WallMart;

/**
 *
 * @author Usuario
 */
public class LeerTextuales {

    public ArrayList<SamsClub> leerArchivoSams(File file) throws FileNotFoundException, IOException {
        ArrayList<SamsClub> lstPedidoSamsClub = new ArrayList<SamsClub>();
        BufferedReader in = new BufferedReader(new FileReader(file));
        String registro;
        String anio;
        String mes;
        String dia;
        String fecha;
        registro = in.readLine();
        while ((registro = in.readLine()) != null) {
            String[] pedidoArray;
            pedidoArray = registro.split(",");
            SamsClub sams = new SamsClub();
            sams.setOrdenCompra(pedidoArray[0]);
            sams.setDepartamento(pedidoArray[1]);
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
            try {
                sams.setEmpaque(Float.parseFloat(pedidoArray[19]));
            } catch (NumberFormatException e) {
                Mensajes.mensajeAlert(e.getMessage());
                break;
            }
            lstPedidoSamsClub.add(sams);
        }
        return lstPedidoSamsClub;
    }

//    public void leerArchivoCHedraui(String lectura) throws IOException {
    public ArrayList<Chedraui> leerArchivoCHedraui(String lectura) throws IOException {
        String registro;;
        String anio;
        String mes;
        String dia;
        String fecha;

        ArrayList<Chedraui> lstChedarui = new ArrayList<>();
        // Abrimos el archivo
        FileInputStream fstream = new FileInputStream(lectura);
        // Creamos el Buffer de Lectura
        try ( // Creamos el objeto de entrada
                DataInputStream entrada = new DataInputStream(fstream)) {
            // Creamos el Buffer de Lectura
            BufferedReader buffer = new BufferedReader(new InputStreamReader(entrada));

            // Leer el archivo linea por linea
            registro = buffer.readLine();
            while ((registro = buffer.readLine()) != null) {
                System.out.println(registro);
                String[] pedidoArray;
                pedidoArray = registro.split(",");
                Chedraui che = new Chedraui();
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
                che.setUpc(pedidoArray[5].substring(2, 13));
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
                lstChedarui.add(che);
            }
            entrada.close();
            return lstChedarui;
        }
    }

    public ArrayList<Imss> leerArchivoImss(File archivoTexto) throws IOException {
        ArrayList<Imss> lstIms = new ArrayList<Imss>();
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
            Imss ims = new Imss();
            ims.setOrdenCompra(pedidoArray[0]);
            anio = pedidoArray[1].substring(6);
            mes = pedidoArray[1].substring(3, 5);
            dia = pedidoArray[1].substring(0, 2);
            fecha = anio + "-" + mes + "-" + dia;
            ims.setFechaElaboracion(Date.valueOf(fecha));
            ims.setFechaEmbarque(Date.valueOf(fecha));
            anio = pedidoArray[2].substring(6);
            mes = pedidoArray[2].substring(3, 5);
            dia = pedidoArray[2].substring(0, 2);
            fecha = anio + "-" + mes + "-" + dia;
            ims.setFechaCancelacion(Date.valueOf(fecha));
            ims.setCodigoTienda(Integer.parseInt(pedidoArray[3]));
            ims.setDescripcion(pedidoArray[12]);
            ims.setSku(pedidoArray[10]);
            ims.setUpc(pedidoArray[11]);
            ims.setCantidad((Float.parseFloat(pedidoArray[14].substring(0, 2))) * (Float.parseFloat(pedidoArray[16])));
            ims.setEmpaque(Float.parseFloat(pedidoArray[14].substring(0, 2)));
            ims.setCosto(Float.parseFloat(pedidoArray[17]));
            ims.setNumeroProveedor(pedidoArray[5]);
            lstIms.add(ims);
        }
        return lstIms;
    }

    public ArrayList<ComercialMexicana> leerArchivoComercialMexicana(File archivoTexto, Date fechaEmbarque, Date fechaConcelacion, boolean anita) throws IOException {
        ArrayList<ComercialMexicana> lstComercialMexicana = new ArrayList<ComercialMexicana>();
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
            Imss ims = new Imss();
            ComercialMexicana comercial = new ComercialMexicana();
            comercial.setOrdenCompra(pedidoArray[0]);
            anio = pedidoArray[1].substring(0, 4);
            mes = pedidoArray[1].substring(4, 6);
            dia = pedidoArray[1].substring(6);
            fecha = anio + "-" + mes + "-" + dia;
            comercial.setFechaElaboracion(Date.valueOf(fecha));
            comercial.setFechaEmbarque(fechaEmbarque);
            comercial.setFechaCancelacion(fechaConcelacion);
            comercial.setNumeroProveedor(pedidoArray[4]);
            if (anita == true) {
                comercial.setUpc("0" + pedidoArray[6]);
            } else {
                comercial.setUpc(pedidoArray[6]);
            }
            comercial.setCodigoTienda(Integer.parseInt(pedidoArray[7].substring(17)));
            comercial.setCantidad((Float.parseFloat(pedidoArray[9]) + Float.parseFloat(pedidoArray[15])));
            comercial.setPendientePorSurtir(Float.parseFloat(pedidoArray[15]));
            comercial.setCamasPorPallet(Double.parseDouble(pedidoArray[16]));
            comercial.setCajasPorCamas(Double.parseDouble(pedidoArray[17]));
            lstComercialMexicana.add(comercial);
        }
        return lstComercialMexicana;
    }

    public ArrayList<WallMart> leerArchivoWallMart(File archivoTexto) throws IOException, SQLException {
        DAOCargaPedidos dao = new DAOCargaPedidos();
        ArrayList<WallMart> lstWallMart = new ArrayList<WallMart>();
        BufferedReader in = new BufferedReader(new FileReader(archivoTexto));
        String registro;
        String anio;
        String mes;
        String dia;
        String fecha;
        HashMap aGln = null;
        try {
            aGln = dao.leeEntregasWallMart();
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        registro = in.readLine();
        while ((registro = in.readLine()) != null) {
            String[] pedidoArray;
            pedidoArray = registro.split(",");
            WallMart wallMart = new WallMart();
            wallMart.setOrdenCompra(pedidoArray[0]);
            wallMart.setDepartamento(pedidoArray[1]);
            anio = pedidoArray[2].substring(0, 4);
            mes = pedidoArray[2].substring(4, 6);
            dia = pedidoArray[2].substring(6);
            fecha = anio + "-" + mes + "-" + dia;
            wallMart.setFechaEmbarque(Date.valueOf(fecha));
            anio = pedidoArray[3].substring(0, 4);
            mes = pedidoArray[3].substring(4, 6);
            dia = pedidoArray[3].substring(6);
            fecha = anio + "-" + mes + "-" + dia;
            wallMart.setFechaCancelacion(Date.valueOf(fecha));
            Object obj = aGln.get(pedidoArray[4].substring(0, 5));
            if (obj != null) {
                try {
                    wallMart.setCodigoTienda(Integer.parseInt(obj.toString().trim()));
                } catch (NumberFormatException e) {
                    Mensajes.mensajeError(e.getMessage());
                }
            }
            wallMart.setUpc(pedidoArray[5]);
            wallMart.setSku(pedidoArray[6]);
            try {
                wallMart.setEmpaque(Float.parseFloat(pedidoArray[18]));
                wallMart.setCantidad(Float.parseFloat(pedidoArray[10]));
                wallMart.setCosto(Double.parseDouble(pedidoArray[11]));
            } catch (NumberFormatException e) {
                Mensajes.mensajeError(e.getMessage());
                break;
            }
            anio = pedidoArray[15].substring(0, 4);
            mes = pedidoArray[15].substring(4, 6);
            dia = pedidoArray[15].substring(6);
            fecha = anio + "-" + mes + "-" + dia;
            wallMart.setFechaElaboracion(Date.valueOf(fecha));
            wallMart.setNumeroProveedor(pedidoArray[14]);
            lstWallMart.add(wallMart);
        }
        return lstWallMart;
    }

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
