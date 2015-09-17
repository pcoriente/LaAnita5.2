package entradas;

import Message.Mensajes;
import almacenes.to.TOAlmacenJS;
import entradas.dao.DAOComprobantes;
import entradas.dominio.Comprobante;
import entradas.to.TOComprobante;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.bean.ManagedProperty;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import monedas.MbMonedas;
import org.primefaces.context.RequestContext;

/**
 *
 * @author jesc
 */
@Named(value = "mbComprobantes")
@SessionScoped
public class MbComprobantes implements Serializable {

    @ManagedProperty(value = "#{mbMonedas}")
    private MbMonedas mbMonedas;
    private int idTipoMovto;
    private int idReferencia;
    private Comprobante comprobante;
    private TOComprobante seleccion;
    private ArrayList<SelectItem> listaComprobantes;
    private DAOComprobantes dao;

    public MbComprobantes() throws NamingException {
        this.mbMonedas = new MbMonedas();

        this.inicializaLocales();
    }

    public void inicializaConAlmacen(TOAlmacenJS toAlmacen) {
        this.inicializaLocales();
    }

    public void inicializar() {
        this.inicializaLocales();
    }

    private void inicializaLocales() {
        this.seleccion = null;
        this.comprobante = new Comprobante();
    }

    public void liberar() {
        boolean ok = false;
        try {
            this.dao = new DAOComprobantes();
            if (this.comprobante.getIdComprobante() != 0) {
                this.dao.liberaComprobante(this.comprobante.getIdComprobante());
            } else {
                this.setSeleccion(null);
            }
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okComprobante", ok);
    }

    public void eliminar() {
        boolean ok = false;
        try {
            this.dao = new DAOComprobantes();
            this.dao.eliminar(this.seleccion.getIdComprobante());
            this.comprobante = new Comprobante(this.idTipoMovto, this.idReferencia);
            this.seleccion = null;
            ok = true;
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okComprobante", ok);
    }

    private TOComprobante convertir(Comprobante c) {
        TOComprobante to = new TOComprobante();
        to.setIdComprobante(c.getIdComprobante());
        to.setIdTipoMovto(c.getIdTipoMovto());
        to.setIdReferencia(c.getIdReferencia());
        to.setTipo(Integer.parseInt(c.getTipo()));
        to.setSerie(c.getSerie());
        to.setNumero(c.getNumero());
        to.setFecha(c.getFecha());
        to.setIdMoneda(c.getMoneda().getIdMoneda());
        to.setIdUsuario(c.getIdUsuario());
        to.setPropietario(c.getPropietario());
        to.setEstatus(c.getEstatus());
        return to;
    }

    public boolean grabar() {
        boolean ok = false;
        try {
            if (this.comprobante.getNumero().equals("")) {
                Mensajes.mensajeAlert("Se requiere el numero del comprobante");
            } else if(this.comprobante.getMoneda().getIdMoneda()==0) {
                Mensajes.mensajeAlert("Se requiere una moneda !!!");
            } else {
                this.dao = new DAOComprobantes();
                TOComprobante to = this.convertir(this.comprobante);
                if (to.getIdComprobante() == 0) {
                    to.setIdComprobante(this.dao.agregar(to));
                    this.comprobante.setIdComprobante(to.getIdComprobante());
                    this.comprobante.setEstatus(to.getEstatus());
                } else {
                    this.dao.modificar(to);
                }
                this.comprobante.setIdUsuario(to.getIdUsuario());
                this.seleccion=to;
                ok = true;
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okComprobante", ok);
        return ok;
    }

    public void mttoComprobante() {
        boolean ok = false;
        if (this.seleccion == null) {
            this.comprobante = new Comprobante(this.idTipoMovto, this.idReferencia);
            ok = true;
        } else {
            try {
                this.dao = new DAOComprobantes();
                if (this.dao.asegurarComprobante(this.seleccion.getIdComprobante())) {
                    ok = true;
                }
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okComprobante", ok);
    }

    public ArrayList<TOComprobante> completeComprobantes(String query) {
        ArrayList<TOComprobante> lstComprobantes = null;
        try {
            this.dao = new DAOComprobantes();
            lstComprobantes = this.dao.completeComprobantes(this.idTipoMovto, this.idReferencia, query);
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        return lstComprobantes;
    }

    public void convierteSeleccion() {
        this.comprobante = this.convertir(this.seleccion);
    }

    private Comprobante convertir(TOComprobante to) {
        Comprobante c = new Comprobante();
        c.setIdComprobante(to.getIdComprobante());
        c.setIdTipoMovto(to.getIdTipoMovto());
        c.setIdReferencia(to.getIdReferencia());
        c.setTipo(Integer.toString(to.getTipo()));
        c.setSerie(to.getSerie());
        c.setNumero(to.getNumero());
        c.setFecha(to.getFecha());
        c.setMoneda(this.mbMonedas.obtenerMoneda(to.getIdMoneda()));
        c.setIdUsuario(to.getIdUsuario());
        c.setPropietario(to.getPropietario());
        c.setEstatus(to.getEstatus());
        return c;
    }

    public Comprobante obtenerComprobante(int idComprobante) {
        Comprobante c = null;
        try {
            this.dao = new DAOComprobantes();
            TOComprobante to = this.dao.obtenerComprobante(idComprobante);
            if (to == null) {
                Mensajes.mensajeAlert("No se encontro el Comprobante solicitado");
            } else {
                c = this.convertir(to);
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        return c;
    }

    public ArrayList<SelectItem> getListaComprobantes() {
        return listaComprobantes;
    }

    public void setListaComprobantes(ArrayList<SelectItem> listaComprobantes) {
        this.listaComprobantes = listaComprobantes;
    }

    public Comprobante getComprobante() {
        return comprobante;
    }

    public void setComprobante(Comprobante comprobante) {
        this.comprobante = comprobante;
    }

    public int getIdTipoMovto() {
        return idTipoMovto;
    }

    public void setIdTipoMovto(int idTipoMovto) {
        this.idTipoMovto = idTipoMovto;
    }

    public int getIdReferencia() {
        return idReferencia;
    }

    public void setIdReferencia(int idReferencia) {
        this.idReferencia = idReferencia;
    }

    public TOComprobante getSeleccion() {
        return seleccion;
    }

    public void setSeleccion(TOComprobante seleccion) {
        this.seleccion = seleccion;
    }

    public MbMonedas getMbMonedas() {
        return mbMonedas;
    }

    public void setMbMonedas(MbMonedas mbMonedas) {
        this.mbMonedas = mbMonedas;
    }
}
