package tiendas.dominio;

import agentes.dominio.Agente;
import direccion.dominio.Direccion;
import formatos.dominio.ClienteFormato;
import impuestos.dominio.ImpuestoZona;
import rutas.dominio.Ruta;

/**
 *
 * @author jesc
 */
public class Tienda {
    private int idTienda;
    private String tienda;
    private Direccion direccion;
    private ClienteFormato formato;
    private Agente agente;
    private Ruta ruta;
    private ImpuestoZona impuestoZona;
    private int codigoTienda;
    private int estado;
    
    public Tienda() {
        this.tienda="";
        this.direccion=new Direccion();
        this.formato=new ClienteFormato();
        this.agente=new Agente();
        this.ruta=new Ruta();
        this.impuestoZona=new ImpuestoZona();
    }
    
    public Tienda(ClienteFormato formato) {
        this.tienda="";
        this.direccion=new Direccion();
        this.formato=formato;
        this.agente=new Agente();
        this.ruta=new Ruta();
        this.impuestoZona=new ImpuestoZona();
    }

    public int getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(int idTienda) {
        this.idTienda = idTienda;
    }

    public String getTienda() {
        return tienda;
    }

    public void setTienda(String tienda) {
        this.tienda = tienda;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }

    public ClienteFormato getFormato() {
        return formato;
    }

    public void setFormato(ClienteFormato formato) {
        this.formato = formato;
    }

    public Agente getAgente() {
        return agente;
    }

    public void setAgente(Agente agente) {
        this.agente = agente;
    }

    public Ruta getRuta() {
        return ruta;
    }

    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }

    public ImpuestoZona getImpuestoZona() {
        return impuestoZona;
    }

    public void setImpuestoZona(ImpuestoZona impuestoZona) {
        this.impuestoZona = impuestoZona;
    }

    public int getCodigoTienda() {
        return codigoTienda;
    }

    public void setCodigoTienda(int codigoTienda) {
        this.codigoTienda = codigoTienda;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }
}
