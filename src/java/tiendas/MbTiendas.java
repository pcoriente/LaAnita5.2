package tiendas;

import Message.Mensajes;
import agentes.MbMiniAgentes;
import clientes.MbMiniClientes;
import direccion.MbDireccion;
import formatos.MbFormatos;
import impuestos.MbZonas;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.bean.ManagedProperty;
import javax.naming.NamingException;
import mbMenuClientesGrupos.MbClientesGrupos;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import rutas.MbRutas;
import tiendas.dao.DAOTiendas;
import tiendas.dominio.Tienda;
import tiendas.to.TOTienda;

/**
 *
 * @author jesc
 */
@Named(value = "mbTiendas")
@SessionScoped
public class MbTiendas implements Serializable {
    @ManagedProperty(value = "#{mbMenuClientesGrupos}")
    private MbClientesGrupos mbGrupos;
    @ManagedProperty(value = "#{mbMiniClientes}")
    private MbMiniClientes mbClientes;
    @ManagedProperty(value = "#{mbFormatos}")
    private MbFormatos mbFormatos;
    @ManagedProperty(value = "#{mbDireccion}")
    private MbDireccion mbDireccion;
    @ManagedProperty(value = "#{mbMiniAgentes}")
    private MbMiniAgentes mbAgentes;
    @ManagedProperty(value = "#{mbRutas}")
    private MbRutas mbRutas;
    @ManagedProperty(value = "#{mbZonas}")
    private MbZonas mbZonas;
    
    private Tienda tienda, tiendaSeleccionada;
    private ArrayList<Tienda> tiendas;
    private DAOTiendas dao;
    
    public MbTiendas() {
        this.mbGrupos=new MbClientesGrupos();
        this.mbClientes=new MbMiniClientes();
        this.mbFormatos = new MbFormatos();
        this.mbDireccion=new MbDireccion();
        this.mbAgentes=new MbMiniAgentes();
        this.mbRutas=new MbRutas();
        this.mbZonas=new MbZonas();
        
        this.tienda=new Tienda();
        this.tiendaSeleccionada=new Tienda();
    }
    
    public String salir() {
        this.mbGrupos.inicializar();
        this.mbClientes.inicializar();
        this.mbFormatos.inicializar();
        this.tiendas=new ArrayList<Tienda>();
        return "index.xhtml";
    }
    
    public void copia() {
        this.tiendaSeleccionada.setIdTienda(this.tienda.getIdTienda());
        this.tiendaSeleccionada.setTienda(this.tienda.getTienda());
        this.tiendaSeleccionada.setDireccion(this.tienda.getDireccion());
        this.tiendaSeleccionada.setFormato(this.tienda.getFormato());
        this.tiendaSeleccionada.setAgente(this.tienda.getAgente());
        this.tiendaSeleccionada.setRuta(this.tienda.getRuta());
        this.tiendaSeleccionada.setImpuestoZona(this.tienda.getImpuestoZona());
        this.tiendaSeleccionada.setCodigoTienda(this.tienda.getCodigoTienda());
        this.tiendaSeleccionada.setEstado(this.tienda.getEstado());
    }
    
    private TOTienda convertir(Tienda tienda) {
        TOTienda to=new TOTienda();
        to.setIdTienda(tienda.getIdTienda());
        to.setTienda(tienda.getTienda());
        to.setIdDireccion(tienda.getDireccion().getIdDireccion());
        to.setIdFormato(tienda.getFormato().getIdFormato());
        to.setIdCliente(tienda.getFormato().getIdCliente());
        to.setIdAgente(tienda.getAgente().getIdAgente());
        to.setIdRuta(tienda.getRuta().getIdRuta());
        to.setIdImpuestoZona(tienda.getImpuestoZona().getIdZona());
        to.setCodigoTienda(tienda.getCodigoTienda());
        to.setEstado(tienda.getEstado());
        return to;
    }
    
    private boolean valida() {
        boolean ok=false;
        if(this.tienda.getTienda().isEmpty()) {
            Mensajes.mensajeAlert("Se requiere el nombre de la tienda");
        } else if(this.tienda.getDireccion().getCalle().isEmpty()) {
            Mensajes.mensajeError("Se requiere la direccion de la tienda !!!");
        } else if(this.tienda.getAgente().getIdAgente()==0) {
            Mensajes.mensajeAlert("Se requiere asignar un agente !!!");
        } else if(this.tienda.getRuta().getIdRuta()==0) {
            Mensajes.mensajeAlert("Se requiere asignar una ruta !!!");
        } else if(this.tienda.getImpuestoZona().getIdZona()==0) {
            Mensajes.mensajeAlert("Se requiere asignar una zona de impuesto !!!");
        } else {
            ok=true;
        }
        return ok;
    }
    
