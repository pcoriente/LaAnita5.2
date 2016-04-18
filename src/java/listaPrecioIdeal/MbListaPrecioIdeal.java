/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package listaPrecioIdeal;

import Message.Mensajes;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.faces.model.SelectItem;

import empresas.MbMiniEmpresas;
import mbMenuClientesGrupos.MbClientesGrupos;
import clientes.MbMiniClientes;
import clientes.MbTiendasFormatos;

import empresas.dominio.MiniEmpresa;
import formatos.dominio.ClienteFormato;
import menuClientesGrupos.dominio.ClienteGrupo;
import clientes.to.TOCliente;
//import clientes.dominio.TiendaFormato;
import listaPrecioIdeal.DAO.DAOListaPrecio;
import listaPrecioIdeal.dominio.ListaPrecioIdeal;
import listaPrecioIdeal.to.TOPrecioListaIdeal;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.primefaces.context.RequestContext;
import org.primefaces.event.RowEditEvent;
import producto2.MbProductosBuscar;
import producto2.dominio.Producto;
import utilerias.Numero_a_Letra;
import utilerias.Utilerias;

/**
 *
 * @author Usuario
 */
@Named(value = "mbListaPrecioIdeal")
@SessionScoped
public class MbListaPrecioIdeal implements Serializable {

    /**
     * Creates a new instance of MbListaPrecioIdeal
     */
    @ManagedProperty(value = "#{mbMiniEmpresas}")
    private MbMiniEmpresas mbEmpresas;
    @ManagedProperty(value = "#{mbClientesGrupos}")
    private MbClientesGrupos mbClientesGrupos = new MbClientesGrupos();
    @ManagedProperty(value = "#{mbTiendasFormatos}")
    private MbTiendasFormatos mbTiendasFormatos = new MbTiendasFormatos();
    @ManagedProperty(value = "#{mbMiniClientes}")
    private MbMiniClientes mbMiniClientes = new MbMiniClientes();

    //private Empresa empresa = new Empresa();
    private ArrayList<Producto> lstProducto = new ArrayList<Producto>();
    private ArrayList<ListaPrecioIdeal> lstListaPrecioIdeal = new ArrayList<ListaPrecioIdeal>();
    private ListaPrecioIdeal precioIdeal = new ListaPrecioIdeal();
    @ManagedProperty(value = "#{mbBuscar}")
    private MbProductosBuscar mbBuscar = new MbProductosBuscar();
    private ArrayList<ListaPrecioIdeal> lstListaPrecioTable;
    private ListaPrecioIdeal listaPrecioSeleccionPrincipal;
    private ListaPrecioIdeal listaPrecioSeleccionSecundaria;
    private boolean actualizar = false;
    private ArrayList<ListaPrecioIdeal> filterLstPrecioIdeal;
    private ArrayList<SelectItem> listaFormatos;

    public MbListaPrecioIdeal() {
        this.mbEmpresas = new MbMiniEmpresas();
        this.mbClientesGrupos = new MbClientesGrupos();
        this.mbTiendasFormatos = new MbTiendasFormatos();
        this.mbMiniClientes = new MbMiniClientes();
               this.mbMiniClientes.setListaClientes(null);// para probar
    }

    public void buscar() {

        this.mbBuscar.buscarLista();
    }

    public void construir() {
        ArrayList<ListaPrecioIdeal> respaldo = new ArrayList<ListaPrecioIdeal>();
        for (Producto p : mbBuscar.getSeleccionados()) {
            ListaPrecioIdeal listaPrecioIdeal = new ListaPrecioIdeal();
            listaPrecioIdeal.setProducto(p);
            lstListaPrecioIdeal.add(listaPrecioIdeal);
        }
        for (int x = 0; x < lstListaPrecioIdeal.size(); x++) {
            for (int y = 0; y < lstListaPrecioTable.size(); y++) {
                if (lstListaPrecioIdeal.get(x).getProducto().getIdProducto() == lstListaPrecioTable.get(y).getProducto().getIdProducto()) {
                    lstListaPrecioIdeal.remove(x);
                    x--;
                    break;
                }
            }
        }
    }

    public void eliminarProducto() {
        for (ListaPrecioIdeal l : lstListaPrecioIdeal) {
            if (l.equals(listaPrecioSeleccionSecundaria)) {
                lstListaPrecioIdeal.remove(l);
                listaPrecioSeleccionSecundaria = null;
                break;
            }
        }
    }

    public String salir() {
        this.mbEmpresas.setEmpresa(new MiniEmpresa());
        this.mbEmpresas.setListaEmpresas(null);
        this.mbClientesGrupos.setClientesGrupos(new ClienteGrupo());
        this.mbClientesGrupos.setItemsClientesGrupos(null);
        this.mbTiendasFormatos.setFormato(new ClienteFormato()); // aqui me pidio importa la clase ClienteFormato
        this.mbTiendasFormatos.setListaFormatos(null);
        this.mbMiniClientes.setCliente(new TOCliente());
        this.mbMiniClientes.setListaClientes(null);
        return "index.xhtml";
    }

