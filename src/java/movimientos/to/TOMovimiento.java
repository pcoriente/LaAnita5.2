package movimientos.to;

import java.util.Date;

/**
 *
 * @author jesc
 */
public class TOMovimiento {
    private int idMovto;
    private int idTipo;
    private int idCedis;
    private int idEmpresa;
    private int idAlmacen;
    private int folio;
    private int idComprobante;
    private int idImpuestoZona;
    private double desctoComercial;
    private double desctoProntoPago;
    private Date fecha;
    private int idUsuario;
    private int idMoneda;
    private double tipoCambio;
    private int idReferencia;
    private int referencia;
    private int statusOficina;
    //////////////////////////
    private int tipoComprobante;
    private String remision;
    private String serie;
    private String numero;
    private int idUsuarioComprobante;
    private Date fechaComprobante;
    private int propietario;
    //////////////////////////
    private int idMovtoAlmacen;
    private Date fechaAlmacen;
    private int idUsuarioAlmacen;
    private int statusAlmacen;
    

    public TOMovimiento() {
        this.fecha=new Date();
        this.remision="";
        this.serie="";
        this.numero="";
        this.fechaComprobante=new Date();
        this.fechaAlmacen=new Date();
    }

    public int getIdMovto() {
        return idMovto;
    }

    public void setIdMovto(int idMovto) {
        this.idMovto = idMovto;
    }

    public int getIdMovtoAlmacen() {
        return idMovtoAlmacen;
    }

    public void setIdMovtoAlmacen(int idMovtoAlmacen) {
        this.idMovtoAlmacen = idMovtoAlmacen;
    }

    public int getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(int idTipo) {
        this.idTipo = idTipo;
    }

    public int getIdAlmacen() {
        return idAlmacen;
    }

    public void setIdAlmacen(int idAlmacen) {
        this.idAlmacen = idAlmacen;
    }

    public int getIdComprobante() {
        return idComprobante;
    }

    public void setIdComprobante(int idComprobante) {
        this.idComprobante = idComprobante;
    }

    public int getIdImpuestoZona() {
        return idImpuestoZona;
    }

    public void setIdImpuestoZona(int idImpuestoZona) {
        this.idImpuestoZona = idImpuestoZona;
    }

    public int getIdMoneda() {
        return idMoneda;
    }

    public void setIdMoneda(int idMoneda) {
        this.idMoneda = idMoneda;
    }

    public double getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(double tipoCambio) {
        this.tipoCambio = tipoCambio;
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

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getIdCedis() {
        return idCedis;
    }

    public void setIdCedis(int idCedis) {
        this.idCedis = idCedis;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public int getFolio() {
        return folio;
    }

    public void setFolio(int folio) {
        this.folio = folio;
    }

    public int getIdUsuarioAlmacen() {
        return idUsuarioAlmacen;
    }

    public void setIdUsuarioAlmacen(int idUsuarioAlmacen) {
        this.idUsuarioAlmacen = idUsuarioAlmacen;
    }

    public Date getFechaAlmacen() {
        return fechaAlmacen;
    }

    public void setFechaAlmacen(Date fechaAlmacen) {
        this.fechaAlmacen = fechaAlmacen;
    }

    public int getIdReferencia() {
        return idReferencia;
    }

    public void setIdReferencia(int idReferencia) {
        this.idReferencia = idReferencia;
    }

    public int getReferencia() {
        return referencia;
    }

    public void setReferencia(int referencia) {
        this.referencia = referencia;
    }

    public int getTipoComprobante() {
        return tipoComprobante;
    }

    public void setTipoComprobante(int tipoComprobante) {
        this.tipoComprobante = tipoComprobante;
    }

    public String getRemision() {
        return remision;
    }

    public void setRemision(String remision) {
        this.remision = remision;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public int getIdUsuarioComprobante() {
        return idUsuarioComprobante;
    }

    public void setIdUsuarioComprobante(int idUsuarioComprobante) {
        this.idUsuarioComprobante = idUsuarioComprobante;
    }

    public Date getFechaComprobante() {
        return fechaComprobante;
    }

    public void setFechaComprobante(Date fechaComprobante) {
        this.fechaComprobante = fechaComprobante;
    }

    public int getStatusOficina() {
        return statusOficina;
    }

    public void setStatusOficina(int statusOficina) {
        this.statusOficina = statusOficina;
    }

    public int getStatusAlmacen() {
        return statusAlmacen;
    }

    public void setStatusAlmacen(int statusAlmacen) {
        this.statusAlmacen = statusAlmacen;
    }

    public int getPropietario() {
        return propietario;
    }

    public void setPropietario(int propietario) {
        this.propietario = propietario;
    }
}
