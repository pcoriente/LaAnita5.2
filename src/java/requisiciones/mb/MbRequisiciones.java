package requisiciones.mb;

import empresas.MbMiniEmpresa;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import org.primefaces.event.RowEditEvent;
import producto2.MbProductosBuscar;
import producto2.dominio.Producto;
import requisiciones.dao.DAORequisiciones;
import requisiciones.dominio.RequisicionDetalle;
import requisiciones.dominio.RequisicionEncabezado;
import requisiciones.to.TORequisicionDetalle;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;
import usuarios.dominio.Usuario;

@Named(value = "mbRequisiciones")
@SessionScoped
public class MbRequisiciones implements Serializable {

    @ManagedProperty(value = "#{mbMiniEmpresa}")
    private MbMiniEmpresa mbMiniEmpresa;
    @ManagedProperty(value = "#{mbDepto}")
    private MbDepto mbDepto = new MbDepto();
    @ManagedProperty(value = "#{mbUsuarios}")
    private MbUsuarios mbUsuarios;
    private ArrayList<SelectItem> listaSubUsuarios = new ArrayList<>();
    private ArrayList<RequisicionEncabezado> listaRequisicionesEncabezado;
    private RequisicionEncabezado requisicionEncabezado = new RequisicionEncabezado();
    private ArrayList<RequisicionEncabezado> requisicionesFiltradas;
    private RequisicionDetalle requisicionDetalle;
    private ArrayList<RequisicionDetalle> requisicionDetalles = new ArrayList<>();
    private RequisicionDetalle empaqueElegido = new RequisicionDetalle();
    private ArrayList<SelectItem> listaMini = new ArrayList<>();
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private ArrayList<Producto> listaEmpaque = new ArrayList<>();
    private Producto empaque;
    private String navega;
    private RequisicionDetalle seleccion = null;
    private RequisicionDetalle seleccionFila = null;
    private RequisicionEncabezado seleccionRequisicionEncabezado = null;
    //SEGURIDAD
    private ArrayList<Accion> acciones = null;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;

    //CONSTRUCTOR
    public MbRequisiciones() throws NamingException {
        this.mbMiniEmpresa = new MbMiniEmpresa();
        this.mbDepto = new MbDepto();
        this.mbUsuarios = new MbUsuarios();
        this.mbBuscar = new MbProductosBuscar();
        //SEGURIDAD
        this.mbAcciones = new MbAcciones(7);
    }

    public void deseleccionar() {
        seleccionFila = null;
        seleccionRequisicionEncabezado = null;
    }

