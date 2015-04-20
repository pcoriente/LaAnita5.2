/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pedidos;

import empresas.MbEmpresas;
import gruposBancos.MbGruposBancos;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
//import java.sql.SQLException;
//import java.util.Date;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedProperty;
import javax.inject.Named;
import mbMenuClientesGrupos.MbClientesGrupos;
import org.primefaces.model.UploadedFile;
import pedidos.LeerTextuales.LeerTextuales;

/**
 *
 * @author Usuario
 */
@Named(value = "mbCargaPedidos")
@SessionScoped
public class MbCargaPedidos implements Serializable {

    private UploadedFile file;
    @ManagedProperty(value = "#{mbEmpresas}")
    private MbEmpresas mbEmpresas = new MbEmpresas();
    @ManagedProperty(value = "#{mbClientesGrupos}")
    private MbGruposBancos mbGruposBancos = new MbGruposBancos();
    @ManagedProperty(value = "#{mbGruposBancos}")
    private MbClientesGrupos mbClientesGrupos = new MbClientesGrupos();
    private final String destino = "C:\\archivos\\";

    /**
     * Creates a new instance of MbPedido
     */
    public MbCargaPedidos() {
    }

    public void upload() {
        if (file != null) {
            try {
                copyFile(file.getFileName(), file.getInputstream());
                File archivo = new File(destino + file.getFileName());
                LeerTextuales textuales = new LeerTextuales();
//                textuales.leerArchivoWallMart(archivo);
//                textuales.leerArchivoSams(archivo);
//                textuales.leerArchivoCHedraui(archivo);
//                textuales.leerArchivoImss(archivo);
                String fecha = 2014 + "-" + 07 + "-" + 23;
//                textuales.leerArchivoComercialMexicana(archivo,  java.sql.Date.valueOf(fecha), java.sql.Date.valueOf(fecha), false);
//                textuales.leerArchivoComa(archivo);
                textuales.leerArchivoCorvi(archivo, java.sql.Date.valueOf(fecha));

            } catch (IOException ex) {
                Message.Mensajes.mensajeError(ex.getMessage());
            }
        } else {
            Message.Mensajes.mensajeAlert("Error, Seleccione un archov de texto a leer");
        }
    }

    public void cargarInformacion() {
//        mbClientesGrupos.getMbFormatos().setLstFormatos(null);
//        mbGruposBancos.setLstGruposBancos(null);
//        mbClientesGrupos.getMbFormatos().cargarListaFormatos(mbClientesGrupos.getCmbClientesGrupos().getIdGrupoCte());
//        mbGruposBancos.cargarGruposBancos(mbClientesGrupos.getCmbClientesGrupos().getIdGrupoCte());
    }

    public void copyFile(String fileName, InputStream in) {
        try {
            OutputStream out = new FileOutputStream(new File(destino + fileName));
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            in.close();
            out.flush();
            out.close();
            System.out.println("New file created!");
        } catch (IOException e) {
            Message.Mensajes.mensajeError(e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    public String salir() {
        return "index.xhtml";
    }

    public UploadedFile getFile() {
        return file;
    }

    public void setFile(UploadedFile file) {
        this.file = file;
    }

    public MbEmpresas getMbEmpresas() {
        return mbEmpresas;
    }

    public void setMbEmpresas(MbEmpresas mbEmpresas) {
        this.mbEmpresas = mbEmpresas;
    }

    public MbClientesGrupos getMbClientesGrupos() {
        return mbClientesGrupos;
    }

    public void setMbClientesGrupos(MbClientesGrupos mbClientesGrupos) {
        this.mbClientesGrupos = mbClientesGrupos;
    }

    public MbGruposBancos getMbGruposBancos() {
        return mbGruposBancos;
    }

    public void setMbGruposBancos(MbGruposBancos mbGruposBancos) {
        this.mbGruposBancos = mbGruposBancos;
    }

}
