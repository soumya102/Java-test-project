/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.dao;

import edi.dao.hibernate.PlanAccionVentaPerdida;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mars.web.common.Utilidades;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
/**
 *
 * @author alvarjor
 */
public class VentaDAO implements IVentaDAO {
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    
    public List<Object[]> listarCausalVtaPerdida() throws Exception {
        Query query;
        String sentencia;

        try {
            //Se construye el SQL
            sentencia = "SELECT " +
                        "a.Descripcion AS Descripcion_Area, " +
                        "c.Cod_Causal, " +
                        "c.Descripcion AS Descripcion_Causal " +
                        "FROM SLS_CAUSAL_VENTA_PERDIDA c, EDI_TIPO a " +
                        "WHERE c.Cod_Causal <> 0 " +
                        "AND c.Cod_Tipo_Area = a.Codigo " +
                        "ORDER BY a.Descripcion, c.Descripcion";
            
            query = this.sessionFactory.getCurrentSession().createSQLQuery(sentencia);
            return query.list();
            
        } catch (Exception ex) {
            throw new Exception("Error listando las causales de venta perdida->" + Utilidades.obtenerMsgError(ex));
        }
    }
    

    public List<Object[]> listarProductoVtaPerdida(String codTerritorio, int anioPeriodo, int semana) throws Exception {
        Query query;
        String sentencia;

        try {
            //Se construye el SQL
            sentencia = "SELECT " + 
                        "vp.Cod_Venta_Perdida, " +
                        "seg.Description AS Descripcion_Segmento, " + 
                        "cla.Descripcion AS Descripcion_Clasificador, " + 
                        "m.Country_Origin_Code AS Cod_Pais_Origen, " + 
                        "vp.Cod_Producto, " + 
                        "m.Material_Description AS Descripcion_Producto, " + 
                        "vp.Cod_Causal, " +
                        "vp.GSV_Venta_Perdida " +
                        "FROM SLS_PRODUCTO_VENTA_PERDIDA vp, ANDINOGLOBAL.SAP_MATERIAL m, ANDINOGLOBAL.SAP_GLOBAL_CLASSIFICATOR seg, FCST_ANDN.FCT_CLASIFICADOR cla " + 
                        "WHERE vp.Anio_Periodo = :anioPeriodo " + 
                        "AND vp.Semana = :semana " + 
                        "AND vp.Cod_Territorio = :codTerritorio " + 
                        "AND vp.Cod_Producto = m.Material_Number " + 
                        "AND seg.Classificator_Type_Code = '01'  " + 
                        "AND m.Business_Segment_Code = seg.Classificator_Code " + 
                        "AND m.Andean_FCT_Classificator_Code = cla.Cod_Clasificador(+) " + 
                        "ORDER BY vp.GSV_Venta_Perdida DESC, seg.Description, cla.Descripcion, m.Country_Origin_Code, m.Material_Description";
            
            query = this.sessionFactory.getCurrentSession().createSQLQuery(sentencia);
            query.setParameter("codTerritorio", codTerritorio);
            query.setParameter("anioPeriodo", anioPeriodo);
            query.setParameter("semana", semana);
            return query.list();
            
        } catch (Exception ex) {
            throw new Exception("Error listando los productos de venta perdida->" + Utilidades.obtenerMsgError(ex));
        }
    }

    
    
    public void guardarCausalVentaPerdida(List<Object[]> arrayCausalVentaPerdida) throws Exception {
        Query query;
        String sentencia, textoLlave;
        Object[] vectorDatos;
        ArrayList arrayLlave;

        try {
            arrayLlave = new ArrayList();
            for (Iterator<Object[]> iterCausal = arrayCausalVentaPerdida.iterator(); iterCausal.hasNext();) {
                vectorDatos = iterCausal.next();
                
                //Se construye el update
                sentencia = "UPDATE ProductoVentaPerdida pv SET pv.codCausal = :codCausal " +
                            "WHERE pv.codVentaPerdida = :codVentaPerdida";

                query = this.sessionFactory.getCurrentSession().createQuery(sentencia);
                query.setParameter("codVentaPerdida", Integer.parseInt(vectorDatos[0].toString()));
                query.setParameter("codCausal", Integer.parseInt(vectorDatos[1].toString()));
                query.executeUpdate();
                
            }            
        } catch (Exception ex) {
            throw new Exception("Error guardando las causales de venta perdida de los productos->" + Utilidades.obtenerMsgError(ex));
        }
    }

    
    
