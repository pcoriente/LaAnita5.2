package clientes;

import Message.Mensajes;
import clientes.dao.DAOClientes;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;
import clientes.to.TOCliente;

/**
 *
 * @author jesc
 */
@Named(value = "mbMiniClientes")
@SessionScoped
public class MbMiniClientes implements Serializable {

    private int idClienteGpo;
//    private int idFormato;
    private ArrayList<SelectItem> listaClientes;
    private ArrayList<TOCliente> clientes;
    private TOCliente cliente;
    private DAOClientes dao;

    public MbMiniClientes() {
        this.cliente = new TOCliente();
        //this.inicializa();
    }

    public void obtenerClientesCedis(int idCedis) {
        this.listaClientes = new ArrayList<>();
        this.listaClientes.add(new SelectItem(new TOCliente(), "Seleccione POR UN CEDIS"));
        try {
            this.dao = new DAOClientes();
            for (TOCliente c : this.dao.obtenerClientesCedis(idCedis)) {
                this.listaClientes.add(new SelectItem(c, c.toString()));
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public void obtenerClientesCedis() {
        this.listaClientes = new ArrayList<>();
        this.listaClientes.add(new SelectItem(new TOCliente(), "Seleccione"));
        try {
            this.dao = new DAOClientes();
            for (TOCliente c : this.dao.obtenerClientesCedis()) {
                this.listaClientes.add(new SelectItem(c, c.toString()));
            }
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        }
    }

    public TOCliente obtenerCliente(int idCliente) {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "obtenerCliente");
        TOCliente mini = null;
        try {
            this.dao = new DAOClientes();
            mini = this.dao.obtenerCliente(idCliente);
            ok = true;
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        return mini;
    }

    public void cargarClientesGrupo(int idGrupo) {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "cargarClientesGrupo");
        this.listaClientes = new ArrayList<>();
        this.listaClientes.add(new SelectItem(new TOCliente(), "Seleccione Cliente"));
        try {
            this.dao = new DAOClientes();
            for (TOCliente c : this.dao.obtenerClientesGrupo(idGrupo)) {
                this.listaClientes.add(new SelectItem(c, c.toString()));
                System.out.println("El cliente es "+c.toString());
            }
            ok = true;
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

//    public void cargarMiniClientes(int idFormato) {
//        this.idFormato=idFormato;
//        this.miniClientes=null;
//        this.listaMiniClientes=new ArrayList<SelectItem>();
//        MiniCliente c0=new MiniCliente();
//        c0.setContribuyente("Seleccione");
//        this.listaMiniClientes.add(new SelectItem(c0, c0.toString()));
//        
//        for(MiniCliente c: this.getMiniClientes()) {
//            this.listaMiniClientes.add(new SelectItem(c, c.toString()));
//        }
//    }
//    private void cargaMiniClientes() {
//        boolean ok = false;
//        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "cargaMiniClientes");
//        this.clientes=new ArrayList<MiniCliente>();
//        try {
//            this.dao=new DAOMiniClientes();
//            this.clientes=this.dao.obtenerClientesCedis();
//            ok=true;
//        } catch (SQLException ex) {
//            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
//        } catch (NamingException ex) {
//            fMsg.setDetail(ex.getMessage());
//        }
//        if (!ok) {
//            FacesContext.getCurrentInstance().addMessage(null, fMsg);
//        }
//    }
    public void inicializar() {
        this.inicializa();
    }

    private void inicializa() {
        this.inicializaLocales();
    }

    private void inicializaLocales() {
        this.idClienteGpo = 0;
//        this.idFormato=0;
//        this.cargarMiniClientes(0);
        this.cargarClientesGrupo(0);
        this.nuevoCliente();
    }

    public void nuevoCliente() {
        this.cliente = new TOCliente();
    }

    // ANTES INICIO **************************************************************
    //    @ManagedProperty(value = "#{mbContribuyentes}")
    //    private MbContribuyentes mbContribuyentes;
    //
    //    private ArrayList<SelectItem> listaClientes;
    //    private Contribuyente cliente;
    //
    //    public MbMiniCliente() {
    //        inicializa();
    //    }
    //
    //    private void cargaListaClientes() {
    //        this.listaClientes=new ArrayList<SelectItem>();
    //        Contribuyente c0=new Contribuyente();
    //        c0.setIdContribuyente(0);
    //        c0.setContribuyente("Seleccione");
    //        this.listaClientes.add(new SelectItem(c0, c0.toString()));
    //
    //        for(Contribuyente c: this.mbContribuyentes.obtenerContribuyentesCliente()) {
    //            this.listaClientes.add(new SelectItem(c, c.toString()));
    //        }
    //    }
    //
    //    public void inicializar_r() {
    //        inicializa();
    //    }
    //
    //    private void inicializa() {
    //        this.mbContribuyentes = new MbContribuyentes();
    //        inicializaLocales();
    //    }
    //
    //    private void inicializaLocales() {
    //        this.listaClientes=null;
    //        this.cliente=new Contribuyente();
    //    }
    //
    //    public MbContribuyentes getMbContribuyentes() {
    //        return mbContribuyentes;
    //    }
    //
    //    public void setMbContribuyentes(MbContribuyentes mbContribuyentes) {
    //        this.mbContribuyentes = mbContribuyentes;
    //    }
    //
    //    public ArrayList<SelectItem> getListaClientes() {
    //        if(this.listaClientes==null) {
    //            this.cargaListaClientes();
    //        }
    //        return listaClientes;
    //    }
    //
    //    public void setListaClientes(ArrayList<SelectItem> listaClientes) {
    //        this.listaClientes = listaClientes;
    //    }
    //
    //    public Contribuyente getCliente() {
    //        return cliente;
    //    }
    //
    //    public void setCliente(Contribuyente cliente) {
    //        this.cliente = cliente;
    //    }
    // ANTES FINAL ***************************************************************
    //    public int getIdFormato() {
    //        return idFormato;
    //    }
    //
    //    public void setIdFormato(int idFormato) {
    //        this.idFormato = idFormato;
    //    }
    public ArrayList<SelectItem> getListaClientes() {
        if (this.listaClientes == null) {
            this.obtenerClientesCedis();
        }
        return listaClientes;
    }

    public void setListaClientes(ArrayList<SelectItem> listaClientes) {
        this.listaClientes = listaClientes;
    }

    public ArrayList<TOCliente> getClientes() {
        return clientes;
    }

    public void setClientes(ArrayList<TOCliente> clientes) {
        this.clientes = clientes;
    }

    public TOCliente getCliente() {
        return cliente;
    }

    public void setCliente(TOCliente cliente) {
        this.cliente = cliente;
    }

    public int getIdClienteGpo() {
        return idClienteGpo;
    }

    public void setIdClienteGpo(int idClienteGpo) {
        this.idClienteGpo = idClienteGpo;
    }
}
