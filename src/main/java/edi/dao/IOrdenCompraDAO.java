/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.dao;

import edi.dao.hibernate.DetalleOrden;
import edi.dao.hibernate.MaestroOrden;
import java.util.Date;
import java.util.List;

/**
 *
 * @author alvarjor
 */
public interface IOrdenCompraDAO {


    public List listarClientesPorFecha(String codTerritorio, Date fecha) throws Exception;
    public List listarOrdenesLog(int codLog);

    public MaestroOrden obtenerOrdenCompra(String codMaestroOrden) throws Exception;

    
    public double[] obtenerTotalOrdenesPorClienteEstado(Date fecha, String codBillTo, String codEstado, String tipo) throws Exception;
    public int obtenerEstadoAvance(String codTerritorio, Date fecha) throws Exception;
    public List consultarOrdenes(String codTerritorio, String operadorFecha, Date fechaReporte, String numOrden, String codCliente, String codError, String tipoError) throws Exception;

    public String cancelarOrdenes(MaestroOrden[] listadoOrdenes);
    public String reprocesarOrden(String codMaestroOrden) throws Exception;
    public boolean puedeforzarPosteo(String codMaestroOrden);
    public String forzarPosteo(MaestroOrden[] listadoOrdenes);

    public String resetearEstadoItem(DetalleOrden[] listadoItems);
    public String cancelarItem(DetalleOrden[] listadoItems);

    public List consultarCaseFill(String codTerritorio, String codBillTo, Date fechaInicial, Date fechaFinal, String listadoEstado);
    public List consultarGeneralOrdRecibidas(String codTerritorio, String codBillTo, Date fechaInicial, Date fechaFinal, String listadoEstado);
    public List consultarDetalleOrdRecibidas(String codTerritorio, String codBillTo, Date fechaInicial, Date fechaFinal, String listadoEstado);
    public int obtenerCantOrdenesRetenidas(String codTerritorio)  throws Exception;

    public void actualizarEstadoEnvioMail(String codTerritorio, String estadoActual, String estadoNuevo) throws Exception;

    public List listarBillTo(String codTerritorio) throws Exception;

}
