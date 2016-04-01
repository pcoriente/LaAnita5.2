/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clientesListas;

import Message.Mensajes;
//import clientesListas.dominio.ClientesListas;
//import clientesListas.formatosDetalleDominio.ClienteListasDetalle;
import java.io.Serializable;
import java.util.ArrayList;
import java.sql.SQLException;
import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedProperty;
import javax.inject.Named;
import javax.naming.NamingException;
import org.primefaces.event.SelectEvent;

import empresas.MbMiniEmpresas;
import mbMenuClientesGrupos.MbClientesGrupos;
import clientes.MbTiendasFormatos;
import clientes.MbMiniClientes;
import producto2.MbProductosBuscar;
import clientesListas.DAOClientesLista.DAOClientesLista;

import clientes.to.TOClienteLista;
import clientesListas.formatosDetalleDominio.ClienteListasDetalle;
import producto2.dominio.Producto;
import clientes.dominio.TiendaFormato;

//import clientesTienda.DAOClientesTienda.DAOClientesTienda;
//import clientesListas.DAOClientesLista.DAOClientesLista;

/**
 *
 * @author Usuario
 */
@Named(value = "mbClientesListas")
@SessionScoped
public class MbClientesListas implements Serializable {
    //esto se puso para que no de errores
// hasta aqui termina para que no de errores

    @ManagedProperty(value = "#{mbMiniEmpresas}")
    private MbMiniEmpresas mbMiniEmpresas = new MbMiniEmpresas();
    @ManagedProperty(value = "#{mbClientesGrupos}")
    private MbClientesGrupos mbClientesGrupos = new MbClientesGrupos();
    @ManagedProperty(value = "#{mbTiendasFormatos}")
    private MbTiendasFormatos mbTiendasFormatos = new MbTiendasFormatos();
    @ManagedProperty(value = "#{mbBuscar}")
    private MbProductosBuscar mbBuscar = new MbProductosBuscar();
//    ArrayList<ClienteListasDetalle> lstListaProductos = new ArrayList<ClienteListasDetalle>();
//    private ClientesListas clientesListas = new ClientesListas();
//    private ClientesListas clientesListaSeleccionada = new ClientesListas();
    private TOClienteLista clientesListas = new TOClienteLista();
    private TOClienteLista clientesListaSeleccionada = new TOClienteLista();
    private ClienteListasDetalle clientesListasDetalle = new ClienteListasDetalle();
    private Producto producto = new Producto();

//    private ClienteListasDetalle clientesListasSeleccion = new ClienteListasDetalle();
    private boolean eliminar = false;
    private ArrayList<TOClienteLista> listaPrecios;
    private ArrayList<Producto> listaEmpaques;

    public MbClientesListas() {
    }

//    public ArrayList<TOClienteLista> getListaPrecios() {
//        if (listaPrecios == null) {
//            //cargarTablaAgente();
//        }
//        return listaPrecios;
//    }
    public void cargarFormatos() {
//        mbClientesGrupos.getMbFormatos().setLstFormatos(null);
//        mbClientesGrupos.getMbFormatos().cargarListaFormatos(mbClientesGrupos.getCmbClientesGrupos().getIdGrupoCte());
//        traerInformacionClientesListas();
    }

