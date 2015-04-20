package clientes2;

import bancos.dao.DAOBancos;
import bancos.dominio.Banco;
import clientes2.dao.DAOClientes;
import clientes2.dominio.Cliente;
import clientes.dominio.ClienteSEA;
import clientes2.to.TOCliente;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;

@ManagedBean(name = "mbClientesSEA")
@SessionScoped
public class MbClientes implements Serializable {

    private DAOClientes dao;
    private Cliente cliente;
    private ArrayList<Cliente> listaClientes;
    private List<SelectItem> LClientesSEA;
    private List<SelectItem> ListaBancos;
//    private ClienteSEA clienteSEA;
//    private Banco banco;
//    private String text6;

    public void setLClientesSEA(List<SelectItem> LClientesSEA) {

        this.LClientesSEA = LClientesSEA;
    }

//    public ClienteSEA getClienteSEA() {
//        return clienteSEA;
//    }
//
//    public void setClienteSEA(ClienteSEA clienteSEA) {
//        this.clienteSEA = clienteSEA;
//    }
//
//    public String getText6() {
//        return text6;
//    }
//
//    public void setText6(String text6) {
//        this.text6 = text6;
//    }
    public MbClientes() {
        this.dao = new DAOClientes();

    }

//    public Banco getBanco() {
//        return banco;
//    }
//
//    public void setBanco(Banco banco) {
//        this.banco = banco;
//    }
    