    public void cargaCombos() {
        //this.listaFormatos = new ArrayList<>();
        //TiendaFormato tf = new TiendaFormato();
        //----esta es la ok this.mbTiendasFormatos.cargarListaCombo(this.mbClientesGrupos.getClienteGrupoSeleccionado().getIdGrupoCte());

        this.mbTiendasFormatos.cargarListaCombo(this.mbClientesGrupos.getClientesGrupos().getIdGrupoCte());
        this.mbMiniClientes.cargarClientesGrupo(this.mbClientesGrupos.getClientesGrupos().getIdGrupoCte());
        //for (TiendaFormato t : this.mbTiendasFormatos.cargarListaCombo(this.mbClientesGrupos.getClienteGrupoSeleccionado().getIdGrupoCte())){
        //    listaFormatos.add(new SelectItem(t.getIdFormato(), t.getFormato()));
        //}
        //TiendaFormato tf = new TiendaFormato(0,"Seleeciones un Formato");
        //this.listaSubUsuarios.add(new SelectItem(usu, usu.toString()));
        //for (Usuario us : this.mbUsuarios.obtenerSubUsuarios(this.mbDepto.getDeptos().getIdDepto())) {
        //    listaSubUsuarios.add(new SelectItem(us, us.toString()));
        //for (TiendaFormato tfl : this.mbTiendasFormatos.cargarListaCombo(this.mbClientesGrupos.)nuevoFormato(idGrupoCte)){
        //}

    }

