/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pedidos;

//import empresas.MbMiniEmpresa;
import Message.Mensajes;
import almacenes.MbAlmacenesJS;
import comprobantes.MbComprobantes;
import empresas.MbMiniEmpresas;
import empresas.dominio.MiniEmpresa;
import mbMenuClientesGrupos.MbClientesGrupos;
import formatos.MbFormatos;
import gruposBancos.MbGruposBancos;
//import pedidos.dominio.Chedraui;
//import pedidos.dominio.SamsClub;
import pedidos.dominio.Textual;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
//import java.sql.SQLException;
//import java.util.Date;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedProperty;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import org.primefaces.model.UploadedFile;
import org.primefaces.event.FileUploadEvent;
import pedidos.LeerTextuales.LeerTextuales;
import formatos.dominio.ClienteFormato;
import java.util.Date;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import pedidos.dao.DAOCargaPedidos;
import pedidos.dao.DAOPedidos;
import pedidos.dominio.Pedido;
import pedidos.to.TOPedido;
import tiendas.MbMiniTiendas;
import tiendas.to.TOTienda;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author Usuario
 */
@Named(value = "mbCargaPedidos")
@SessionScoped
public class MbCargaPedidos implements Serializable {

    private String destination = "c:\\textuales\\";
    @ManagedProperty(value = "#{mbMiniEmpresas}")
    private MbMiniEmpresas mbEmpresas;
//    private ArrayList<SelectItem> listaMini = new ArrayList<>();

    @ManagedProperty(value = "#{mbClientesGrupos}")
    private MbClientesGrupos mbClientesGrupos;
//    private ArrayList<SelectItem> lstClientesGrupos = new ArrayList<>();

    @ManagedProperty(value = "#{mbFormatos}")
    private MbFormatos mbFormatos;
    private ArrayList<SelectItem> lstFormatos = new ArrayList<>();

    @ManagedProperty(value = "#{mbMiniTiendas}")
    private MbMiniTiendas mbTiendas;

    @ManagedProperty(value = "#{mbAlmacenesJS}")
    private MbAlmacenesJS mbAlmacenes;

    @ManagedProperty(value = "#{mbComprobantes}")
    private MbComprobantes mbComprobantes;

    @ManagedProperty(value = "#{mbGruposBancos}")
    private MbGruposBancos mbGruposBancos = new MbGruposBancos();

    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    private ArrayList<Accion> acciones;

    private ArrayList<Textual> textual;
    private Date fechaEntrega;
    private Date fechaCancelacion;
    private DAOPedidos daoPed;
    private DAOCargaPedidos dao;
    private String file = "";

    /**
     * Creates a new instance of MbPedido
     */
    public MbCargaPedidos() throws NamingException {
        this.mbEmpresas = new MbMiniEmpresas();
        this.mbClientesGrupos = new MbClientesGrupos();
        this.mbFormatos = new MbFormatos();
        this.mbTiendas = new MbMiniTiendas();
        this.mbAlmacenes = new MbAlmacenesJS();
        this.mbComprobantes = new MbComprobantes();
        this.mbAcciones = new MbAcciones();
        this.daoPed = new DAOPedidos();
        this.dao = new DAOCargaPedidos();

    }

    private Pedido convertir(TOPedido toPed) {
        Pedido ped = new Pedido(this.mbAlmacenes.obtenerAlmacen(toPed.getIdAlmacen()), this.mbTiendas.obtenerTienda(toPed.getIdReferencia()), this.mbComprobantes.obtenerComprobante(toPed.getIdComprobante()));
        Pedidos.convertirPedido(toPed, ped);
        return ped;
    }