    public void consolidarCausalVentaPerdida(String codTerritorio, int anioPeriodo, int semana) throws Exception {
        Query query;
        
        try {            
            //Se obtiene el texto de la interfaz generado
            query=sessionFactory.getCurrentSession().getNamedQuery("SP_ConsolidarCausalVtaPerdida");
            query.setParameter("codTerritorio", codTerritorio);
            query.setParameter("anioPeriodo", anioPeriodo);
            query.setParameter("semana", semana);
            query.list();
            
        } catch (Exception ex) {
            throw new Exception("Error consolidando las causales de venta perdida->" + Utilidades.obtenerMsgError(ex));
        }
    }
    
    
    
    public List<Object[]> listarCabeceraPlanAccion(String codTerritorio) throws Exception {
        Query query;
        String sentencia;

        try {
            //Se construye el SQL
            sentencia = "SELECT DISTINCT " + 
                        "cv.Cod_Seguimiento, " + 
                        "s.Description AS Segmento, " + 
                        "cv.Descripcion_Clasificador AS Clasificador, " + 
                        "c.Descripcion AS Causal " +                         
                        "FROM SLS_CONSOL_VENTA_PERDIDA cv, ANDINOGLOBAL.MS_SEGMENT s, SLS_CAUSAL_VENTA_PERDIDA c " + 
                        "WHERE cv.Cod_Territorio = :codTerritorio " + 
                        "AND cv.Cod_Segmento = s.Segment_Code " + 
                        "AND cv.Cod_Causal = c.Cod_Causal " + 
                        "AND (cv.Cod_Seguimiento IN (SELECT cvd.Cod_Seguimiento " + 
                                                   "FROM SLS_CONSOL_VENTA_PERDIDA_DTLL cvd, ANDINOGLOBAL.SAP_PERIOD per " + 
                                                   "WHERE TRUNC(SYSDATE-7) BETWEEN Start_Date AND End_Date " + 
                                                   "AND per.Year_Period = cvd.Anio_Periodo) " +
                            "OR EXISTS (SELECT 1  " + 
                                       "FROM SLS_PLAN_ACCION_VENTA_PERDIDA pa " + 
                                       "WHERE pa.Cod_Seguimiento = cv.Cod_Seguimiento " + 
                                       "AND pa.Cod_Estado = 'TAR001') " + 
                        ")";
            
            query = this.sessionFactory.getCurrentSession().createSQLQuery(sentencia);
            query.setParameter("codTerritorio", codTerritorio);
            return query.list();
        } catch (Exception ex) {
            throw new Exception("Error listando las cabeceras del plan de accion->" + Utilidades.obtenerMsgError(ex));
        }
    }
    
    
    public List<Object[]> listarValorVentaPerdidaSeguimiento(int codSeguimiento) throws Exception {
        Query query;
        String sentencia;

        try {
            //Se construye el SQL
            sentencia = "SELECT " + 
                        "cvd.Anio_Periodo, " + 
                        "cvd.Semana, " + 
                        "cvd.GSV_Venta_Perdida " + 
                        "FROM SLS_CONSOL_VENTA_PERDIDA_DTLL cvd " + 
                        "WHERE cvd.Cod_Seguimiento = :codSeguimiento " + 
                        "AND (cvd.Anio_Periodo = (SELECT per.Year_Period " + 
                                                 "FROM ANDINOGLOBAL.SAP_PERIOD per " + 
                                                 "WHERE TRUNC(SYSDATE-7) BETWEEN per.Start_Date AND per.End_Date) " +
                             "OR EXISTS (SELECT 1  " + 
                                        "FROM SLS_PLAN_ACCION_VENTA_PERDIDA pa " + 
                                        "WHERE pa.Cod_Seguimiento = cvd.Cod_Seguimiento " + 
                                        "AND pa.Periodo_Creado = cvd.Anio_Periodo " +
                                        "AND pa.Semana_Creado = cvd.Semana " +
                                        "AND pa.Cod_Estado = 'TAR001') " +
                        ") " +
                        "ORDER BY cvd.Anio_Periodo, cvd.Semana";
            
            query = this.sessionFactory.getCurrentSession().createSQLQuery(sentencia);
            query.setParameter("codSeguimiento", codSeguimiento);
            return query.list();
        } catch (Exception ex) {
            throw new Exception("Error listando la venta perdida de un seguimiento->" + Utilidades.obtenerMsgError(ex));
        }
    }
    
    
    public List<Object[]> listarDetallePlanAccion(int codSeguimiento) throws Exception {
        Query query;
        String sentencia;

        try {
            //Se construye el SQL
            sentencia = "SELECT " +
                        "pa.Cod_Seguimiento, " +
                        "pa.Sec_Plan, " +
                        "pa.Periodo_Creado, " + 
                        "pa.Semana_Creado, " +
                        "pa.Periodo_Creado || 'W' || pa.Semana_Creado AS Periodo_Semana_Creado, " +
                        "pa.Descripcion_Plan, " +
                        "pa.Responsable, " +
                        "pa.Periodo_Finalizacion_Plan, " + 
                        "pa.Semana_Finalizacion_Plan, " +
                        "pa.Periodo_Finalizacion_Plan || 'W' || pa.Semana_Finalizacion_Plan AS Periodo_Semana_Finalizacion, " +
                        "pa.Cod_Estado, " +
                        "e.Descripcion AS Descripcion_Estado " +
                        "FROM SLS_PLAN_ACCION_VENTA_PERDIDA pa, EDI_ESTADO e " +
                        "WHERE pa.Cod_Seguimiento = :codSeguimiento " +
                        "AND (pa.Periodo_Creado = (SELECT Year_Period  " + 
                                                  "FROM ANDINOGLOBAL.SAP_PERIOD  " + 
                                                  "WHERE TRUNC(SYSDATE-7) BETWEEN Start_Date AND End_Date) " + 
                             "OR pa.Cod_Estado = 'TAR001' " + 
                        ") " +
                        "AND pa.Cod_Estado = e.Codigo " +
                        "ORDER BY 2";
            
            query = this.sessionFactory.getCurrentSession().createSQLQuery(sentencia);
            query.setParameter("codSeguimiento", codSeguimiento);
            return query.list();
            
        } catch (Exception ex) {
            throw new Exception("Error listando el detalle del plan de accion->" + Utilidades.obtenerMsgError(ex));
        }
    }
    
    
    
