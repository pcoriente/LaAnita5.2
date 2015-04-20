package proveedores.dominio;

import contactos.dominio.Contacto;
import contribuyentes.Contribuyente;
import direccion.dominio.Direccion;
import impuestos.dominio.ImpuestoZona;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Julio
 */
public class Proveedor implements Serializable {

    private int idProveedor;
    private String nombreComercial;
    private Contribuyente contribuyente;
    private Clasificacion clasificacion;
    private SubClasificacion subClasificacion;
    private TipoOperacion tipoOperacion;
    private TipoTercero tipoTercero;
    private ImpuestoZona impuestoZona;
    private Direccion direccionFiscal;
    private Direccion direccionEntrega;
    private int diasCredito;
    private double limiteCredito;
    private String fechaAlta;
    private ArrayList<Contacto> contactos;
    private double desctoComercial;
    private double desctoProntoPago;

    public Proveedor() {
        this.idProveedor = 0;
        this.nombreComercial = "";
        this.contribuyente = new Contribuyente();
        this.clasificacion = new Clasificacion();
        this.subClasificacion = new SubClasificacion();
        this.tipoOperacion = new TipoOperacion();
        this.tipoTercero = new TipoTercero();
        this.contactos = new ArrayList<Contacto>();
        this.impuestoZona = new ImpuestoZona(0, "");
        this.direccionEntrega = new Direccion();
        this.diasCredito = 0;
        this.limiteCredito = 0.00;
        this.fechaAlta = "";
        this.desctoComercial = 0.00;
        this.desctoProntoPago = 0.00;
    }

    @Override
    public String toString() {
        //return this.nombreComercial;
        return this.contribuyente.getContribuyente();
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public Contribuyente getContribuyente() {
        return contribuyente;
    }

    public void setContribuyente(Contribuyente contribuyente) {
        this.contribuyente = contribuyente;
    }

    public Clasificacion getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(Clasificacion clasificacion) {
        this.clasificacion = clasificacion;
    }

    public TipoOperacion getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(TipoOperacion tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
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

    public Direccion getDireccionFiscal() {
        return direccionFiscal;
    }

    public void setDireccionFiscal(Direccion direccionFiscal) {
        this.direccionFiscal = direccionFiscal;
    }

    public Direccion getDireccionEntrega() {
        return direccionEntrega;
    }

    public void setDireccionEntrega(Direccion direccionEntrega) {
        this.direccionEntrega = direccionEntrega;
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

    public SubClasificacion getSubClasificacion() {
        return subClasificacion;
    }

    public void setSubClasificacion(SubClasificacion subClasificacion) {
        this.subClasificacion = subClasificacion;
    }

    public ArrayList<Contacto> getContactos() {
        return contactos;
    }

    public void setContactos(ArrayList<Contacto> contactos) {
        this.contactos = contactos;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public double getDesctoComercial() {
        return desctoComercial;
    }

    public void setDesctoComercial(double desctoComercial) {
        this.desctoComercial = desctoComercial;
    }

    public double getDesctoProntoPago() {
        return desctoProntoPago;
    }

    public void setDesctoProntoPago(double desctoProntoPago) {
        this.desctoProntoPago = desctoProntoPago;
    }
}