    //GET Y SETS REQUISICIONES
    //SEGURIDAD GETS Y SETS
    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
    }

    public ArrayList<Accion> getAcciones() {
//        if (acciones == null) {
//            acciones = mbAcciones.obtenerAcciones(7);
//        }
        return acciones;
    }

    public boolean validarAccion(String dato) {
        boolean ok = false;
        try {
            ok = mbAcciones.validarAccion(dato);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
        return ok;
    }

    public void setAcciones(ArrayList<Accion> acciones) {
        this.acciones = acciones;
    }

    public MbDepto getMbDepto() {
        return mbDepto;
    }

    public void setMbDepto(MbDepto mbDepto) {
        this.mbDepto = mbDepto;
    }

    public MbMiniEmpresa getMbMiniEmpresa() {
        return mbMiniEmpresa;
    }

    public void setMbMiniEmpresa(MbMiniEmpresa mbMiniEmpresa) {
        this.mbMiniEmpresa = mbMiniEmpresa;
    }

    public MbUsuarios getMbUsuarios() {
        return mbUsuarios;
    }

    public void setMbUsuarios(MbUsuarios mbUsuarios) {
        this.mbUsuarios = mbUsuarios;
    }

    public ArrayList<SelectItem> getListaSubUsuarios() {
        return listaSubUsuarios;
    }

    public void setListaSubUsuarios(ArrayList<SelectItem> listaSubUsuarios) {
        this.listaSubUsuarios = listaSubUsuarios;
    }

    public ArrayList<RequisicionEncabezado> getListaRequisicionesEncabezado() throws NamingException {
        try {
            if (listaRequisicionesEncabezado == null) {
                cargaRequisiciones();
            }
        } catch (SQLException ex) {
            Logger.getLogger(MbRequisiciones.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listaRequisicionesEncabezado;
    }

    public void setListaRequisicionesEncabezado(ArrayList<RequisicionEncabezado> listaRequisicionesEncabezado) {
        this.listaRequisicionesEncabezado = listaRequisicionesEncabezado;
    }

    public RequisicionEncabezado getRequisicionEncabezado() {
        return requisicionEncabezado;
    }

    public void setRequisicionEncabezado(RequisicionEncabezado requisicionEncabezado) {
        this.requisicionEncabezado = requisicionEncabezado;
    }

    public String salirMenuRequisiciones() throws NamingException {
        this.limpiaRequisicion();
        String salir = "index.xhtml";
        return salir;
    }

    public ArrayList<RequisicionEncabezado> getRequisicionesFiltradas() {
        return requisicionesFiltradas;
    }

    public void setRequisicionesFiltradas(ArrayList<RequisicionEncabezado> requisicionesFiltradas) {
        this.requisicionesFiltradas = requisicionesFiltradas;
    }

    public ArrayList<SelectItem> getListaMini() throws SQLException {
        listaMini = this.mbMiniEmpresa.obtenerListaMiniEmpresas();
        return listaMini;
    }

    public void setListaMini(ArrayList<SelectItem> listaMini) {
        this.listaMini = listaMini;
    }

    public RequisicionDetalle getRequisicionDetalle() {
        return requisicionDetalle;
    }

    public void setRequisicionDetalle(RequisicionDetalle requisicionDetalle) {
        this.requisicionDetalle = requisicionDetalle;
    }

    public ArrayList<RequisicionDetalle> getRequisicionDetalles() {
        return requisicionDetalles;
    }

    public void setRequisicionDetalles(ArrayList<RequisicionDetalle> requisicionDetalles) {
        this.requisicionDetalles = requisicionDetalles;
    }

    public RequisicionDetalle getEmpaqueElegido() {
        return empaqueElegido;
    }

    public void setEmpaqueElegido(RequisicionDetalle empaqueElegido) {
        this.empaqueElegido = empaqueElegido;
    }

    public RequisicionDetalle getSeleccion() {
        return seleccion;
    }

    public void setSeleccion(RequisicionDetalle seleccion) {
        this.seleccion = seleccion;
    }

    public void eliminar() {
        requisicionDetalles.remove(seleccion);
        seleccion = null;
    }

    public RequisicionDetalle getSeleccionFila() {
        return seleccionFila;
    }

    public void setSeleccionFila(RequisicionDetalle seleccionFila) {
        this.seleccionFila = seleccionFila;
    }

    public RequisicionEncabezado getSeleccionRequisicionEncabezado() {
        return seleccionRequisicionEncabezado;
    }

    public void setSeleccionRequisicionEncabezado(RequisicionEncabezado seleccionRequisicionEncabezado) {
        this.seleccionRequisicionEncabezado = seleccionRequisicionEncabezado;
    }

    //EMPAQUES
    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public ArrayList<Producto> getListaEmpaque() {
        return listaEmpaque;
    }

    public void setListaEmpaque(ArrayList<Producto> listaEmpaque) {
        this.listaEmpaque = listaEmpaque;
    }

    public Producto getEmpaque() {
        return empaque;
    }

    public void setEmpaque(Producto empaque) {
        this.empaque = empaque;
    }

    //METODOS REQUISICIONES
    public void cargaSubUsuarios() throws SQLException {
        this.listaSubUsuarios = new ArrayList<>();
        Usuario usu = new Usuario(0, "Seleccione un usuario: ");
        this.listaSubUsuarios.add(new SelectItem(usu, usu.toString()));
        for (Usuario us : this.mbUsuarios.obtenerSubUsuarios(this.mbDepto.getDeptos().getIdDepto())) {
            listaSubUsuarios.add(new SelectItem(us, us.toString()));
        }
    }

    public void guardaRequisicion() throws NamingException, SQLException {
        int idEmpresa;
        int idDepto;
        int idUsuario;
        FacesMessage fMsg = null;
        DAORequisiciones daoReq = new DAORequisiciones();
        try {
            idEmpresa = this.mbMiniEmpresa.getEmpresa().getIdEmpresa();
        } catch (Exception e) {
            idEmpresa = 0;
        }
        idDepto = this.mbDepto.getDeptos().getIdDepto();
        try {
            idUsuario = this.mbUsuarios.getUsuario().getId();
        } catch (Exception e) {
            idUsuario = 0;
        }

        if (idEmpresa == 0) {
            fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "Seleccione una Empresa.");
        } else if (idDepto == 0) {
            fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "Seleccione un depto.");
        } else if (idUsuario == 0) {
            fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "Seleccione un Usuario.");
        } else if (this.requisicionDetalles.isEmpty()) {
            fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "Especificar detalle");
        } else {
            boolean control = false;
            for (RequisicionDetalle p : this.requisicionDetalles) {
                if (p.getCantidad() <= 0) {
                    fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso: Para el producto " + p.getProducto().toString(), " Capture una cantidad. ");
                    control = true;
                    break;
                }
            }
            if (control == false) {
                daoReq.guardarRequisicion(idEmpresa, idDepto, idUsuario, this.requisicionDetalles);
                this.limpiaRequisicion();
                fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "La requisición se ha generado...");
            }
        }
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
    }

    public void cargaRequisiciones() throws NamingException, SQLException {
        listaRequisicionesEncabezado = new ArrayList<>();
        DAORequisiciones daoReq = new DAORequisiciones();
        ArrayList<RequisicionEncabezado> toLista = daoReq.dameRequisicion();
        for (RequisicionEncabezado e : toLista) {
            listaRequisicionesEncabezado.add(e);
        }

    }

    private void cargaRequisicionesDetalle(int id) throws NamingException, SQLException {
        requisicionDetalles = new ArrayList<>();
        DAORequisiciones daoReq = new DAORequisiciones();
        for (TORequisicionDetalle rd : daoReq.dameRequisicionDetalle(id)) {
            requisicionDetalles.add(this.convertir(rd));
        }
    }

    private RequisicionDetalle convertir(TORequisicionDetalle to) {

        RequisicionDetalle rd = new RequisicionDetalle();
        rd.setIdRequisicion(to.getIdRequisicion());
        rd.setProducto(this.mbBuscar.obtenerProducto(to.getIdProducto()));
        rd.setCantidad(to.getCantidad());
        rd.setCantidadAutorizada(to.getCantidadAutorizada());

        return rd;
    }

    public void cargaRequisicionesDetalleAprobar() throws NamingException, SQLException {
        int id = seleccionRequisicionEncabezado.getIdRequisicion();
        requisicionDetalles = new ArrayList<>();
        DAORequisiciones daoReq = new DAORequisiciones();
        for (TORequisicionDetalle rd : daoReq.dameRequisicionDetalleAprobar(id)) {
            requisicionDetalles.add(convertir(rd));
        }
    }

    public String salir() throws NamingException {
        this.limpiaRequisicion();
        navega = "menuRequisiciones.xhtml";
        return navega;
    }

    public String salirMenu(int opcion) throws NamingException {
        this.limpiaRequisicion();
        this.listaRequisicionesEncabezado = null;
        if (opcion == 0) {
            navega = "index.xhtml";
        } else if (opcion == 1) {
            navega = "menuRequisiciones.xhtml";
        } else if (opcion == 2) {
            navega = "menuCotizaciones.xhtml";
        }
        return navega;
    }

    public String nuevo() throws NamingException {
        this.limpiaRequisicion();
        navega = "requisiciones.xhtml";
        return navega;
    }

    public void limpiaRequisicion() throws NamingException {
        navega = "";
        this.requisicionDetalle = new RequisicionDetalle();
        requisicionDetalles = new ArrayList<>();
        this.listaRequisicionesEncabezado = null;
        this.requisicionesFiltradas = null;
        this.mbMiniEmpresa = new MbMiniEmpresa();
        this.mbDepto = new MbDepto();
        this.mbUsuarios = new MbUsuarios();
        this.seleccionRequisicionEncabezado = null;
        this.seleccionFila = null;

    }

    public void eliminarProducto(int idEmpaque) {
        for (RequisicionDetalle d : requisicionDetalles) {
            if (d.getProducto().getIdProducto() == idEmpaque) {
                requisicionDetalles.remove(d);
                break;
            }
        }
    }

    public void aprobarRequisicion(int estado) throws SQLException, NamingException {
        int idReq = seleccionRequisicionEncabezado.getIdRequisicion();

        DAORequisiciones daoReq = new DAORequisiciones();
        FacesMessage msg = null;
        try {
            int longitud = requisicionDetalles.size();
            if (longitud < 0) {
                for (int y = 0; y < longitud; y++) {
                    double ca = requisicionDetalles.get(y).getCantidadAutorizada();
                    if (ca < 0) {
                        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "No sé realizó la operación de aprobación");
                        break;
                    } else if (estado == 2) {
                        daoReq.actualizaRequisicion(idReq, estado);
                        this.requisicionEncabezado.setStatus(estado);
                        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "La aprobación se ha realizado..");
                        this.cargaRequisiciones();
                        this.cargaRequisicionesDetalle(idReq);
                    } else if (estado == 0) {
                        daoReq.actualizaRequisicion(idReq, estado);
                        this.requisicionEncabezado.setStatus(estado);
                        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "La requisición ha sido RECHAZADA..");
                        this.cargaRequisiciones();
                        this.cargaRequisicionesDetalle(idReq);
                    }
                }
            } else {
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "La requisición NO debe ser VACIA, en su caso CANCELE..");
            }
        } catch (NamingException ex) {
            Logger.getLogger(MbRequisiciones.class.getName()).log(Level.SEVERE, null, ex);
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso:", "Error en la aprobación, verifique su información...");
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
        deseleccionar();

    }

    public void eliminaProductoAprobar() throws NamingException, SQLException {
        DAORequisiciones daoReq = new DAORequisiciones();
        requisicionDetalles.remove(seleccionFila);
        int idReq = seleccionFila.getIdRequisicion();
        int idProd = seleccionFila.getProducto().getIdProducto();
        daoReq.eliminaProductoAprobar(idReq, idProd);
        seleccionFila = null;

    }

    public void modificaProductoAprobar(int idReq, int idProd) throws NamingException, SQLException {
        DAORequisiciones daoReq = new DAORequisiciones();
        int longitud = requisicionDetalles.size();
        for (int y = 0; y < longitud; y++) {
            int idProducto = requisicionDetalles.get(y).getProducto().getIdProducto();
            int idRequi = requisicionDetalles.get(y).getIdRequisicion();
            if (idProducto == idProd || idRequi == idReq) {
                double cantidad = requisicionDetalles.get(y).getCantidadAutorizada();
                daoReq.modificaProductoAprobar(idReq, idProd, cantidad);
                break;
            }
        }
    }

    public void onEdit(RowEditEvent event) throws NamingException, SQLException {
        DAORequisiciones daoReq = new DAORequisiciones();
        RequisicionEncabezado encab = new RequisicionEncabezado();
        RequisicionDetalle deta = (RequisicionDetalle) event.getObject();
        FacesMessage msg = null;
        int idReq = deta.getIdRequisicion();
        int idProd = deta.getProducto().getIdProducto();
        double cantidad = deta.getCantidadAutorizada();
        if (cantidad > 0) {
            daoReq.modificaProductoAprobar(idReq, idProd, cantidad);
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso: ", "Modificación exitosa");
        } else if (cantidad < 0) {
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso: ", "Capture cantidades positivas");
            deta.getCantidadAutorizada();
        } else if (cantidad == 0) {
            daoReq.modificaProductoAprobar(idReq, idProd, cantidad);
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso: ", "El producto ha sido eliminado");
        }
    }

    public void modificarRequisicionStatus() throws SQLException, NamingException {
        int idReq = seleccionRequisicionEncabezado.getIdRequisicion();
        int status = seleccionRequisicionEncabezado.getStatus();


        DAORequisiciones daoReq = new DAORequisiciones();
        FacesMessage msg;
        try {
            daoReq.modificarAprobacion(idReq, status);
            this.cargaRequisiciones();
            this.requisicionDetalle = null;
            this.cargaRequisicionesDetalleAprobar(); // PERMITEME
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso: ", "Modificación de status exitosa");
        } catch (NamingException ex) {
            Logger.getLogger(MbRequisiciones.class.getName()).log(Level.SEVERE, null, ex);
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso: ", "No se realizó la modificación de status..");
        }
        deseleccionar();
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.actualizaProductoSeleccionado();
        }
    }

    public void actualizaProductosSeleccionados() {
        for (Producto e : this.mbBuscar.getSeleccionados()) {
            RequisicionDetalle rd = new RequisicionDetalle();
            rd.setProducto(e);
            requisicionDetalles.add(rd);
        }
        HashSet hs = new HashSet();
        hs.addAll(requisicionDetalles);
        requisicionDetalles.removeAll(requisicionDetalles);
        requisicionDetalles.addAll(hs);
    }

    public void actualizaProductoSeleccionado() {
        FacesMessage msg = null;
        boolean ok = true;
        boolean nuevo = true;
        RequisicionDetalle rd = new RequisicionDetalle();
        rd.setProducto(this.mbBuscar.getProducto());
        this.requisicionDetalles.add(rd);
        FacesContext.getCurrentInstance().addMessage(null, msg);
                
    }
}