    public void grabar() {
        boolean ok=false;
        RequestContext context = RequestContext.getCurrentInstance();
        if(this.valida()) {
            try {
                this.dao=new DAOTiendas();
                if(this.tienda.getIdTienda()==0) {
                    int idDireccion=this.mbDireccion.agregar(this.tienda.getDireccion());
                    if(idDireccion!=0) {
                        this.tienda.getDireccion().setIdDireccion(idDireccion);
                        this.tienda.setIdTienda(this.dao.agregar(this.convertir(this.tienda)));
                        this.tiendas.add(this.tienda);
                        ok=true;
                    }
                } else {
                    this.dao.modificar(this.convertir(this.tienda));
                    ok=true;
                }
                if(ok) {
                    this.copia();
//                    Mensajes.mensajeSucces("La tienda se grabo con exito !!!");
                }
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
            }
        }
        context.addCallbackParam("okTienda", ok);
    }
    
    public void modificar(SelectEvent event) {
        this.tiendaSeleccionada=(Tienda) event.getObject();
        this.tienda.setIdTienda(this.tiendaSeleccionada.getIdTienda());
        this.tienda.setTienda(this.tiendaSeleccionada.getTienda());
        this.tienda.setDireccion(this.tiendaSeleccionada.getDireccion());
        this.tienda.setFormato(this.tiendaSeleccionada.getFormato());
        this.tienda.setAgente(this.tiendaSeleccionada.getAgente());
        this.tienda.setRuta(this.tiendaSeleccionada.getRuta());
        this.tienda.setImpuestoZona(this.tiendaSeleccionada.getImpuestoZona());
        this.tienda.setCodigoTienda(this.tiendaSeleccionada.getCodigoTienda());
        this.tienda.setEstado(this.tiendaSeleccionada.getEstado());
    }
    
    public void nuevaTienda() {
        this.tienda=new Tienda(this.mbFormatos.getFormatoSeleccion());
    }
    
    private Tienda convertir(TOTienda to) {
        Tienda t=new Tienda();
        t.setIdTienda(to.getIdTienda());
        t.setTienda(to.getTienda());
        t.setDireccion(this.mbDireccion.obtener(to.getIdDireccion()));
        t.setFormato(this.mbFormatos.getFormatoSeleccion());
        t.setAgente(this.mbAgentes.obtenerAgente(to.getIdAgente()));
        t.setRuta(this.mbRutas.obtenerRuta(to.getIdRuta()));
        t.setImpuestoZona(this.mbZonas.obtenerZona(to.getIdImpuestoZona()));
        t.setCodigoTienda(to.getCodigoTienda());
        t.setEstado(to.getEstado());
        return t;
    }
    
    public void obtenerTiendasFormato(int idFormato) {
        this.tiendas=new ArrayList<Tienda>();
        if(idFormato!=0) {
            try {
                this.dao=new DAOTiendas();
                for(TOTienda to: this.dao.obtenerTiendasFormato(idFormato)) {
                    this.tiendas.add(this.convertir(to));
                }
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getErrorCode()+" "+ex.getMessage());
            }
        }
    }
    
    public void cambioDeCliente() {
        this.mbFormatos.cargarFormatosCliente(this.mbClientes.getCliente().getIdCliente());
        this.obtenerTiendasFormato(0);
    }
    
    public void cambioDeGrupo() {
        this.mbClientes.cargarClientesGrupo(this.mbGrupos.getClienteGrupoSeleccionado().getIdGrupoCte());
        this.mbFormatos.cargarFormatosCliente(0);
//        this.obtenerTiendasFormato(0);
    }

    public Tienda getTienda() {
        return tienda;
    }

    public void setTienda(Tienda tienda) {
        this.tienda = tienda;
    }

    public ArrayList<Tienda> getTiendas() {
        return tiendas;
    }

    public void setTiendas(ArrayList<Tienda> tiendas) {
        this.tiendas = tiendas;
    }

    public MbClientesGrupos getMbGrupos() {
        return mbGrupos;
    }

    public void setMbGrupos(MbClientesGrupos mbGrupos) {
        this.mbGrupos = mbGrupos;
    }

    public MbMiniClientes getMbClientes() {
        return mbClientes;
    }

    public void setMbClientes(MbMiniClientes mbClientes) {
        this.mbClientes = mbClientes;
    }

    public MbFormatos getMbFormatos() {
        return mbFormatos;
    }

    public void setMbFormatos(MbFormatos mbFormatos) {
        this.mbFormatos = mbFormatos;
    }

    public MbDireccion getMbDireccion() {
        return mbDireccion;
    }

    public void setMbDireccion(MbDireccion mbDireccion) {
        this.mbDireccion = mbDireccion;
    }

    public MbMiniAgentes getMbAgentes() {
        return mbAgentes;
    }

    public void setMbAgentes(MbMiniAgentes mbAgentes) {
        this.mbAgentes = mbAgentes;
    }

    public MbRutas getMbRutas() {
        return mbRutas;
    }

    public void setMbRutas(MbRutas mbRutas) {
        this.mbRutas = mbRutas;
    }

    public MbZonas getMbZonas() {
        return mbZonas;
    }

    public void setMbZonas(MbZonas mbZonas) {
        this.mbZonas = mbZonas;
    }
}
