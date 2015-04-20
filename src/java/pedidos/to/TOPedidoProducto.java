package pedidos.to;

/**
 *
 * @author jesc
 */
public class TOPedidoProducto {
    private int idPedido;
    private int idEmpaque;
    private double cantFacturada;
    private double cantSinCargo;
    private double unitario;
    private int idImpuestoGrupo;

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdEmpaque() {
        return idEmpaque;
    }

    public void setIdEmpaque(int idEmpaque) {
        this.idEmpaque = idEmpaque;
    }

    public double getCantFacturada() {
        return cantFacturada;
    }

    public void setCantFacturada(double cantFacturada) {
        this.cantFacturada = cantFacturada;
    }

    public double getCantSinCargo() {
        return cantSinCargo;
    }

    public void setCantSinCargo(double cantSinCargo) {
        this.cantSinCargo = cantSinCargo;
    }

    public double getUnitario() {
        return unitario;
    }

    public void setUnitario(double unitario) {
        this.unitario = unitario;
    }

    public int getIdImpuestoGrupo() {
        return idImpuestoGrupo;
    }

    public void setIdImpuestoGrupo(int idImpuestoGrupo) {
        this.idImpuestoGrupo = idImpuestoGrupo;
    }
}
