/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.servicio;

import edi.dao.hibernate.DetalleOrden;
import edi.dao.hibernate.MaestroOrden;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author alvarjor
 */
public interface IOrdenCompraServicio {

    


    public MaestroOrden obtenerOrdenCompra(String codMaestroOrden) throws Exception;

    public ArrayList consultarEstadoOrden(String codTerritorio, Date fechaReporte) throws Exception;
    public ArrayList consultarResumenEstadoOrden(String codTerritorio, Date fechaReporte) throws Exception;
    public List consultarOrdenes(String codTerritorio, String operadorFecha, Date fechaReporte, String numOrden, String codCliente, String codError, String tipoError) throws Exception;
    public int obtenerEstadoAvance(String codTerritorio, Date fechaReporte) throws Exception;

    public String cancelarOrdenes(MaestroOrden[] listadoOrdenes);
    //public String actualizarBanderaProcesamiento(boolean bloquear);
    public String reprocesarOrdenes(MaestroOrden[] listadoOrdenes) throws Exception;
    public String reprocesarOrdLog(int codLog) throws Exception;
    public String forzarPosteo(MaestroOrden[] listadoOrdenes);

    public String resetearEstadoItem(DetalleOrden[] listadoItems);
    public String cancelarItem(DetalleOrden[] listadoItems);

    public List consultarCaseFill(String codTerritorio, String codBillTo, Date fechaInicial, Date fechaFinal, String listadoEstado);
    public List consultarGeneralOrdRecibidas(String codTerritorio, String codBillTo, Date fechaInicial, Date fechaFinal, String listadoEstado);
    public List consultarDetalleOrdRecibidas(String codTerritorio, String codBillTo, Date fechaInicial, Date fechaFinal, String listadoEstado);
    public int obtenerCantOrdenesRetenidas(String codTerritorio) throws Exception;

    public void actualizarEstadoEnvioMail(String codTerritorio, String estadoActual, String estadoNuevo) throws Exception;

    public List listarBillTo(String codTerritorio) throws Exception;
}