    public List<Object[]> listarVentaPerdidaProductoPorSeguimiento(int codSeguimiento) throws Exception {
        Query query;
        String sentencia;

        try {
            //Se construye el SQL
            sentencia = "SELECT " + 
                        "vp.Anio_Periodo, " + 
                        "vp.Semana, " + 
                        "vp.Cod_Producto, " +
                        "p.Description AS Desc_Producto, " +
                        "vp.GSV_Venta_Perdida " + 
                        "FROM SLS_PRODUCTO_VENTA_PERDIDA vp, PRODUCTO p " + 
                        "WHERE vp.Cod_Seguimiento = :codSeguimiento " + 
                        "AND (vp.Anio_Periodo = (SELECT per.Year_Period " + 
                                               "FROM ANDINOGLOBAL.SAP_PERIOD per " + 
                                               "WHERE TRUNC(SYSDATE-7) BETWEEN per.Start_Date AND per.End_Date) " +
                             "OR EXISTS (SELECT 1  " + 
                                        "FROM SLS_PLAN_ACCION_VENTA_PERDIDA pa " + 
                                        "WHERE pa.Cod_Seguimiento = vp.Cod_Seguimiento " + 
                                        "AND pa.Periodo_Creado = vp.Anio_Periodo " +
                                        "AND pa.Semana_Creado = vp.Semana " +
                                        "AND pa.Cod_Estado = 'TAR001') " +
                        ") " +
                        "AND vp.Cod_Producto = p.Material_Number " +
                        "ORDER BY vp.Anio_Periodo, vp.Semana, vp.GSV_Venta_Perdida DESC, p.Description";
            
            query = this.sessionFactory.getCurrentSession().createSQLQuery(sentencia);
            query.setParameter("codSeguimiento", codSeguimiento);
            return query.list();
        } catch (Exception ex) {
            throw new Exception("Error listando los productos asociado a un seguimiento->" + Utilidades.obtenerMsgError(ex));
        }
    }
    
    
    public void guardarPlanAccion(PlanAccionVentaPerdida oPlanAccion) throws Exception {
        Query query;
        String sentencia;
        int secPlan, anioPeriodoActual, semanaActual;
        Object[] vectorDatos;
        
        try {
            anioPeriodoActual = 0;
            semanaActual = 0;
            if (oPlanAccion.getCodEstado().equals("TAR002")) { //Si esta marcada la tarea como realizada
                //Se busca el anio/periodo actual
                sentencia = "SELECT per.Year_Period, per.Week " + 
                            "FROM ANDINOGLOBAL.SAP_PERIOD per " + 
                            "WHERE TRUNC(SYSDATE) BETWEEN per.Start_Date AND per.End_Date";
                vectorDatos = (Object[])this.sessionFactory.getCurrentSession().createSQLQuery(sentencia).uniqueResult();
                anioPeriodoActual = Integer.parseInt(vectorDatos[0].toString());
                semanaActual = Integer.parseInt(vectorDatos[1].toString());
            }
            
            if (oPlanAccion.getId().getSecPlan() == 0) {
                //Se busca el proximo secuencial
                sentencia = "SELECT NVL(MAX(pa.id.secPlan), 0) +  1 " +
                            "FROM PlanAccionVentaPerdida pa " +
                            "WHERE pa.id.codSeguimiento = :codSeguimiento";
                query = this.sessionFactory.getCurrentSession().createQuery(sentencia);
                query.setParameter("codSeguimiento", oPlanAccion.getId().getCodSeguimiento());
                secPlan = Integer.parseInt(query.uniqueResult().toString());
                
                //Se guarda el registro
                oPlanAccion.getId().setSecPlan(secPlan);
                oPlanAccion.setPeriodoFinalizacionReal(anioPeriodoActual);
                oPlanAccion.setSemanaFinalizacionReal(semanaActual);
                this.sessionFactory.getCurrentSession().save(oPlanAccion);
            } else {
                //Se actualiza el registro
                sentencia = "UPDATE SLS_Plan_Accion_Venta_Perdida pa SET " +
                            "pa.Descripcion_Plan = :descripcionPlan, " + 
                            "pa.Responsable = :responsable, " +
                            "pa.Periodo_Finalizacion_Plan = :periodoFinalizacionPlan, " +
                            "pa.Semana_Finalizacion_Plan = :semanaFinalizacionPlan, " +
                            "pa.Periodo_Finalizacion_Real = CASE WHEN :codEstado <> 'TAR002' THEN 0 " + 
                                                                "WHEN pa.Periodo_Finalizacion_Real = 0 THEN :periodoFinalizacionReal " + 
                                                                "ELSE pa.Periodo_Finalizacion_Real END, " +
                            "pa.Semana_Finalizacion_Real = CASE WHEN :codEstado <> 'TAR002' THEN 0 " + 
                                                               "WHEN pa.Semana_Finalizacion_Real = 0 THEN :semanaFinalizacionReal " + 
                                                               "ELSE pa.Semana_Finalizacion_Real END, " +
                            "pa.Cod_Estado = :codEstado " +
                            "WHERE pa.Cod_Seguimiento = :codSeguimiento " +
                            "AND pa.Sec_Plan = :secPlan";
                
                query = this.sessionFactory.getCurrentSession().createSQLQuery(sentencia);
                query.setParameter("codSeguimiento", oPlanAccion.getId().getCodSeguimiento());
                query.setParameter("secPlan", oPlanAccion.getId().getSecPlan());
                query.setParameter("descripcionPlan", oPlanAccion.getDescripcionPlan());
                query.setParameter("responsable", oPlanAccion.getResponsable());
                query.setParameter("periodoFinalizacionPlan", oPlanAccion.getPeriodoFinalizacionPlan());
                query.setParameter("semanaFinalizacionPlan", oPlanAccion.getSemanaFinalizacionPlan());
                query.setParameter("periodoFinalizacionReal", anioPeriodoActual);
                query.setParameter("semanaFinalizacionReal", semanaActual);
                query.setParameter("codEstado", oPlanAccion.getCodEstado());
                query.executeUpdate();
            }
        } catch (Exception ex) {
            throw new Exception("Error guardando el plan de accion->" + Utilidades.obtenerMsgError(ex));
        }
    }
    
    
    