    public void cargaTablaListas() throws NamingException, SQLException {
        DAOClientesLista dao = new DAOClientesLista();
        listaPrecios = dao.cargaListas();

        for (TOClienteLista clientes : listaPrecios) {
            clientes.setMiniemp(this.mbMiniEmpresas.obtenerEmpresa(clientes.getIdEmpresa()));
            clientes.setClientegrupo(this.mbClientesGrupos.obtenerGrupo(clientes.getIdGrupoCte()));
            if (clientes.getIdFormato() != 0) {
                clientes.setClienteformato(this.mbTiendasFormatos.obtenerFormato(clientes.getIdFormato()));
            }
            //clientes.setMiniemp(this.mbMiniEmpresas.getDao().obtenerMiniEmpresa(clientes.getIdEmpresa()));
            //System.out.print(clientes.getIdEmpresa() + " lista " + clientes.getIdClienteLista() + " " + clientes.getMiniemp().getNombreComercial() + " " + clientes.getIdCliente());
        }

    }

//    public void guardar() {
//        boolean ok = validar();
//        if (ok == true) {
//            Mensajes.mensajeSucces("Exito!! Datos guardados exitosamente");
//        }
//    }
    public void limpiarBuscador() {
        mbBuscar.limpiarBuscador();
    }

//    public boolean validar() {
//        boolean ok = false;
//        if (mbMiniEmpresas.getEmpresa().getIdEmpresa() == 0) {
//            Mensajes.mensajeAlert("Seleccione una empresa");
//        } else if (mbClientesGrupos.getCmbClientesGrupos().getIdGrupoCte() == 0) {
//            Mensajes.mensajeAlert("Seleccione un un grupo");
//        } else if (clientesListas.getMercanciaSinCargo() == 0) {
//            Mensajes.mensajeAlert("Ingrese una mercancia sin cargo");
//        } else if (clientesListas.getDescuetos().equals("")) {
//            Mensajes.mensajeAlert("Ingrese un descuento");
//        } else if (clientesListas.getMercanciaConCargo() == 0) {
//            Mensajes.mensajeAlert("Ingrese Cantidad mercancia con cargo");
////        } else if (mbClientesGrupos.getMbFormatos().getLstFormatos().size() > 1 && mbClientesGrupos.getMbFormatos().getCmbClientesFormatos().getIdFormato() == 0) {
////            Mensajes.mensajeAlert("Seleccione un formato");
//        } else if (clientesListas.getBoletin() == 0.00) {
//            Mensajes.mensajeAlert("Ingrese un boletin");
//        } else if (clientesListas.getNumeroProveedor().equals("") || clientesListas.getNumeroProveedor() == null) {
//            Mensajes.mensajeAlert("Ingrese un numero de proveedor");
//        } else if (lstListaProductos.isEmpty() || lstListaProductos == null) {
//            Mensajes.mensajeAlert("Ingrese productos a la tabla");
//        } else {
//            ok = true;
//        }
//        return ok;
//    }
    public void obtenerListaSeleccionada(SelectEvent selectEvent) {
        this.clientesListaSeleccionada = (TOClienteLista) selectEvent.getObject();
        //this.mbBuscar.obtenerProducto(clientesListaSeleccionada.)
        //System.out.println("estamos" + this.clientesListaSeleccionada.getIdClienteLista());
    }

//    public void traerInformacionClientesListas(SelectEvent event) {
//        System.out.print("entrando al metodo");
//        this.clientesListaSeleccionada = (TOClienteLista) event.getObject();
//        System.out.print("saliendo " + clientesListaSeleccionada.getIdClienteLista());
//        try {
//            if (mbEmpresas.getEmpresa().getIdEmpresa() > 0 && mbClientesGrupos.getCmbClientesGrupos().getIdGrupoCte() > 0 && mbClientesGrupos.getMbFormatos().getLstFormatos().size() > 1 && mbClientesGrupos.getMbFormatos().getCmbClientesFormatos().getIdFormato() > 0) {
//                System.err.println("entro a buscar los datos los 3 parametros");
//                DAOClientesLista dao = new DAOClientesLista();
//                int idFormato = mbClientesGrupos.getMbFormatos().getCmbClientesFormatos().getIdFormato();
//                clientesListas = dao.dameInformacion(mbEmpresas.getEmpresa().getIdEmpresa(), mbClientesGrupos.getCmbClientesGrupos().getIdGrupoCte(), idFormato);
//            } else if (mbEmpresas.getEmpresa().getIdEmpresa() > 0 && mbClientesGrupos.getCmbClientesGrupos().getIdGrupoCte() > 0 && mbClientesGrupos.getMbFormatos().getLstFormatos().size() == 1) {
//                System.err.println("entro a buscar los datos los 2 parametros");
//                DAOClientesLista dao = new DAOClientesLista();
//                clientesListas = dao.dameInformacion(mbEmpresas.getEmpresa().getIdEmpresa(), mbClientesGrupos.getCmbClientesGrupos().getIdGrupoCte());
//            }
//        } catch (NamingException ex) {
//            Mensajes.mensajeError(ex.getMessage());
//            Logger.getLogger(MbClientesListas.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (SQLException ex) {
//            Mensajes.mensajeError(ex.getMessage());
//            Logger.getLogger(MbClientesListas.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (NumberFormatException ex) {
//            Mensajes.mensajeError(ex.getMessage());
//        }
//    }

//    public void quitarProductoTabla() {
//        for (ClienteListasDetalle clientes : lstListaProductos) {
//            if (clientes.equals(clientesListasSeleccion)) {
//                lstListaProductos.remove(clientesListasSeleccion);
//                break;
//            }
//        }
//        clientesListasSeleccion = null;
//        eliminar = false;
//    }
    public String salir() {
        return "index.xhtml";
    }

