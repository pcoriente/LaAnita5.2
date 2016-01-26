package comprobantes;

import Message.Mensajes;
import almacenes.to.TOAlmacenJS;
import comprobantes.dao.DAOComprobantes;
import comprobantes.dominio.Comprobante;
import comprobantes.to.TOComprobante;
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
    private int idEmpresa;
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
                if (this.comprobante.isGrabable()) {
                    this.dao.liberaComprobante(this.comprobante.getIdComprobante());
                    this.comprobante.setPropietario(0);
                }
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
            if(this.comprobante.getIdComprobante()!=0) {
                this.dao = new DAOComprobantes();
                this.dao.eliminar(this.comprobante.getIdComprobante());
            }
            this.comprobante = new Comprobante(this.idTipoMovto, this.idEmpresa, this.idReferencia);
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
    
    public void cerrarAlmacen() {
        try {
            this.dao = new DAOComprobantes();
            this.dao.cerrarComprobanteAlmacen(this.comprobante.getIdComprobante());
            this.comprobante.setCerradoAlmacen(true);
            this.comprobante.setEstatus(7);
            Mensajes.mensajeSucces("El comprobante se cerró con exito !!!");
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }
    
    public void cerrarOficina() {
        try {
            this.dao = new DAOComprobantes();
            this.dao.cerrarComprobanteOficina(this.comprobante.getIdComprobante());
            this.comprobante.setCerradoOficina(true);
            this.comprobante.setEstatus(7);
            Mensajes.mensajeSucces("El comprobante se cerró con exito !!!");
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }
    
    public TOComprobante convertir(Comprobante c) {
//        return this.convierte(c);
        return comprobantes.Comprobantes.convertir(c);
    }

//    private TOComprobante convierte(Comprobante c) {
//        TOComprobante to = new TOComprobante();
//        to.setIdComprobante(c.getIdComprobante());
//        to.setIdTipoMovto(c.getIdTipoMovto());
//        to.setIdEmpresa(c.getIdEmpresa());
//        to.setIdReferencia(c.getIdReferencia());
//        to.setTipo(Integer.parseInt(c.getTipo()));
//        to.setSerie(c.getSerie());
//        to.setNumero(c.getNumero());
//        to.setFechaFactura(c.getFechaFactura());
//        to.setIdMoneda(c.getMoneda().getIdMoneda());
//        to.setIdUsuario(c.getIdUsuario());
//        to.setPropietario(c.getPropietario());
//        to.setCerradoOficina(c.isCerradoOficina());
//        to.setCerradoAlmacen(c.isCerradoAlmacen());
//        to.setEstatus(c.getEstatus());
//        return to;
//    }

    public boolean grabar() {
        boolean ok = false;
        try {
            if (this.comprobante.getNumero().equals("")) {
                Mensajes.mensajeAlert("Se requiere el numero del comprobante");
//            } else if (this.comprobante.getMoneda().getIdMoneda() == 0) {
//                Mensajes.mensajeAlert("Se requiere una moneda !!!");
            } else {
                this.dao = new DAOComprobantes();
//                TOComprobante to = this.convierte(this.comprobante);
                TOComprobante to = comprobantes.Comprobantes.convertir(this.comprobante);
                if (to.getIdComprobante() == 0) {
                    this.dao.agregar(to);
                    this.comprobante.setIdComprobante(to.getIdComprobante());
                    this.comprobante.setIdUsuario(to.getIdUsuario());
                    this.comprobante.setPropietario(to.getPropietario());
                    this.comprobante.setEstatus(to.getEstatus());
                } else {
                    this.dao.modificar(to);
                }
                this.seleccion = to;
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
            this.comprobante = new Comprobante(this.idTipoMovto, this.idEmpresa, this.idReferencia);
            ok = true;
        } else {
            try {
                this.dao = new DAOComprobantes();
                if (this.dao.asegurarComprobante(this.seleccion.getIdComprobante())) {
                    this.seleccion.setPropietario(this.seleccion.getIdUsuario());
                }
                this.comprobante=this.convertir(this.seleccion);
                ok = true;
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
            }
        }
        RequestContext context = RequestContext.getCurrentInstance();
        context.addCallbackParam("okComprobante", ok);
    }
    
    public ArrayList<TOComprobante> completeComprobantes28(String query) {
        ArrayList<TOComprobante> lstComprobantes = null;
        try {
            this.dao = new DAOComprobantes();
            lstComprobantes = this.dao.completeComprobantes28(this.idEmpresa, this.idReferencia, query);
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
        return lstComprobantes;
    }

    public ArrayList<TOComprobante> completeComprobantes(String query) {
        ArrayList<TOComprobante> lstComprobantes = null;
        try {
            this.dao = new DAOComprobantes();
            lstComprobantes = this.dao.completeComprobantes(this.idTipoMovto, this.idEmpresa, this.idReferencia, query);
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
        c.setIdEmpresa(to.getIdEmpresa());
        c.setIdReferencia(to.getIdReferencia());
        c.setTipo(Integer.toString(to.getTipo()));
        c.setSerie(to.getSerie());
        c.setNumero(to.getNumero());
        c.setFechaFactura(to.getFechaFactura());
        if(to.getIdMoneda()!=0) {
//            c.setMoneda(this.mbMonedas.obtenerMonedaCero());
//        } else {
            c.setMoneda(this.mbMonedas.obtenerMoneda(to.getIdMoneda()));
        }
        c.setIdUsuario(to.getIdUsuario());
        c.setPropietario(to.getPropietario());
        c.setCerradoOficina(to.isCerradoOficina());
        c.setCerradoAlmacen(to.isCerradoAlmacen());
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

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
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