    public String obtenerObservacionVtaPerdida(String codTerritorio, int anioPeriodo, int semana) throws Exception {
        Query query;
        String sentencia;
        List resultado;

        try {
            //Se construye el SQL
            sentencia = "SELECT Texto " +
                        "FROM SLS_OBSERVACION_VENTA_PERDIDA " +
                        "WHERE Cod_Territorio = :codTerritorio " +
                        "AND Anio_Periodo = :anioPeriodo " +
                        "AND Semana = :semana";
            
            query = this.sessionFactory.getCurrentSession().createSQLQuery(sentencia);
            query.setParameter("codTerritorio", codTerritorio);
            query.setParameter("anioPeriodo", anioPeriodo);
            query.setParameter("semana", semana);
            resultado = query.list();
            
            if (resultado.isEmpty()) {
                return "";
            } else {
                return resultado.toArray()[0].toString();
            }
        } catch (Exception ex) {
            throw new Exception("Error obteniendo la observacion de venta perdida->" + Utilidades.obtenerMsgError(ex));
        }
    }
    
    
    
    public void guardarObservacionVtaPerdida(String codTerritorio, int anioPeriodo, int semana, String observaciones) throws Exception {
        Query query;
        String sentencia;

        try {
            //Se verifica si ya existe
            sentencia = "SELECT COUNT(*) " +
                        "FROM SLS_OBSERVACION_VENTA_PERDIDA " +
                        "WHERE Cod_Territorio = :codTerritorio " +
                        "AND Anio_Periodo = :anioPeriodo " +
                        "AND Semana = :semana";
            
            query = this.sessionFactory.getCurrentSession().createSQLQuery(sentencia);
            query.setParameter("codTerritorio", codTerritorio);
            query.setParameter("anioPeriodo", anioPeriodo);
            query.setParameter("semana", semana);
            
            if (Integer.parseInt(query.uniqueResult().toString()) == 0) {
                //Se inserta
                sentencia = "INSERT INTO SLS_OBSERVACION_VENTA_PERDIDA ( " +
                            "Cod_Territorio, " +
                            "Anio_Periodo, " +
                            "Semana, " +
                            "Texto" +
                            ") VALUES ( " +
                            ":codTerritorio, " +
                            ":anioPeriodo, " +
                            ":semana, " +
                            ":texto " +
                            ")";
            } else {
                //Se actualiza
                sentencia = "UPDATE SLS_OBSERVACION_VENTA_PERDIDA SET Texto = :texto " +
                            "WHERE Cod_Territorio = :codTerritorio " +
                            "AND Anio_Periodo = :anioPeriodo " +
                            "AND Semana = :semana";
            }
            
            //Se ejecuta la sentencia
            query = this.sessionFactory.getCurrentSession().createSQLQuery(sentencia);
            query.setParameter("codTerritorio", codTerritorio);
            query.setParameter("anioPeriodo", anioPeriodo);
            query.setParameter("semana", semana);
            query.setParameter("texto", observaciones);
            query.executeUpdate();
        } catch (Exception ex) {
            throw new Exception("Error guardando la observacion de venta perdida->" + Utilidades.obtenerMsgError(ex));
        }
    }

}