    public String terminar() {
        return "menuClientes.terminar";
    }
    
    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    //----- P R O C E D I M I E N T O S
    public List<SelectItem> getListaBancos() {
        if (this.ListaBancos == null) {
            try {
                MbClientes cd = new MbClientes();
                this.ListaBancos = cd.obtenerBancos();
            } catch (NamingException ex) {
                Logger.getLogger(MbClientes.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(MbClientes.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ListaBancos;
    }

    public String eliminar(int id) throws SQLException {
        dao.eliminarUsuario(id);
        String Eliminado = "Dato.eliminado";
        return Eliminado;
    }

    private List<SelectItem> obtenerBancos() throws SQLException, NamingException {
        List<SelectItem> bancos = new ArrayList<SelectItem>();
        Banco B = new Banco();
        B.setIdBanco(0);
        B.setNombreCorto("Seleccione banco: ");
        SelectItem cero = new SelectItem(B, B.getNombreCorto());
        bancos.add(cero);
        DAOBancos daoBancos = new DAOBancos();
        Banco[] aBancos = daoBancos.obtenerBancos();
        for (Banco po : aBancos) {
            bancos.add(new SelectItem(po, po.getNombreCorto()));
        }
        return bancos;
    }

    //CAMBIO DE CODIGO
    //////////////////// M E T O D O S  ////////////
    public void setListaClientes(ArrayList<Cliente> listaClientes) {
        this.listaClientes = listaClientes;
    }

    public ArrayList<Cliente> getListaClientes() throws NamingException {
        try {
            if (listaClientes == null) {
                cargaClientes();
            }
        } catch (SQLException ex) {
            Logger.getLogger(MbClientes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listaClientes;

    }

    private void cargaClientes() throws SQLException, NamingException {
        listaClientes = new ArrayList<Cliente>();
        ArrayList<TOCliente> toLista = dao.obtenerCliente();

        for (TOCliente e : toLista) {
            listaClientes.add(convertir(e));
        }
    }

    private Cliente convertir(TOCliente to) throws SQLException, NamingException {
        Cliente cli = new Cliente();
        cli.setIdCliente(to.getIdCliente());
        cli.setClienteSEA(dao.obtenerClienteSEA(to.getCodigoCliente()));
        DAOBancos daoBancos = new DAOBancos();
        cli.setBanco(daoBancos.obtener(to.getIdBanco()));
        cli.setNombre(to.getNombre().trim()); //ADICIONAL
        cli.setNumCtaPago(to.getNumCtaPago());
        cli.setMedioPago(to.getMedioPago());
//        cli.setCodigoCliente(to.getCodigoCliente());
//        cli.setIdBanco(to.getIdBanco());//Le falto asignarle esto
//        cli.setNombreCorto(to.getNombreCorto());
        return cli;
    }

    public String mantenimiento(int idCliente) throws SQLException, NamingException {
        String destino = "clientes.mantenimiento";
        //    this.clienteSEA=null;

        try {
            if (idCliente == 0) {
                this.cliente = nuevoCliente();
            } else {
                TOCliente toCliente = this.dao.obtenerUnCiente(idCliente);
                if (toCliente == null) {
                    destino = null;
                } else {
                    this.cliente = convertir(toCliente);
                }
            }
        } catch (SQLException ex) {
            destino = null;
            Logger.getLogger(MbClientes.class.getName()).log(Level.SEVERE, null, ex);
        }
        return destino;
    }

    private Cliente nuevoCliente() throws SQLException {
        ClienteSEA cSEA = new ClienteSEA();
        cSEA.setCod_cli(0);
        cSEA.setNombre("Seleccione un cliente: ");

        Banco bco = new Banco();
        bco.setIdBanco(0);
        bco.setNombreCorto("Seleccione un banco: ");

        Cliente cc = new Cliente();
        cc.setIdCliente(0);
        cc.setClienteSEA(cSEA);
        cc.setBanco(bco);
//        cc.setCodigoCliente(0);
//        cc.setIdbanco(0);
        cc.setNumCtaPago("");
        cc.setMedioPago("");
        cc.setNombre("");
//        cc.setNombreCorto("");
        return cc;
    }

    public String grabar() {
        String destino = "grabar.cliente";
        FacesMessage fMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso:", "");

        int codigo = this.cliente.getClienteSEA().getCod_cli();
        //int idBanco = this.banco.getIdBanco();
        int idBanco = this.cliente.getBanco().getIdBanco();
        String numCtaPago = this.cliente.getNumCtaPago();
        String medioPago = this.cliente.getMedioPago();
        System.out.println("El dato obtenido es " + codigo);

        try {
            if (this.cliente.getIdCliente() == 0) {
                System.out.println("El dato obtenido es " + codigo);
                this.cliente.setIdCliente(this.dao.agregar(codigo, idBanco, numCtaPago, medioPago));
            } else {
                this.dao.modificar(this.cliente.getIdCliente(), codigo, idBanco, numCtaPago, medioPago);
            }
            this.listaClientes = null;
            fMsg.setSeverity(FacesMessage.SEVERITY_INFO);
            fMsg.setDetail("El CLIENTE se grabó correctamente !!");
        } catch (SQLException ex) {
            fMsg.setSeverity(FacesMessage.SEVERITY_ERROR);
            fMsg.setDetail(ex.getErrorCode() + " " + ex.getMessage());
            Logger.getLogger(MbClientes.class.getName()).log(Level.SEVERE, null, ex);
        }

        FacesContext.getCurrentInstance().addMessage(null, fMsg);
        return destino;
    }

    public List<SelectItem> getLClientesSEA() throws SQLException {
        MbClientes d = new MbClientes();
        this.LClientesSEA = d.obtenerClientesSEA();
        return LClientesSEA;
    }

    private List<SelectItem> obtenerClientesSEA() throws SQLException {
        List<SelectItem> cs = new ArrayList<SelectItem>();
        ClienteSEA db = new ClienteSEA();
        db.setCod_cli(0);
        db.setNombre("Seleccione el Cliente: ");

        SelectItem p0 = new SelectItem(db, db.getNombre());
        cs.add(p0);

        DAOClientes daoClientes = new DAOClientes();
        ClienteSEA[] rDbs = daoClientes.obtenerClienteSEA();

        for (ClienteSEA po : rDbs) {
            cs.add(new SelectItem(po, po.toString()));
        }

        return cs;
    }
    //MODULOS DE REPORTES
//    JasperPrint jasperPrint;
//    DAOClientes daoRep;
//
//    public void init() throws JRException, SQLException {
//        Connection cn = daoRep.dameConexion();
//        
//      //  JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(listaClientes);
//         jasperPrint = JasperFillManager.fillReport("C:\\Users\\david\\Desktop\\reporteClientes.jasper", null, cn);
//    }
//
//    public void PDF() throws JRException, IOException, SQLException {
//        init();
//
//        HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getContext();
//        ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
//        JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
//
//
//
//    }
    
   
}
