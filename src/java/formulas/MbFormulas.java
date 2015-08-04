package formulas;

import Message.Mensajes;
import empresas.MbMiniEmpresas;
import empresas.dominio.MiniEmpresa;
import formulas.dominio.Formula;
import formulas.dominio.Insumo;
import formulas.dao.DAOFormulas;
import formulas.dominio.Linea;
import formulas.to.TOFormula;
import formulas.to.TOInsumo;
import hojasDeCalculo.Generador;
import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import producto2.MbProductosBuscar;
import producto2.dominio.Producto;
import usuarios.MbAcciones;
import usuarios.dominio.Accion;

/**
 *
 * @author jesc
 */
@Named(value = "mbFormulas")
@SessionScoped
public class MbFormulas implements Serializable {

    private ArrayList<Accion> acciones;
    @ManagedProperty(value = "#{mbAcciones}")
    private MbAcciones mbAcciones;
    @ManagedProperty(value = "#{mbMiniEmpresas}")
    private MbMiniEmpresas mbEmpresas;
    @ManagedProperty(value = "#{mbProductosBuscar}")
    private MbProductosBuscar mbBuscar;
    private ArrayList<Formula> formulas;
    private Formula formula;
    private Insumo insumo;
    private Insumo respInsumo;
//    private String update;
    private Producto insumo1, insumo2;
    private int caso;
    private Generador generador;
    private DAOFormulas dao;