    public void eliminarProduco() {
        eliminar = true;
    }

    public MbMiniEmpresas getMbMiniEmpresas() {
        return mbMiniEmpresas;
    }

    public void setMbMiniEmpresas(MbMiniEmpresas mbMiniEmpresas) {
        this.mbMiniEmpresas = mbMiniEmpresas;
    }

    public MbClientesGrupos getMbClientesGrupos() {
        return mbClientesGrupos;
    }

    public void setMbClientesGrupos(MbClientesGrupos mbClientesGrupos) {
        this.mbClientesGrupos = mbClientesGrupos;
    }

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
        }
    }

//    public void construir() {
//        boolean ok = false;
//        for (Producto p : mbBuscar.getSeleccionados()) {
//            if (lstListaProductos.isEmpty()) {
//                ClienteListasDetalle listaDetalle = new ClienteListasDetalle();
//                listaDetalle.setProducto(p);
//                lstListaProductos.add(listaDetalle);
//            } else {
//                for (ClienteListasDetalle detalle : lstListaProductos) {
//                    if (p.getIdProducto() == detalle.getProducto().getIdProducto()) {
//                        ok = false;
//                        break;
//                    } else {
//                        ok = true;
//                    }
//                }
//            }
//            if (ok == true) {
//                ClienteListasDetalle listaDetalle = new ClienteListasDetalle();
//                listaDetalle.setProducto(p);
//                lstListaProductos.add(listaDetalle);
//            }
//        }
//    }
//    public ArrayList<ClienteListasDetalle> getLstListaProductos() {
//        return lstListaProductos;
//    }
//
//    public void setLstListaProductos(ArrayList<ClienteListasDetalle> lstListaProductos) {
//        this.lstListaProductos = lstListaProductos;
//    }
//
//    public ClienteListasDetalle getClientesListasSeleccion() {
//        return clientesListasSeleccion;
//    }
//
//    public void setClientesListasSeleccion(ClienteListasDetalle clientesListasSeleccion) {
//        this.clientesListasSeleccion = clientesListasSeleccion;
//    }
    public boolean isEliminar() {
        return eliminar;
    }

    public void setEliminar(boolean eliminar) {
        this.eliminar = eliminar;
    }

    public ArrayList<TOClienteLista> getListaPrecios() throws NamingException, SQLException {
        if (listaPrecios == null) {
            cargaTablaListas();
        }
        return listaPrecios;
    }

    public void setListaPrecios(ArrayList<TOClienteLista> listaPrecios) {
        this.listaPrecios = listaPrecios;
    }

    public TOClienteLista getClientesListas() {
        return clientesListas;
    }

    public void setClientesListas(TOClienteLista clientesListas) {
        this.clientesListas = clientesListas;
    }

    public TOClienteLista getClientesListaSeleccionada() {
        return clientesListaSeleccionada;
    }

    public void setClientesListaSeleccionada(TOClienteLista clientesListaSeleccionada) {
        this.clientesListaSeleccionada = clientesListaSeleccionada;
    }
    public ClienteListasDetalle getClientesListasDetalle() {
        return clientesListasDetalle;
    }

    public void setClientesListasDetalle(ClienteListasDetalle clientesListasDetalle) {
        this.clientesListasDetalle = clientesListasDetalle;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public ArrayList<Producto> getListaEmpaques() {
        return listaEmpaques;
    }

    public void setListaEmpaques(ArrayList<Producto> listaEmpaques) {
        this.listaEmpaques = listaEmpaques;
    }


}
