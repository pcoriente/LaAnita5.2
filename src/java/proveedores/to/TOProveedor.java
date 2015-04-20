package proveedores.to;

import contribuyentes.Contribuyente;
import contribuyentes.TOContribuyente;
import impuestos.dominio.ImpuestoZona;
import proveedores.dominio.Clasificacion;
import proveedores.dominio.TipoOperacion;
import proveedores.dominio.TipoTercero;

/**
 *
 * @author Julio
 */
public class TOProveedor {
    private int idProveedor;
    private TOContribuyente contribuyente;
    private Clasificacion clasificacion;
    private TipoOperacion tipoOeracion;
    private TipoTercero tipoTercero;
    private ImpuestoZona impuestoZona;
    private int idDireccionEntrega;
    private String telefono;
    private String fax;
    private String correo;
    private int diasCredito;
    private double limiteCredito;
    private String fechaAlta;

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public TOContribuyente getContribuyente() {
        return contribuyente;
    }

    public void setContribuyente(TOContribuyente contribuyente) {
        this.contribuyente = contribuyente;
    }

    public Clasificacion getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(Clasificacion clasificacion) {
        this.clasificacion = clasificacion;
    }

    public TipoOperacion getTipoOeracion() {
        return tipoOeracion;
    }

    public void setTipoOeracion(TipoOperacion tipoOeracion) {
        this.tipoOeracion = tipoOeracion;
    }

    public TipoTercero getTipoTercero() {
        return tipoTercero;
    }

    public void setTipoTercero(TipoTercero tipoTercero) {
        this.tipoTercero = tipoTercero;
    }

    public ImpuestoZona getImpuestoZona() {
        return impuestoZona;
    }

    public void setImpuestoZona(ImpuestoZona impuestoZona) {
        this.impuestoZona = impuestoZona;
    }

    public int getIdDireccionEntrega() {
        return idDireccionEntrega;
    }

    public void setIdDireccionEntrega(int idDireccionEntrega) {
        this.idDireccionEntrega = idDireccionEntrega;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public int getDiasCredito() {
        return diasCredito;
    }

    public void setDiasCredito(int diasCredito) {
        this.diasCredito = diasCredito;
    }

    public double getLimiteCredito() {
        return limiteCredito;
    }

    public void setLimiteCredito(double limiteCredito) {
        this.limiteCredito = limiteCredito;
    }

    public String getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(String fechaAlta) {
        this.fechaAlta = fechaAlta;
    }
}
