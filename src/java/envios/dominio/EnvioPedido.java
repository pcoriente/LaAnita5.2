package envios.dominio;

import java.util.Date;

/**
 *
 * @author jesc
 */
public class EnvioPedido {
    private int idEnvio;
    private int idMovto;
//    private int idTipoMovto;
    private boolean agregado;
    private boolean directo;
    private String ordenDeCarga;
    private int folio;
    private Date fecha;
    private String formato;
    private int idTienda;
    private String tienda;
    
    public EnvioPedido() {
        this.ordenDeCarga="";
        this.fecha=new Date();
        this.formato="";
        this.tienda="";
    }

    public int getIdEnvio() {
        return idEnvio;
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
    }

    public int getIdMovto() {
        return idMovto;
    }

    public void setIdMovto(int idMovto) {
        this.idMovto = idMovto;
    }

    public boolean isAgregado() {
        return agregado;
    }

    public void setAgregado(boolean agregado) {
        this.agregado = agregado;
    }

    public boolean isDirecto() {
        return directo;
    }

    public void setDirecto(boolean directo) {
        this.directo = directo;
    }

    public String getOrdenDeCarga() {
        return ordenDeCarga;
    }

    public void setOrdenDeCarga(String ordenDeCarga) {
        this.ordenDeCarga = ordenDeCarga;
    }

    public int getFolio() {
        return folio;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getTienda() {
        return tienda;
    }

    public void setTienda(String tienda) {
        this.tienda = tienda;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public int getIdTienda() {
        return idTienda;
    }

    public void setIdTienda(int idTienda) {
        this.idTienda = idTienda;
    }
}
