package proveedores.dominio;

import java.util.ArrayList;

/**
 *
 * @author jsolis
 */
public class ProveedorProductoLista {
    private ProveedorProducto producto;
    //private ArrayList<ProveedorProductoPrecio> precios;
    //private ArrayList<ProveedorProductoOferta> ofertas;

    public ProveedorProductoLista() {
        this.producto=new ProveedorProducto();
        //this.precios=new ArrayList<ProveedorProductoPrecio>();
        //this.ofertas=new ArrayList<ProveedorProductoOferta>();
    }

    public ProveedorProducto getProducto() {
        return producto;
    }

    public void setProducto(ProveedorProducto producto) {
        this.producto = producto;
    }
    /*
    public ArrayList<ProveedorProductoPrecio> getPrecios() {
        return precios;
    }

    public void setPrecios(ArrayList<ProveedorProductoPrecio> precios) {
        this.precios = precios;
    }

    public ArrayList<ProveedorProductoOferta> getOfertas() {
        return ofertas;
    }

    public void setOfertas(ArrayList<ProveedorProductoOferta> ofertas) {
        this.ofertas = ofertas;
    }
    * */
}