    public void actualizarPrecioLista(ListaPrecioIdeal ls) {
//        System.out.println(ls.getPrecioLista());

//        FacesContext context = FacesContext.getCurrentInstance();
        try {
            DAOListaPrecio dao = new DAOListaPrecio();
            dao.actualizar(ls);
            Mensajes.mensajeSucces("Lista de precio actualizada");
//            context.addMessage(null, new FacesMessage("Exito", "Lista de Precio Actualizado "));
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
//            context.addMessage(null, new FacesMessage("Error", ex.getMessage()));
            Logger.getLogger(MbListaPrecioIdeal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getMessage());
//            context.addMessage(null, new FacesMessage("Error", ex.getMessage()));
            Logger.getLogger(MbListaPrecioIdeal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void cancelar() {
        this.actualizar = false;
        this.listaPrecioSeleccionPrincipal = null;
        lstListaPrecioIdeal = new ArrayList<ListaPrecioIdeal>();
        listaPrecioSeleccionSecundaria = null;
    }

    public void cargarDatos() {
        this.setActualizar(true);
        lstListaPrecioIdeal.add(this.getListaPrecioSeleccionPrincipal());
    }

    public void guardar() {
        RequestContext context = RequestContext.getCurrentInstance();
        boolean ok = this.validar();
        FacesMessage fMsg = null;
        if (ok == true) {
            try {
                DAOListaPrecio dao = new DAOListaPrecio();
                dao.guardarLista(lstListaPrecioIdeal);
                fMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Exito:", "Nuevas listas de precio disponibles");
            } catch (NamingException ex) {
                ok = false;
                fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Error:", ex.getMessage());
                Logger.getLogger(MbListaPrecioIdeal.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                ok = false;
                fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Error:", ex.getMessage());
            }
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
            context.addCallbackParam("ok", ok);
            this.cancelar();
        }

    }

    public boolean validar() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Error:", "");
        fMsg.setSeverity(FacesMessage.SEVERITY_WARN);
        for (ListaPrecioIdeal lista : lstListaPrecioIdeal) {
            if (lista.getPrecioLista() == 0.00) {
                ok = false;
                fMsg.setDetail("Producto " + lista.getProducto().toString() + " requiere una lista precio");
                FacesContext.getCurrentInstance().addMessage(null, fMsg);
                break;
            } else {
                ok = true;
            }
        }
        context.addCallbackParam("ok", ok);
        return ok;
    }

    public ArrayList<Producto> getLstProducto() {
        return lstProducto;
    }

    public void setLstProducto(ArrayList<Producto> lstProducto) {
        this.lstProducto = lstProducto;
    }

    public ListaPrecioIdeal getPrecioIdeal() {
        return precioIdeal;
    }

    public void setPrecioIdeal(ListaPrecioIdeal precioIdeal) {
        this.precioIdeal = precioIdeal;
    }

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public ArrayList<ListaPrecioIdeal> getLstListaPrecioIdeal() {
        return lstListaPrecioIdeal;
    }

    public void setLstListaPrecioIdeal(ArrayList<ListaPrecioIdeal> lstListaPrecioIdeal) {
        this.lstListaPrecioIdeal = lstListaPrecioIdeal;
    }

    public ArrayList<ListaPrecioIdeal> getLstListaPrecioTable() {
        if (lstListaPrecioTable == null) {
            try {
                lstListaPrecioTable = new ArrayList<ListaPrecioIdeal>();
                DAOListaPrecio dao = new DAOListaPrecio();
                for (TOPrecioListaIdeal p : dao.dameValores()) {
                    lstListaPrecioTable.add(this.convertir(p));
                }
            } catch (NamingException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (SQLException ex) {
                Mensajes.mensajeError(ex.getMessage());
            } catch (NullPointerException ex) {
                System.err.println("Hubo un error" + ex.getMessage());
            }
        }
        return lstListaPrecioTable;
    }

    public ListaPrecioIdeal convertir(TOPrecioListaIdeal to) {
        ListaPrecioIdeal precioIdeal = new ListaPrecioIdeal();
        Producto pr = this.mbBuscar.obtenerProducto(to.getIdProducto());
        if (pr == null) {
            pr = new Producto();
            pr.setIdProducto(to.getIdProducto());
            System.out.println(to.getIdProducto());
        }
        precioIdeal.setProducto(pr);
        precioIdeal.setPrecioLista(to.getPrecioLista());
        return precioIdeal;
    }

    public void setLstListaPrecioTable(ArrayList<ListaPrecioIdeal> lstListaPrecioTable) {
        this.lstListaPrecioTable = lstListaPrecioTable;
    }

    public ListaPrecioIdeal getListaPrecioSeleccionPrincipal() {
        return listaPrecioSeleccionPrincipal;
    }

    public void setListaPrecioSeleccionPrincipal(ListaPrecioIdeal listaPrecioSeleccionPrincipal) {
        this.listaPrecioSeleccionPrincipal = listaPrecioSeleccionPrincipal;
    }

    public ListaPrecioIdeal getListaPrecioSeleccionSecundaria() {
        return listaPrecioSeleccionSecundaria;
    }

    public void setListaPrecioSeleccionSecundaria(ListaPrecioIdeal listaPrecioSeleccionSecundaria) {
        this.listaPrecioSeleccionSecundaria = listaPrecioSeleccionSecundaria;
    }

    public boolean isActualizar() {
        return actualizar;
    }

    public void setActualizar(boolean actualizar) {
        this.actualizar = actualizar;
    }

    public void generarReporte() throws JRException {
        try {
            DAOListaPrecio dao = new DAOListaPrecio();
            dao.generarReporte();
////        String ruta = "C:\\Reportes\\listaPrecioIdeal.pdf";
//        String ubicacionCompilado = "C:\\Reportes\\miprimerreporte.jasper";
//        JasperPrint jasperprint;
//        JasperReport report;
//        Map<String, Object> parametros = new HashMap<String, Object>();
//        parametros.put("fecha", "27/06/2014");
//        parametros.put("hola", "Mi primer Reporte");
//        try {
//            report = (JasperReport) JRLoader.loadObjectFromFile(ubicacionCompilado);
//            jasperprint = JasperFillManager.fillReport(report, parametros, new JRBeanCollectionDataSource(lstListaPrecioTable));
//            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
//            httpServletResponse.addHeader("Content-disposition", "attachment; filename=listaPrecio.pdf");
//            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
//            JasperExportManager.exportReportToPdfStream(jasperprint, servletOutputStream);
//            try {
////                JasperExportManager.exportReportToPdfFile(jasperprint, ruta);
//            } catch (Exception e) {
//                System.out.println(e);
//            }
//            FacesContext.getCurrentInstance().responseComplete();
//        } catch (Exception e) {
//            System.out.println(e);
//        }
        } catch (NamingException ex) {
            Logger.getLogger(MbListaPrecioIdeal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(MbListaPrecioIdeal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<ListaPrecioIdeal> getFilterLstPrecioIdeal() {
        return filterLstPrecioIdeal;
    }

    public void setFilterLstPrecioIdeal(ArrayList<ListaPrecioIdeal> filterLstPrecioIdeal) {
        this.filterLstPrecioIdeal = filterLstPrecioIdeal;
    }

    public MbMiniEmpresas getMbEmpresas() {
        return mbEmpresas;
    }

    public void setMbEmpresas(MbMiniEmpresas mbEmpresas) {
        this.mbEmpresas = mbEmpresas;
    }

    public MbClientesGrupos getMbClientesGrupos() {
        return mbClientesGrupos;
    }

    public void setMbClientesGrupos(MbClientesGrupos mbClientesGrupos) {
        this.mbClientesGrupos = mbClientesGrupos;
    }

    public ArrayList<SelectItem> getListaFormatos() {
        return listaFormatos;
    }

    public void setListaFormatos(ArrayList<SelectItem> listaFormatos) {
        this.listaFormatos = listaFormatos;
    }

    public MbTiendasFormatos getMbTiendasFormatos() {
        return mbTiendasFormatos;
    }

    public void setMbTiendasFormatos(MbTiendasFormatos mbTiendasFormatos) {
        this.mbTiendasFormatos = mbTiendasFormatos;
    }

    public MbMiniClientes getMbMiniClientes() {
        return mbMiniClientes;
    }

    public void setMbMiniClientes(MbMiniClientes mbMiniClientes) {
        this.mbMiniClientes = mbMiniClientes;
    }

}
