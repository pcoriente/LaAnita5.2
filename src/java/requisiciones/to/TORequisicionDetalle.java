package requisiciones.to;

public class TORequisicionDetalle {

    private int idRequisicion;
    private int idProducto;
    private double cantidad;
    private double cantidadAutorizada;

    public int getIdRequisicion() {
        return idRequisicion;
    }

    public void setIdRequisicion(int idRequisicion) {
        this.idRequisicion = idRequisicion;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    //    public int getCantidad() {
    //        return cantidad;
    //    }
    //
    //    public void setCantidad(int cantidad) {
    //        this.cantidad = cantidad;
    //    }
    //
    //    public int getCantidadAutorizada() {
    //        return cantidadAutorizada;
    //    }
    //
    //    public void setCantidadAutorizada(int cantidadAutorizada) {
    //        this.cantidadAutorizada = cantidadAutorizada;
    //    }
    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public double getCantidadAutorizada() {
        return cantidadAutorizada;
    }

    public void setCantidadAutorizada(double cantidadAutorizada) {
        this.cantidadAutorizada = cantidadAutorizada;
    }
}