    public void upload(FileUploadEvent event) {
        try {
            copyFile(event.getFile().getFileName(), event.getFile().getInputstream());
            String entrada = (destination + event.getFile().getFileName());
            int idEmp = this.mbEmpresas.getEmpresa().getIdEmpresa();
            int idFto = this.mbFormatos.getFormato().getIdFormato();
            int idGpoCte = this.mbClientesGrupos.getClientesGrupos().getIdGrupoCte();
            LeerTextuales leerTextuales = new LeerTextuales();
            switch (idGpoCte) {
                case 190: //Grupo Nueva WallMart
                    if (idFto == 233) {
                        textual = leerTextuales.leerArchivoSams(entrada);
                        System.out.print("SAMS CLUB " + idFto);
                    }
                    if (idFto == 230) {
                        System.out.print("WALLMART " + idFto);
                    }
                    if (idFto == 231) {
                        System.out.print("BODEGA AURRERA " + idFto);
                    }
                    if (idFto == 232) {
                        System.out.print("SUPERAMA " + idFto);
                    }
                    break;
                case 188:
                    textual = leerTextuales.leerArchivoCHedraui(entrada);
                    this.dao.crearPedidos(idEmp, idGpoCte, idFto, textual, event.getFile().getFileName());
//                    Pedido pedido;
//                    for(TOPedido to : this.dao.crearPedidos(idEmp, idGpoCte, idFto, chedraui, event.getFile().getFileName())) {
//                        pedido = this.convertir(to);
//                        this.obtenDetalle(pedido);
//                    }           
////                    String oc = "";
////                    TOTienda toTienda;
////                    TOPedido toPed = new TOPedido(28);
////                    toPed.setElectronico(event.getFile().getFileName());
////
////                    for (Chedraui che : this.chedraui) {
////                        if (!che.getOrdenCompra().equals(oc)) {
////                            toTienda = this.mbTiendas.obtenerTienda(this.dao.validaTienda(che.getCodigoTienda(), idGpoCte, idFto));
////                            oc = che.getOrdenCompra();
////                            toPed.setIdEmpresa(idEmp);
////                            toPed.setOrdenDeCompra(oc);
////                            toPed.setOrdenDeCompraFecha(che.getFechaElaboracion());
////                            toPed.setIdImpuestoZona(idGpoCte);
////                            toPed.setIdReferencia(toTienda.getIdTienda());
////                            toPed.setIdImpuestoZona(toTienda.getIdImpuestoZona());
////                            this.daoPed.agregarPedido(toPed, 1);
////                        }
////                    }
                    break;
                case 176: //Comercial Mexicana
                    textual = leerTextuales.leerArchivoComercialMexicana(entrada, fechaEntrega, fechaCancelacion);
                    this.dao.crearPedidos(idEmp, idGpoCte, idFto, textual, event.getFile().getFileName());
                    break;
                default:
                    System.out.println("No hay ningun grupo de clientes");
                    break;
            }
//                leerTextuales.leerArchivoCHedraui(entrada);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }

    }

    public void cargaFormatos() throws SQLException {
        this.lstFormatos = new ArrayList<>();
        ClienteFormato cteFto = new ClienteFormato(0, "Seleccione un Formato:");
        this.lstFormatos.add(new SelectItem(cteFto, cteFto.toString()));
        for (ClienteFormato cf : this.mbFormatos.cargarArrayListListaFormatos(this.mbClientesGrupos.getClientesGrupos().getIdGrupoCte())) {
            lstFormatos.add(new SelectItem(cf, cf.toString()));
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
            try (OutputStream salida = new FileOutputStream(new File(destination + fileName))) {
                int read = 0;
                byte[] bytes = new byte[1024];
                while ((read = in.read(bytes)) != -1) {
                    salida.write(bytes, 0, read);
                }
                in.close();
                salida.flush();
            }
        } catch (IOException e) {
            Message.Mensajes.mensajeError(e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    public String terminar() {
        this.mbEmpresas.setEmpresa(new MiniEmpresa());
        this.mbEmpresas.setListaEmpresas(null);

        this.mbClientesGrupos = new MbClientesGrupos();
        return "index.xhtml";
    }

    public MbClientesGrupos getMbClientesGrupos() {
        return mbClientesGrupos;
    }

    public void setMbClientesGrupos(MbClientesGrupos mbClientesGrupos) {
        this.mbClientesGrupos = mbClientesGrupos;
    }

    public MbFormatos getMbFormatos() {
        return mbFormatos;
    }

    public void setMbFormatos(MbFormatos mbFormatos) {
        this.mbFormatos = mbFormatos;
    }

    public ArrayList<SelectItem> getLstFormatos() {
        return lstFormatos;
    }

    public void setLstFormatos(ArrayList<SelectItem> lstFormatos) {
        this.lstFormatos = lstFormatos;
    }

    public MbGruposBancos getMbGruposBancos() {
        return mbGruposBancos;
    }

    public void setMbGruposBancos(MbGruposBancos mbGruposBancos) {
        this.mbGruposBancos = mbGruposBancos;
    }

    public MbMiniEmpresas getMbEmpresas() {
        return mbEmpresas;
    }

    public void setMbEmpresas(MbMiniEmpresas mbEmpresas) {
        this.mbEmpresas = mbEmpresas;
    }

    public Date getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(Date fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public Date getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(Date fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

}