    public MbFormulas() {
        this.formula = new Formula();
        this.insumo = new Insumo();
        this.respInsumo = new Insumo();

        this.mbAcciones = new MbAcciones();
        this.mbEmpresas = new MbMiniEmpresas();
        this.mbBuscar = new MbProductosBuscar();
    }

//    public void validaCantidad() {
//        this.formula.setSumaCantidad(this.formula.getSumaCantidad()-this.respInsumo.getCantidad()+this.insumo.getCantidad());
//        this.formula.setSumaCosto(this.formula.getSumaCosto()-this.respInsumo.getCostoPromedio()+this.insumo.getCostoPromedio());
//    }
    public void remplazar() {
        try {
            this.dao = new DAOFormulas();
            this.dao.remplazaInsumo(this.insumo1.getIdProducto(), this.insumo2.getIdProducto());
            Mensajes.mensajeSucces("El insumo fue remplazado con exito !!!");
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void buscarNuevo() {
        this.caso = 4;
        this.mbBuscar.inicializar();
        this.mbBuscar.setUpdate(":main:mttoNuevo");
    }

    public void buscarInsumo() {
        this.caso = 3;
        this.mbBuscar.inicializar();
        this.mbBuscar.setUpdate(":main:mttoSustituir");
    }

//    public void generarDoc() {
////        String ubicacion = "E:\\LaAnita\\Reportes\\formula.jasper";
//        Map<String, Object> parametros = new HashMap<String, Object>();
//        parametros.put("empresa", "LA ANITA, S.A. DE C.V.");
//    }
    public void generarDoc() {
        // Escribe directamente el archivo PDF
        String sourceFileName = "C:\\Carlos Pat\\Reportes\\formula.jasper";
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(this.formula.getInsumos());
        Map parameters = new HashMap();
        parameters.put("empresa", this.mbEmpresas.getEmpresa().getNombreComercial());
        parameters.put("cod_pro", this.formula.getCod_pro());
        parameters.put("empaque", this.formula.getEmpaque()); 
        parameters.put("mermaPtje", this.formula.getMerma());
        parameters.put("mermaCant", this.formula.getMermaCant()); 
        parameters.put("costoPromedio", this.formula.getCostoPromedio());
//        parameters.put("observaciones", this.formula.getObservaciones());
        ArrayList<Linea> listaObservaciones = new ArrayList<>();
        for (String str : this.formula.getObservaciones().split("\r\n")) {
            listaObservaciones.add(new Linea(str));
        }
        parameters.put("listaObservaciones", listaObservaciones);
        try {
//            String printFileName = printFileName = JasperFillManager.fillReportToFile(sourceFileName, parameters, beanColDataSource);
//            if(printFileName != null){
//                JasperExportManager.exportReportToPdfFile(printFileName, "E:\\LaAnita\\Reportes\\formula.pdf");
//            }
            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=formulas.pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (JRException e) {
            Mensajes.mensajeError(e.getMessage());
        } catch (IOException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void generarDocs() {
        String sourceFileName = "C:\\Carlos Pat\\Reportes\\formulas2.jasper";
        JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(this.formulas);
        Map parameters = new HashMap();
        parameters.put("empresa", this.mbEmpresas.getEmpresa().getNombreComercial());
        try {
            JasperReport report = (JasperReport) JRLoader.loadObjectFromFile(sourceFileName);
            JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, beanColDataSource);

            HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            httpServletResponse.setContentType("application/pdf");
            httpServletResponse.addHeader("Content-disposition", "attachment; filename=formulas.pdf");
            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
            JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
            FacesContext.getCurrentInstance().responseComplete();
        } catch (JRException e) {
            Mensajes.mensajeError(e.getMessage());
        } catch (IOException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
    }

    public void generarPdf(String buscarPor) {
//        if(this.generar(buscarPor, idTipo, idGrupo, idSubGrupo)) {
        System.out.println("buscarPor="+buscarPor);
        if (buscarPor == null || buscarPor.equals("") || buscarPor.equals("BUSCAR") || buscarPor.equals("INSUMO")) {
            System.out.println("Entro a generarDocs, muchas formulas.");
            this.generarDocs();
        } else {
            System.out.println("Entro a generarDoc, solo una formula.");
//                this.setFormulas(new ArrayList<Formula>());
//                this.getFormulas().add(this.formula);
            this.generarDoc();
        }
//        }
    }

    public void generaExcels() {
        this.generador = new Generador();
        for (Formula f : this.formulas) {
            this.formula = f;
            this.generaFormulaExcel();
        }
        try {
            this.generador.generaDocumento();
        } catch (IOException ex) {
            Mensajes.mensajeError("IOExcepcion: " + ex.getMessage());
        }
    }

    private void generaFormulaExcel() {
        ArrayList<String> fila;
        fila = new ArrayList<>();
        fila.add(this.mbEmpresas.getEmpresa().getNombreComercial());
        generador.agregarFilaEncabezado(fila);
        fila = new ArrayList<>();
        fila.add(this.formula.getCod_pro());
        fila.add(this.formula.getEmpaque());
        generador.agregarFilaEncabezado(fila);
        fila = new ArrayList<>();
        fila.add("SKU");            // A
        fila.add("INSUMO");         // B
        fila.add("CANTIDAD");       // C
        fila.add("VARIACION -");    // D
        fila.add("VARIACION +");    // E
        fila.add("COSTO PROMEDIO"); // F
        fila.add("IMPORTE");        // G
        fila.add("% IMPORTE PARTICIPACION");
        fila.add("% CANTIDAD PARTICIPACION");
        generador.agregarFilaEncabezado(fila);
        generador.inicializaFilas();
        for (Insumo ins : this.formula.getInsumos()) {
            generador.agregaFila(ins);
        }
        int t = generador.agregarTotales();
        String form;
        for (int f = 1; f <= this.generador.getFilas(); f++) {
            form = "G" + (t - f) + "*100.00/G" + t;
            this.generador.setCeldaFormatoNumero(this.generador.agregarColumnaFormula(t - f, form));
            form = "C" + (t - f) + "*100.00/C" + t;
            this.generador.setCeldaFormatoNumero(this.generador.agregarColumnaFormula(t - f, form));
        }
    }

    public void generaExcel() {
        this.generador = new Generador();
        this.generaFormulaExcel();
        try {
            this.generador.generaDocumento();
        } catch (IOException ex) {
            Mensajes.mensajeError("IOExcepcion: " + ex.getMessage());
        }
    }

    public void generarXls(String buscarPor) {
        System.out.println("buscarPor="+buscarPor);
        if (buscarPor == null || buscarPor.equals("") || buscarPor.equals("BUSCAR") || buscarPor.equals("INSUMO")) {
            System.out.println("Entro a generarDocs, muchas formulas.");
            this.generaExcels();
        } else {
            System.out.println("Entro a generarDoc, solo una formula.");
            this.generaExcel();
        }
    }

    private boolean generarFormulas() {
        boolean ok = false;
        this.formulas = new ArrayList<>();
        try {
            this.dao = new DAOFormulas();
            TOFormula toF;
            for (Producto producto : this.mbBuscar.getProductos()) {
                toF=this.dao.obtenerFormula(this.mbEmpresas.getEmpresa().getIdEmpresa(), producto.getIdProducto());
                if(toF.getIdFormula()!=0) {
                    this.convertirFormula(producto, toF);
                    this.formulas.add(this.formula);
                }
            }
            if (this.formulas.isEmpty()) {
                Mensajes.mensajeAlert("No se encontraron elementos para imprimir !!!");
            } else {
                Mensajes.mensajeSucces("Las formulas se generaron correctamente !!!");
                ok = true;
            }
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        return ok;
    }

    private boolean generarFormula() {
        boolean ok = false;
        try {
            this.dao = new DAOFormulas();
            this.convertirFormula(this.mbBuscar.getProducto(), this.dao.obtenerFormula(this.mbEmpresas.getEmpresa().getIdEmpresa(), this.mbBuscar.getProducto().getIdProducto()));
            Mensajes.mensajeSucces("La formula se genero correctamente !!!");
            ok = true;
        } catch (SQLException ex) {
            Mensajes.mensajeError(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            Mensajes.mensajeError(ex.getMessage());
        }
        return ok;
    }

    public boolean generar(String buscarPor, int idTipo, int idGrupo, int idSubGrupo) {
        boolean ok = false;
        if (this.getMbEmpresas().getEmpresa().getIdEmpresa() == 0) {
            Mensajes.mensajeError("Se requiere seleccionar una empresa !!!");
        } else {
            if (buscarPor == null) {
//                Mensajes.mensajeError("Error de parametro buscar !!!");
                buscarPor="BUSCAR";
            }
            if (buscarPor.equals("FORMULA")) {
                if (this.getMbBuscar().getProducto() == null) {
                    Mensajes.mensajeError("Se requiere la formula a buscar !!!");
                } else {
                    ok = generarFormula();
                }
            } else {
                int idInsumo = 0;
                if (this.getMbBuscar().getProducto() != null) {
                    idInsumo = this.getMbBuscar().getProducto().getIdProducto();
                }
                if (buscarPor.equals("INSUMO")) {
                    if(idInsumo==0) {
                        Mensajes.mensajeError("Se requiere el insumo a buscar !!!");
                    } else if (this.getMbBuscar().buscarPorClasificacion(idTipo, idGrupo, idSubGrupo, idInsumo)) {
                        ok = generarFormulas();
                    }
                } else if (this.getMbBuscar().buscarPorClasificacion(idTipo, idGrupo, idSubGrupo, idInsumo)) {
                    ok = generarFormulas();
                }
            }
        }
        return ok;
    }

    public String salir() {
        this.mbEmpresas.setEmpresa(new MiniEmpresa());
        this.mbEmpresas.setListaEmpresas(null);
        this.mbBuscar.inicializar();
        this.insumo1 = null;
        this.insumo2 = null;
        this.formula = new Formula();
        return "index.xhtml";
    }

    public void insumoSeleccionado(SelectEvent event) {
        this.insumo = (Insumo) event.getObject();
        respaldaInsumo();
    }

    public void respaldaInsumo() {
        this.respInsumo.setCantidad(this.insumo.getCantidad());
        this.respInsumo.setCod_pro(this.insumo.getCod_pro());
        this.respInsumo.setCostoPromedio(this.insumo.getCostoPromedio());
        this.respInsumo.setEmpaque(this.insumo.getEmpaque());
        this.respInsumo.setIdEmpaque(this.insumo.getIdEmpaque());
        this.respInsumo.setVariacion(this.insumo.getVariacion());
    }

    private Insumo convertirInsumo(TOInsumo to) {
        Producto producto = this.mbBuscar.obtenerProducto(to.getIdEmpaque());

        Insumo tmpInsumo = new Insumo();
        tmpInsumo.setNuevo(false);
        tmpInsumo.setIdEmpaque(to.getIdEmpaque());
        tmpInsumo.setCod_pro(producto.getCod_pro());
        tmpInsumo.setEmpaque(producto.toString());
        tmpInsumo.setCantidad(to.getCantidad());
        tmpInsumo.setVariacion(to.getPorcentVariacion());
        tmpInsumo.setCostoPromedio(to.getCostoUnitario());
        return tmpInsumo;
    }

    private void convertirFormula(Producto producto, TOFormula toFormula) throws SQLException {
        this.formula = new Formula();
        this.formula.setIdEmpresa(this.mbEmpresas.getEmpresa().getIdEmpresa());
        this.formula.setIdEmpaque(producto.getIdProducto());
        this.formula.setCod_pro(producto.getCod_pro());
        this.formula.setEmpaque(producto.toString());
        this.formula.setIdTipo(producto.getArticulo().getTipo().getIdTipo());
        this.formula.setIdFormula(toFormula.getIdFormula());
        this.formula.setMerma(toFormula.getMerma());
        this.formula.setManoDeObra(toFormula.getManoDeObra());
        this.formula.setPiezas(producto.getPiezas());
        this.formula.setCostoPromedio(toFormula.getCostoPromedio());
        this.formula.setObservaciones(toFormula.getObservaciones());
        this.formula.setSumaCantidad(0.00);
        this.formula.setSumaCosto(0.00);
        for (TOInsumo toInsumo : this.dao.obtenerInsumos(toFormula.getIdFormula())) {
            this.formula.getInsumos().add(this.convertirInsumo(toInsumo));
        }
        this.calculaSumas();
    }

    public String getTotalSumaCostoPromedio() {
        return new DecimalFormat("###,##0.000000").format(this.formula.getSumaCosto());
    }

    public String getTotalSumaCantidad() {
        return new DecimalFormat("###,##0.000000").format(this.formula.getSumaCantidad());
    }

    public void validaProductoSeleccionado() {
        boolean nuevo = true;
        Insumo producto = new Insumo();
        producto.setIdEmpaque(this.mbBuscar.getProducto().getIdProducto());
        producto.setCod_pro(this.mbBuscar.getProducto().getCod_pro());
        producto.setEmpaque(this.mbBuscar.getProducto().toString());
        for (Insumo p : this.formula.getInsumos()) {
            if (p.equals(producto)) {
                this.insumo = p;
                nuevo = false;
                break;
            }
        }
        if (nuevo) {
            boolean ok = false;
            FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "validaProductoSeleccionado");
            try {
                this.dao = new DAOFormulas();
                producto.setCostoPromedio(this.dao.agregarInsumo(this.formula.getIdFormula(), this.formula.getIdEmpresa(), this.convertTOInsumo(producto)));
                this.insumo = producto;
                this.insumo.setNuevo(false);
                this.formula.setSumaCosto(this.formula.getSumaCosto() + this.insumo.getCostoPromedio());
                this.formula.getInsumos().add(this.insumo);
                ok = true;
            } catch (SQLException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            } catch (NamingException ex) {
                fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
                fMsg.setDetail(ex.getMessage());
            }
            if (!ok) {
                FacesContext.getCurrentInstance().addMessage(null, fMsg);
            }
        }
    }

    public void actualizaProductoSeleccionado() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "actualizaProductoSeleccionado");
        try {
            this.dao = new DAOFormulas();
            if (this.caso == 1) {
                this.convertirFormula(this.mbBuscar.getProducto(), this.dao.obtenerFormula(this.mbEmpresas.getEmpresa().getIdEmpresa(), this.mbBuscar.getProducto().getIdProducto()));
            } else if (this.caso == 2) {
                this.validaProductoSeleccionado();
            } else if (this.caso == 3) {
                this.insumo1 = this.mbBuscar.getProducto();
            } else {
                this.insumo2 = this.mbBuscar.getProducto();
            }
            ok = true;
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public void buscar() {
        this.mbBuscar.buscarLista();
        if (this.mbBuscar.getProducto() != null) {
            this.actualizaProductoSeleccionado();
        }
    }

    public void buscarEmpaqueInsumo() {
        this.caso = 2;
        this.mbBuscar.inicializar();
        this.mbBuscar.setUpdate(":main:mttoFormulas :main:formulaInsumos :main:mttoFormulasTotales :main:messages");
    }

    public void buscarEmpaqueFormula() {
        this.caso = 1;
        this.mbBuscar.inicializar();
        this.mbBuscar.setUpdate(":main:mttoFormulasDatos :main:mttoFormulas :main:formulaInsumos :main:mttoFormulasTotales :main:btnGrabarFormula :main:messages");
    }

    private TOInsumo convertTOInsumo(Insumo tmp) {
        TOInsumo to = new TOInsumo();
        to.setIdEmpaque(tmp.getIdEmpaque());
        to.setCantidad(tmp.getCantidad());
        to.setPorcentVariacion(tmp.getVariacion());
        to.setCostoUnitarioPromedio(tmp.getCostoPromedio());
        return to;
    }

    public void eliminarInsumo() {
        boolean ok = false;
        RequestContext context = RequestContext.getCurrentInstance();
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "eliminarInsumo");
        try {
            this.dao = new DAOFormulas();
            this.dao.eliminarInsumo(this.formula.getIdFormula(), this.insumo.getIdEmpaque());
            this.formula.setSumaCantidad(this.formula.getSumaCantidad() - this.insumo.getCantidad());
            this.formula.setSumaCosto(this.formula.getSumaCosto() - this.insumo.getCostoPromedio());
            this.formula.getInsumos().remove(this.insumo);
            ok = true;
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
        context.addCallbackParam("okInsumo", ok);
    }

    public void cancelarInsumo() {
        this.insumo.setCantidad(this.respInsumo.getCantidad());
        this.insumo.setVariacion(this.respInsumo.getVariacion());
    }

    public void calculaSumas() {
        this.formula.setSumaCantidad(0);
        this.formula.setSumaCosto(0);
        for (Insumo ins : this.formula.getInsumos()) {
            this.formula.setSumaCantidad(this.formula.getSumaCantidad() + ins.getCantidad());
            this.formula.setSumaCosto(this.formula.getSumaCosto() + ins.getCostoPromedio() * ins.getCantidad());
        }
        for (Insumo ins : this.formula.getInsumos()) {
            ins.setPtjeCantParticipacion(ins.getCantidad() / this.formula.getSumaCantidad());
            ins.setPtjeCtoParticipacion(ins.getCostoPromedio() * ins.getCantidad() / this.formula.getSumaCosto());
        }
//        this.formula.setCostoPromedio(this.formula.getSumaCosto()-this.formula.getMerma());
        if (this.formula.getIdTipo() == 4) { // Semiterminado
            this.formula.setMermaCant(this.formula.getSumaCantidad() * this.formula.getMerma() / 100);
            this.formula.setCostoPromedio(this.formula.getSumaCosto() / (this.formula.getSumaCantidad() - this.formula.getMermaCant()));
        } else if (this.formula.getIdTipo() == 5) {  // Terminado
            this.formula.setMermaCant(this.formula.getPiezas() * this.formula.getMerma() / 100);
            this.formula.setCostoPromedio(this.formula.getSumaCosto() / (this.formula.getPiezas() - this.formula.getMermaCant()));
        } else if (this.formula.getIdTipo() == 3) {  // Material de empaque
            int piezas = 1;   // La cantidad = 1 (uno) para todas las formulas de este tipo
            this.formula.setMermaCant(piezas * this.formula.getMerma() / 100);
            this.formula.setCostoPromedio(this.formula.getSumaCosto() / (piezas - this.formula.getMermaCant()));
        }
        double ctoManoObraConMerma=this.formula.getManoDeObra()*this.formula.getSumaCantidad()/(this.formula.getSumaCantidad()*(1-this.formula.getManoDeObra()/100));
        this.formula.setCostoPrimo(this.formula.getCostoPromedio()+ctoManoObraConMerma);
    }

    public void grabarInsumo() {
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "grabarInsumo");
        try {
            this.dao = new DAOFormulas();
            if (this.insumo.isNuevo()) {
                this.insumo.setCostoPromedio(this.dao.agregarInsumo(this.formula.getIdFormula(), this.formula.getIdEmpresa(), convertTOInsumo(this.insumo)));
                this.insumo.setNuevo(false);
//                this.respInsumo.setCostoPromedio(0);
            } else {
                this.dao.modificarInsumo(this.formula.getIdFormula(), convertTOInsumo(this.insumo));
//                this.formula.setSumaCantidad(this.formula.getSumaCantidad()-this.respInsumo.getCantidad()+this.insumo.getCantidad());
            }
//            this.formula.setSumaCosto(this.formula.getSumaCosto()-this.respInsumo.getCostoPromedio()+this.insumo.getCostoPromedio());
            this.respaldaInsumo();
            this.calculaSumas();
            this.dao.modificarFormula(this.convertTOFormula(this.formula));
            fMsg.setDetail("La modificacion se realizo correctamente !!!");
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        }
        FacesContext.getCurrentInstance().addMessage(null, fMsg);
    }

    private TOFormula convertTOFormula(Formula formula) {
        TOFormula to = new TOFormula();
        to.setIdFormula(formula.getIdFormula());
        to.setIdEmpresa(formula.getIdEmpresa());
        to.setIdEmpaque(formula.getIdEmpaque());
        to.setIdTipo(formula.getIdTipo());
        to.setMerma(formula.getMerma());
        to.setPiezas(formula.getPiezas());
        to.setManoDeObra(formula.getManoDeObra());
        to.setCostoPromedio(formula.getCostoPromedio());
        to.setObservaciones(formula.getObservaciones());
        return to;
    }

    public void grabarFormula() {
        boolean ok = false;
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Aviso:", "grabarFormula");
        try {
            this.dao = new DAOFormulas();
            if (this.formula.getIdFormula() == 0) {
                this.formula.setIdFormula(this.dao.agregarFormula(this.convertTOFormula(this.formula)));
            } else {
                this.dao.modificarFormula(this.convertTOFormula(this.formula));
            }
            ok = true;
        } catch (SQLException ex) {
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
        } catch (NamingException ex) {
            fMsg.setDetail(ex.getMessage());
        }
        if (!ok) {
            FacesContext.getCurrentInstance().addMessage(null, fMsg);
        }
    }

    public Insumo getInsumo() {
        return insumo;
    }

    public void setInsumo(Insumo insumo) {
        this.insumo = insumo;
    }

    public Formula getFormula() {
        return formula;
    }

    public void setFormula(Formula formula) {
        this.formula = formula;
    }

    public ArrayList<Accion> getAcciones() {
        if (this.acciones == null) {
            this.acciones = this.mbAcciones.obtenerAcciones(29);
        }
        return acciones;
    }

    public void setAcciones(ArrayList<Accion> acciones) {
        this.acciones = acciones;
    }

    public MbAcciones getMbAcciones() {
        return mbAcciones;
    }

    public void setMbAcciones(MbAcciones mbAcciones) {
        this.mbAcciones = mbAcciones;
    }

    public MbProductosBuscar getMbBuscar() {
        return mbBuscar;
    }

    public void setMbBuscar(MbProductosBuscar mbBuscar) {
        this.mbBuscar = mbBuscar;
    }

    public MbMiniEmpresas getMbEmpresas() {
        return mbEmpresas;
    }

    public void setMbEmpresas(MbMiniEmpresas mbEmpresas) {
        this.mbEmpresas = mbEmpresas;
    }

    public int getCaso() {
        return caso;
    }

    public void setCaso(int caso) {
        this.caso = caso;
    }

    public Generador getGenerador() {
        return generador;
    }

    public void setGenerador(Generador generador) {
        this.generador = generador;
    }

    public ArrayList<Formula> getFormulas() {
        return formulas;
    }

    public void setFormulas(ArrayList<Formula> formulas) {
        this.formulas = formulas;
    }

    public Producto getInsumo1() {
        return insumo1;
    }

    public void setInsumo1(Producto insumo1) {
        this.insumo1 = insumo1;
    }

    public Producto getInsumo2() {
        return insumo2;
    }

    public void setInsumo2(Producto insumo2) {
        this.insumo2 = insumo2;
    }
}
