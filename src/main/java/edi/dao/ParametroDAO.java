/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edi.dao;

import edi.dao.hibernate.Parametro;
import java.util.List;
import org.hibernate.SessionFactory;
/**
 *
 * @author alvarjor
 */
public class ParametroDAO implements IParametroDAO {
    private SessionFactory sessionFactory;

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Parametro obtener(String codigo) throws Exception {
        Parametro parametro;
        String sentencia;

        try {
            //Se extrae la ruta donde se encuentran los archivos a cargar
            sentencia = "FROM Parametro WHERE codigo='" + codigo + "'";
            parametro = (Parametro)sessionFactory.getCurrentSession().createQuery(sentencia).uniqueResult();
            if (parametro == null) {
                throw new Exception("Parámetro " + codigo + " no existe");
            }
        } catch (Exception ex) {
            throw new Exception("Ocurrió un error obtiendo el parámetro: " + ex.getMessage());
        }

        return parametro;
    }


    public void guardar(Parametro parametro) {
         sessionFactory.getCurrentSession().saveOrUpdate(parametro);
         sessionFactory.getCurrentSession().flush();
    }



    public List listar(String prefijo) throws Exception {
        String sentencia;
        List resultado;

        try {

            sentencia="SELECT p " +
                      "FROM Parametro p " +
                      "WHERE p.codigo LIKE '" + prefijo + "%'";

            resultado=sessionFactory.getCurrentSession().createQuery(sentencia).list();
        } catch (Exception ex) {
            throw new Exception("Ocurrió un error listando los parámetros: " + ex.getMessage());
        }

        return resultado;
    }
}
